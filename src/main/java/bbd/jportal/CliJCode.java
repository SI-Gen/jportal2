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
import java.io.File;

public class CliJCode extends Generator
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
        outLog.println(args[i] + ": generate Java code");
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
  /**
   * Generates the procedure classes for each table present.
   */
  public static String description()
  {
    return "generate Client Java code";
  }
  public static String documentation()
  {
    return "generate Client Java code";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    output = database.packageMerge(output);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table)database.tables.elementAt(i);
      generateStructs(table, output, outLog);
    }
  }
  private static String[] seperate(String value, String delim)
  {
    int n = 0, p = 0;
    for (; ; )
    {
      n++;
      int q = value.indexOf(delim, p);
      if (q < 0)
        break;
      p = q + 1;
    }
    String[] result = new String[n];
    p = 0;
    for (int i = 0; i < n; i++)
    {
      int q = value.indexOf(delim, p);
      if (q < 0)
        result[i] = value.substring(p);
      else
        result[i] = value.substring(p, q);
      p = q + 1;
    }
    return result;
  }
  private static String outputDir(Database database, String output)
  {
    StringBuffer result = new StringBuffer();
    String delim = "/";
    if (output.indexOf("\\") != -1)
      delim = "\\";
    String[] mp = seperate(database.packageName, "");
    String[] op = seperate(output, delim);
    int n = op.length - 1;
    int b = 1;
    if (op[0].charAt(1) == ':')
      b++;
    for (int oi = op.length - 1; oi >= b; oi--)
    {
      if (mp[0].compareTo(op[oi]) == 0)
      {
        n = oi;
        break;
      }
    }
    for (int oi = 0; oi < n; oi++)
    {
      result.append(op[oi]);
      result.append(delim);
    }
    for (int mi = 0; mi < mp.length; mi++)
    {
      result.append(mp[mi]);
      result.append(delim);
    }
    File f = new File(result.toString());
    f.mkdirs();
    return result.toString();
  }
  private static void stdReaderWriters(Table table, PrintWriter outData)
  {
    outData.println("    public void write(Writer writer) throws Exception");
    outData.println("    {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      String name = field.useLowerName();
      writeParms(field, outData);
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("      writer.putBoolean(" + name + "IsNull);writer.filler(6);");
    }
    outData.println("    }");
    outData.println("    public void read(Reader reader) throws Exception");
    outData.println("    {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      String name = field.useLowerName();
      readType(field, outData);
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("      " + name + "IsNull = reader.getBoolean();reader.skip(6);");
    }
    outData.println("    }");
  }
  private static final String ABCDEFGHIJKLMNOPQRSTUVWXYZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static String underScoreWords(String input)
  {
    char[] bits = input.toCharArray();
    StringBuffer buffer = new StringBuffer();
    buffer.append(bits[0]);
    for (int i = 1; i < bits.length; i++)
    {
      if (ABCDEFGHIJKLMNOPQRSTUVWXYZ.indexOf(bits[i]) >= 0
        && bits[i - 1] != ' ' && ABCDEFGHIJKLMNOPQRSTUVWXYZ.indexOf(bits[i - 1]) < 0)
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
  private static void generateEnum(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("  public static enum E" + field.useUpperName());
        outData.println("  {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = (Enum)field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)element.value + "'";
          String keyName = underScoreWords(element.name).toUpperCase();
          outData.println("    " + keyName + "(" + evalue + ", \"" + splitWords(element.name) + "\")"+ (((j+1) < field.enums.size()) ? "," : ";"));
        }
        outData.println("    public int key;");
        outData.println("    public String value;");
        outData.println("    E" + field.useUpperName() + "(int key, String value)");
        outData.println("    {");
        outData.println("      this.key = key;");
        outData.println("      this.value = value;");
        outData.println("    }");
        outData.println("    public static E" + field.useUpperName() + " get(int key)");
        outData.println("    {");
        outData.println("      for (E" + field.useUpperName() + " op : values())");
        outData.println("        if (op.key == key) return op;");
        outData.println("      return null;");
        outData.println("    }");
        outData.println("    public String toString()");
        outData.println("    {");
        outData.println("      return value;");
        outData.println("    }");
        outData.println("  }");
      }
    }
  }
  private static void otherOutputReaderWriters(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("    public void write(Writer writer) throws Exception");
    outData.println("    {");
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field)proc.outputs.elementAt(j);
      String name = field.useLowerName();
      writeParms(field, outData);
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("      writer.putBoolean(" + name + "IsNull);writer.filler(6);");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      Integer size = (Integer)proc.dynamicSizes.elementAt(j);
      int no = size.intValue();
      outData.print("      writer.putString(" + s + ", " + no + ");");
      int n = 8 - no % 8;
      outData.println("writer.filler(" + n + ");");
    }
    outData.println("    }");
    outData.println("    public void read(Reader reader) throws Exception");
    outData.println("    {");
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field)proc.outputs.elementAt(j);
      String name = field.useLowerName();
      readType(field, outData);
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("      " + name + "IsNull = reader.getBoolean();reader.skip(6);");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      Integer size = (Integer)proc.dynamicSizes.elementAt(j);
      int no = size.intValue();
      outData.print("      " + s + " = reader.getString(" + no + ");");
      int n = 8 - no % 8;
      outData.println("reader.skip(" + n + ");");
    }
    outData.println("    }");
  }
  private static void otherInputReaderWriters(Table table, Proc proc, PrintWriter outData, boolean useSuper)
  {

    outData.println("    public void write(Writer writer) throws Exception");
    outData.println("    {");
    if (useSuper)
      outData.println("      super.write(writer);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (proc.hasOutput(field.name))
        continue;
      writeParms(field, outData);
    }
    outData.println("    }");
    outData.println("    public void read(Reader reader) throws Exception");
    outData.println("    {");
    if (useSuper)
      outData.println("      super.read(reader);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (proc.hasOutput(field.name))
        continue;
      //String name = field.useLowerName();
      readType(field, outData);
    }
    outData.println("    }");
  }
  private static void generateStructs(Table table, String stdOutput, PrintWriter outLog)
  {
    try
    {
      String output = outputDir(table.database, stdOutput);
      outLog.println("Code: " + output + table.useName() + ".java");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
        if (table.database.packageName.length() > 0)
        {
          outData.println("package " + table.database.packageName.toLowerCase() + ";");
          outData.println();
        }
        outData.println("import bbd.crackle.rdc.*;");
        outData.println("import bbd.crackle.util.*;");
        outData.println();
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
        outData.println("public class " + table.useName());
        outData.println("{"); 
        if (table.fields.size() > 0)
          generateStdProcStruct(table, outData, outLog);
        generateOtherProcStructs(table, outData, outLog);
        outData.println("}");
        outData.flush();
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
  private static void generateStdProcStruct(Table table, PrintWriter outData, PrintWriter outLog)
  {
    outData.println("  public static class O" + table.useName());
    outData.println("  {");
    generateEnum(table, outData);
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.comments.size() > 0)
      {
        outData.println("    /**");
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("     *" + s);
        }
        outData.println("     */");
      }
      outData.println("    public " + javaVar(field) + ";");
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("    public boolean " + field.useLowerName() + "IsNull;");
    }
    outData.println("    public O" + table.useName() + "()");
    outData.println("    {");
    int maxSize = 0;
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.useLowerName().length() > maxSize)
        maxSize = field.useLowerName().length();
      outData.println("      " + initJavaVar(field));
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("      " + field.useLowerName() + "IsNull = false;");
    }
    outData.println("    }");
    if (table.fields.size() > 0)
    {
      outData.println("    public String toString()");
      outData.println("    {");
      outData.println("      String CRLF = (String) System.getProperty(\"line.separator\");");
      for (int i = 0; i < table.fields.size(); i++)
      {
        if (i == 0)
          outData.print("      return ");
        else
          outData.print("           + ");
        Field field = (Field)table.fields.elementAt(i);
        int no = maxSize - field.useLowerName().length();
        outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
      }
      outData.println("      ;");
      outData.println("    }");
    }
    stdReaderWriters(table, outData);
    outData.println("  }");
    outData.println("  public static class D" + table.useName() + " extends O" + table.useName());
    outData.println("  {");
    outData.println("  }");
  }
  private static void generateOtherProcStructs(Table table, PrintWriter outData, PrintWriter outLog)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if ((!proc.isStdExtended() && !proc.hasNoData()))
        generateOtherProcStruct(table, proc, outData, outLog);
    }
  }
  private static void generateOtherProcStruct(Table table, Proc proc, PrintWriter outData, PrintWriter outLog)
  {
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
    String extendsStd = "";
    boolean extendsUsed = false;
    if (proc.extendsStd == true && proc.useStd == false)
    {
      extendsStd = " extends D" + table.useName();
      extendsUsed = true;
    }
    int maxSize = 0;
    String ret = "      return ";
    if (proc.outputs.size() + proc.dynamics.size() > 0)
    {
      outData.println("  public static class O" + table.useName() + proc.upperFirst() + extendsStd);
      outData.println("  {");
      extendsStd = " extends O" + table.useName() + proc.upperFirst();
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field)proc.outputs.elementAt(j);
        if (field.useLowerName().length() > maxSize)
          maxSize = field.useLowerName().length();
        if (extendsUsed == true && table.hasField(field.name) == true)
          continue;
        outData.println("    public " + javaVar(field) + ";");
        if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
          outData.println("    public boolean " + field.useLowerName() + "IsNull;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        if (s.length() > maxSize)
          maxSize = s.length();
        outData.println("    public String " + s + ";");
      }
      outData.println("    public O" + table.useName() + proc.upperFirst() + "()");
      outData.println("    {");
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field)proc.outputs.elementAt(j);
        outData.println("      " + initJavaVar(field));
        if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
          outData.println("      " + field.useLowerName() + "IsNull = false;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        outData.println("      " + s + " = \"\";");
      }
      outData.println("    }");
      outData.println("    public String toString()");
      outData.println("    {");
      if (proc.outputs.size() + proc.dynamics.size() > 0)
      {
        outData.println("      String CRLF = (String) System.getProperty(\"line.separator\");");
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field)proc.outputs.elementAt(j);
          outData.print(ret);
          ret = "           + ";
          int no = maxSize - field.useLowerName().length();
          outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.print(ret);
          ret = "         + ";
          int no = maxSize - s.length();
          outData.println("\"  " + s + padded(no + 1) + ": \" + " + s + " + CRLF");
        }
        outData.println("      ;");
      }
      else
        outData.println("      return \"\";");
      outData.println("    }");
      otherOutputReaderWriters(table, proc, outData);
      outData.println("  }");
    }
    outData.println("  public static class D" + table.useName() + proc.upperFirst() + extendsStd);
    outData.println("  {");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (proc.hasOutput(field.name) == true)
        continue;
      if (field.useLowerName().length() > maxSize)
        maxSize = field.useLowerName().length();
      if (extendsUsed == true && table.hasField(field.name) == true)
        continue;
      if (field.comments.size() > 0)
      {
        outData.println("  /**");
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("   *" + s);
        }
        outData.println("   * (input)");
        outData.println("   */");
      }
      outData.println("    public " + javaVar(field) + ";");
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("  public boolean " + field.useLowerName() + "IsNull;");
    }
    outData.println("    public D" + table.useName() + proc.upperFirst() + "()");
    outData.println("    {");
    if (extendsStd.length() > 0)
      outData.println("      super();");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (proc.hasOutput(field.name))
        continue;
      outData.println("      " + initJavaVar(field));
      if (field.isNull == true && field.isCharEmptyOrAnsiAsNull() == false)
        outData.println("      " + field.useLowerName() + "IsNull = false;");
    }
    outData.println("    }");
    if (proc.inputs.size() > 0)
    {
      boolean useIt = false;
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field)proc.inputs.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        useIt = true;
        break;
      }
      if (useIt == true)
      {
        outData.println("    public String toString()");
        outData.println("    {");
        outData.println("      String CRLF = (String) System.getProperty(\"line.separator\");");
        if (extendsStd.length() > 0)
        {
          outData.println("      return super.toString()");
          ret = "         + ";
        }
        else
          ret = "      return ";
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field)proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          outData.print(ret);
          ret = "         + ";
          int no = maxSize - field.useLowerName().length();
          outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
        }
        outData.println("      ;");
        outData.println("    }");
      }
      otherInputReaderWriters(table, proc, outData, extendsStd.length() > 0);
    }
    outData.println("  }");
  }
  /**
   * Translates field type to java data member type
   */
  private static String javaVar(Field field)
  {
    String name = field.useLowerName();
    switch (field.type)
    {
      case Field.BYTE:
        return "byte " + name;
      case Field.SHORT:
        return "short " + name;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "int " + name;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "long " + name;
      case Field.ANSICHAR:
        //if (field.length == 1)
        //  return "char " + name;
      case Field.CHAR:
        return "String " + name;
      case Field.DATE:
        return "String " + name;
      case Field.DATETIME:
        return "String " + name;
      case Field.TIME:
        return "String " + name;
      case Field.TIMESTAMP:
        return "String " + name;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "String " + name;
        else
          return "double " + name;
      case Field.BLOB:
        return "JPBlob " + name;
      case Field.TLOB:
        return "String " + name;
      case Field.MONEY:
        return "String " + name;
      case Field.USERSTAMP:
        return "String " + name;
    }
    return "unknown";
  }
  private static void writeParms(Field field, PrintWriter outData)
  {
    String name = field.useLowerName();
    switch (field.type)
    {
      case Field.BYTE:
      case Field.SHORT:
        outData.println("      writer.putShort(" + name + ");writer.filler(6);");
        break;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        outData.println("      writer.putInt(" + name + ");writer.filler(4);");
        break;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        outData.println("      writer.putLong(" + name + ");");
        break;
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("      writer.putString(" + name + ", 1);writer.filler(7);");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
        outData.print("      writer.putString(" + name + ", " + (field.length) + ");");
        int n = 8 - (field.length) % 8;
        outData.println("writer.filler(" + n + ");");
        break;
      case Field.USERSTAMP:
        outData.println("      writer.putString(" + name + ", 8);writer.filler(8);");
        break;
      case Field.DATE:
        outData.println("      writer.putString(" + name + ", 8);writer.filler(8);");
        break;
      case Field.DATETIME:
        outData.println("      writer.putString(" + name + ", 14);writer.filler(2);");
        break;
      case Field.TIME:
        outData.println("      writer.putString(" + name + ", 6);writer.filler(2);");
        break;
      case Field.TIMESTAMP:
        outData.println("      writer.putString(" + name + ", 14);writer.filler(2);");
        break;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
        {
          outData.print("      writer.putString(" + name + ", " + (field.precision + 2) + ");");
          n = 8 - (field.precision + 2) % 8;
          outData.println("writer.filler(" + n + ");");
        }
        else
          outData.println("      writer.putDouble(" + name + ");");
        break;
      case Field.BLOB:
        outData.println("      " + name + ".write(writer);");
        break;
      case Field.MONEY:
        outData.println("      writer.putString(" + name + ", 20);writer.filler(4);");
        break;
    }
  }
  private static void readType(Field field, PrintWriter outData)
  {
    String name = field.useLowerName();
    switch (field.type)
    {
      case Field.BYTE:
        outData.println("      " + name + " = (byte)reader.getShort();reader.skip(6);");
        break;
      case Field.SHORT:
        outData.println("      " + name + " = reader.getShort();reader.skip(6);");
        break;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        outData.println("      " + name + " = reader.getInt();reader.skip(4);");
        break;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        outData.println("      " + name + " = reader.getLong();");
        break;
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("      " + name + " = reader.getString(1);reader.skip(7);");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
        outData.print("      " + name + " = reader.getString(" + (field.length) + ");");
        int n = 8 - (field.length) % 8;
        outData.println("reader.skip(" + n + ");");
        break;
      case Field.USERSTAMP:
        outData.println("      " + name + " = reader.getString(8);reader.skip(8);");
        break;
      case Field.DATE:
        outData.println("      " + name + " = reader.getString(8);reader.skip(8);");
        break;
      case Field.DATETIME:
        outData.println("      " + name + " = reader.getString(14);reader.skip(2);");
        break;
      case Field.TIME:
        outData.println("      " + name + " = reader.getString(6);reader.skip(2);");
        break;
      case Field.TIMESTAMP:
        outData.println("      " + name + " = reader.getString(14);reader.skip(2);");
        break;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
        {
          outData.print("      " + name + " = reader.getString(" + (field.precision + 2) + ");");
          n = 8 - (field.precision + 2) % 8;
          outData.println("reader.skip(" + n + ");");
        }
        else
          outData.println("      " + name + " = reader.getDouble();");
        break;
      case Field.BLOB:
        outData.println("      " + name + ".read(reader);");
        break;
      case Field.MONEY:
        outData.println("      " + name + " = reader.getString(20);reader.skip(4);");
        break;
    }
  }
  /**
   * returns the data member initialisation code (not always neccessary in java but
   * still we do it)
   */
  private static String initJavaVar(Field field)
  {
    String name = field.useLowerName();
    return name + " = " + defaultValue(field) + ";";
  }
  private static String defaultValue(Field field)
  {
    if (field.defaultValue.length() > 0)
      return field.defaultValue;
    switch (field.type)
    {
      case Field.BYTE:
        return "0";
      case Field.ANSICHAR:
        if (field.length == 1)
          return "\"\"";
      case Field.CHAR:
        return "\"\"";
      case Field.DATE:
        return "\"\"";
      case Field.DATETIME:
        return "\"\"";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "\"0.0\"";
        return "0.0";
      case Field.BLOB:
        return "new JPBlob(" + field.length + ")";
      case Field.TLOB:
        return "\"\"";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "0";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "0";
      case Field.MONEY:
        return "\"0.0\"";
      case Field.SHORT:
        return "0";
      case Field.TIME:
        return "\"\"";
      case Field.TIMESTAMP:
        return "\"\"";
      case Field.USERSTAMP:
        return "\"\"";
    }
    return "\"\"";
  }
  private static String padString = "                                                         ";
  private static String padded(int size)
  {
    if (size <= 0)
      return "";
    if (size > padString.length())
      size = padString.length();
    return padString.substring(0, size);
  }
}
