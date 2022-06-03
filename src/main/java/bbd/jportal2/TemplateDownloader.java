package bbd.jportal2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

        Path generatorDownloadDirectoryPath = Paths.get(templateDownloadLocationFile.getAbsolutePath(), generatorName);
        Path fullGeneratorDownloadPath = Paths.get(generatorDownloadDirectoryPath.toString(), FilenameUtils.getName(generatorURL.getPath()));

        if (Files.exists(generatorDownloadDirectoryPath)) {
            logger.info("Template {} already exists in template-download-location {}. If you want to force a fresh download, delete the directory {}", generatorName, generatorDownloadDirectoryPath.toString(), generatorDownloadDirectoryPath.toAbsolutePath());
            return true;
        }

        if (FilenameUtils.getExtension(fullGeneratorDownloadPath.toString()).equals("zip")) {
                return downloadZippedGenerator(generatorName, generatorURL, generatorParameters.downloadSpecifier, generatorDownloadDirectoryPath, fullGeneratorDownloadPath);
        }

        if (FilenameUtils.getExtension(fullGeneratorDownloadPath.toString()).equals("git")) {
            return downloadGittedGenerator(generatorName, generatorURL, generatorParameters.downloadSpecifier, generatorDownloadDirectoryPath);
        }

        logger.error("JPortal can only deal with URL's that end in .git or .zip. Something is wrong with {}", generatorURL);
        return false;
    }

    private boolean downloadZippedGenerator(String generatorName, URL generatorURL, String mustStripBaseDir, Path GeneratorDownloadDirectoryPath, Path fullGeneratorDownloadPath) {
        try {
            boolean stripBaseDir = (mustStripBaseDir != null && mustStripBaseDir.toLowerCase().contains("stripbasedir")?true:false);
            logger.info("Downloading generator {} from {} (stripBaseDir = {})", generatorName, generatorURL.toString(),stripBaseDir);
            downloadFromURL(generatorURL, fullGeneratorDownloadPath.toString());
            if (FilenameUtils.getExtension(fullGeneratorDownloadPath.toString()).equals("zip")) {
                logger.info("Unzipping {} to {}", fullGeneratorDownloadPath.toString(), GeneratorDownloadDirectoryPath.toString());
                //unzipFolderZip4j(fullGeneratorDownloadPath, GeneratorDownloadDirectoryPath);
                unzipFolder(fullGeneratorDownloadPath,GeneratorDownloadDirectoryPath, stripBaseDir);
            }
            else {
                logger.error("JPortal only supports automatic downloading of templates in .zip format.");
            }

        } catch (Exception e) {
            logger.error("Error downloading: {}", generatorURL.toString(), e);
            return false;
        }
        return true;
    }

    private boolean downloadGittedGenerator(String generatorName, URL generatorURL, String gitTag, Path generatorDownloadDirectoryPath) {
        try {
            logger.info("Downloading generator {} from {}", generatorName, generatorURL.toString());
            if (gitTag.isEmpty()) {
                logger.error("No Git tag was specified. If you want to download a template from a git repo, you need " +
                        "to use the command '--download-template <template-name>:<git-repo>|<git-tag>', for " +
                        "example '--download-template " +
                        "SQLAlchemy:https://github.com/SI-Gen/jportal2-generator-vanguard-sqlalchemy.git|1.2' or " +
                        "'--download-template " +
                        "SQLAlchemy:https://github.com/SI-Gen/jportal2-generator-vanguard-sqlalchemy.git|develop'. We " +
                        "STRONGLY recommend you use a TAG, instead of HEAD, master, main or develop!");
            }

            logger.info("Cloning repo: {}", generatorURL);
            Git git = Git.cloneRepository()
                    .setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out)))
                    .setURI(generatorURL.toString())
                    .setDirectory(generatorDownloadDirectoryPath.toFile())
                    .setBranchesToClone(Collections.singleton("refs/tags/" + gitTag))
                    //.setBranch("refs/tags/" + gitTag)
                    .call();

            logger.info("Checking out tag/branch: {}", gitTag);
            git.checkout()
                    .setName("refs/tags/" + gitTag)
                    .call();
        } catch (Exception e) {
            logger.error("Error downloading: {}", generatorURL.toString(), e);
            return false;
        }
        return true;
    }

    private void unzipFolder(Path source, Path target, boolean stripBaseDir) throws IOException {

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {

            ZipEntry zipEntry = zis.getNextEntry();
            String baseDir = "";
            if (stripBaseDir) {
                //If stripBaseDir is true, we will strip the root dir from all files,
                //i.e. data/mystuff.txt will just extract to mystuff.txt
                baseDir = zipEntry.getName().split(File.separator)[0] + File.separator;
            }
            while (zipEntry != null) {

                boolean isDirectory = false;
                // example 1.1
                // some zip stored files and folders separately
                // e.g data/
                //     data/folder/
                //     data/folder/file.txt
                if (zipEntry.getName().endsWith(File.separator)) {
                    isDirectory = true;
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                //Strip basedir if needed
                String tmpNewPath = newPath.toString();
                tmpNewPath = tmpNewPath.replace(baseDir,"");
                newPath = Paths.get(tmpNewPath);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    // example 1.2
                    // some zip stored file path only, need create parent directories
                    // e.g data/folder/file.txt
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);

                    // copy files, classic
                    /*try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }*/
                }

                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();

        }

    }

    // protect zip slip attack
    private Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
            throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }

    private void downloadFromURL(URL url, String fileName) throws IOException {
        FileUtils.copyURLToFile(url, new File(fileName));
    }


    private class GeneratorDownloadParameters {
        private final String generator;
        private String generatorName;

        private String downloadSpecifier;

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

        String getDownloadSpecifier() {
            return downloadSpecifier;
        }

        TemplateDownloader.GeneratorDownloadParameters extractParametersFromOption() {
            //split causes absolute paths on windows to fail.
            //get the index of the first : in the generator. split by that. the first index is the generator name
            // everything else after that we treat as a URL. If there is a pipe '|', split the <URL>|<git-tag> into URL
            // and git-tag
            int strchr = generator.indexOf(':');
            if (strchr != -1) {
                generatorName = generator.substring(0, strchr);
                generatorURL = generator.substring(strchr + 1, generator.length());
                int strchr2 = generatorURL.lastIndexOf('|');
                if (strchr2 != -1) {
                    downloadSpecifier = generatorURL.substring(strchr2 + 1, generatorURL.length());
                    generatorURL = generatorURL.substring(0, strchr2);
                }
                return this;
            }
            throw new RuntimeException();
        }


    }

}


