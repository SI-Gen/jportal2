package bbd.jportal2;

import org.slf4j.Logger;

import java.io.File;

public class PathHelpers {

    public static String addTrailingSlash(String generatorDirectory) {
        char term = File.separatorChar;
        char ch = generatorDirectory.charAt(generatorDirectory.length() - 1);
        if (ch != term)
            generatorDirectory = generatorDirectory + term;
        return generatorDirectory;
    }

    public static void createOutputDirectory(String generatorDirectory, Logger logger) {
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
}
