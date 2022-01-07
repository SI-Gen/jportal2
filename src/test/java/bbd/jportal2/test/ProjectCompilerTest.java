package bbd.jportal2.test;

import bbd.jportal2.SingleFileCompilerException;
import bbd.jportal2.ProjectCompiler;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class ProjectCompilerTest {
    private final String SI_FILE1 = "src/main/resources/example_si_files/Contingency.si";

    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final String TEMPLATE_OUT_PATH = "test-freemarker-template-out";
    private final Path OUTPUT_DIR = Paths.get("target", TEMPLATE_OUT_PATH, "project-compiler-tests");
    private final String TEMPLATE_DIR = "src/test/resources/freemarker_input_dirs";
    private final String FREEMARKER_GENERATOR1 = "testFreeMarkerSingleFileInDir:" + Paths.get(OUTPUT_DIR.toString(), "testFreeMarkerSingleFileInDir").toString();
    private final String FREEMARKER_GENERATOR2 = "testFreeMarkerMultipleSubDirMultipleFiles:" + Paths.get(OUTPUT_DIR.toString(), "testFreeMarkerMultipleSubDirMultipleFiles").toString();
    private final String GENERATOR_3_NAME = "testFreeMarkerMultipleTemplateInDir";
    private final String FREEMARKER_GENERATOR3 = GENERATOR_3_NAME + ":" + Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME).toString();
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

    @Test
    public void testProjectCompilerMultipleFiles() throws Exception {
        ProjectCompiler pj = new ProjectCompiler(false);
        pj.addTemplateLocation(TEMPLATE_DIR);
        pj.addTemplateBasedSIProcessor(FREEMARKER_GENERATOR3);
        pj.addInputDir(SI_DIR);
        pj.compileAll();

        assertTrue(Files.exists(Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME, "Contingency", "file1.txt")));
        assertTrue(Files.exists(Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME, "ContingencyStatus", "file1.txt")));
        assertTrue(Files.exists(Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME, "DBHealthCheck", "file1.txt")));
        assertTrue(Files.exists(Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME, "ExampleTable", "file1.txt")));
        assertTrue(Files.exists(Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME, "Selection", "file1.txt")));
        assertTrue(Files.exists(Paths.get(OUTPUT_DIR.toString(), GENERATOR_3_NAME, "SelectionStatus", "file1.txt")));

    }

}

