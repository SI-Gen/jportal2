package bbd.jportal2;

import bbd.jportal2.generators.FreeMarker.FreeMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class SingleFileCompiler {
    private static final Logger logger = LoggerFactory.getLogger(SingleFileCompiler.class);
    private List<String> templateLocations;

    public int compile(String source, List<String> compilerFlags, List<String> builtinSIProcessors, List<String> templateBasedSIProcessors, List<String> builtinPostProcessors, List<String> templateBasedPostProcessors, List<String> templateLocations)
            throws Exception {
        this.templateLocations = templateLocations;

        String[] pieces = source.split("\\+");
        Database database = null;
        boolean hasErrors = false;
        for (String piece : pieces) {
            String nubDir = "";
            database = JPortal.run(piece, nubDir);
            if (database == null) {
                logger.error("::>" + piece + "<:: compile has errors");
                hasErrors = true;
                continue;
            }
        }

        if (hasErrors) {
            logger.error("Error while parsing DB...");
            return 1;
        }

        for (String flag : compilerFlags) {
            database.flags.addElement(flag);
        }

        for (String generator : builtinSIProcessors) {
            if (ExecuteBuiltinGenerator(database, generator)) return 1;
        }


        for (String templateGenerator : templateBasedSIProcessors) {
            if (!ExecuteTemplateGenerator(database, templateGenerator)) return 1;
        }

        for (String generator : builtinPostProcessors) {
            if (ExecuteBuiltinGenerator(database, generator)) return 1;
        }


        for (String templateGenerator : templateBasedPostProcessors) {
            if (!ExecuteTemplateGenerator(database, templateGenerator)) return 1;
        }

        return 0;
    }

    private boolean ExecuteTemplateGenerator(Database database, String templateGenerator) throws Exception {
        if (!templateGenerator.contains(":") || templateGenerator.split(":").length < 2) {
            logger.error("Error in template-generator parameter. The correct format is --template-generator=<name>:<output_directory>, but --template-generator='{}' was specified instead.", templateGenerator);
            return false;
        }

        GeneratorParameters generatorParameters = new GeneratorParameters(templateGenerator).extractParametersFromOption();
        String generatorName = generatorParameters.getGeneratorName();
        String generatorDirectory = generatorParameters.getGeneratorDirectory();

        logger.info("Executing: " + generatorName);
        String templateBaseDir = null;
        for (String dirToSearch : this.templateLocations) {
            //git rid of the extension in a generator name
            if (generatorName.indexOf(".") > 0)
                generatorName = generatorName.substring(0, generatorName.lastIndexOf("."));
            Path fullGeneratorPath = Paths.get(dirToSearch, generatorName);
            //if (Files.exists(fullGeneratorPath)) {
            if (this.isTemplateOnDiskOrInJar(fullGeneratorPath.toString())) {
                templateBaseDir = dirToSearch;
                break;
            }
        }

        if (templateBaseDir == null) {
            StringBuilder templateLocationsAsString = new StringBuilder();
            templateLocationsAsString.append('\n');
            templateLocations.forEach(name -> {
                templateLocationsAsString.append(name);
                templateLocationsAsString.append('\n');
            });

            throw new SingleFileCompilerException(String.format("Template %1$s does not exist. Make sure a directory called %1$s exists in one of the template locations. See the --template-location option for more information.\\nThe templateLocation is currently set to: %2$s", generatorName, templateLocationsAsString.toString()));
        }

        logger.info("Executing generator [{}] found in [{}]", generatorName, templateBaseDir);

        createOutputDirectory(generatorDirectory);
        generatorDirectory = addTrailingSlash(generatorDirectory);
        File templateLocationFile = Paths.get(templateBaseDir).toFile();
        try {
            FreeMarker fm = new FreeMarker();
            fm.generateTemplate(database, templateLocationFile.getAbsolutePath(), generatorName, new File(generatorDirectory));
            database.addGeneratedOutputFiles(fm.getGeneratedOutputFiles());
        } catch (Exception e) {
            logger.error("Error executing {}", generatorName, e);
            return false;
        }
        return true;
    }

    private boolean ExecuteBuiltinGenerator(Database database, String generator) throws Exception {
        GeneratorParameters generatorParameters = new GeneratorParameters(generator).extractParametersFromOption();
        String generatorName = generatorParameters.getGeneratorName();
        String generatorDirectory = generatorParameters.getGeneratorDirectory();

        logger.info("Executing: " + generatorName);

        createOutputDirectory(generatorDirectory);
        generatorDirectory = addTrailingSlash(generatorDirectory);

        Class<?> c;
        IBuiltInGenerator instanceOfC;
        try {
            c = Class.forName("bbd.jportal2.generators." + generatorName);
            instanceOfC = (IBuiltInGenerator) c.newInstance();
        } catch (ClassNotFoundException cnf) {
            logger.error("Could not find generator {}. Make sure there is a class bbd.jportal2.generators.{}", generatorName);
            return true;
        }

        instanceOfC.generate(database, generatorDirectory);
        database.addGeneratedOutputFiles(instanceOfC.getGeneratedOutputFiles());
        return false;
    }

    private String addTrailingSlash(String generatorDirectory) {
        char term = File.separatorChar;
        char ch = generatorDirectory.charAt(generatorDirectory.length() - 1);
        if (ch != term)
            generatorDirectory = generatorDirectory + term;
        return generatorDirectory;
    }

    private void createOutputDirectory(String generatorDirectory) {
        File outputDirectory = new File(generatorDirectory);
        // if the directory does not exist, create it
        if (!outputDirectory.exists()) {
            logger.info("creating directory: " + outputDirectory.getName());
            boolean result = false;

            try {
                outputDirectory.mkdirs();
            } catch (SecurityException se) {
                //handle it
                logger.error("A Security Exception occurred:", se);
            }
        }
    }

    private class GeneratorParameters {
        private final String generator;
        private String generatorName;
        private String generatorDirectory;

        GeneratorParameters(String generator) {
            this.generator = generator;
        }

        String getGeneratorName() {
            return generatorName;
        }

        String getGeneratorDirectory() {
            return generatorDirectory;
        }

        GeneratorParameters extractParametersFromOption() {
            //split causes absolute paths on windows to fail.
            //get the index of the first : in the generator. split by that. the first index is the generator name 
            // everything else after that we treat as a path
            int strchr = generator.indexOf(':');
            if (strchr != -1)
            {
                generatorName = generator.substring(0, strchr);
                generatorDirectory = generator.substring(strchr + 1, generator.length());
                return this;
            }
            throw new RuntimeException();
        }
    }

    //Find directories in a JAR on on the disk
    private Boolean isTemplateOnDiskOrInJar(String fullGeneratorPath) throws Exception {
        if (Files.exists(Paths.get(fullGeneratorPath)))
            return true;

        //On windows, we need to convert the windows path to URL format
        fullGeneratorPath = fullGeneratorPath.replace(File.separator, "/");

        URL url = getClass().getResource(fullGeneratorPath);
        if (url != null) {
            return true;
        }
        return false;
//        Stream<Path> walk = Files.walk(template_generatorsPath).filter(Files::isDirectory);
//        //Set<String> templateDirs = new HashSet<>();
//        for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
//            Path item = it.next();
//            if (item.toFile().isDirectory()
//                    && !item.toFile().getName().equals(template_generatorsPath.getFileName().toString())    //Ignore the parent path
//                    && !item.toFile().getName().equalsIgnoreCase("helpers"))    //Ignore the helpers dir
//                return true;
//        }
//
//        return false;
    }
}
