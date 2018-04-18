/// ------------------------------------------------------------------
/// Copyright (c) from 1996 Vincent Risi 
///                           
/// All rights reserved. 
/// This program and the accompanying materials are made available 
/// under the terms of the Common Public License v1.0 
/// which accompanies this distribution and is available at 
/// http://www.eclipse.org/legal/cpl-v10.html 
/// Contributors:
///    Vincent Risi
/// ------------------------------------------------------------------

package bbd.jportal2;

import bbd.jportal2.generators.FreeMarker;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class Main
{
    //===============================================================================================
    //Command-line parameters
    @Parameter(names = { "--log", "-l"}, description = "Logfile name i.e. --log=jportal2.log")
    private String logFileName = null;

    @Parameter(names = { "--inputdir", "-d"}, description = "Input dir")
    private String inputDir = "";

    @Parameter(description = "InputFiles")
    private List<String> inputFiles = new ArrayList<>();

    @Parameter(names = {"--generator", "-o"}, description = "Generator to run. Format is <generator_name>:<dest_dir> i.e. --generator=CSNetCode:./cs")
    private List<String> generators = new ArrayList<>();

    @Parameter(names = {"--template-generator", "-t"},
            description =
                    "FreeMarker-based generator to run."
                            + "Format is <free_marker_generator_name>:<dest_dir> i.e. "
                            + "'--template-generator=MyCustomGenerator:./output'. "
                            + "The template must exist as a directory under the location specified by --template-location.")
    private List<String> templateGenerators = new ArrayList<>();

    @Parameter(names = {"--template-location", "-tl"}, description = "Freemarker template location. Default is <current_working_directory>/jportal2_templates")
    private String templateLocation = Paths.get(System.getProperty("user.dir"), "jportal2_templates").toString();

    @Parameter(names = { "--flag", "-F"}, description = "Flags to pass to the generator")
    private List<String> flags = new ArrayList<>();

    @Parameter(names = { "--help", "-h", "-?" }, help = true)
    private boolean help;    
    //===============================================================================================  


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private BufferedReader bufferedReader;


    /**
     * Reads input from stored repository
     */
    public static void main(String args[])
    {
        Main main = new Main();

        try 
        {
            JCommander jCommander = JCommander.newBuilder()
                                        .addObject(main)
                                        .build();

            jCommander.setProgramName("JPortal2");
            jCommander.parse(args);

            if (main.help || args.length == 0) {
                jCommander.usage();
                return;
            }            
        }
        catch (ParameterException exc)
        {
            logger.error("Error", exc);
            exc.getJCommander().usage();
            System.exit(1);
        }

        //Set this before the logger starts.
        if (main.logFileName != null)
            System.setProperty("log.name", main.logFileName);

        try
        {
            //If inputdir is specified, add its contents to inputFiles list
            if (main.inputDir != null && main.inputDir.isEmpty()) {
                addFilesToList(main.inputDir, main.inputFiles);
            }

            for (String filename : main.inputFiles)
            {
                logger.info("Generating for: " + filename);
                int rc = main.compile(filename);
                if (rc != 0)
                    System.exit(2);
            }
            System.exit(0);
        } catch (Exception e)
        {
            logger.error("General Exception caught", e);        
            System.exit(3);
        }
    }

    private static void addFilesToList(String inputDir, List<String> list) {
        File folder = new File(inputDir);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    Path path = Paths.get(inputDir, file.getName());
                    list.add(path.toString());
                }
            }
        }
    }


    private int compile(String source)
            throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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

        for (String flag : flags) {
            database.flags.addElement(flag);
        }

        for (String generator : generators) {
            if (ExecuteGenerator(database, generator)) return 1;
        }


        for (String templateGenerator : templateGenerators) {
            if (ExecuteTemplateGenerator(database, templateGenerator)) return 1;
        }

        return 0;
    }

    private boolean ExecuteTemplateGenerator(Database database, String templateGenerator) {
        if (!templateGenerator.contains(":") || templateGenerator.split(":").length < 2) {
            logger.error("Error in template-generator parameter. The correct format is --template-generator=<name>:<output_directory>, but --template-generator='{}' was specified instead.", templateGenerator);
            return true;
        }

        GeneratorParameters generatorParameters = new GeneratorParameters(templateGenerator).extractParametersFromOption();
        String generatorName = generatorParameters.getGeneratorName();
        String generatorDirectory = generatorParameters.getGeneratorDirectory();

        logger.info("Executing: " + generatorName);

        createOutputDirectory(generatorDirectory);
        generatorDirectory = addTrailingSlash(generatorDirectory);
        File templateLocationFile = Paths.get(templateLocation).toFile();
        try {
            FreeMarker.generateAdvanced(database, templateLocationFile.getAbsolutePath(), generatorName, new File(generatorDirectory));
        } catch (IOException e) {
            logger.error("Error executing {}", generatorName, e);
            return false;
        }
        return true;
    }

    private boolean ExecuteGenerator(Database database, String generator) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        GeneratorParameters generatorParameters = new GeneratorParameters(generator).extractParametersFromOption();
        String generatorName = generatorParameters.getGeneratorName();
        String generatorDirectory = generatorParameters.getGeneratorDirectory();

        logger.info("Executing: " + generatorName);

        createOutputDirectory(generatorDirectory);
        generatorDirectory = addTrailingSlash(generatorDirectory);

        Class<?> c;
        try {
            c = Class.forName("bbd.jportal2.generators." + generatorName);
        } catch (ClassNotFoundException cnf) {
            logger.error("Could not find generator {}. Make sure there is a class bbd.jportal2.generators.{}", generatorName);
            return true;
        }

        Class<?> d[] = {database.getClass(), generatorDirectory.getClass()};
        Method m = c.getMethod("generate", d);
        Object o[] = {database, generatorDirectory};
        m.invoke(database, o);
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


//    private static String abbreviate(List<String> sources)
//    {
//        if (sources.size() > 5)
//            return sources.get(0) + " ... " + sources.get(sources.size() - 1);
//        return sources;
//    }


}
