package bbd.jportal2;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.UnzipParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
        String templateDownloadDirectory = addTrailingSlash(downloadedTemplateLocation);
        File templateDownloadLocationFile = Paths.get(templateDownloadDirectory).toFile();

        URL generatorURL = null;
        try {
            generatorURL = new URL(generatorURLString);
        } catch (IOException e) {
            logger.error("Invalid URL: {}",generatorURLString, e);
            return false;
        }

        Path GeneratorDownloadDirectoryPath = Paths.get(templateDownloadLocationFile.getAbsolutePath(), generatorName);
        Path fullGeneratorDownloadPath = Paths.get(GeneratorDownloadDirectoryPath.toString(), FilenameUtils.getName(generatorURL.getPath()));

        if (Files.exists(GeneratorDownloadDirectoryPath)) {
            logger.info("Template {} already exists in template-download-location {}. If you want to force a fresh download, delete the directory {}", generatorName, templateDownloadLocationFile.toString(), GeneratorDownloadDirectoryPath.toAbsolutePath());
            return true;
        }

        if (FilenameUtils.getExtension(fullGeneratorDownloadPath.toString()).equals("zip")) {
                return downloadZippedGenerator(generatorName, generatorURL, GeneratorDownloadDirectoryPath, fullGeneratorDownloadPath);
        }

        return true;
    }

    private boolean downloadZippedGenerator(String generatorName, URL generatorURL, Path GeneratorDownloadDirectoryPath, Path fullGeneratorDownloadPath) {
        try {
            logger.info("Downloading generator {} from {}", generatorName, generatorURL.toString());
            downloadFromURL(generatorURL, fullGeneratorDownloadPath.toString());
            if (FilenameUtils.getExtension(fullGeneratorDownloadPath.toString()).equals("zip")) {
                logger.info("Unzipping {} to {}", fullGeneratorDownloadPath.toString(), GeneratorDownloadDirectoryPath.toString());
                unzipFolderZip4j(fullGeneratorDownloadPath, GeneratorDownloadDirectoryPath);
            }
            else {
                logger.error("JPortal only supports automatic downloading of templates in .zip format.");
            }

        } catch (Exception e) {
            logger.error("Error downloading: {}", generatorURL.toString(), e);
            return true;
        }
        return false;
    }

    // it takes `File` as arguments
    public static void unzipFolderZip4j(Path source, Path target)
            throws IOException {
        new ZipFile(source.toFile())
                .extractAll(target.toString());
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


