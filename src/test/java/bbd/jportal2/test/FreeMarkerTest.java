package bbd.jportal2.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bbd.jportal2.JPortal;
import bbd.jportal2.Database;

import freemarker.template.TemplateException;
import org.junit.Test;

import bbd.jportal2.generators.FreeMarker;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class FreeMarkerTest {
    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerTest.class);

    private String  INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";
    private String SI_DIR = "src/test/resources/example_si_files/";
    private String OUTPUT_DIR = "target/test-freemarker-template-out/";


    @Test
    public void testFreeMarkerSingleFileInDir() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerSingleFileInDir";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory);

        //Test output
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "single_file"));
        assertEquals("Hello World jportal_example_db",lines.get(0));
    }

    @Test
    public void testFreeMarkerSingleSubDirMultipleFiles() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerSingleSubDirMultipleFiles";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
    
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory);

        //Test output - file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file1.txt"));
        assertEquals("Database Name: jportal_example_db" ,lines.get(0));
        assertEquals("Field Name: ID"           ,lines.get(1));
        assertEquals("Static: 1"                ,lines.get(2));

        //Test output - file2.txt
        List<String> lines2 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file2.txt"));
        assertEquals("Database Name: jportal_example_db" ,lines2.get(0));
        assertEquals("Field Name: IntField" ,lines2.get(1));
        assertEquals("Static: 2"                ,lines2.get(2));
    }

    @Test
    public void testFreeMarkerMultipleSubDirMultipleFiles() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerMultipleSubDirMultipleFiles";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file1.txt"));
        assertEquals("Database Name: jportal_example_db" ,lines.get(0));
        assertEquals("Field Name: ID"           ,lines.get(1));
        assertEquals("Static: 1"                ,lines.get(2));

        //Test output - dir1/file2.txt
        List<String> lines2 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file2.txt"));
        assertEquals("Database Name: jportal_example_db" ,lines2.get(0));
        assertEquals("Field Name: IntField" ,lines2.get(1));
        assertEquals("Static: 2"                ,lines2.get(2));


        //Test output - dir2/file1.txt
        List<String> lines3 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir2/file1.txt"));
        assertEquals("Database Name: jportal_example_db"         ,lines3.get(0));
        assertEquals("Field Name: UniqueInt"    ,lines3.get(1));
        assertEquals("Static: 3"                        ,lines3.get(2));

        //Test output - dir2/file2.txt
        List<String> lines4 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir2/file2.txt"));
        assertEquals("Database Name: jportal_example_db"             ,lines4.get(0));
        assertEquals("Field Name: StandardString" ,lines4.get(1));
        assertEquals("Static: 4"                            ,lines4.get(2));
    }



    @Test
    public void testFreeMarkerGenerateSimpleHarness() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerGenerateSimpleHarness";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "TestExampleTable.java"));
        assertEquals("class TestExampleTable" ,lines.get(0));
        assertEquals("{"                     ,lines.get(1));
        //assertEquals("      }"               ,lines.get(2));
    }
}

