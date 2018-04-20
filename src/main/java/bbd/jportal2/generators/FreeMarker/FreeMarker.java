/// ------------------------------------------------------------------
/// Copyright (c) from 1996 Vincent Risi 
///                           
/// All rights reserved. 
/// This program and the accompanying materials are made available 
/// under the terms of the Common Public License v1.0 
/// which accompanies this distribution and is available at 
/// http://www.eclipse.org/legal/cpl-v10.html 
/// Contributors:
///    Vincent Risi
/// ------------------------------------------------------------------

package bbd.jportal2.generators.FreeMarker;

import bbd.jportal2.AdvancedGenerator;
import bbd.jportal2.Database;
import bbd.jportal2.Main;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static freemarker.log.Logger.selectLoggerLibrary;


public class FreeMarker extends AdvancedGenerator {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    //  /**
//   * Reads input from stored repository
//   */
//  public static void main(String[] args)
//  {
//    try
//    {
//      for (int i = 0; i < args.length; i++)
//      {
//        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
//        Database database = (Database)in.readObject();
//        in.close();
//        i++;
//        Map<String,String> params = new HashMap<>();
//        params.put("Template",args[i]);
//        generateAdvanced(database, params,"");
//      }
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
//  }
    public static String description() {
        return "Generate according to a given FreeMarker template.";
    }

    public static String documentation() {
        return "Generate according to a given FreeMarker template. Usage is TODO";
    }

    /**
     * Generates code using a given FreeMarker template
     */
    //public static void generateAdvanced(Database database, Map<String,String> parameters, File outputDirectory) throws IOException, TemplateException
    public static void generateAdvanced(Database database, String templateBaseDir, String generatorName, File outputDirectory) throws IOException {

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.ftl");
        Path fullGeneratorPath = Paths.get(templateBaseDir, generatorName);
        Files.walkFileTree(Paths.get(fullGeneratorPath.toString()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (matcher.matches(file)) {
                    //Subtract source directory from found path
                    Path pathToTemplateBaseDir = Paths.get(templateBaseDir);
                    Path relativePathToFTL = fullGeneratorPath.relativize(file);
                    try {
                        GenerateTemplate(templateBaseDir, generatorName, relativePathToFTL.toString(), outputDirectory, database);
                    } catch (Exception te) {
                        throw new RuntimeException(te);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
            //Empty string

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

    }

    private static Configuration configure(File templateDir) throws IOException {
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.25) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);


        // Specify the source where the template files come from. We allow use of the built-in
        //templates (inside of src/main/java/bbd.jportal2/generators/FreeMarker/FreeMarkerHelpers
        //as well as the location specified by the user
        FileTemplateLoader ftl1 = new FileTemplateLoader(templateDir);
        ClassTemplateLoader ctl = new ClassTemplateLoader(FreeMarker.class, "templates");
        MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[]{ftl1, ctl});
        cfg.setTemplateLoader(mtl);


        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        cfg.setLogTemplateExceptions(false);
        return cfg;
    }

    private static void GenerateTemplate(String templateBaseDir, String generatorName, String templateName, File outputDir, Database database) throws TemplateException, IOException, ClassNotFoundException {

        freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_SLF4J);
        Configuration cfg = configure(new File(templateBaseDir));


        //Set up FreeMarker object maps
        java.util.Map<String, Object> root = new HashMap<>();
        root.put("database", database);

        // Create the builder:
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_23);
        // Set desired BeansWrapper configuration properties:
        builder.setUseModelCache(true);
        builder.setExposeFields(true);
        builder.setExposureLevel(BeansWrapper.EXPOSE_ALL);
        BeansWrapper wrapper = builder.build();

        //This is a bit crappy of FreeMarker. It doesn't expose static and enum members of classes,
        //so we need to manually expose them. To access, use such as:
        //${STATICS.Field.BLOB} to access the static member BLOB, defined in the Field class.
        //${ENUMS.Field.BLOB} to access the static member BLOB, defined in the Field class.

        //Expose static variables
        root.put("STATICS", new HashMap<String, TemplateModel>());
        TemplateHashModel staticModels = wrapper.getStaticModels();
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("Database", staticModels.get("bbd.jportal2.Database"));
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("Table", staticModels.get("bbd.jportal2.Table"));
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("Field", staticModels.get("bbd.jportal2.Field"));
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("PlaceHolder", staticModels.get("bbd.jportal2.PlaceHolder"));


        //Expose enums
        root.put("ENUMS", new HashMap<String, TemplateModel>());
        TemplateHashModel enumModels = wrapper.getEnumModels();
        ((HashMap<String, TemplateModel>) root.get("ENUMS")).put("Database", enumModels.get("bbd.jportal2.Database"));
        ((HashMap<String, TemplateModel>) root.get("ENUMS")).put("Table", enumModels.get("bbd.jportal2.Table"));
        ((HashMap<String, TemplateModel>) root.get("ENUMS")).put("Field", enumModels.get("bbd.jportal2.Field"));
        ((HashMap<String, Object>) root.get("ENUMS")).put("PlaceHolder", enumModels.get("bbd.jportal2.PlaceHolder"));


        String destFileName;
        Path templateRelativePath = Paths.get(templateName);
        destFileName = templateRelativePath.toFile().getName().replaceAll(".ftl", "");

        //Replace variables in filename with correct values
        Template fileNameTemplate = new Template("fileNameTemplate", new StringReader(destFileName), cfg);
        Writer fileNameOut = new StringWriter();
        fileNameTemplate.process(root, fileNameOut);


        Path destDir;
        Path fullGeneratorPath = Paths.get(templateBaseDir, generatorName);
        if (templateRelativePath.getParent() == null)
            destDir = Paths.get("");
        else
            destDir = templateRelativePath.getParent();


        destFileName = fileNameOut.toString();

        //Create directory if it doesn't exist.
        Path fullDestinationPath = Paths.get(outputDir.toString(), destDir.toString());
        fullDestinationPath.toFile().mkdirs();


        Path fullTemplatePath = Paths.get(fullGeneratorPath.toString(), templateName);
        Path fullDestinationFile = Paths.get(fullDestinationPath.toString(), destFileName);

        Path templateFileFullLocation = Paths.get(generatorName, templateName);
        Template temp = cfg.getTemplate(templateFileFullLocation.toString());
        logger.info("\t [{}]: Generating [{}]", generatorName, fullDestinationFile.toString());
        try (OutputStream outFile = new FileOutputStream(fullDestinationFile.toString())) {
            PrintWriter outData = new PrintWriter(outFile);
            temp.process(root, outData);
        }

    }
//  /**
//   * @param database
//   * @param table
//   * @param outData
//   */
//  private static String tableOwner;
//  private static void generateTable(Database database, Table table, PrintWriter outData)
//  {
//    if (database.schema.length() > 0)
//      tableOwner = database.schema + "";
//    else
//      tableOwner = "";
//    String comma = "( ";
//    boolean hasNotNull = false;
//    if (table.fields.size() > 0)
//    {
//      outData.println("DROP TABLE " + tableOwner + table.name + " CASCADE;");
//      outData.println();
//      outData.println("CREATE TABLE " + tableOwner + table.name);
//      for (int i = 0; i < table.fields.size(); i++, comma = ", ")
//      {
//        Field field = (Field)table.fields.elementAt(i);
//        outData.println(comma + field.name + " " + varType(field));
//        if (field.defaultValue.length() > 0)
//          hasNotNull = true;
//        if (field.checkValue.length() > 0)
//          hasNotNull = true;
//        else if (!field.isNull)
//          hasNotNull = true;
//      }
//      outData.print(")");
//      for (int i = 0; i < table.options.size(); i++)
//      {
//        String option = (String)table.options.elementAt(i);
//        if (option.toLowerCase().indexOf("tablespace") == 0)
//        {
//          outData.println();
//          outData.print(option);
//        }
//      }
//      outData.println(";");
//      outData.println();
//      for (int i = 0; i < table.grants.size(); i++)
//      {
//        Grant grant = (Grant)table.grants.elementAt(i);
//        generateGrant(grant, outData, tableOwner + table.name);
//      }
//      for (int i = 0; i < table.keys.size(); i++)
//      {
//        Key key = (Key)table.keys.elementAt(i);
//        if (!key.isPrimary && !key.isUnique)
//          generateIndex(table, key, outData);
//      }
//    }
//    for (int i = 0; i < table.views.size(); i++)
//    {
//      View view = (View)table.views.elementAt(i);
//      generateView(view, outData, table.name, tableOwner);
//    }
//    if (hasNotNull == true)
//    {
//      String alterTable = "ALTER TABLE " + tableOwner + table.name;
//      for (int i = 0; i < table.fields.size(); i++)
//      {
//        Field field = (Field)table.fields.elementAt(i);
//        if (field.isNull && field.defaultValue.length() == 0 && field.checkValue.length() == 0)
//          continue;
//        outData.print(alterTable + " ALTER " + field.name + " SET");
//        if (field.defaultValue.length() > 0)
//          outData.print(" DEFAULT " + field.defaultValue);
//        if (field.checkValue.length() > 0)
//          outData.print(" CHECK (" + field.checkValue + ")");
//        else
//          outData.print(" NOT NULL");
//        outData.println(";");
//      }
//      outData.println();
//    }
//    if (table.keys.size() > 0)
//    {
//      for (int i = 0; i < table.keys.size(); i++)
//      {
//        Key key = (Key)table.keys.elementAt(i);
//        if (key.isPrimary)
//        {
//          outData.println("ALTER TABLE " + tableOwner + table.name);
//          generatePrimary(table, key, outData);
//          outData.println(";");
//        }
//        else if (key.isUnique)
//        {
//          outData.println("ALTER TABLE " + tableOwner + table.name);
//          generateUnique(table, key, outData);
//          outData.println(";");
//        }
//      }
//      outData.println();
//    }
//    if (table.links.size() > 0)
//    {
//      for (int i = 0; i < table.links.size(); i++)
//      {
//        Link link = (Link)table.links.elementAt(i);
//        outData.println("ALTER TABLE " + tableOwner + table.name);
//        if (link.linkName.length() == 0)
//          link.linkName = table.name.toUpperCase() + "_FK" + bSO(i);
//        generateLink(link, tableOwner, outData);
//        outData.println(";");
//      }
//      outData.println();
//    }
//    for (int i = 0; i < table.procs.size(); i++)
//    {
//      Proc proc = (Proc)table.procs.elementAt(i);
//      if (proc.isData)
//        generateProc(proc, outData);
//    }
//  }
//  /**
//   * @param proc
//   * @param outData
//   */
//  private static void generateProc(Proc proc, PrintWriter outData)
//  {
//    for (int i = 0; i < proc.lines.size(); i++)
//    {
//      String l = proc.lines.elementAt(i).line;
//      outData.println(l);
//    }
//    outData.println();
//  }
//  /**
//   * @param link
//   * @param outData
//   * @param comma
//   */
//  private static void generateLink(Link link, String tableOwner, PrintWriter outData)
//  {
//    String comma = "  ( ";
//    outData.println(" ADD CONSTRAINT " + link.linkName + " FOREIGN KEY");
//    for (int i = 0; i < link.fields.size(); i++, comma = "  , ")
//    {
//      String name = (String)link.fields.elementAt(i);
//      outData.println(comma + name);
//    }
//    outData.println("  ) REFERENCES " + tableOwner + link.name);
//    if (link.linkFields.size() > 0)
//    {
//      comma = "  ( ";
//      for (int i = 0; i < link.linkFields.size(); i++, comma = "  , ")
//      {
//        String name = (String)link.linkFields.elementAt(i);
//        outData.println(comma + name);
//      }
//      outData.println("  )");
//    }
//    if (link.isDeleteCascade)
//      outData.println("  ON DELETE CASCADE");
//    if (link.isUpdateCascade)
//      outData.println("  ON UPDATE CASCADE");
//    //outData.println("  MATCH FULL");
//  }
//  /**
//   * @param table
//   * @param key
//   * @param outData
//   * @param comma
//   */
//  private static void generateUnique(Table table, Key key, PrintWriter outData)
//  {
//    String comma = "  ( ";
//    String keyname = key.name.toUpperCase();
//    if (keyname.indexOf(table.name.toUpperCase()) == -1)
//      keyname = table.name.toUpperCase() + "_" + keyname;
//    outData.println(" ADD CONSTRAINT " + keyname + " UNIQUE");
//    for (int i = 0; i < key.fields.size(); i++, comma = "  , ")
//    {
//      String name = (String)key.fields.elementAt(i);
//      outData.println(comma + name);
//    }
//    outData.println("  )");
//  }
//  /**
//   * @param table
//   * @param key
//   * @param outData
//   * @param comma
//   */
//  private static void generatePrimary(Table table, Key key, PrintWriter outData)
//  {
//    String comma = "  ( ";
//    String keyname = key.name.toUpperCase();
//    if (keyname.indexOf(table.name.toUpperCase()) == -1)
//      keyname = table.name.toUpperCase() + "_" + keyname;
//    outData.println(" ADD CONSTRAINT " + keyname + " PRIMARY KEY");
//    for (int i = 0; i < key.fields.size(); i++, comma = "  , ")
//    {
//      String name = (String)key.fields.elementAt(i);
//      outData.println(comma + name);
//    }
//    outData.print("  )");
//    for (int i = 0; i < key.options.size(); i++)
//    {
//      String option = (String)key.options.elementAt(i);
//      if (option.toLowerCase().indexOf("tablespace") == 0)
//      {
//        outData.print(" USING INDEX " + option);
//      }
//    }
//    outData.println();
//  }
//  /**
//   * @param i
//   * @return
//   */
//  private static String bSO(int i)
//  {
//    String x = "" + (101 + i);
//    return x.substring(1);
//  }
//  /**
//   * @param view
//   * @param outData
//   * @param name
//   * @param tableOwner
//   */
//  private static void generateView(View view, PrintWriter outData, String tableName, String tableOwner)
//  {
//    outData.println("CREATE OR REPLACE VIEW " + tableOwner + tableName + view.name);
//    if (view.aliases.size() > 0)
//    {
//      String comma = "( ";
//      for (int i = 0; i < view.aliases.size(); i++)
//      {
//        String alias = (String)view.aliases.elementAt(i);
//        outData.println(comma + alias);
//        comma = ", ";
//      }
//      outData.println(")");
//    }
//    outData.println("AS (");
//    for (int i = 0; i < view.lines.size(); i++)
//    {
//      String line = (String)view.lines.elementAt(i);
//      outData.println(line);
//    }
//    outData.println(");");
//    outData.println();
//    for (int i = 0; i < view.users.size(); i++)
//    {
//      String user = (String)view.users.elementAt(i);
//      outData.println("GRANT SELECT ON " + tableOwner + tableName + view.name + " TO " + user + ";");
//    }
//    outData.println();
//  }
//  /**
//   * @param table
//   * @param key
//   * @param outData
//   */
//  private static void generateIndex(Table table, Key key, PrintWriter outData)
//  {
//    String comma = "( ";
//    String keyname = key.name.toUpperCase();
//    if (keyname.indexOf(table.name.toUpperCase()) == -1)
//      keyname = table.name.toUpperCase() + "_" + keyname;
//    outData.println("-- DROP INDEX " + keyname + ";");
//    outData.println("");
//    outData.println("CREATE INDEX " + keyname + " ON " + tableOwner + table.name);
//    for (int i = 0; i < key.fields.size(); i++, comma = ", ")
//    {
//      String name = (String)key.fields.elementAt(i);
//      outData.println(comma + name);
//    }
//    outData.print(")");
//    for (int i = 0; i < key.options.size(); i++)
//    {
//      String option = (String)key.options.elementAt(i);
//      if (option.toLowerCase().indexOf("tablespace") == 0)
//      {
//        outData.println();
//        outData.print(option);
//      }
//    }
//    outData.println(";");
//    outData.println();
//  }
//  /**
//   * @param grant
//   * @param outData
//   * @param string
//   */
//  private static void generateGrant(Grant grant, PrintWriter outData, String on)
//  {
//    for (int i = 0; i < grant.perms.size(); i++)
//    {
//      String perm = (String)grant.perms.elementAt(i);
//      for (int j = 0; j < grant.users.size(); j++)
//      {
//        String user = (String)grant.users.elementAt(j);
//        outData.println("GRANT " + perm + " ON " + on + " TO " + user + ";");
//        outData.println();
//      }
//    }
//  }
//  /**
//   * @param field
//   * @return
//   */
//  private static String varType(Field field)
//  {
//    switch (field.type)
//    {
//      case Field.BYTE:
//        return "smallint";
//      case Field.SHORT:
//        return "smallint";
//      case Field.INT:
//        return "integer";
//      case Field.SEQUENCE:
//        return "serial";
//      case Field.BIGSEQUENCE:
//        return "bigserial";
//      case Field.LONG:
//        return "bigint";
//      case Field.CHAR:
//        return "varchar(" + String.valueOf(field.length) + ")";
//      case Field.ANSICHAR:
//        return "character(" + String.valueOf(field.length) + ") -- beware char to varchar morph";
//      case Field.DATE:
//        return "date";
//      case Field.DATETIME:
//        return "timestamp";
//      case Field.TIME:
//        return "time";
//      case Field.TIMESTAMP:
//        return "timestamp";
//      case Field.FLOAT:
//      case Field.DOUBLE:
//        if (field.precision == 0 && field.scale == 0) return "float8";
//        return "numeric(" + field.precision + ", " + field.scale + ")";
//      case Field.BLOB:
//        return "bytea";
//      case Field.TLOB:
//        return "text";
//      case Field.MONEY:
//        return "numeric(18,2)";
//      case Field.USERSTAMP:
//        return "VARCHAR(50)";
//      case Field.IDENTITY:
//        return "<not supported>";
//    }
//    return "unknown";
//  }
}
