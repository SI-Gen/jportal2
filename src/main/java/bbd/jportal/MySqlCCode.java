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
package bbd.jportal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

public class MySqlCCode extends Generator
{
  /**
   * Reads input from stored repository
   */
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i < args.length; i++)
      {
        outLog.println(args[i] + ": Generate MySql C Code");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
        in.close();
        generate(database, "", outLog);
      }
      outLog.flush();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  public static String description()
  {
    return "Generate MySql C Code";
  }
  public static String documentation()
  {
    return "Generate MySql C Code";
  }
  /**
   * Padder function
   */
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  /**
   * Generates the procedure classes for each table present.
   */
  public static void generate(Database database, String output,
      PrintWriter outLog)
  {
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output, outLog);
    }
  }
  static String fileName(String output, String node, String ext)
  {
    node = node.toLowerCase();
    return output + node + ext;
  }
  /**
   * Build of standard and user defined procedures
   */
  static void generate(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + fileName(output, table.useName().toLowerCase(), ".h"));
      OutputStream outFile = new FileOutputStream(fileName(output, table.useName().toLowerCase(), ".h"));
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("/* ---------------------------------------------------------------------------------");
          outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println(" * ---------------------------------------------------------------------------------");
          outData.println(" */");
          outData.println("#ifndef " + table.useName().toLowerCase() + "H");
          outData.println("#define " + table.useName().toLowerCase() + "H");
          outData.println();
          outData.println("#include <stddef.h>");
          outData.println("#include \"myapi.h\"");
          outData.println();
          if (table.hasStdProcs)
            generateStdOutputRec(table, outData);
          generateUserOutputRecs(table, outData);
          generateInterface(table, outData);
          outData.println("#endif");
          outData.flush();
        } finally
        {
          outData.flush();
        }
        outFile.close();
        outLog.println("Code: " + fileName(output, table.useName().toLowerCase(), ".c"));
        outFile = new FileOutputStream(fileName(output, table.useName().toLowerCase(), ".c"));
        outData = new PrintWriter(outFile);
        try
        {
          outData.println("/* ---------------------------------------------------------------------------------");
          outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println(" * ---------------------------------------------------------------------------------");
          outData.println(" */");
          outData.println();
          outData.println("#include \"" + fileName("", table.useName().toLowerCase(), ".h")
              + "\"");
          outData.println();
          generateImplementation(table, outData);
        } finally
        {
          outData.flush();
        }
      } finally
      {
        outFile.close();
      }
    } catch (IOException e1)
    {
      outLog.println("Generate Procs IO Error");
    }
  }
  /**
   * Build of output data record for standard procedures
   */
  static String structName = "";
  static void generateStdOutputRec(Table table, PrintWriter outData)
  {
    if (table.comments.size() > 0)
    {
      outData.println("/**");
      for (int i = 0; i < table.comments.size(); i++)
      {
        String s = (String)table.comments.elementAt(i);
        outData.println(" *" + s);
      }
      outData.println(" */");
    }
    outData.println("typedef struct S_" + table.useName());
    outData.println("{");
    Vector<Field> fields = table.fields;
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (field.comments.size() > 0)
      {
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  /*" + s + " */");
        }
      }
      outData.println("  " + cppVar(field) + ";");
      if (isNull(field))
        outData.println("  int    " + field.useName() + "_is_null;");
    }
    outData.println("} T_" + table.useName() + ";");
    outData.println();
  }
  /**
   * Build of output data record for user procedures
   */
  static void generateUserOutputRecs(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStdExtended() || proc.hasNoData())
        continue;
      if (proc.comments.size() > 0)
      {
        outData.println("/**");
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = (String)proc.comments.elementAt(j);
          outData.println(" *" + comment);
        }
        outData.println(" */");
      }
      Vector<Field> fields;
      fields = proc.outputs;
      outData.println("typedef struct S_" + table.useName() + "_" + proc.name.toUpperCase());
      outData.println("{");
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field)fields.elementAt(j);
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("  /*" + s + "*/");
        }
        outData.println("  " + cppVar(field) + ";");
        if (isNull(field))
          outData.println("  "
              + "int    " + field.useName() + "_is_null;");
      }
      fields = proc.inputs;
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field)fields.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("  /*" + s + " */");
        }
        outData.println("  " + cppVar(field) + ";");
        if (isNull(field))
          outData.println("  int    " + field.useName() + "_is_null;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        Integer n = (Integer)proc.dynamicSizes.elementAt(j);
        outData.println("  char " + s + "[" + n.intValue() + "];");
      }
      outData.println("}  T_" + table.useName() + "_" + proc.name.toUpperCase() + ";");
      outData.println();
    }
  }
  static void generateInterface(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateInterface(table, proc, outData);
    }
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateInterface(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStdExtended())
      dataStruct = "T_" + table.useName();
    else
      dataStruct = "T_" + table.useName() + "_" + proc.name.toUpperCase();
    String query = "";
    if (proc.outputs.size() > 0)
      query = ", DB_QUERY *query";
    outData.print("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase() + "_execute");
    if (proc.hasNoData() || (proc.inputs.size() == 0 && proc.dynamics.size() == 0))
      outData.println("(DB_CONNECTOR *connector" + query + ");");
    else
      outData.println("(DB_CONNECTOR *connector" + query + ", " + dataStruct + " *data);");
    if (proc.outputs.size() > 0)
    {
      outData.println("/**");
      outData.println(" * Return of -1 indicates MySql error.");
      outData.println(" *            0 indicates no (more) data found.");
      outData.println(" *            1 indicates data found.");
      outData.println(" */");
      outData.println("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase()
          + "_fetch(DB_QUERY *query, " + dataStruct + " *data);");
      if (proc.isSingle == false)
      {
        outData.println("/**");
        outData.println(" * This function loads the full result associated with 'data'.");
        outData.println(" * Use with the data_seek function below, row will populate 'data' supplied here.");
        outData.println(" */");
        outData.println("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase()
            + "_load_result(DB_QUERY *query, " + dataStruct + " *data);");
        outData.println("/**");
        outData.println(" * Use in conjunction with the load_result function above.");
        outData.println(" * The 'data' record will be populated with the result row at offset.");
        outData.println(" */");
        outData.println("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase()
            + "_data_seek(DB_QUERY *query, my_ulonglong offset);");
      }
    }
    outData.println();
  }
  /**
   * 
   */
  static void generateImplementation(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateImplementation(table, proc, outData);
    }
  }
  static public PlaceHolder placeHolder;
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    Vector<String> lines = placeHolder.getLines();
    if (lines.size() > 0)
    {
      outData.println("static char* "+proc.table.useName().toUpperCase()+"_"+proc.name.toUpperCase()+"_COMMAND =");
      for (int i = 0; i < lines.size(); i++)
      {
        String l = (String) lines.elementAt(i);
        if (l.charAt(0) != '"')
          outData.println("\"[>" + l.trim() + "<]\\n\"");
        else
          outData.println(l+"\"\\n\"");
      }
      outData.println(";");
      int dynamicsize = placeHolder.getTotalDynamicSize();
      outData.println("#define "+proc.table.useName().toUpperCase()+"_"
        +proc.name.toUpperCase()+"_DYNAMIC_SIZE "+dynamicsize);
    }
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    generateCommand(proc, outData);
    Vector<PlaceHolderPairs> pairs = placeHolder.getPairs();
    String NOBINDS = table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_NOBINDS";
    String NODEFINES = table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_NODEFINES";
    outData.println("#define " + NOBINDS + " " + pairs.size());
    outData.println("#define " + NODEFINES + " " + proc.outputs.size());
    outData.println();
    String dataStruct;
    if (proc.isStdExtended())
      dataStruct = "T_" + table.useName();
    else
      dataStruct = "T_" + table.useName() + "_" + proc.name.toUpperCase();
    String query = "";
    if (proc.outputs.size() > 0)
      query = ", DB_QUERY *query";
    outData.print("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase() + "_execute");
    if (proc.hasNoData() || (proc.inputs.size() == 0 && proc.dynamics.size() == 0))
      outData.println("(DB_CONNECTOR *connector" + query + ")");
    else
      outData.println("(DB_CONNECTOR *connector" + query + ", " + dataStruct + " *data)");
    outData.println("{");
    outData.println("  int rc = 0;");
    if (proc.outputs.size() == 0)
    {
      outData.println("  DB_QUERY _query;");
      outData.println("  DB_QUERY *query = &_query;");
    }
    else if (proc.inputs.size() == 0 && proc.dynamics.size() == 0)
    {
      outData.println("  " + dataStruct + " *data;");
      outData.println("  int data_length = sizeof(" + dataStruct + ");");
    }
    String COMMAND = proc.table.useName().toUpperCase() + "_" + proc.name.toUpperCase() + "_COMMAND";
    String DYNAMIC_SIZE = proc.table.useName().toUpperCase() + "_" + proc.name.toUpperCase() + "_DYNAMIC_SIZE";
    outData.println("  rc = db_query_init(query, connector");
    outData.println("                   , " + COMMAND);
    outData.println("                   , " + DYNAMIC_SIZE);
    outData.println("                   , " + NOBINDS);
    outData.println("                   , " + NODEFINES);
    outData.println("                   );");
    for (int i = 0; i < proc.dynamics.size(); i++) 
    {
      String var = (String) proc.dynamics.elementAt(i);
      outData.println("  if (rc == 0) rc = db_query_dynamic(query, \"[>"+var+"<]\", data->"+ var+");");
    }
    for (int i = 0; i < pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)pairs.elementAt(i);
      Field field = pair.field;
      String nullField = "0";
      if (isNull(field) == true)
        nullField = "&data->" + field.useName() + "_is_null";
      outData.println("  if (rc == 0) rc = db_query_bind_" + bindType(field) + "(query, " + i
        + ", &data->" + field.useName() + bindLength(field)
        + ", " + nullField       
        + ");");
    }
    outData.println("  if (rc == 0) rc = db_query_execute(query);");
    if (proc.outputs.size() == 0)
      outData.println("  if (rc == 0) rc = db_query_close(query);");
    outData.println("  return rc;");
    outData.println("}");
    outData.println();
    if (proc.outputs.size() > 0)
    {
      outData.println("/**");
      outData.println(" * Return of -1 indicates MySql error.");
      outData.println(" *            0 indicates no (more) data found.");
      outData.println(" *            1 indicates data found.");
      outData.println(" */");
      outData.println("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase()
          + "_fetch(DB_QUERY *query, " + dataStruct + " *data)");
      outData.println("{");
      outData.println("  int rc = 0;");
      outData.println("  int rebind = 0;");
      outData.println("  if (data != query->data)");
      outData.println("  {");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        String nullField = "0";
        if (isNull(field) == true)
          nullField = "&data->" + field.useName() + "_is_null";
        outData.println("    if (rc == 0) rc = db_query_define_" + bindType(field) + "(query, " + i
          + ", &data->" + field.useName() + bindLength(field)
          + ", " + nullField
          + ");");
      }
      outData.println("    query->data = data;");
      outData.println("    rebind = 1;");
      outData.println("  }");
      outData.println("  if (rc == 0) return db_query_fetch(query, rebind);");
      outData.println("  return -1;");
      outData.println("}");
      outData.println();
      if (proc.isSingle == false)
      {
        outData.println("/**");
        outData.println(" * This function loads the full result associated with 'data'.");
        outData.println(" * Use with the data_seek function below, row will populate 'data' supplied here.");
        outData.println(" */");
        outData.println("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase()
            + "_load_result(DB_QUERY *query, " + dataStruct + " *data)");
        outData.println("{");
        outData.println("  int rc = 0;");
        for (int i = 0; i < proc.outputs.size(); i++)
        {
          Field field = (Field)proc.outputs.elementAt(i);
          String nullField = "0";
          if (isNull(field) == true)
            nullField = "&data->" + field.useName() + "_is_null";
          outData.println("  if (rc == 0) rc = db_query_define_" + bindType(field) + "(query, " + i
            + ", &data->" + field.useName() + bindLength(field)
            + ", " + nullField
            + ");");
        }
        outData.println("  query->data = data;");
        outData.println("  if (rc == 0) rc = db_query_load_result(query);");
        outData.println("  return rc;");
        outData.println("}");
        outData.println();
        outData.println("/**");
        outData.println(" * Use in conjunction with the load_result function above.");
        outData.println(" * The 'data' record will be populated with the result row at offset.");
        outData.println(" */");
        outData.println("int " + table.useName().toLowerCase() + "_" + proc.name.toLowerCase()
            + "_data_seek(DB_QUERY *query, int offset)");
        outData.println("{");
        outData.println("  db_query_data_seek(query, offset);");
        outData.println("  return 0;");
        outData.println("}");
        outData.println();
      }
    }  
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppVar(Field field)
  {
    switch (field.type)
    {
    case Field.BYTE:
      return "char   " + field.useName();
    case Field.SHORT:
      return "short  " + field.useName();
    case Field.BOOLEAN:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int    " + field.useName();
    case Field.LONG:
      return "long   " + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.USERSTAMP:
      return "char   " + field.useName() + "[51]";
    case Field.BLOB:
    case Field.TLOB:
      return "TJLob  " + field.useName();
    case Field.DATE:
      return "char   " + field.useName() + "[9]";
    case Field.TIME:
      return "char   " + field.useName() + "[7]";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char   " + field.useName() + "[15]";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double " + field.useName();
    }
    return field.useName() + " <unsupported>";
  }
  static boolean isNull(Field field)
  {
    if (field.isNull == false)
      return false;
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.FLOAT:
      case Field.DOUBLE:
      case Field.MONEY:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.IDENTITY:
      case Field.SEQUENCE:
      case Field.BLOB:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.TIME:
        return true;
    }
    return false;
  }
  static String bindLength(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.FLOAT:
      case Field.DOUBLE:
      case Field.MONEY:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        return "";
    }
    return "[0], " + field.length;
  }
  static String bindType(Field field)
  {
    switch (field.type)
    {
      case Field.BYTE:
        return "tinyint";
      case Field.SHORT:
        return "short";
      case Field.BOOLEAN:
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "int";
      case Field.LONG:
        return "long";
      case Field.CHAR:
      case Field.ANSICHAR:
        return "char";
      case Field.USERSTAMP:
        return "char";
      case Field.BLOB:
        return "blob";
      case Field.TLOB:
        return "tlob";
      case Field.DATE:
        return "date";
      case Field.TIME:
        return "time";
      case Field.DATETIME:
        return "datetime";
      case Field.TIMESTAMP:
        return "timestamp";
      case Field.FLOAT:
      case Field.DOUBLE:
        return "double";
      case Field.MONEY:
        return "double";
    }
    return field.useName() + " <unsupported>";
  }
}
