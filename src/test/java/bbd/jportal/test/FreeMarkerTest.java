package bbd.jportal.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bbd.jportal.JPortal;
import freemarker.template.TemplateException;
import org.junit.Test;

import bbd.jportal.Database;
import bbd.jportal.FreeMarker;

import javax.imageio.IIOException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class FreeMarkerTest {
    private String  INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";
    private String SI_DIR = "src/test/resources/example_si_files/";
    private String OUTPUT_DIR = "target/test-freemarker-template-out/";


    @Test
    public void testFreeMarkerSingleFileInDir() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerSingleFileInDir";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"Contingency.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        PrintWriter outLog = new PrintWriter(System.out);
        Database db = JPortal.run(siFile.toString(), nubDir, outLog);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory, outLog);

        //Test output
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "single_file"));
        assertEquals("Hello World testDatabase",lines.get(0));
    }

    @Test
    public void testFreeMarkerSingleSubDirMultipleFiles() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerSingleSubDirMultipleFiles";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"Contingency.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        PrintWriter outLog = new PrintWriter(System.out);
        Database db = JPortal.run(siFile.toString(), nubDir, outLog);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory, outLog);

        //Test output - file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file1.txt"));
        assertEquals("Database Name: testDatabase" ,lines.get(0));
        assertEquals("Field Name: ID"           ,lines.get(1));
        assertEquals("Static: 1"                ,lines.get(2));

        //Test output - file2.txt
        List<String> lines2 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file2.txt"));
        assertEquals("Database Name: testDatabase" ,lines2.get(0));
        assertEquals("Field Name: DataSourceId" ,lines2.get(1));
        assertEquals("Static: 2"                ,lines2.get(2));
    }

    @Test
    public void testFreeMarkerMultipleSubDirMultipleFiles() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerMultipleSubDirMultipleFiles";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"Contingency.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        PrintWriter outLog = new PrintWriter(System.out);
        Database db = JPortal.run(siFile.toString(), nubDir, outLog);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory, outLog);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file1.txt"));
        assertEquals("Database Name: testDatabase" ,lines.get(0));
        assertEquals("Field Name: ID"           ,lines.get(1));
        assertEquals("Static: 1"                ,lines.get(2));

        //Test output - dir1/file2.txt
        List<String> lines2 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file2.txt"));
        assertEquals("Database Name: testDatabase" ,lines2.get(0));
        assertEquals("Field Name: DataSourceId" ,lines2.get(1));
        assertEquals("Static: 2"                ,lines2.get(2));


        //Test output - dir2/file1.txt
        List<String> lines3 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir2/file1.txt"));
        assertEquals("Database Name: testDatabase"         ,lines3.get(0));
        assertEquals("Field Name: ExternalReference"    ,lines3.get(1));
        assertEquals("Static: 3"                        ,lines3.get(2));

        //Test output - dir2/file2.txt
        List<String> lines4 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir2/file2.txt"));
        assertEquals("Database Name: testDatabase"             ,lines4.get(0));
        assertEquals("Field Name: FixtureExternalReference" ,lines4.get(1));
        assertEquals("Static: 4"                            ,lines4.get(2));
    }



    @Test
    public void testFreeMarkerGenerateSimpleHarness() throws IOException, TemplateException {
        String nameOfTest = "testFreeMarkerGenerateSimpleHarness";
        Path templateDir = Paths.get(INPUT_DIRS,nameOfTest);
        Path siFile = Paths.get(SI_DIR,"Contingency.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        HashMap<String, String> params = new HashMap<>();
        params.put("TemplateDir", templateDir.toString());

        String nubDir="";
        PrintWriter outLog = new PrintWriter(System.out);
        Database db = JPortal.run(siFile.toString(), nubDir, outLog);
        File outputDirectory = new File(outputDir.toString());
        FreeMarker.generateAdvanced(db, params, outputDirectory, outLog);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "TestContingency.java"));
        assertEquals("class TestContingency" ,lines.get(0));
        assertEquals("{"                     ,lines.get(1));
        //assertEquals("      }"               ,lines.get(2));
    }
}

