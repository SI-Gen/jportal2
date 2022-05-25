package bbd.jportal2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static bbd.jportal2.JPortal.database;
import static bbd.jportal2.PathHelpers.addTrailingSlash;


public class TemplateDownloader {
    private static final Logger logger = LoggerFactory.getLogger(TemplateDownloader.class);

    public int downloadTemplates(JPortal2Arguments arguments) {
        for (String templateGenerator : arguments.getTemplatesToDownload()) {
            if (!downloadGenerator(templateGenerator, arguments.getDownloadedTemplateLocation())) return 1;
        }
        return 0;
    }

    private boolean downloadGenerator(String templateGenerator, String downloadedTemplateLocation) {
        if (!templateGenerator.contains(":") || templateGenerator.split(":").length < 2) {
            logger.error("Error in template-generator parameter. The correct format is --template-generator=<name>:<output_directory>, but --template-generator='{}' was specified instead.", templateGenerator);
            return false;
        }

        TemplateDownloader.GeneratorDownloadParameters generatorParameters = new TemplateDownloader.GeneratorDownloadParameters(templateGenerator).extractParametersFromOption();
        String generatorName = generatorParameters.getGeneratorName();
        String generatorURLString = generatorParameters.getGeneratorURL();
        String generatorDownloadDirectory = addTrailingSlash(downloadedTemplateLocation);
        File templateDownloadLocationFile = Paths.get(generatorDownloadDirectory).toFile();
        URL generatorURL = null;
        try {
            generatorURL = new URL(generatorURLString);
        } catch (IOException e) {
            logger.error("Error downloading template " + generatorName + " from  " + generatorURLString, e);
            return false;
        }

        Path fullGeneratorPath = Paths.get(templateDownloadLocationFile.getAbsolutePath(), generatorName,
                FilenameUtils.getName(generatorURL.getPath()));
        try {
            logger.info("Downloading generator {} from {}",generatorName, generatorURL);
            downloadFromURL(generatorURL,fullGeneratorPath.toString());
        } catch (IOException e) {
            logger.error("Error downloading: " + generatorURLString, e);
            return false;
        }
        return true;
    }


    private void downloadFromURL(URL url, String fileName) throws IOException {
        FileUtils.copyURLToFile(url, new File(fileName));
    }


    private class GeneratorDownloadParameters {
        private final String generator;
        private String generatorName;
        private String generatorURL;

        GeneratorDownloadParameters(String generator) {
            this.generator = generator;
        }

        String getGeneratorName() {
            return generatorName;
        }

        String getGeneratorURL() {
            return generatorURL;
        }

        TemplateDownloader.GeneratorDownloadParameters extractParametersFromOption() {
            //split causes absolute paths on windows to fail.
            //get the index of the first : in the generator. split by that. the first index is the generator name
            // everything else after that we treat as a path
            int strchr = generator.indexOf(':');
            if (strchr != -1) {
                generatorName = generator.substring(0, strchr);
                generatorURL = generator.substring(strchr + 1, generator.length());
                return this;
            }
            throw new RuntimeException();
        }
    }

}


