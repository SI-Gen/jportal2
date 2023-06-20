package bbd.jportal2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BaseGenerator {
    private static final Logger logger = LoggerFactory.getLogger(BaseGenerator.class);

    //A list of the generated output files that this generator has created.
    private GeneratedFiles generatedOutputFiles;

    public GeneratedFiles getGeneratedOutputFiles() {
        return generatedOutputFiles;
    }

    //multiGeneration: True if the generator generates all tables in one iteration and false if it needs to be looped over
    //first: True if first iteration, otherwise false and skip generation
    public BaseGenerator(Class inheritedGeneratorClass) {
        String className = inheritedGeneratorClass.getSimpleName();
        generatedOutputFiles = new GeneratedFiles(className);
    }

    public Vector<Flag> getFlags() {
        return new Vector<>();
    }

    public Boolean toBoolean(Object value) {
        String s = value.toString();
        return s.equalsIgnoreCase("true");
    }


    protected void addFileToOutputtedFilesList(String fileType, Path generatedFile) {
        Optional<GeneratedFileGroup> ofg = generatedOutputFiles.getFileGroups().stream()
                .filter(f -> f.getFileGroupName().equalsIgnoreCase(fileType))
                .findFirst();

        GeneratedFileGroup fg;
        if (ofg.isEmpty()) {
            fg = new GeneratedFileGroup(fileType);
            generatedOutputFiles.getFileGroups().add(fg);
        } else {
            fg = ofg.get();
        }
        fg.getFiles().add(generatedFile);
    }

    protected void addFileToOutputtedFilesList(String fileType, String generatedFile) {
        addFileToOutputtedFilesList(fileType, Paths.get(generatedFile));
    }

    //This function should be used to open a file stream for writing a generated file.
    //It opens the stream, logs the fact that the file is opened, and add the file to the generatedOutputFiles
    //vector.
    //fileType is a small description that is printed in the log file, that describes the type of file that is being
    //created. Usually contains the value "code" or "DDL" or similar.
    protected PrintWriter openOutputFileForGeneration(String fileType, Path fileName) throws FileNotFoundException {
        logger.info("{}: {} ", fileType, fileName);
        this.addFileToOutputtedFilesList(fileType, fileName);
        return new PrintWriter(new FileOutputStream(fileName.toString()));
    }

    protected PrintWriter openOutputFileForGeneration(String fileType, String fileName) throws FileNotFoundException {
        return openOutputFileForGeneration(fileType, Paths.get(fileName));
    }

}
