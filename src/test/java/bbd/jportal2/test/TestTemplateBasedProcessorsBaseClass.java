package bbd.jportal2.test;

import bbd.jportal2.ITemplateBasedGenerator;
import bbd.jportal2.ITemplateBasedPostProcessor;
import bbd.jportal2.ITemplateBasedSIProcessor;
import bbd.jportal2.ProjectCompiler;
import org.junit.Assert;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class TestTemplateBasedProcessorsBaseClass<TYPE_TO_TEST extends ITemplateBasedGenerator> {
    private static final Logger logger = LoggerFactory.getLogger(TestTemplateBasedProcessorsBaseClass.class);

    //private final String INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";

    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final Class<TYPE_TO_TEST> CLASS_TO_TEST;

    public TestTemplateBasedProcessorsBaseClass(Class<TYPE_TO_TEST> type) {
        CLASS_TO_TEST = type;
    }

    public int testAllTemplateBasedProcessorsOfType(String templateLocation, String outputLocation) throws Exception {
        ProjectCompiler pj = getProjectCompiler(templateLocation);

        return testAllTemplateBasedProcessorsOfType(pj, outputLocation);
    }

    public ProjectCompiler getProjectCompiler(String templateLocation) {
        ProjectCompiler pj = new ProjectCompiler();
        pj.addTemplateLocation(templateLocation);
        return pj;
    }

    public int testAllTemplateBasedProcessorsOfType(ProjectCompiler pj, String outputLocation) throws Exception {
        Set<Class<? extends TYPE_TO_TEST>> ALL_FREEMARKER_GENERATORS = findClasses();

        List<String> templateGenerators = new ArrayList<>();
        for (Class generatorClass : ALL_FREEMARKER_GENERATORS) {
            TYPE_TO_TEST instanceOfC = (TYPE_TO_TEST) generatorClass.newInstance();

            Set<String> templateDirs = findTemplateDirectories(instanceOfC);
            for (String template : templateDirs) {
                //String generator = generatorClass.getSimpleName();
                Path outputPath = Paths.get(outputLocation, "AllBuiltInGeneratorsBasicTest", "TEMPLATE", template);
                templateGenerators.add(template + ":" + outputPath.toString());
            }
        }
        if (ITemplateBasedSIProcessor.class.isAssignableFrom(CLASS_TO_TEST))
            pj.addTemplateBasedSIProcessors(templateGenerators);
        else
            pj.addTemplateBasedPostProcessors(templateGenerators);

        pj.addInputDir(SI_DIR);
        return pj.compileAll();
    }


    private Set<Class<? extends TYPE_TO_TEST>> findClasses() {

        Reflections reflections = new Reflections("bbd.jportal2");
        Set<Class<? extends TYPE_TO_TEST>> foundClasses = reflections.getSubTypesOf(CLASS_TO_TEST);

        return foundClasses;
    }

    //Find directories in a JAR on on the disk
    private Set<String> findTemplateDirectories(ITemplateBasedGenerator generator) throws Exception {
        String templateFilesLocation;
        if (ITemplateBasedSIProcessor.class.isAssignableFrom(CLASS_TO_TEST))
            templateFilesLocation = ((ITemplateBasedSIProcessor) generator).getSIProcessorTemplateFilesLocation().toString();
        else
            templateFilesLocation = ((ITemplateBasedPostProcessor) generator).getPostProcessorTemplateFilesLocation().toString();

        //On windows, we need to convert the windows path to URL format
        templateFilesLocation = templateFilesLocation.replace(File.separator, "/");

        URI uri = ITemplateBasedGenerator.class.getResource(templateFilesLocation).toURI();
        Path template_generatorsPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            template_generatorsPath = fileSystem.getPath(templateFilesLocation);
        } else {
            template_generatorsPath = Paths.get(uri);
        }
        Stream<Path> walk = Files.walk(template_generatorsPath).filter(Files::isDirectory);
        Set<String> templateDirs = new HashSet<>();
        for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
            Path item = it.next();
            if (item.toFile().isDirectory()
                    && !item.toFile().getName().equals(template_generatorsPath.getFileName().toString())    //Ignore the parent path
                    && !item.toFile().getName().equalsIgnoreCase("helpers"))    //Ignore the helpers dir
                templateDirs.add(item.toFile().getName().toString());
        }

        return templateDirs;
    }
}

