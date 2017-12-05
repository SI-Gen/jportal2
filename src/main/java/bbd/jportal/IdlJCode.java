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
import java.lang.*;
import java.util.Vector;

public class IdlJCode extends Generator
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
        outLog.println(args[i] + ": Generate IDL Code for 3 Tier Access");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database)in.readObject();
        in.close();
        generate(database, "", outLog);
      }
      outLog.flush();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  public static String description()
  {
    return "Generate IDL Code for 3 Tier Access";
  }
  public static String documentation()
  {
    return "Generate IDL Code for 3 Tier Access";
  }
  /**
  * Generates the procedure classes for each table present.
  */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table)database.tables.elementAt(i);
      generateStructs(table, output, outLog);
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      String namespace = table.database.packageName;
      outLog.println("Code: " + output + table.useName() + ".cs");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".cs");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("using System;");
          outData.println("using bbd.idl2;");
          outData.println("using bbd.idl2.Rpc;");
          outData.println();
          outData.println("namespace " + namespace);
          outData.println("{");
          generateStructs(table, outData);
          outData.println("}");
        }
        finally
        {
          outData.flush();
        }
      }
      finally
      {
        outFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate Procs IO Error");
    }
  }
  private static void generateStructs(Table table, PrintWriter outData)
  {
    if (table.fields.size() > 0)
    {
      if (table.comments.size() > 0)
      {
        outData.println("  /// <summary>");
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println("  /// " + s);
        }
        outData.println("  /// </summary>");
      }
      generateTableStructs(table.fields, table.useName(), outData);
      generateEnumOrdinals(table, outData);
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      if (proc.comments.size() > 0)
      {
        outData.println("  /// <summary>");
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String s = (String)proc.comments.elementAt(j);
          outData.println("  /// " + s);
        }
        outData.println("  /// </summary>");
      }
      generateStructPairs(proc, table.useName() + proc.upperFirst(), outData);
    }
  }
  private static void generateTableStructs(Vector<?> fields, String mainName, PrintWriter outData)
  {
    outData.println("  [Serializable()]");
    outData.println("  public class " + mainName + "Rec");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("    " + fieldDef(field));
      if (isNull(field) == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    public bool " + field.useLowerName() + "IsNull;");
    }
    outData.println("  }");
  }
  private static void generateStructPairs(Proc proc, String mainName, PrintWriter outData)
  {
    outData.println("  [Serializable()]");
    outData.println("  public class " + mainName + "Rec");
    outData.println("  {");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      outData.println("    " + fieldDef(field));
      if (isNull(field) == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    public bool " + field.useLowerName() + "IsNull;");
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      outData.println("    " + fieldDef(field));
      if (isNull(field) == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    public bool " + field.useLowerName() + "IsNull;");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      Integer n = (Integer)proc.dynamicSizes.elementAt(j);
      Field field = new Field();
      field.name = s;
      field.type = Field.CHAR;
      field.length = n.intValue();
      outData.println("    " + fieldDef(field));
    }
    outData.println("  }");
  }
  private static String underScoreWords(String input)
  {
    char[] bits = input.toCharArray();
    StringBuffer buffer = new StringBuffer();
    buffer.append(bits[0]);
    for (int i = 1; i < bits.length; i++)
    {
      if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(bits[i]) >= 0
        && bits[i - 1] != ' ')
      {
        buffer.append('_');
        buffer.append(bits[i]);
      }
      else
        buffer.append(bits[i]);
    }
    return buffer.toString();
  }
  private static String splitWords(String input)
  {
    char[] bits = underScoreWords(input).toCharArray();
    StringBuffer buffer = new StringBuffer();
    buffer.append(bits[0]);
    for (int i = 1; i < bits.length; i++)
    {
      if (bits[i] == '_')
        buffer.append(' ');
      else
        buffer.append(bits[i]);
    }
    return buffer.toString();
  }
  private static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("  public class " + table.useName() + field.useUpperName() + "Ord");
        outData.println("  {");
        String datatype = "Int32";
        if (field.type == Field.ANSICHAR && field.length == 1)
          datatype = "byte";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "(byte)'" + (char)en.value + "'";
          outData.println("    public const " + datatype + " " + en.name + " = " + evalue + ";");
        }
        outData.println("    public static string ToString(" + datatype + " ordinal)");
        outData.println("    {");
        outData.println("      switch (ordinal)");
        outData.println("      {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "(byte)'" + (char)en.value + "'";
          outData.println("      case " + evalue + ": return \"" + splitWords(en.name) + "\";");
        }
        outData.println("      }");
        outData.println("      return \"unknown ordinal: \"+ordinal;");
        outData.println("    }");
        outData.println("  }");
      }
    }
  }
  private static String fieldDef(Field field)
  {
    String result = "";
    String newer = "";
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          result = "byte";
          break;
        } 
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
        result = "string";
        break;
      case Field.MONEY:
        result = "decimal";
        break;
      case Field.BLOB:
        result = "JPBlob";
        newer = " = new JPBlob()";
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        result = "DateTime";
        break;
      case Field.BOOLEAN:
        result = "bool";
        break;
      case Field.BYTE:
        result = "byte";
        break;
      case Field.STATUS:
        result = "short";
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          result = "decimal";
        else
          result = "double";
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        result = "int";
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        result = "long";
        break;
      case Field.SHORT:
        result = "short";
        break;
      case Field.DYNAMIC:
        return "public string " + field.useName();
      default:
        result = "whoknows";
        break;
    }
    return rpcAttributes(field)
      + "public " + result + " " + field.useLowerName() + newer + ";"
      + attributes(field)
      + " public " + result + " " + field.useUpperName()
      + " { get { return " + field.useLowerName() + ";}"
      + " set { " + field.useLowerName() + " = value; } }";
  }
  private static boolean isStringOrDate(Field field)
  {
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
          return false;
      case Field.CHAR:
      case Field.MONEY:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        return true;
      case Field.DOUBLE:
        return field.precision > 15;
      default:
        break;
    }
    return false;
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
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BLOB:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.TIME:
        return true;
    }
    return false;
  }
  private static String rpcAttributes(Field field)
  {
    if (field.type == Field.BLOB)
      return "";
    if (field.type == Field.ANSICHAR && field.length == 1)
      return "";
    int l = 0;
    if (field.type == Field.MONEY)
      l = 21;
    else if (field.type == Field.DOUBLE && field.precision > 15)
      l = field.precision + 3;
    else if (field.type == Field.USERSTAMP) // special case (used to be 8)
      l = 17;
    else if (isStringOrDate(field))
        l = field.length + 1;
    if (l != 0)
      return "[Field(Size=" + (l) + ")] ";
    if (field.type == Field.BLOB)
      return "[Field(Size=" + (field.length - 4) + ")] ";
    else
      return "";
  }
  private static String attributes(Field field)
  {
    String result = "";
    boolean continued = false;
    for (int i = 0; i < field.comments.size(); i++)
    {
      String comment = (String)field.comments.elementAt(i);
      int last = comment.length() - 1;
      if (continued == true || comment.charAt(0) == '[')
      {
        result += " " + comment;
        continued = comment.charAt(last) == ',';
      }
    }
    return result;
  }
}
