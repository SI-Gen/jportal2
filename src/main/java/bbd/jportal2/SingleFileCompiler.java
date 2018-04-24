package bbd.jportal2;

import bbd.jportal2.generators.FreeMarker.FreeMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SingleFileCompiler {
    private static final Logger logger = LoggerFactory.getLogger(SingleFileCompiler.class);
    private List<String> templateLocations;

    public int compile(String source, List<String> compilerFlags, List<String> builtinGenerators, List<String> templateGenerators, List<String> templateLocations)
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

        for (String generator : builtinGenerators) {
            if (ExecuteGenerator(database, generator)) return 1;
        }


        for (String templateGenerator : templateGenerators) {
            if (!ExecuteTemplateGenerator(database, templateGenerator)) return 1;
        }

        return 0;
    }

    private boolean ExecuteTemplateGenerator(Database database, String templateGenerator) {
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
            Path fullGeneratorPath = Paths.get(dirToSearch, generatorName);
            if (Files.exists(fullGeneratorPath)) {
                templateBaseDir = dirToSearch;
                break;
            }
        }
        if (templateBaseDir == null) {
            StringBuilder templateLocationsAsString = new StringBuilder();
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
        } catch (Exception e) {
            logger.error("Error executing {}", generatorName, e);
            return false;
        }
        return true;
    }

    private boolean ExecuteGenerator(Database database, String generator) throws Exception {
        GeneratorParameters generatorParameters = new GeneratorParameters(generator).extractParametersFromOption();
        String generatorName = generatorParameters.getGeneratorName();
        String generatorDirectory = generatorParameters.getGeneratorDirectory();

        logger.info("Executing: " + generatorName);

        createOutputDirectory(generatorDirectory);
        generatorDirectory = addTrailingSlash(generatorDirectory);

        Class<?> c;
        Generator instanceOfC;
        try {
            c = Class.forName("bbd.jportal2.generators." + generatorName);
            instanceOfC = (Generator) c.newInstance();
        } catch (ClassNotFoundException cnf) {
            logger.error("Could not find generator {}. Make sure there is a class bbd.jportal2.generators.{}", generatorName);
            return true;
        }

        instanceOfC.generate(database, generatorDirectory);
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
            generatorName = generator.split(":")[0];
            generatorDirectory = generator.split(":")[1];
            return this;
        }
    }

}
