package bbd.jportal2;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private List<String> builtinSIProcessors = new ArrayList<>();
    private List<String> templateBasedSIProcessors = new ArrayList<>();
    private List<String> builtinPostProcessors = new ArrayList<>();
    private List<String> templateBasedPostProcessors = new ArrayList<>();
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
            logger.info("No compiler getFlags detected.");

        int rc = 0;
        if (builtinSIProcessors.size() == 0 && templateBasedSIProcessors.size() == 0) {
            //\n does not get expanded. put the log and two calls to get formatting on the console right
            logger.error("No generators were specified!");
            logger.error("You need to specify at least one builtin generator (using --generator) or one template-based generator (using --template-generator).");
            rc = 1;
        }
        
        //Only run if return code is still 0
        if (rc == 0)
        {
            SingleFileCompiler sfCompiler = new SingleFileCompiler();
            for (String filename : allInputFiles) {
                if ("si".compareTo(FilenameUtils.getExtension(filename)) == 0) {
                    logger.info("Generating for SI File: " + filename);
                    rc |= sfCompiler.compile(filename, compilerFlags, builtinSIProcessors, templateBasedSIProcessors, builtinPostProcessors, templateBasedPostProcessors, templateLocations);
                }
            }
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

    public void addBuiltinSIProcessors(List<String> builtinSIProcessors) {
        this.builtinSIProcessors.addAll(builtinSIProcessors);
    }

    public void addBuiltinSIProcessor(String builtInSIProcessor) {
        this.builtinSIProcessors.add(builtInSIProcessor);
    }

    public void addTemplateBasedSIProcessors(List<String> templateBasedSIProcessors) {
        this.templateBasedSIProcessors.addAll(templateBasedSIProcessors);
    }

    public void addTemplateBasedSIProcessor(String templateBasedSIProcessor) {
        this.templateBasedSIProcessors.add(templateBasedSIProcessor);
    }

    public void addBuiltinPostProcessors(List<String> builtinPostProcessors) {
        this.builtinSIProcessors.addAll(builtinPostProcessors);
    }

    public void addBuiltinPostProcessor(String builtInPostProcessor) {
        this.builtinSIProcessors.add(builtInPostProcessor);
    }

    public void addTemplateBasedPostProcessors(List<String> templateBasedPostProcessors) {
        this.templateBasedPostProcessors.addAll(templateBasedPostProcessors);
    }

    public void addTemplateBasedPostProcessor(String templateBasedPostProcessor) {
        this.templateBasedPostProcessors.add(templateBasedPostProcessor);
    }

    public void addTemplateLocations(List<String> templateLocations) {
        this.templateLocations.addAll(templateLocations);
    }

    public void addTemplateLocation(String templateLocation) {
        this.templateLocations.add(templateLocation);
    }
}
