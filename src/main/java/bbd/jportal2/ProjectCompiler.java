package bbd.jportal2;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


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
    private JPortalTemplateOutputOptions templateOutputOptions = JPortalTemplateOutputOptions.defaultTemplateOptions();

    private Boolean projectCompile = false;

    public ProjectCompiler(Boolean projectCompile) {
        this.projectCompile = projectCompile;
    }

    public void addInputDirs(List<String> listOfInputDirs) {
        this.inputDirs.addAll(listOfInputDirs);
    }

    public void addInputDir(String inputDir) {
        this.inputDirs.add(inputDir);
    }

    public void addInputFiles(List<String> inputFiles) {
        for (String filePath : inputFiles) {
            addInputFile(filePath);
        }
    }

    public void addInputFile(String inputFileName) {
        if ("si".compareTo(FilenameUtils.getExtension(inputFileName)) == 0)
            inputFiles.add(inputFileName);
    }

    public int compileAll() throws Exception {
        List<String> allInputFiles = new ArrayList<>();

        addAllInputDirsToList(allInputFiles);
        addAllInputFilesToList(allInputFiles);
        if (compilerFlags.size() == 0)
            logger.info("No compiler getFlags detected.");

        if (builtinSIProcessors.size() == 0 && templateBasedSIProcessors.size() == 0) {
            //\n does not get expanded. put the log and two calls to get formatting on the console right
            logger.error("No generators were specified!");
            logger.error("You need to specify at least one builtin generator (using --generator) or one template-based generator (using --template-generator).");
            return 1;
        }

        SingleFileCompiler sfCompiler = new SingleFileCompiler();

        int preCompileRc = sfCompiler.preCompile(allInputFiles, compilerFlags, templateLocations, templateOutputOptions);
        if (preCompileRc > 0)
            return preCompileRc;

        if (sfCompiler.compileBuiltIn(builtinSIProcessors, builtinPostProcessors) > 0)
            return 1;

        if (sfCompiler.compileFreemarker(templateBasedSIProcessors, templateBasedPostProcessors) > 0)
            return 1;

        return 0;
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

    public void overrideWithPropertiesFile(String propertiesFileName) throws RuntimeException {
        if (propertiesFileName.isEmpty())
            return;

        try (InputStream input = new FileInputStream(propertiesFileName)) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);
            prop.forEach((key,value) -> this.setTemplateOutputProperty((String)key, (String)value));

        } catch (IOException ex) {
            logger.error("Error processing properties file " + propertiesFileName, ex);
            throw new RuntimeException(ex);
        }
    }

    private void setTemplateOutputProperty(String key, String value){
        logger.debug("Property specified: " + key + " -> " + value);
        switch (key) {
            case "TemplateOutputOptions.DatabaseNamePrefix" : templateOutputOptions.DatabaseNamePrefix  = value;
                break;
            case "TemplateOutputOptions.DatabaseNameSuffix" : templateOutputOptions.DatabaseNameSuffix = value;
                break;
            case "TemplateOutputOptions.SchemaNamePrefix" : templateOutputOptions.SchemaNamePrefix  = value;
                break;
            case "TemplateOutputOptions.SchemaNameSuffix" : templateOutputOptions.SchemaNameSuffix  = value;
                break;
            case "TemplateOutputOptions.TableNamePrefix" : templateOutputOptions.TableNamePrefix  = value;
                break;
            case "TemplateOutputOptions.TableNameSuffix" : templateOutputOptions.TableNameSuffix  = value;
                break;
            case "TemplateOutputOptions.FieldNamePrefix" : templateOutputOptions.FieldNamePrefix  = value;
                break;
            case "TemplateOutputOptions.FieldNameSuffix" : templateOutputOptions.FieldNameSuffix  = value;
                break;
            case "TemplateOutputOptions.FieldVariablePrefix" : templateOutputOptions.FieldVariablePrefix  = value;
                break;
            case "TemplateOutputOptions.FieldVariableSuffix" : templateOutputOptions.FieldVariableSuffix  = value;
                break;
            case "TemplateOutputOptions.EngineSugarPrefix" : templateOutputOptions.EngineSugarPrefix  = value;
                break;
            case "TemplateOutputOptions.EngineSugarSuffix" : templateOutputOptions.EngineSugarSuffix  = value;
                break;
            case "TemplateOutputOptions.EngineSugarOutput" : templateOutputOptions.EngineSugarOutput  = value;
                break;
            case "TemplateOutputOptions.EngineSugarSequence" : templateOutputOptions.EngineSugarSequence  = value;
                break;
            case "TemplateOutputOptions.EngineSugarTail" : templateOutputOptions.EngineSugarSuffix  = value;
                break;
            case "TemplateOutputOptions.DynamicVariablePrefix" : templateOutputOptions.DynamicVariablePrefix  = value;
                break;
            case "TemplateOutputOptions.DynamicVariableSuffix" : templateOutputOptions.DynamicVariableSuffix  = value;
                break;


            default:
                logger.warn("Property: " + key + " not matched!");
        }
    }
}
