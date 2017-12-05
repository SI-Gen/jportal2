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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.*;

public class JavaRWCode extends Generator
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
    return "generate Java rw code";
  }
  public static String documentation()
  {
    return "generate Java rw code";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    output = database.packageMerge(output);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table)database.tables.elementAt(i);
      generateStructs(table, output, outLog);
      generate(table, output, outLog);
    }
  }
  private static void readCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("    " + var + " = reader.getChar();");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.println("    " + var + " = reader.getString(" + (field.length) + ");");
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.println("    " + var + " = reader.getString(" + (field.length) + ");");
        break;
      case Field.MONEY:
        outData.println("    " + var + " = reader.getString(20);");
        break;
      case Field.BLOB:
        outData.println("    int _" + var + "_len = reader.getInt();");
        outData.println("    " + var + ".setBytes(reader.getString(_" + var + "_len).getBytes());");
        break;
      case Field.XML:
        outData.println("    " + var + ".len = reader.getInt();");
        outData.println("    " + var + ".data, reader.getBytes(" + (field.length - 4) + ");");
        break;
      case Field.BOOLEAN:
      case Field.BYTE:
        outData.println("    " + var + " = reader.getByte();");
        break;
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    " + var + " = reader.getShort();");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          outData.println("    " + var + " = reader.getString(" + (field.precision + 2) + ");");
        else
          outData.println("    " + var + " = reader.getDouble();");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("    " + var + " = reader.getInt();");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("    " + var + " = reader.getLong();");
        break;
      default:
        outData.println("    // " + var + " unsupported");
        break;
    }
    if (isNull(field) == true)
      outData.println("    " + var + "IsNull = reader.getByte() == 0 ? false : true;");

  }
  private static boolean isEmptyOrAnsiAsNull(Field field)
  {
    if (field.isNull == false) return false;
    switch (field.type)
    {
      case Field.ANSICHAR: if (field.length == 1) break;
      case Field.CHAR:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.BLOB:
      case Field.XML:
        return true;
    }
    return false;
  }
  private static boolean isNull(Field field)
  {
    return field.isNull == true && isEmptyOrAnsiAsNull(field) == false;
  }
  private static void writeCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("    writer.putChar(" + var + ");");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.println("    writer.putString(" + var + ", " + (field.length) + ");");
        break;
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
      case Field.DATE:
        outData.println("    writer.putString(" + var + ", " + (field.length) + ");");
        break;
      case Field.MONEY:
        outData.println("    writer.putString(" + var + ", 20);");
        break;
      case Field.BLOB:
        outData.println("    writer.putInt(" + var + ".length());");
        outData.println("    writer.putBytes(" + var + ".getBytes());");
        break;
      case Field.XML:
        outData.println("    writer.putInt(" + var + ".len);");
        outData.println("    writer.putBytes(" + var + ".data, " + (field.length - 4) + ");");
        break;
      case Field.BOOLEAN:
      case Field.BYTE:
        outData.println("    writer.putByte(" + var + ");");
        break;
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    writer.putShort(" + var + ");");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          outData.println("    writer.putString(" + var + ", " + (field.precision + 2) + ");");
        else
          outData.println("    writer.putDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("    writer.putInt(" + var + ");");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("    writer.putLong(" + var + ");");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
    if (isNull(field) == true)
      outData.println("    writer.putByte((byte)(" + var + "IsNull == true ? 1 : 0));");
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
    if (op[0].length() > 1 && op[0].charAt(1) == ':')
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
  /**
   * Build of standard and user defined procedures
   */
  private static void generate(Table table, String stdOutput, PrintWriter outLog)
  {
    try
    {
      String output = outputDir(table.database, stdOutput);
      outLog.println("Code: " + output + table.useName() + "Tab.java");
      OutputStream outFile = new FileOutputStream(output + table.useName() + "Tab.java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        generateStdProcs(table, outData);
        generateOtherProcs(table, outData, output, outLog);
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
  private static String extendsName;
  /**
   * Build of all required standard procedures
   */
  private static void generateStdCopiers(Table table, PrintWriter outData)
  {
    String extendsName = table.useName() + "Rec";
    outData.println("  public " + extendsName + " getCopy()");
    outData.println("  {");
    outData.println("    " + extendsName + " result = new " + extendsName + "();");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      String name = field.useLowerName();
      outData.println("    result." + name + " = " + name + ";");
    if (isNull(field) == true)
        outData.println("    result." + name + "IsNull = " + name + "IsNull;");
    }
    outData.println("    return result;");
    outData.println("  }");
    outData.println("  public void assign(" + extendsName + " _value)");
    outData.println("  {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      String name = field.useLowerName();
      outData.println("    " + name + " = _value." + name + ";");
      if (isNull(field) == true)
        outData.println("    " + name + "IsNull = _value." + name + "IsNull;");
    }
    outData.println("  }");
    outData.println("  public void read(Reader reader) throws Exception");
    outData.println("  {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      readCall(field, outData);
    }
    outData.println("  }");
    outData.println("  public void write(Writer writer) throws Exception");
    outData.println("  {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      writeCall(field, outData);
    }
    outData.println("  }");
  }
  private static void generateStdProcs(Table table, PrintWriter outData)
  {
    outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
    if (table.database.packageName.length() > 0)
      outData.println("package " + table.database.packageName.toLowerCase() + ";");
    outData.println("import bbd.jportal.*;");
    outData.println("import bbd.jportal.util.*;");
    outData.println("import bbd.crackle.util.*;");
    outData.println("import bbd.crackle.rw.*;");
    outData.println("import java.sql.*;");
    outData.println("import java.util.ArrayList;");
    outData.println("/**");
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = (String)table.comments.elementAt(i);
      outData.println("*" + s);
    }
    outData.println("*/");
    outData.println("public class " + table.useName() + "Tab");
    outData.println("{");
    if (table.fields.size() > 0)
    {
      extendsName = table.useName() + "Rec";
      outData.println("  public static class " + table.useName()); // + " extends " + extendsName);
      outData.println("  {");
      outData.println("    Connector connector;");
      outData.println("    Connection connection;");
      outData.println("    String _EOL_;");
      outData.println("    public " + table.useName() + "(Connector connector)");
      outData.println("    {");
      outData.println("      this.connector = connector;");
      outData.println("      connection = connector.connection;");
      outData.println("      _EOL_ = (String) System.getProperty(\"line.separator\");");
      outData.println("    }");
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc = (Proc)table.procs.elementAt(i);
        if (proc.isData)
          continue;
        if (proc.isStdExtended())
          emitProc(proc, outData);
      }
      outData.println("  }");
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStdExtended())
        continue;
      else if (proc.hasNoData())
        emitStaticProc(proc, outData);
    }
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
  private static void generateOtherCopiers(Table table, Proc proc, PrintWriter outData)
  {
    String extendsName = table.useName() + "Rec";
    if ((!proc.isStdExtended() && !proc.hasNoData()))
      extendsName = table.useName() + proc.upperFirst() + "Rec";
    outData.println("  public " + extendsName + " getProcCopy()");
    outData.println("  {");
    outData.println("    " + extendsName + " result = new " + extendsName + "();");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      String name = field.useLowerName();
      outData.println("    result." + name + " = " + name + ";");
      if (isNull(field) == true)
        outData.println("    result." + name + "IsNull = " + name + "IsNull;");
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      String name = field.useLowerName();
      outData.println("    result." + name + " = " + name + ";");
      if (isNull(field) == true)
        outData.println("    result." + name + "IsNull = " + name + "IsNull;");
    }
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      String s = (String)proc.dynamics.elementAt(i);
      outData.println("    result." + s + " = " + s + ";");
    }
    outData.println("    return result;");
    outData.println("  }");
    outData.println("  public void assign(" + extendsName + " _value)");
    outData.println("  {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      String name = field.useLowerName();
      outData.println("    " + name + " = _value." + name + ";");
      if (isNull(field) == true)
        outData.println("    " + name + "IsNull = _value." + name + "IsNull;");
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      String name = field.useLowerName();
      outData.println("    " + name + " = _value." + name + ";");
      if (isNull(field) == true)
        outData.println("    " + name + "IsNull = _value." + name + "IsNull;");
    }
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      String s = (String)proc.dynamics.elementAt(i);
      outData.println("    " + s + " = _value." + s + ";");
    }
    outData.println("  }");
    outData.println("  public void read(Reader reader)  throws Exception");
    outData.println("  {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      readCall(field, outData);
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      readCall(field, outData);
    }
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      Field field = new Field();
      field.name = (String)proc.dynamics.elementAt(i);
      field.length = ((Integer)proc.dynamicSizes.elementAt(i)).intValue();
      field.type = Field.CHAR;
      readCall(field, outData);
    }
    outData.println("  }");
    outData.println("  public void write(Writer writer) throws Exception");
    outData.println("  {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      writeCall(field, outData);
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      writeCall(field, outData);
    }
    for (int i = 0; i < proc.dynamics.size(); i++)
    {
      Field field = new Field();
      field.name = (String)proc.dynamics.elementAt(i);
      field.length = ((Integer)proc.dynamicSizes.elementAt(i)).intValue();
      field.type = Field.CHAR;
      writeCall(field, outData);
    }
    outData.println("  }");
  }
  /**
   * Build of user defined procedures
   */
  private static void generateOtherProcs(Table table, PrintWriter outData, String output, PrintWriter outLog)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if ((!proc.isStdExtended() && !proc.hasNoData()))
      {
        extendsName = table.useName() + proc.upperFirst() + "Rec";
        outData.println("  /**");
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = (String)proc.comments.elementAt(j);
          outData.println("  *" + comment);
        }
        outData.println("  */");
        outData.println("  public static class " + proc.upperFirst());// + " extends " + extendsName);
        outData.println("  {");
        outData.println("    Connector connector;");
        outData.println("    Connection connection;");
        outData.println("    String _EOL_;");
        outData.println("    public " + proc.upperFirst() + "(Connector connector)");
        outData.println("    {");
        outData.println("      this.connector = connector;");
        outData.println("      connection = connector.connection;");
        outData.println("      _EOL_ = (String) System.getProperty(\"line.separator\");");
        outData.println("    }");
        emitProc(proc, outData);
        outData.println("  }");
        outData.flush();
      }
    }
  }
  /**
   *
   */
  private static int checkPlaceHolders(Proc proc, PrintWriter outData, String l, String parmField, int phIndex)
  {
    if (phIndex >= proc.placeHolders.size())
    {
      checkForExclusion(outData, l, parmField);
      return phIndex;
    }
    int n = 0;
    while (phIndex < proc.placeHolders.size())
    {
      String placeHolder = ":" + (String)proc.placeHolders.elementAt(phIndex);
      n = l.indexOf(placeHolder);
      if (n == -1)
        break;
      phIndex++;
      String work = "";
      if (n > 0)
        work = work + l.substring(0, n);
      work = work + "?";
      n += placeHolder.length();
      if (n < l.length()) ;
      work = work + l.substring(n);
      l = work;
    }
    checkForExclusion(outData, l, parmField);
    return phIndex;
  }
  private static void checkForExclusion(PrintWriter outData, String l, String parmField)
  {
    if (parmField.length() > 0 && l.indexOf(parmField) > 0)
      outData.println(l.substring(0, 8) + "connector.checkExclude(" + l.substring(8) + ", _ret.dropField)");
    else
      outData.println(l);
  }
  /**
   * Emits a static or class method
   */
  private static void emitStaticProc(Proc proc, PrintWriter outData)
  {
    outData.println("  /**");
    outData.println("  * class method as it has no input or output.");
    outData.println("  * @exception Exception is passed through");
    outData.println("  */");
    outData.println("  public static void " + proc.lowerFirst() + "(Connector connector) throws Exception");
    outData.println("  {");
    outData.println("    String statement = ");
    int phIndex = 0;
    String plus = "     ";
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = (Line)proc.lines.elementAt(i);
      if (l.isVar)
        outData.println(plus + l.line);
      else
        phIndex = checkPlaceHolders(proc, outData, plus + " \"" + l.line + "\"", "", phIndex);
      plus = "      +_EOL_+";
    }
    outData.println("    ;");
    outData.println("    PreparedStatement prepared = connector.prepareStatement(statement);");
    outData.println("    prepared.executeUpdate();");
    outData.println("    prepared.close();");
    outData.println("  }");
  }
  private static void emitOutputBinds(Proc proc, Field field, String prep, String indent, int pos, PrintWriter outData)
  {
    if (field.isOut == true)
    {
      if (field.type == Field.BLOB)
      {
        outData.println(indent + "    rec." + field.useLowerName() + ".setBlob(new ByteArrayBlob(" + prep + ".getBlob(" + pos + ")));");
      }
      else
      {
        outData.print(indent + "    rec." + field.useLowerName() + " = ");
        boolean addBracket = true;
        switch (field.type)
        {
          case Field.DATE:
            outData.print("DataHandler.date(");
            break;
          case Field.DATETIME:
            outData.print("DataHandler.dateTime(");
            break;
          case Field.TIMESTAMP:
            outData.print("DataHandler.timeStamp(");
            break;
          case Field.TIME:
            outData.print("DataHandler.time(");
            break;
          default:
            addBracket = false;
        }
        outData.print(prep + ".get");
        outData.print(setType(field));
        outData.print("(");
        outData.print(pos);
        if (addBracket)
          outData.print(")");
        outData.print(")");
        if (field.type == Field.ANSICHAR && field.length == 1)
          outData.print(".charAt(0)");
        outData.println(";");
      }
    }
  }
  private static void emitInputBinds(Proc proc, Field field, String prep, String indent, int pos, PrintWriter outData)
  {
    if (field.isIn == true || field.isOut == false)
    {
      String pad = "";
      if (field.isNull)
      {
        pad = "  ";
        if (field.isEmptyAsNull() == true)
          outData.println(indent + "    if (rec." + field.useLowerName() + ".trim().length() == 0)");
        else if (field.ansiIsNull() == true)
          outData.println(indent + "    if (rec." + field.useLowerName() + " == 0 || rec." + field.useLowerName() + " == ' ')");
        else
          outData.println(indent + "    if (rec." + field.useLowerName() + "IsNull)");
        outData.println(indent + pad + "    " + prep + ".setNull(" + (pos) + ", " + javaType(field) + ");");
        outData.println(indent + "    else");
      }
      if (field.type == Field.BLOB)
      {
        outData.println(indent + pad + "    " + prep + ".setBlob(" + pos + ", rec." + field.useLowerName() + ".getBlob());");
      }
      else
      {
        outData.print(indent + pad + "    ");
        outData.print(prep + ".set");
        outData.print(setType(field));
        outData.print("(");
        outData.print(pos);
        outData.print(", ");
        boolean addBracket = true;
        switch (field.type)
        {
          case Field.DATE:
            outData.print("DataHandler.date(");
            break;
          case Field.DATETIME:
            outData.print("DataHandler.dateTime(");
            break;
          case Field.TIMESTAMP:
            outData.print("DataHandler.timeStamp(");
            break;
          case Field.TIME:
            outData.print("DataHandler.time(");
            break;
          case Field.ANSICHAR:
            if (field.length == 1)
            {
              outData.print("String.valueOf(");
              break;
            }
          default:
            addBracket = false;
        }
        outData.print("rec." + field.useLowerName() + ")");
        if (addBracket)
          outData.print(")");
        outData.println(";");
      }
    }
    if (field.isOut == true && proc.isSProc == true)
      outData.println(indent + "    " + prep + ".registerOutParameter(" + (pos) + ", " + javaType(field) + ");");
  }
  private static String possibleAnd(Field field, int no)
  {
    if (field.type == Field.ANSICHAR && field.length == 1)
      return " && result.getString(" + no + ").length() >= 1";
    return "";
  }
  private static String getParmField(Proc proc)
  {
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      if (field.type == Field.IDENTITY)
        return field.useName();
      if (field.isSequence)
        return field.useName();
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (field.type == Field.IDENTITY)
        return field.useName();
      if (field.isSequence)
        return field.useName();
    }
    return "junked";
  }
  private static void emitProc(Proc proc, PrintWriter outData)
  {
    String indent = "  ";
    outData.println(indent + "  /**");
    if (proc.comments.size() > 0)
    {
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = (String)proc.comments.elementAt(i);
        outData.println(indent + "  *" + comment);
      }
    }
    if (proc.outputs.size() == 0)
      outData.println(indent + "  * Returns no output.");
    else if (proc.isSingle)
    {
      outData.println(indent + "  * Returns at most one record.");
      outData.println(indent + "  * @return true if a record is found");
    }
    else
    {
      outData.println(indent + "  * Returns any number of records.");
      outData.println(indent + "  * @return result set of records found");
    }
    outData.println(indent + "  * @exception Exception is passed through");
    outData.println(indent + "  */");
    String procName = proc.lowerFirst();
    if (proc.isMultipleInput)
      outData.println(indent + "  public void " + procName + "(" + extendsName + "[] recs) throws Exception");
    else if (proc.outputs.size() == 0 || proc.isSProc == true)
      outData.println(indent + "  public void " + procName + "(" + extendsName + " rec) throws Exception");
    else if (proc.isSingle)
      outData.println(indent + "  public boolean " + procName + "(" + extendsName + " rec) throws Exception");
    else if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
      outData.println(indent + "  public Query " + procName + "(" + extendsName + " rec) throws Exception");
    else
      outData.println(indent + "  public Query " + procName + "() throws Exception");
    outData.println(indent + "  {");
    String parmField = "";
    if (proc.hasReturning || proc.isMultipleInput)
    {
      parmField = getParmField(proc);
      String parms = "\"" + proc.table.useName() + "\", \"" + parmField + "\"";
      outData.println(indent + "    Connector.Returning _ret = connector.getReturning(" + parms + ");");
    }
    outData.println(indent + "    String stmt = ");
    int phIndex = 0;
    String plus = "     ";
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = proc.lines.elementAt(i);
      if (l.isVar)
      {
        if (l.line.indexOf("_ret.") >= 0)
          outData.println(plus + " " + l.line);
        else
          outData.println(plus + " rec." + l.line);
        if (plus.length() == 5)
          plus = "      + ";
      }
      else
      {
        phIndex = checkPlaceHolders(proc, outData, plus + " \"" + l.line + "\"", parmField, phIndex);
        plus = "      + _EOL_ +";
      }
    }
    outData.println(indent + "    ;");
    String prep = "prep";
    if (proc.isSProc == true)
    {
      prep = "_call";
      outData.println(indent + "    CallableStatement " + prep + " = connector.prepareCall(stmt);");
    }
    else if (proc.hasReturning)
      outData.println(indent + "    PreparedStatement " + prep + " = connector.prepareStatement(stmt, _ret.doesGeneratedKeys);");
    else
      outData.println(indent + "    PreparedStatement " + prep + " = connector.prepareStatement(stmt);");
    String indent2 = indent;
    if (proc.isMultipleInput)
    {
      outData.println(indent + "    for (" + extendsName + " rec: recs)" );
      outData.println(indent + "    {");
      outData.println(indent + "      try");
      outData.println(indent + "      {");
      indent2 += "    ";
    }
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      if (proc.isInsert == true && proc.hasReturning == false && field.isSequence == true)
      {
        if (field.type == Field.BIGSEQUENCE)
          outData.println(indent2 + "    rec." + field.useLowerName() + " = connector.getBigSequence(\"" + proc.table.name + "\", \"" + field.useName() + "\");");
        else
          outData.println(indent2 + "    rec." + field.useLowerName() + " = connector.getSequence(\"" + proc.table.name + "\", \"" + field.useName() + "\");");
      }
      if (field.type == Field.TIMESTAMP)
        outData.println(indent2 + "    rec." + field.useLowerName() + " = DataHandler.timeStamp(connector.getTimestamp());");
      if (field.type == Field.USERSTAMP)
        outData.println(indent2 + "    rec." + field.useLowerName() + " = connector.getUserstamp();");
    }
    if (proc.placeHolders.size() > 0)
    {
      for (int ph = 0; ph < proc.placeHolders.size(); ph++)
      {
        String placeHolder = (String)proc.placeHolders.elementAt(ph);
        int i = proc.indexOf(placeHolder);
        Field field = (Field)proc.inputs.elementAt(i);
        emitInputBinds(proc, field, prep, indent2, ph + 1, outData);
      }
    }
    else
    {
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        emitInputBinds(proc, field, prep, indent2, i + 1, outData);
      }
    }
    if (proc.outputs.size() > 0 && proc.isSProc == false)
    {
      outData.println(indent + "    ResultSet result;");
      if (proc.hasReturning)
      {
        outData.println(indent + "    if (_ret.doesGeneratedKeys == false)");
        outData.println(indent + "      result = " + prep + ".executeQuery();");
        outData.println(indent + "    else");
        outData.println(indent + "    {");
        outData.println(indent + "      " + prep + ".executeUpdate();");
        outData.println(indent + "      result = " + prep + ".getGeneratedKeys();");
        outData.println(indent + "    }");
      }
      else
        outData.println(indent + "    result = " + prep + ".executeQuery();");
      if (proc.outputs.size() > 0)
      {
        outData.println(indent + "    ResultSetMetaData _rsmd_ = result.getMetaData();");
        outData.println(indent + "    int _columns_ = _rsmd_.getColumnCount();");
        outData.println(indent + "    if (_columns_ != " + proc.outputs.size() + ")");
        outData.println(indent + "      throw new Exception(\"Columns Read=\"+_columns_+\" != Expected=" + proc.outputs.size() + "\");");
      }
      if (!proc.isSingle)
      {
        outData.println(indent + "    Query query = new Query(" + prep + ", result);");
        outData.println(indent + "    return query;");
        outData.println(indent + "  }");
        outData.println(indent + "  /**");
        outData.println(indent + "  * Returns the next record in a result set.");
        outData.println(indent + "  * @param result The result set for the query.");
        outData.println(indent + "  * @return true while records are found.");
        outData.println(indent + "  * @exception Exception is passed through");
        outData.println(indent + "  */");
        outData.println(indent + "  public " + extendsName + " " + procName + "(Query query) throws Exception");
        outData.println(indent + "  {");
        outData.println(indent + "    if (!query.result.next())");
        outData.println(indent + "    {");
        outData.println(indent + "      query.close();");
        outData.println(indent + "      return null;");
        outData.println(indent + "    }");
        outData.println(indent + "    ResultSet result = query.result;");
        outData.println(indent + "    " + extendsName + " rec = new " + extendsName + "();");
      }
      else
      {
        outData.println(indent + "    if (!result.next())");
        outData.println(indent + "    {");
        outData.println(indent + "      result.close();");
        outData.println(indent + "      " + prep + ".close();");
        outData.println(indent + "      return false;");
        outData.println(indent + "    }");
      }
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        String pad = "";
        if (field.isNull)
        {
          if (isEmptyOrAnsiAsNull(field) == false)
          {
            outData.println(indent + "    rec." + field.useLowerName() + "IsNull = result.getObject("
              + (i + 1) + ") == null;");
            outData.println(indent + "    if (" + field.useLowerName() + "IsNull == false" + possibleAnd(field, i + 1) + ")");
          }
          else
            outData.println(indent + "    if (result.getObject(" + (i + 1) + ") != null" + possibleAnd(field, i + 1) + ")");
          pad = "  ";
        }
        if (field.type == Field.BLOB)
        {
          int pos = i + 1;
          outData.println(indent + "    rec." + field.useLowerName() + ".setBlob(new ByteArrayBlob(result.getBlob(" + pos + ")));");
        }
        else
        {
          outData.print(indent + pad + "    rec." + field.useLowerName() + " = ");
          boolean addBracket = true;
          switch (field.type)
          {
            case Field.DATE:
              outData.print("DataHandler.date(");
              break;
            case Field.DATETIME:
              outData.print("DataHandler.dateTime(");
              break;
            case Field.TIMESTAMP:
              outData.print("DataHandler.timeStamp(");
              break;
            case Field.TIME:
              outData.print("DataHandler.time(");
              break;
            default:
              addBracket = false;
          }
          outData.print("result.get");
          outData.print(setType(field));
          outData.print("(");
          outData.print(i + 1);
          if (addBracket)
            outData.print(")");
          outData.print(")");
          if (field.type == Field.ANSICHAR && field.length == 1)
            outData.print(".charAt(0)");
          outData.println(";");
        }
        if (field.isNull)
        {
          outData.println(indent + "    else");
          outData.println(indent + "      rec." + initJavaVar(field));
        }
      }
      if (proc.isSingle)
      {
        outData.println(indent + "    result.close();");
        outData.println(indent + "    " + prep + ".close();");
        outData.println(indent + "    return true;");
      }
      else if (proc.outputs.size() > 0)
      {
        outData.println(indent + "    return rec;");
      }
    }
    else
    {
      if (proc.isMultipleInput)
      {
        outData.println(indent + "        " + prep + ".addBatch();");
        outData.println(indent + "      }");
        outData.println(indent + "      catch (Exception ex)");
        outData.println(indent + "      {");
        outData.println(indent + "        " + prep + ".clearBatch();");
        outData.println(indent + "        throw(ex);");
        outData.println(indent + "      }");
        outData.println(indent + "      " + prep + ".executeBatch();");
        outData.println(indent + "    }");
      }
      else
        outData.println(indent + "    " + prep + ".executeUpdate();");
      if (proc.isSProc == true)
      {
        if (proc.placeHolders.size() > 0)
        {
          for (int ph = 0; ph < proc.placeHolders.size(); ph++)
          {
            String placeHolder = (String)proc.placeHolders.elementAt(ph);
            int i = proc.indexOf(placeHolder);
            Field field = (Field)proc.inputs.elementAt(i);
            emitOutputBinds(proc, field, prep, indent, ph + 1, outData);
          }
        }
        else
        {
          for (int i = 0; i < proc.inputs.size(); i++)
          {
            Field field = (Field)proc.inputs.elementAt(i);
            emitOutputBinds(proc, field, prep, indent, i + 1, outData);
          }
        }
      }
      outData.println(indent + "    " + prep + ".close();");
    }
    outData.println(indent + "  }");
    if (proc.outputs.size() > 0 && !proc.isSingle)
    {
      outData.println("    /**");
      outData.println("    * Returns all the records in a result set as array of " + extendsName + "");
      outData.println("    * @return array of " + extendsName + "");
      outData.println("    * @exception Exception is passed through");
      outData.println("    */");
      if (proc.inputs.size() > 0)
      {
        outData.println("    public " + extendsName + "[] " + procName + "Load(" + extendsName + " rec) throws Exception");
        outData.println("    {");
        outData.println("      Query query = " + procName + "(rec);");
      }
      else
      {
        outData.println("    public " + extendsName + "[] " + procName + "Load() throws Exception");
        outData.println("    {");
        outData.println("      " + extendsName + " rec = new " + extendsName + "();");
        outData.println("      Query query = " + procName + "();");
      }
      outData.println("      ArrayList<" + extendsName + "> recs = new ArrayList<" + extendsName + ">();");
      outData.println("      while ((rec = " + procName + "(query)) != null)");
      outData.println("        recs.add(rec);");
      outData.println("      " + extendsName + "[] result = new " + extendsName + "[recs.size()];");
      outData.println("      for (int i=0; i<recs.size();i++)");
      outData.println("        result[i] = recs.get(i); ");
      outData.println("      return result;");
      outData.println("    }");
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    if (table.fields.size() > 0)
      generateStdProcStruct(table, output, outLog);
    generateOtherProcStructs(table, output, outLog);
  }
  private static void generateStdProcStruct(Table table, String stdOutput, PrintWriter outLog)
  {
    try
    {
      String output = outputDir(table.database, stdOutput);
      outLog.println("Code: " + output + table.useName() + "Rec.java");
      OutputStream outFile = new FileOutputStream(output + table.useName() + "Rec.java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
        if (table.database.packageName.length() > 0)
        {
          outData.println("package " + table.database.packageName.toLowerCase() + ";");
          outData.println();
        }
        outData.println("import java.sql.*;");
        outData.println("import bbd.jportal.*;");
        outData.println("import bbd.jportal.util.*;");
        outData.println("import bbd.crackle.util.*;");
        outData.println("import bbd.crackle.rw.*;");
        outData.println();
        outData.println("/**");
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println(" *" + s);
        }
        outData.println(" */");
        outData.println("public class " + table.useName() + "Rec");
        outData.println("{");
        generateEnum(table, outData);
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field)table.fields.elementAt(i);
          if (field.comments.size() > 0)
          {
            outData.println("  /**");
            for (int c = 0; c < field.comments.size(); c++)
            {
              String s = (String)field.comments.elementAt(c);
              outData.println("   *" + s);
            }
            outData.println("   */");
          }
          outData.println("  public " + javaVar(field) + ";");
          if (isNull(field) == true)
            outData.println("  public boolean " + field.useLowerName() + "IsNull;");
        }
        outData.println("  public " + table.useName() + "Rec()");
        outData.println("  {");
        int maxSize = 0;
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field)table.fields.elementAt(i);
          if (field.useLowerName().length() > maxSize)
            maxSize = field.useLowerName().length();
          outData.println("    " + initJavaVar(field));
          if (isNull(field) == true)
            outData.println("    " + field.useLowerName() + "IsNull = false;");
        }
        outData.println("  }");
        generateStdCopiers(table, outData);
        outData.println("  public String toString()");
        outData.println("  {");
        outData.println("    String CRLF = (String) System.getProperty(\"line.separator\");");
        for (int i = 0; i < table.fields.size(); i++)
        {
          if (i == 0)
            outData.print("    return ");
          else
            outData.print("         + ");
          Field field = (Field)table.fields.elementAt(i);
          int no = maxSize - field.useLowerName().length();
          outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
        }
        outData.println("    ;");
        outData.println("  }");
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
  private static void generateOtherProcStructs(Table table, String output, PrintWriter outLog)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if ((!proc.isStdExtended() && !proc.hasNoData()))
        generateOtherProcStruct(table, proc, output, outLog);
    }
  }
  private static void generateOtherProcStruct(Table table, Proc proc, String stdOutput, PrintWriter outLog)
  {
    try
    {
      String output = outputDir(table.database, stdOutput);
      outLog.println("Code: " + output + table.useName() + proc.upperFirst() + "Rec.java");
      OutputStream outFile = new FileOutputStream(output + table.useName() + proc.upperFirst() + "Rec.java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        if (table.database.packageName.length() > 0)
        {
          outData.println("package " + table.database.packageName.toLowerCase() + ";");
          outData.println();
        }
        outData.println("import java.sql.*;");
        outData.println("import bbd.jportal.*;");
        outData.println("import bbd.jportal.util.*;");
        outData.println("import bbd.crackle.util.*;");
        outData.println("import bbd.crackle.rw.*;");
        outData.println();
        outData.println("/**");
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = (String)proc.comments.elementAt(j);
          outData.println(" *" + comment);
        }
        outData.println(" */");
        String extendsStd = "";
        boolean extendsUsed = false;
        if (proc.extendsStd == true && proc.useStd == false)
        {
          extendsStd = " extends " + table.useName() + "Rec ";
          extendsUsed = true;
        }
        outData.println("public class " + table.useName() + proc.upperFirst() + "Rec" + extendsStd);
        outData.println("{");
        int maxSize = 0;
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field)proc.inputs.elementAt(j);
          if (field.useLowerName().length() > maxSize)
            maxSize = field.useLowerName().length();
          if (extendsUsed == true && table.hasField(field.name) == true)
            continue;
          outData.println("  /**");
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String)field.comments.elementAt(c);
            outData.println("   *" + s);
          }
          if (!proc.hasOutput(field.name))
            outData.println("   * (input)");
          else
            outData.println("   * (input/output)");
          outData.println("   */");
          outData.println("  public " + javaVar(field) + ";");
          if (isNull(field) == true)
            outData.println("  public boolean " + field.useLowerName() + "IsNull;");
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field)proc.outputs.elementAt(j);
          if (field.useLowerName().length() > maxSize)
            maxSize = field.useLowerName().length();
          if (extendsUsed == true && table.hasField(field.name) == true)
            continue;
          if (!proc.hasInput(field.name))
          {
            outData.println("  /**");
            for (int c = 0; c < field.comments.size(); c++)
            {
              String s = (String)field.comments.elementAt(c);
              outData.println("   *" + s);
            }
            outData.println("   * (output)");
            outData.println("   */");
            outData.println("  public " + javaVar(field) + ";");
            if (isNull(field) == true)
              outData.println("  public boolean " + field.useLowerName() + "IsNull;");
          }
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          if (s.length() > maxSize)
            maxSize = s.length();
          outData.println("  /**");
          outData.println("   * (dynamic)");
          outData.println("   */");
          outData.println("  public String " + s + ";");
        }
        outData.println("  public " + table.useName() + proc.upperFirst() + "Rec()");
        outData.println("  {");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field)proc.inputs.elementAt(j);
          outData.println("    " + initJavaVar(field));
          if (isNull(field) == true)
            outData.println("    " + field.useLowerName() + "IsNull = false;");
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field)proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
            outData.println("    " + initJavaVar(field));
          if (isNull(field) == true)
            outData.println("    " + field.useLowerName() + "IsNull = false;");
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.println("    " + s + " = \"\";");
        }
        outData.println("  }");
        generateOtherCopiers(table, proc, outData);
        outData.println("  public String toString()");
        outData.println("  {");
        outData.println("    String CRLF = (String) System.getProperty(\"line.separator\");");
        String _ret = "    return ";
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          outData.print(_ret);
          _ret = "         + ";
          Field field = (Field)proc.inputs.elementAt(j);
          int no = maxSize - field.useLowerName().length();
          outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field)proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
          {
            outData.print(_ret);
            _ret = "         + ";
            int no = maxSize - field.useLowerName().length();
            outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
          }
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.print(_ret);
          _ret = "         + ";
          int no = maxSize - s.length();
          outData.println("\"  " + s + padded(no + 1) + ": \" + " + s + " + CRLF");
        }
        outData.println("    ;");
        outData.println("  }");
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
        if (field.length == 1)
          return "char " + name;
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
      case Field.XML:
        return "JPXML " + name;
      case Field.TLOB:
        return "String " + name;
      case Field.MONEY:
        return "String " + name;
      case Field.USERSTAMP:
        return "String " + name;
    }
    return "unknown";
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
          return "0";
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
      case Field.XML:
        return "new JPXML(" + field.length + ")";
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
  private static String javaType(Field field)
  {
    switch (field.type)
    {
      case Field.BYTE:
        return "java.sql.Types.TINYINT";
      case Field.CHAR:
        return "java.sql.Types.VARCHAR";
      case Field.ANSICHAR:
        return "java.sql.Types.CHAR";
      case Field.DATE:
        return "java.sql.Types.DATE";
      case Field.DATETIME:
        return "java.sql.Types.TIMESTAMP";
      case Field.FLOAT:
      case Field.DOUBLE:
        return "java.sql.Types.DOUBLE";
      case Field.BLOB:
        return "java.sql.Types.LONGVARBINARY";
      case Field.XML:
        return "java.sql.Types.LONGVARCHAR";
      case Field.TLOB:
        return "java.sql.Types.LONGVARCHAR";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "java.sql.Types.INTEGER";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "java.sql.Types.BIGINT";
      case Field.MONEY:
        return "java.sql.Types.DOUBLE";
      case Field.SHORT:
        return "java.sql.Types.SMALLINT";
      case Field.TIME:
        return "java.sql.Types.TIME";
      case Field.TIMESTAMP:
        return "java.sql.Types.TIMESTAMP";
      case Field.USERSTAMP:
        return "java.sql.Types.VARCHAR";
    }
    return "java.sql.Types.OTHER";
  }
  /**
   * JDBC get and set type for field data transfers
   */
  private static String setType(Field field)
  {
    switch (field.type)
    {
      case Field.BYTE:
        return "Byte";
      case Field.ANSICHAR:
      case Field.CHAR:
        return "String";
      case Field.DATE:
        return "Date";
      case Field.DATETIME:
        return "Timestamp";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "String";
        return "Double";
      case Field.BLOB:
        return "Blob";
      case Field.XML:
        return "String";
      case Field.TLOB:
        return "String";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "Int";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "Long";
      case Field.MONEY:
        return "String";
      case Field.SHORT:
        return "Short";
      case Field.TIME:
        return "Time";
      case Field.TIMESTAMP:
        return "Timestamp";
      case Field.USERSTAMP:
        return "String";
    }
    return "unknown";
  }
  private static String padString = "                                                         ";
  private static String padded(int size)
  {
    if (size == 0)
      return "";
    if (size > padString.length())
      size = padString.length();
    return padString.substring(0, size);
  }
}
