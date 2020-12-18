package bbd.jportal2.test;

import bbd.jportal2.Database;
import bbd.jportal2.JPortal;
import bbd.jportal2.Table;
import bbd.jportal2.generators.FreeMarker.FreeMarker;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by dieter on 2017/05/30.
 * Unittest to test the FreeMarker template based generator
 */
public class FreeMarkerTest {
    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerTest.class);

    private final String INPUT_DIRS = "src/test/resources/freemarker_input_dirs/";
    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final String OUTPUT_DIR = "target/test-freemarker-template-out/";
    private final String TEMPLATE_DIR = "src/test/resources/freemarker_input_dirs";


    @Test
    public void testFreeMarkerSingleFileInDir() throws Exception {
        String nameOfTest = "testFreeMarkerSingleFileInDir";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        (new FreeMarker()).generateTemplate(db, db.tables.get(0), TEMPLATE_DIR, nameOfTest, outputDirectory);

        //Test output
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "single_file"));
        assertEquals("Hello World jportal_example_db",lines.get(0));
        JPortal.clearDatabase();
    }

    @Test
    public void testFreeMarkerSingleSubDirMultipleFiles() throws Exception {
        String nameOfTest = "testFreeMarkerSingleSubDirMultipleFiles";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);

        String nubDir="";
    
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        (new FreeMarker()).generateTemplate(db, db.tables.get(0), TEMPLATE_DIR, nameOfTest, outputDirectory);

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
        JPortal.clearDatabase();
    }

    @Test
    public void testFreeMarkerMultipleSubDirMultipleFiles() throws Exception {
        String nameOfTest = "testFreeMarkerMultipleSubDirMultipleFiles";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR, nameOfTest);

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());

        Table tab = null;
        for (Table t : db.tables) {
            if (t.name.equals("ExampleTable")) {
                tab = t;
                break;
            }
        }

        (new FreeMarker()).generateTemplate(db, tab, TEMPLATE_DIR, nameOfTest, outputDirectory);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file1.txt"));
        assertEquals("Database Name: jportal_example_db",lines.get(0));
        assertEquals("Field Name: ID"                   ,lines.get(1));
        assertEquals("Static: 1"                        ,lines.get(2));

        //Test output - dir1/file2.txt
        List<String> lines2 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir1/file2.txt"));
        assertEquals("Database Name: jportal_example_db",lines2.get(0));
        String line21 = lines2.get(1);
        int result = line21.compareTo("Field Name: IntField");
        assertEquals("Field Name: IntField"             ,line21);
        assertEquals("Static: 2"                        ,lines2.get(2));

        //Test output - dir2/file1.txt
        List<String> lines3 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir2/file1.txt"));
        assertEquals("Database Name: jportal_example_db",lines3.get(0));
        assertEquals("Field Name: UniqueInt"            ,lines3.get(1));
        assertEquals("Static: 3"                        ,lines3.get(2));

        //Test output - dir2/file2.txt
        List<String> lines4 = Files.readAllLines(Paths.get(outputDirectory.toString(), "dir2/file2.txt"));
        assertEquals("Database Name: jportal_example_db",lines4.get(0));
        assertEquals("Field Name: StandardString"       ,lines4.get(1));
        assertEquals("Static: 4"                        ,lines4.get(2));
        JPortal.clearDatabase();
    }

    @Test
    public void testFreeMarkerMultipleTemplateInDir() throws Exception {
        String nameOfTest = "testFreeMarkerMultipleTemplateInDir";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR, nameOfTest);

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        (new FreeMarker()).generateTemplate(db, db.tables.get(0), TEMPLATE_DIR, nameOfTest, outputDirectory);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "ExampleTable/file1.txt"));
        assertEquals("Database Name: jportal_example_db" ,lines.get(0));
        assertEquals("Field Name: ID"                    ,lines.get(1));
        assertEquals("Static: 1"                         ,lines.get(2));
        JPortal.clearDatabase();
    }

    @Test
    public void testFreeMarkerMultipleProcFileGen() throws Exception {
        String nameOfTest = "testFreeMarkerMultipleProcFileGen";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR, nameOfTest);

        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        (new FreeMarker()).generateTemplate(db, db.tables.get(0), TEMPLATE_DIR, nameOfTest, outputDirectory);

        //Test output - SelectForUpdateByUniqueInt.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "SelectForUpdateByUniqueInt.txt"));
        assertEquals("Database Name: jportal_example_db"     ,lines.get(0));
        assertEquals("Field Name: SelectForUpdateByUniqueInt",lines.get(1));
        assertEquals("Static: 1"                             ,lines.get(2));

        //Test output - GetByUniqueInt.txt
        List<String> lines1 = Files.readAllLines(Paths.get(outputDirectory.toString(), "GetByUniqueInt.txt"));
        assertEquals("Database Name: jportal_example_db" ,lines1.get(0));
        assertEquals("Field Name: GetByUniqueInt"        ,lines1.get(1));
        assertEquals("Static: 1"                         ,lines1.get(2));
        JPortal.clearDatabase();

    }

    @Test
    public void testFreeMarkerGenerateSimpleHarness() throws Exception {
        String nameOfTest = "testFreeMarkerGenerateSimpleHarness";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR,"ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR,nameOfTest);


        String nubDir="";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        (new FreeMarker()).generateTemplate(db, db.tables.get(0), TEMPLATE_DIR, nameOfTest, outputDirectory);

        //Test output - dir1/file1.txt
        List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "TestExampleTable.java"));
        assertEquals("class TestExampleTable" ,lines.get(0));
        assertEquals("{"                     ,lines.get(1));
        JPortal.clearDatabase();
        //assertEquals("      }"               ,lines.get(2));
    }

    @Test
    public void testFreeMarkerDumpDB() throws Exception {
        String nameOfTest = "testFreeMarkerDumpDB";
        Path templateDir = Paths.get(INPUT_DIRS);
        Path siFile = Paths.get(SI_DIR, "ExampleTable.si");
        Path outputDir = Paths.get(OUTPUT_DIR, nameOfTest);

        String nubDir = "";
        Database db = JPortal.run(siFile.toString(), nubDir);
        File outputDirectory = new File(outputDir.toString());
        (new FreeMarker()).generateTemplate(db, db.tables.get(0), TEMPLATE_DIR, nameOfTest, outputDirectory);
        JPortal.clearDatabase();

        //Test output
        //List<String> lines = Files.readAllLines(Paths.get(outputDirectory.toString(), "single_file"));
        //assertEquals("Hello World jportal_example_db",lines.get(0));
    }

}

