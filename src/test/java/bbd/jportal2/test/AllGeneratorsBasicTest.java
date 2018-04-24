package bbd.jportal2.test;

import bbd.jportal2.TemplateBasedGenerator;
import bbd.jportal2.Generator;
import bbd.jportal2.ProjectCompiler;
import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class AllGeneratorsBasicTest {
    private static final Logger logger = LoggerFactory.getLogger(AllGeneratorsBasicTest.class);

    //private final String INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";

    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final String OUTPUT_DIR = "target/test-all-generators-basic-test-out/";
    private final String TEMPLATE_DIR = "src/main/resources/generator_templates";


    @Test
    public void testAllBuiltinGenerators() throws Exception {
        ProjectCompiler pj = new ProjectCompiler();
        pj.addTemplateLocation(TEMPLATE_DIR);

        Set<Class<?>> ALL_BUILTIN_GENERATORS = findClasses(Generator.class);

        List<String> builtinGenerators = new ArrayList<>();
        for (Class generatorClass : ALL_BUILTIN_GENERATORS) {
            String generator = generatorClass.getSimpleName();
            Path outputPath = Paths.get(OUTPUT_DIR, "AllGeneratorsBasicTest", "BUILTIN", generator);
            builtinGenerators.add(generator + ":" + outputPath);
        }
        pj.addBuiltinGenerators(builtinGenerators);

        pj.addInputDir(SI_DIR);
        pj.compileAll();
    }


    @Test
    public void testAllAdvancedGenerators() throws Exception {
        ProjectCompiler pj = new ProjectCompiler();
        pj.addTemplateLocation(TEMPLATE_DIR);

        Set<Class<?>> ALL_FREEMARKER_GENERATORS = findClasses(TemplateBasedGenerator.class);

        List<String> templateGenerators = new ArrayList<>();
        for (Class generatorClass : ALL_FREEMARKER_GENERATORS) {
            TemplateBasedGenerator instanceOfC = (TemplateBasedGenerator) generatorClass.newInstance();

            Set<String> templateDirs = findTemplateDirectories(instanceOfC);
            for (String template : templateDirs) {
                //String generator = generatorClass.getSimpleName();
                Path outputPath = Paths.get(OUTPUT_DIR, "AllGeneratorsBasicTest", "TEMPLATE", template);
                templateGenerators.add(template + ":" + outputPath.toString());
            }
        }
        pj.addTemplateGenerators(templateGenerators);

        pj.addInputDir(SI_DIR);
        pj.compileAll();
    }

    private Set<Class<?>> findClasses(Class T) {
        final Set<Class<?>> foundClasses = new HashSet<>();
        ComponentScanner scanner = new ComponentScanner();

        Set<Class<?>> classes = scanner.getClasses(new ComponentQuery() {

            protected void query() {
                select().from("bbd.jportal2").andStore(
                        thoseImplementing(T).into(foundClasses));
                //thoseAnnotatedWith(SampleAnnotation.class).into(samples));
            }

        });
        return foundClasses;
    }

    //Find directories in a JAR on on the disk
    private Set<String> findTemplateDirectories(TemplateBasedGenerator generator) throws Exception {
        URI uri = TemplateBasedGenerator.class.getResource("/" + generator.getTemplateFilesLocation().toString()).toURI();
        Path template_generatorsPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            template_generatorsPath = fileSystem.getPath("/" + generator.getTemplateFilesLocation().toString());
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

