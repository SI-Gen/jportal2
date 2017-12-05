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

public class IdlJRWCode extends Generator
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
  private static void readCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("      " + var + " = reader.getByte();");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.println("      " + var + " = reader.getString(" + (field.length) + ");");
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.println("      " + var + " = reader.getDateTime(" + (field.length) + ");");
        break;
      case Field.MONEY:
        outData.println("      " + var + " = reader.getDecimal(21);");
        break;
      case Field.BLOB:
        outData.println("      " + var + " = reader.getJPBlob(" + field.length + ");");
        break;
      case Field.XML:
        outData.println("      " + var + " = reader.getJPXML(" + (field.length) + ");");
        break;
      case Field.BOOLEAN:
        outData.println("      " + var + " = reader.getBoolean();");
        break;
      case Field.BYTE:
      case Field.STATUS:
        outData.println("      " + var + " = reader.getByte();");
        break;
      case Field.SHORT:
        outData.println("      " + var + " = reader.getShort();");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          outData.println("      " + var + " = reader.getDecimal(" + (field.precision + 3) + ");");
        else
          outData.println("      " + var + " = reader.getDouble();");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      " + var + " = reader.getInt();");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      " + var + " = reader.getLong();");
        break;
      default:
        outData.println("      // " + var + " unsupported");
        break;
    }
  }
  private static void writeCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("      writer.putByte(" + var + ");");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.println("      writer.putString(" + var + ", " + (field.length) + ");");
        break;
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
        outData.println("      writer.putDateTime(" + var + ", " + (field.length) + ");");
        break;
      case Field.MONEY:
        outData.println("      writer.putDecimal(" + var + ", 21);");
        break;
      case Field.BLOB:
        outData.println("      writer.putJPBlob(" + var + ", " + (field.length) + ");");
        break;
      case Field.XML:
        outData.println("      writer.putJPXML(" + var + ", " + (field.length) + ");");
        break;
      case Field.BOOLEAN:
        outData.println("      writer.putBoolean(" + var + ");");
        break;
      case Field.BYTE:
      case Field.STATUS:
        outData.println("      writer.putByte(" + var + ");");
        break;
      case Field.SHORT:
        outData.println("      writer.putShort(" + var + ");");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          outData.println("      writer.putDecimal(" + var + ", " + (field.precision + 3) + ");");
        else
          outData.println("      writer.putDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      writer.putInt(" + var + ");");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      writer.putLong(" + var + ");");
        break;
      default:
        outData.println("      //" + var + " unsupported");
        break;
    }
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
          outData.println("using bbd.crackle.rw;");
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
      generateTableRec(table.fields, table.useName(), outData);
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
      generateProcRec(proc, table.useName() + proc.upperFirst(), outData);
    }
  }
  private static void generateTableRec(Vector<?> fields, String mainName, PrintWriter outData)
  {
    outData.println("  public partial class " + mainName + "Rec");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("    " + fieldDef(field));
      if (isNull(field) == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    public bool " + field.useLowerName() + "IsNull;");
    }
    outData.println("    public void read(Reader reader) throws Exception");
    outData.println("    {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      readCall(field, outData);
    }
    outData.println("    }");
    outData.println("    public void write(Writer writer) throws Exception");
    outData.println("    {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      writeCall(field, outData);
    }
    outData.println("    }");
    outData.println("  }");
  }
  private static void generateProcRec(Proc proc, String mainName, PrintWriter outData)
  {
    outData.println("  public partial class " + mainName + "Rec");
    outData.println("  {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
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
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      String s = (String)proc.dynamics.elementAt(i);
      Integer n = (Integer)proc.dynamicSizes.elementAt(i);
      Field field = new Field();
      field.name = s;
      field.type = Field.CHAR;
      field.length = n.intValue();
      outData.println("    " + fieldDef(field));
    }
    outData.println("    public void read(Reader reader) throws Exception");
    outData.println("    {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      readCall(field, outData);
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      readCall(field, outData);
    }
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      Integer n = (Integer)proc.dynamicSizes.elementAt(i);
      Field field = new Field();
      field.name = (String)proc.dynamics.elementAt(i);;
      field.type = Field.CHAR;
      field.length = n.intValue();
      readCall(field, outData);
    }
    outData.println("    }");
    outData.println("    public void write(Writer writer) throws Exception");
    outData.println("    {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      writeCall(field, outData);
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      writeCall(field, outData);
    }
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      Integer n = (Integer)proc.dynamicSizes.elementAt(i);
      Field field = new Field();
      field.name = (String)proc.dynamics.elementAt(i);;
      field.type = Field.CHAR;
      field.length = n.intValue();
      writeCall(field, outData);
    }
    outData.println("    }");
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
        outData.println("  public partial class " + table.useName() + field.useUpperName() + "Ord");
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
      case Field.XML:
        result = "JPXML";
        newer = " = new JPXML()";
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
    return "public " + result + " " + field.useLowerName() + newer + ";"
      + " public " + result + " " + field.useUpperName()
      + " { get { return " + field.useLowerName() + ";}"
      + " set { " + field.useLowerName() + " = value; } }";
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
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.TIME:
        return true;
    }
    //Field.BLOB
    //Field.XML
    return false;
  }
}
