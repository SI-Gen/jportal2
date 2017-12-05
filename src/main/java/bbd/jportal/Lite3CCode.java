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

public class Lite3CCode extends Generator
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
        outLog.println(args[i] + ": Generate Lite3 C Code");
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
    return "Generate Lite3 C Code";
  }
  public static String documentation()
  {
    return "Generate Lite3 C Code";
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
          outData.println("// ---------------------------------------------------------------------------------");
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("// ---------------------------------------------------------------------------------");
          outData.println();
          outData.println("#ifndef " + table.useName().toLowerCase() + "H");
          outData.println("#define " + table.useName().toLowerCase() + "H");
          outData.println();
          outData.println("#include <stddef.h>");
          outData.println("#include \"lite3api.h\"");
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
        outLog.println("Code: " + fileName(output, table.useName().toLowerCase(), ".cpp"));
        outFile = new FileOutputStream(fileName(output, table.useName().toLowerCase(), ".cpp"));
        outData = new PrintWriter(outFile);
        try
        {
          outData.println("// ---------------------------------------------------------------------------------");
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("// ---------------------------------------------------------------------------------");
          outData.println();
          outData.println("#include \"" + fileName("", table.useName().toLowerCase(), ".h") + "\"");
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
    outData.println("struct " + table.useName());
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
          outData.println("  //" + s);
        }
      }
      outData.println("  " + cppVar(field) + ";");
      if (isNull(field))
        outData.println("  int    " + field.useName() + "IsNull;");
    }
    outData.println("  " + table.useName() + "()");
    outData.println("  {");
    outData.println("    memset(this, 0, sizeof(*this));");
    outData.println("  }");
    outData.println("};");
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
      outData.println("struct " + table.useName() + proc.upperFirst());
      outData.println("{");
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field)fields.elementAt(j);
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("  //" + s);
        }
        outData.println("  " + cppVar(field) + ";");
        if (isNull(field))
          outData.println("  "
              + "int    " + field.useName() + "IsNull;");
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
          outData.println("  //" + s);
        }
        outData.println("  " + cppVar(field) + ";");
        if (isNull(field))
          outData.println("  int    " + field.useName() + "IsNull;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        Integer n = (Integer)proc.dynamicSizes.elementAt(j);
        outData.println("  char " + s + "[" + n.intValue() + "];");
      }
      outData.println("  " + table.useName() + proc.upperFirst() + "()");
      outData.println("  {");
      outData.println("    memset(this, 0, sizeof(*this));");
      outData.println("  }");
      outData.println("};");
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
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    if (proc.hasNoData())
      outData.println("struct X" + table.useName() + proc.upperFirst());
    else
      outData.println("struct X" + table.useName() + proc.upperFirst() + " : public " + dataStruct);
    outData.println("{");
    outData.println("  Lite3Query query;");
    outData.println("  X" + table.useName() + proc.upperFirst() + "(Lite3Connector *connector)");
    outData.println("  {");
    outData.println("    query.connector = connector;");
    outData.println("  }");
    outData.println("  void Exec();");
    if (proc.outputs.size() > 0)
      outData.println("  bool Fetch();");
    if (proc.isSingle)
      outData.println("  bool ReadOne();");
    outData.println("};");
    outData.println();
  }
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
    placeHolder = new PlaceHolder(proc, PlaceHolder.COLON, "");
    Vector<String> lines = placeHolder.getLines();
    if (lines.size() > 0)
    {
      outData.println("const char* "+proc.table.useName()+proc.upperFirst()+"Command =");
      for (int i = 0; i < lines.size(); i++)
      {
        String l = lines.elementAt(i);
        if (l.charAt(0) != '"')
        {
          l = l.trim();
          String quotes = "";
          if (proc.isStrung(l) == true)
            quotes = "'";
          outData.println("\"" + quotes + "[>" + l + "<]" + quotes + "\\n\"");
        }
        else
          outData.println(l + "\"\\n\"");
      }
      outData.println(";");
      int dynamicsize = placeHolder.getTotalDynamicSize();
      outData.println("const int " + proc.table.useName() + proc.upperFirst() + "DynamicSize = " + dynamicsize + ";");
    }
  }
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    generateCommand(proc, outData);
    outData.println();
    if (proc.isStdExtended()) {
	} else {
	}
    outData.println("void X" + table.useName() + proc.upperFirst() + "::Exec()");
    outData.println("{");
    String COMMAND = proc.table.useName() + proc.upperFirst() + "Command";
    String DYNAMIC_SIZE = proc.table.useName() + proc.upperFirst() + "DynamicSize";
    outData.println("  query.init(" + COMMAND + ", " + DYNAMIC_SIZE + ");");
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      String var = (String)proc.dynamics.elementAt(i);
      outData.println("  query.dynamic(\"[>" + var + "<]\", " + var + ");");
    }
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      String nullField = "";
      if (isNull(field) == true)
        nullField = ", &" + field.useName() + "IsNull";
      outData.println("  query.bind" + bindType(field) + "(" + (i+1)
        + ", &" + field.useName() + bindLength(field)
        + nullField
        + ");");
    }
    if (proc.outputs.size() == 0)
    {
      outData.println("  query.exec();");
      outData.println("  query.close();");
    }
    outData.println("}");
    outData.println();
    if (proc.outputs.size() > 0)
    {
      outData.println("bool X" + table.useName() + proc.upperFirst() + "::Fetch()");
      outData.println("{");
      outData.println("  bool result = query.fetch();");
      outData.println("  if (result != false)");
      outData.println("  {");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        String nullField = "";
        if (isNull(field) == true)
          nullField = ", &" + field.useName() + "IsNull";
        outData.println("    query.get" + bindType(field) + "(" + i
          + ", &" + field.useName() + bindLength(field)
          + nullField
          + ");");
      }
      outData.println("  }");
      if (proc.isSingle)
        outData.println("  query.close();");
      outData.println("  return result;");
      outData.println("}");
      outData.println();
      if (proc.isSingle)
      {
        outData.println("bool X" + table.useName() + proc.upperFirst() + "::ReadOne()");
        outData.println("{");
        outData.println("  Exec();");
        outData.println("  return Fetch();");
        outData.println("}");
        outData.println();
      }
    }
  }
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
      return "char   " + field.useName() + "[9]";
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
        return "TinyInt";
      case Field.SHORT:
        return "Short";
      case Field.BOOLEAN:
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "Int";
      case Field.LONG:
        return "Long";
      case Field.CHAR:
      case Field.ANSICHAR:
        return "Char";
      case Field.USERSTAMP:
        return "Char";
      case Field.BLOB:
        return "Blob";
      case Field.TLOB:
        return "Char";
      case Field.DATE:
        return "Date";
      case Field.TIME:
        return "Time";
      case Field.DATETIME:
        return "DateTime";
      case Field.TIMESTAMP:
        return "TimeStamp";
      case Field.FLOAT:
      case Field.DOUBLE:
        return "Double";
      case Field.MONEY:
        return "Double";
    }
    return field.useName() + " <unsupported>";
  }
}
