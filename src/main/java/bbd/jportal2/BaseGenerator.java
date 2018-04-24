package bbd.jportal2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

public class BaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(bbd.jportal2.CliCCode.class);

    //A list of the generated output files that this generator has created.
    private Vector<Path> outputtedFiles = new Vector<>();

    public Vector<Path> getOutputtedFiles() {
        return outputtedFiles;
    }


    protected void addFileToOutputtedFilesList(Path generatedFile) {
        outputtedFiles.add(generatedFile);
    }

    protected void addFileToOutputtedFilesList(String generatedFile) {
        addFileToOutputtedFilesList(Paths.get(generatedFile));
    }

    //This function should be used to open a file stream for writing a generated file.
    //It opens the stream, logs the fact that the file is opened, and add the file to the outputtedFiles
    //vector.
    //fileType is a small description that is printed in the log file, that describes the type of file that is being
    //created. Usually contains the value "code" or "DDL" or similar.
    protected PrintWriter openOutputFileForGeneration(Path fileName, String fileType) throws FileNotFoundException {
        logger.info("{}: {} ", fileType, fileName);
        this.addFileToOutputtedFilesList(fileName);
        return new PrintWriter(new FileOutputStream(fileName.toString()));
    }

    protected PrintWriter openOutputFileForGeneration(String fileName, String fileType) throws FileNotFoundException {
        return openOutputFileForGeneration(Paths.get(fileName), fileType);
    }

}
