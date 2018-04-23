package bbd.jportal2;

import bbd.jportal2.generators.FreeMarker.FreeMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class ProjectCompiler {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCompiler.class);


    private List<String> inputDirs = new ArrayList<>();
    private List<String> inputFiles = new ArrayList<>();
    private List<String> builtin = new ArrayList<>();
    private List<String> compilerFlags = new ArrayList<>();
    private List<String> builtinGenerators = new ArrayList<>();
    private List<String> templateGenerators = new ArrayList<>();
    private List<String> templateLocations = new ArrayList<>();

    public void addInputDirs(List<String> listOfInputDirs) {
        this.inputDirs.addAll(listOfInputDirs);
    }

    public void addInputDir(String inputDir) {
        this.inputDirs.add(inputDir);
    }

    public void addInputFiles(List<String> inputFiles) {
        this.inputFiles.addAll(inputFiles);
    }

    public void addInputFile(String inputFile) {
        this.inputFiles.add(inputFile);
    }


    public int compileAll() throws Exception {
        List<String> allInputFiles = new ArrayList<>();

        addAllInputDirsToList(allInputFiles);
        addAllInputFilesToList(allInputFiles);
        if (compilerFlags.size() == 0)
            logger.info("No compiler flags detected.");

        int rc = 0;
        if (builtinGenerators.size() == 0 && templateGenerators.size() == 0) {
            logger.error("No generators were specified!\\nYou need to specify at least one builtin generator (using --generator) or one template-based generator (using --template-generator).");
            rc = 1;
        }

        SingleFileCompiler sfCompiler = new SingleFileCompiler();
        for (String filename : allInputFiles) {
            logger.info("Generating for: " + filename);
            rc |= sfCompiler.compile(filename, compilerFlags, builtinGenerators, templateGenerators, templateLocations);

        }
        return rc;
    }

    private void addAllInputFilesToList(List<String> listToAddTo) {
        listToAddTo.addAll(this.inputFiles);
    }

    private void addAllInputDirsToList(List<String> listToAddTo) {
        for (String inputDir : this.inputDirs) {
            List<String> listOfFiles = new ArrayList<>();
            directoryToList(inputDir, listOfFiles);
            listToAddTo.addAll(listOfFiles);
        }
    }

    private void directoryToList(String inputDir, List<String> outList) {
        File folder = new File(inputDir);
        if (!folder.isDirectory()) {
            logger.warn("{} is not a directory! Ignoring it...", inputDir);
            return;
        }

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            logger.warn("{} is an empty directory! Ignoring it...", inputDir);
            return;
        }

        for (File file : listOfFiles) {
            if (file.isDirectory()) {
                directoryToList(file.toString(), outList);
            } else if (file.isFile()) {
                Path path = Paths.get(inputDir, file.getName());
                outList.add(path.toString());
            }
        }
    }

    public void addCompilerFlags(List<String> compilerFlags) {
        this.compilerFlags.addAll(compilerFlags);
    }

    public void addBuiltinGenerators(List<String> builtinGenerators) {
        this.builtinGenerators.addAll(builtinGenerators);
    }

    public void addBuiltinGenerator(String builtinGenerator) {
        this.builtinGenerators.add(builtinGenerator);
    }

    public void addTemplateGenerators(List<String> templateGenerators) {
        this.templateGenerators.addAll(templateGenerators);
    }

    public void addTemplateGenerator(String templateGenerator) {
        this.templateGenerators.add(templateGenerator);
    }

    public void addTemplateLocations(List<String> templateLocations) {
        this.templateLocations.addAll(templateLocations);
    }

    public void addTemplateLocation(String templateLocation) {
        this.templateLocations.add(templateLocation);
    }
}
