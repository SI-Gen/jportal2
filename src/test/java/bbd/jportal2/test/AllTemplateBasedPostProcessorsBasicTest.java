package bbd.jportal2.test;

import bbd.jportal2.BuiltInGeneratorHelpers;
import bbd.jportal2.IBuiltInSIProcessor;
import bbd.jportal2.ITemplateBasedPostProcessor;
import bbd.jportal2.ProjectCompiler;
import org.junit.Assert;
import org.junit.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class AllTemplateBasedPostProcessorsBasicTest extends TestTemplateBasedProcessorsBaseClass<ITemplateBasedPostProcessor> {
    //private final String INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";

    private final String OUTPUT_DIR = "target/test-all-generators-basic-test-out/";
    private final String POST_PROCESSOR_TEMPLATE_DIR = ITemplateBasedPostProcessor.POST_PROCESSOR_TEMPLATE_LOCATION;

    public AllTemplateBasedPostProcessorsBasicTest() {
        super(ITemplateBasedPostProcessor.class);
    }

    @Test
    public void testAllTemplateBasedPostProcessors() throws Exception {

        //In order to test post processors, we need to add some SI processors to the project.
        ProjectCompiler pj = getProjectCompiler(POST_PROCESSOR_TEMPLATE_DIR);

        BuiltInGeneratorHelpers helper = new BuiltInGeneratorHelpers();

        List<String> builtinGenerators = new ArrayList<>();

        Vector<String> generators = helper.findAllBuiltInGeneratorsOfType(IBuiltInSIProcessor.class);
        for (String generator : generators) {
            Path outputPath = Paths.get(OUTPUT_DIR, "AllBuiltInGeneratorsBasicTest", "BUILTIN", generator);
            builtinGenerators.add(generator + ":" + outputPath);
        }

        pj.addBuiltinSIProcessors(builtinGenerators);


        //Test the actual post processors
        int success = this.testAllTemplateBasedProcessorsOfType(pj, OUTPUT_DIR);
        Assert.assertEquals(0, success);
    }

}

