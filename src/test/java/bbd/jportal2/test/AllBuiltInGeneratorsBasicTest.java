package bbd.jportal2.test;

import bbd.jportal2.*;
import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.*;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class AllBuiltInGeneratorsBasicTest {

    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final String OUTPUT_DIR = "target/test-all-generators-basic-test-out/";
    private final String SI_PROCESSOR_TEMPLATE_DIR = "src/main/resources/si_processor_templates";

    @Test
    public void testAllBuiltinSIProcessors() throws Exception {
        this.testBuiltInProcessorsOfType(IBuiltInSIProcessor.class);
    }

    @Test
    public void testAllBuiltinPostProcessors() throws Exception {
        this.testBuiltInProcessorsOfType(IBuiltInPostProcessor.class);
    }

    public <TYPE_TO_TEST> void testBuiltInProcessorsOfType(Class<TYPE_TO_TEST> classType) throws Exception {
        ProjectCompiler pj = new ProjectCompiler();
        pj.addTemplateLocation(SI_PROCESSOR_TEMPLATE_DIR);
        BuiltInGeneratorHelpers helper = new BuiltInGeneratorHelpers();

        List<String> builtinGenerators = new ArrayList<>();

        Vector<String> generators = helper.findAllBuiltInGeneratorsOfType(classType);
        for (String generator : generators) {
            Path outputPath = Paths.get(OUTPUT_DIR, "AllBuiltInGeneratorsBasicTest", "BUILTIN", generator);
            builtinGenerators.add(generator + ":" + outputPath);
        }

        if (IBuiltInSIProcessor.class.isAssignableFrom(classType))
            pj.addBuiltinSIProcessors(builtinGenerators);
        else
            pj.addBuiltinPostProcessors(builtinGenerators);

        pj.addInputDir(SI_DIR);
        pj.compileAll();
    }


}

