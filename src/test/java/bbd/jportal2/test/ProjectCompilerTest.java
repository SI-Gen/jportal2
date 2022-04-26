package bbd.jportal2.test;

import bbd.jportal2.SingleFileCompilerException;
import bbd.jportal2.ProjectCompiler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class ProjectCompilerTest {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCompilerTest.class);

    private final String INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";

    private final String SI_FILE1 = "src/main/resources/example_si_files/Contingency.si";

    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final Path OUTPUT_DIR = Paths.get("target", "test-freemarker-template-out", "project-compiler-tests");
    private final String TEMPLATE_DIR = "src/test/resources/freemarker_input_dirs";
    private final String FREEMARKER_GENERATOR1 = "testFreeMarkerSingleFileInDir:" + Paths.get(OUTPUT_DIR.toString(), "testFreeMarkerSingleFileInDir").toString();
    private final String FREEMARKER_GENERATOR2 = "testFreeMarkerMultipleSubDirMultipleFiles:" + Paths.get(OUTPUT_DIR.toString(), "testFreeMarkerMultipleSubDirMultipleFiles").toString();
    private final String BUILTIN_GENERATOR1 = "CSNetCode:" + OUTPUT_DIR.toString() + "/cs";
    private final String BUILTIN_GENERATOR2 = "MSSqlDDL:" + OUTPUT_DIR.toString() + "/mssql";

    @Test
    public void testProjectCompilerTestSimple() throws Exception {
        ProjectCompiler pj = new ProjectCompiler(false);
        setupSimpleListsParameters(pj);
        pj.compileAll();
    }

    @Test(expected = SingleFileCompilerException.class)
    public void testProjectCompilerThrowsExceptionIfGeneratorNotFound() throws Exception {
        ProjectCompiler pj = new ProjectCompiler(false);
        setupSimpleListsParameters(pj);
        pj.addTemplateBasedSIProcessor("NON_EXISTENT_GENERATOR:NON_EXISTENT_PATH");
        assertEquals(0, pj.compileAll());
    }

    private void setupSimpleListsParameters(ProjectCompiler pj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        pj.addTemplateLocation(TEMPLATE_DIR);

        List<String> templateGenerators = Arrays.asList(FREEMARKER_GENERATOR1, FREEMARKER_GENERATOR2);
        pj.addTemplateBasedSIProcessors(templateGenerators);

        List<String> builtinGenerators = Arrays.asList(BUILTIN_GENERATOR1, BUILTIN_GENERATOR2);
        pj.addBuiltinSIProcessors(builtinGenerators);

        List<String> inputFiles = Arrays.asList(SI_FILE1);
        pj.addInputFiles(inputFiles);

    }

}

