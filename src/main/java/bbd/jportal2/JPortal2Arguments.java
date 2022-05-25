package bbd.jportal2;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=")
public class JPortal2Arguments {

    @Parameter(names = {"--debug", "-D"}, description = "Enable debug logging")
    private boolean debug = false;

    @Parameter(names = {"--projectCompile", "-pc"}, description = "Enable project-level compilation")
    public boolean projectCompile = false;

    @Parameter(names = {"--log", "-l"}, description = "Logfile name i.e. --log=jportal2.log")
    private String logFileName = null;

    @Parameter(names = {"--inputdir", "-d"}, description = "Input dir")
    private List<String> inputDirs = new ArrayList<>();

    @Parameter(description = "InputFiles")
    private List<String> inputFiles = new ArrayList<>();

    @Parameter(names = {"--builtin-generator", "-o"}, description = "Built-In (Java-based) generatorName to run. Format is <generator_name>:<dest_dir> i.e. --builtin-generator=CSNetCode:./cs")
    private List<String> builtinSIProcessors = new ArrayList<>();

    @Parameter(names = {"--download-template-location", "-dtl"}, description = "Location in which to place downloaded Freemarker templates. Default is <current_working_directory>/jportal2_downloaded_templates")
    private String downloadedTemplateLocation =  "jportal2_downloaded_templates";

    @Parameter(names = {"--download-template", "-dt"},
            description =
                    "Download the specified template from the given location. "
                            + "Zip files as well as git repos are supported."
                            + "Format is <freemarker_generator_name>:<url> i.e. "
                            + "'--download-template=MyCustomGenerator:https://github/SI-Gen/SQLAlchemy.zip'. "
                            + "The files will be downloaded to and unzipped in, to <template-location>/<freemarker_generator_name>")
    private List<String> templatesToDownload = new ArrayList<>();

    @Parameter(names = {"--template-generator", "-t"},
            description =
                    "FreeMarker-based generator to run."
                            + "Format is <free_marker_generator_name>:<dest_dir> i.e. "
                            + "'--template-generator=MyCustomGenerator:./output'. "
                            + "The template must exist as a directory under the location specified by --template-location.")
    private List<String> templateSIProcessors = new ArrayList<>();

    @Parameter(names = {"--builtin-postprocessor", "-bpp"}, description = "Built-In (Java-based) generatorName to run. Format is <generator_name>:<dest_dir> i.e. --builtin-postprocessor=CSNetCode:./cs")
    private List<String> builtinPostProcessors = new ArrayList<>();

    @Parameter(names = {"--template-postprocessor", "-tpp"},
            description =
                    "FreeMarker-based post-processor to run."
                            + "Format is <free_marker_generator_name>:<dest_dir> i.e. "
                            + "'--template-postprocessor=MyCustomGenerator:./output'. "
                            + "The template must exist as a directory under the location specified by --template-location.")
    private List<String> templatePostProcessors = new ArrayList<>();

    @Parameter(names = {"--template-location", "-tl"}, description = "Freemarker template location. Default is <current_working_directory>/jportal2_templates")
    private List<String> templateLocations = Arrays.asList(Paths.get(System.getProperty("user.dir"), "jportal2_templates").toString());

    @Parameter(names = {"--flag", "-F"}, description = "Flags to pass to the generator")
    private List<String> flags = new ArrayList<>();

    @Parameter(names = {"--help", "-h", "-?"}, help = true)
    private boolean help;

    public String getLogFileName() {
        return logFileName;
    }

    public List<String> getInputDirs() {
        return inputDirs;
    }

    public List<String> getInputFiles() {
        return inputFiles;
    }

    public List<String> getBuiltinSIProcessors() {
        return builtinSIProcessors;
    }

    public List<String> getTemplateSIProcessors() {
        return templateSIProcessors;
    }

    public List<String> getBuiltinPostProcessors() {
        return builtinPostProcessors;
    }

    public List<String> getTemplatePostProcessors() {
        return templatePostProcessors;
    }

    public List<String> getTemplateLocations() {
        return templateLocations;
    }

    public String getDownloadedTemplateLocation() {
        return downloadedTemplateLocation;
    }

    public List<String> getTemplatesToDownload() { return templatesToDownload; }

    public List<String> getFlags() {
        return flags;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean mustDebug() {
        return debug;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public void setInputDirs(List<String> inputDirs) {
        this.inputDirs = inputDirs;
    }

    public void setInputFiles(List<String> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void setBuiltinSIProcessors(List<String> builtinSIProcessors) {
        this.builtinSIProcessors = builtinSIProcessors;
    }

    public void setTemplateSIProcessors(List<String> templateSIProcessors) {
        this.templateSIProcessors = templateSIProcessors;
    }

    public void setBuiltinPostProcessors(List<String> builtinPostProcessors) {
        this.builtinPostProcessors = builtinPostProcessors;
    }

    public void setTemplatePostProcessors(List<String> templatePostProcessors) {
        this.templatePostProcessors = templatePostProcessors;
    }

    public void setTemplateLocations(List<String> templateLocations) {
        this.templateLocations = templateLocations;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    @Override
    public String toString() {
        return "JPortal2Arguments{" +
                "logFileName='" + logFileName + '\'' +
                ", inputDirs=" + inputDirs +
                ", inputFiles=" + inputFiles +
                ", builtinSIProcessors=" + builtinSIProcessors +
                ", templateSIProcessors=" + templateSIProcessors +
                ", builtinPostProcessors=" + builtinPostProcessors +
                ", templatePostProcessors=" + templatePostProcessors +
                ", templateLocations=" + templateLocations +
                ", templatesToDownload=" + templatesToDownload +
                ", flags=" + flags +
                '}';
    }
}
