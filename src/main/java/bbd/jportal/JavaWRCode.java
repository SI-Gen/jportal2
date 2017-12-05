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

public class JavaWRCode extends Generator
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
        outLog.println(args[i] + ": generate Java with recs code");
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
    return "generate Java with recs code - generates separate data recs but uses Standard and Other inner class";
  }
  public static String documentation()
  {
    return "generate Java with recs code - generates separate data recs but uses Standard and Other inner class";
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
  /**
   * Build of standard and user defined procedures
   */
  private static void generate(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName() + ".java");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".java");
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
    outData.println("    " + extendsName + " _result = new " + extendsName + "();");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      String name = field.useLowerName();
      outData.println("    _result." + name + " = " + name + ";");
      if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    _result." + name + "IsNull = " + name + "IsNull;");
    }
    outData.println("    return _result;");
    outData.println("  }");
    outData.println("  public void assign(" + extendsName + " _value)");
    outData.println("  {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      String name = field.useLowerName();
      outData.println("    " + name + " = _value." + name + ";");
      if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    " + name + "IsNull = _value." + name + "IsNull;");
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
    outData.println("import java.sql.*;");
    outData.println("import java.util.Vector;");
    outData.println("/**");
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = (String)table.comments.elementAt(i);
      outData.println("*" + s);
    }
    outData.println("*/");
    outData.println("public class " + table.useName());
    outData.println("{");
    outData.println("  Connector connector;");
    outData.println("  Connection connection;");
    outData.println("  String _EOL_;");
    outData.println("  public " + table.useName() + "(Connector connector)");
    outData.println("  {");
    outData.println("    this.connector = connector;");
    outData.println("    connection = connector.connection;");
    outData.println("    _EOL_ = (String) System.getProperty(\"line.separator\");");
    outData.println("  }");
    if (table.fields.size() > 0)
    {
      extendsName = table.useName() + "Rec";
      outData.println("  public class Standard extends " + extendsName);
      outData.println("  {");
      outData.println("    private static final long serialVersionUID = 1L;");
      outData.println("    /**");
      outData.println("    * @param Connector for specific database");
      outData.println("    */");
      outData.println("    public Standard()");
      outData.println("    {");
      outData.println("      super();");
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
      outData.println("  public Standard getStandard()");
      outData.println("  {");
      outData.println("    return new Standard();");
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
    outData.println("    " + extendsName + " _result = new " + extendsName + "();");
    for (int jj = 0; jj < proc.inputs.size(); jj++)
    {
      Field field = (Field)proc.inputs.elementAt(jj);
      String name = field.useLowerName();
      outData.println("    _result." + name + " = " + name + ";");
      if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    _result." + name + "IsNull = " + name + "IsNull;");
    }
    for (int jj = 0; jj < proc.outputs.size(); jj++)
    {
      Field field = (Field)proc.outputs.elementAt(jj);
      if (proc.hasInput(field.name))
        continue;
      String name = field.useLowerName();
      outData.println("    _result." + name + " = " + name + ";");
      if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    _result." + name + "IsNull = " + name + "IsNull;");
    }
    for (int jj = 0; jj < proc.dynamics.size(); jj++)
    {
      String s = (String)proc.dynamics.elementAt(jj);
      outData.println("    _result." + s + " = " + s + ";");
    }
    outData.println("    return _result;");
    outData.println("  }");
    outData.println("  public void assign(" + extendsName + " _value)");
    outData.println("  {");
    for (int jj = 0; jj < proc.inputs.size(); jj++)
    {
      Field field = (Field)proc.inputs.elementAt(jj);
      String name = field.useLowerName();
      outData.println("    " + name + " = _value." + name + ";");
      if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    " + name + "IsNull = _value." + name + "IsNull;");
    }
    for (int jj = 0; jj < proc.outputs.size(); jj++)
    {
      Field field = (Field)proc.outputs.elementAt(jj);
      if (proc.hasInput(field.name))
        continue;
      String name = field.useLowerName();
      outData.println("    " + name + " = _value." + name + ";");
      if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    " + name + "IsNull = _value." + name + "IsNull;");
    }
    for (int jj = 0; jj < proc.dynamics.size(); jj++)
    {
      String s = (String)proc.dynamics.elementAt(jj);
      outData.println("    " + s + " = _value." + s + ";");
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
        outData.println("  public class " + proc.upperFirst() + " extends " + extendsName);
        outData.println("  {");
        outData.println("    private static final long serialVersionUID = 1L;");
        outData.println("    public " + proc.upperFirst() + "()");
        outData.println("    {");
        outData.println("      super();");
        outData.println("    }");
        emitProc(proc, outData);
        outData.println("  }");
        outData.println("  public " + proc.upperFirst() + " get" + proc.upperFirst() + "()");
        outData.println("  {");
        outData.println("    return new " + proc.upperFirst() + "();");
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
    //outData.println("    String CRLF = (String) System.getProperty(\"line.separator\");");
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
        outData.println(indent + "    " + field.useLowerName() + ".setBlob(new ByteArrayBlob(" + prep + ".getBlob(" + pos + ")));");
      }
      else
      {
        outData.print(indent + "    " + field.useLowerName() + " = ");
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
          outData.println(indent + "    if (" + field.useLowerName() + ".trim().length() == 0)");
        else if (field.ansiIsNull() == true)
          outData.println(indent + "    if (" + field.useLowerName() + " == 0 || " + field.useLowerName() + " == ' ')");
        else
          outData.println(indent + "    if (" + field.useLowerName() + "IsNull)");
        outData.println(indent + pad + "    " + prep + ".setNull(" + (pos) + ", " + javaType(field) + ");");
        outData.println(indent + "    else");
      }
      if (field.type == Field.BLOB)
      {
        outData.println(indent + pad + "    " + prep + ".setBlob(" + pos + ", " + field.useLowerName() + ".getBlob());");
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
        outData.print(field.useLowerName() + ")");
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
  /** Emits class method for processing the database activity */
  private static String getParmField(Proc proc)
  {
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      if (field.type == Field.IDENTITY)
        return field.useName();
      if (Table.isSequence(field))
        return field.useName();
    }
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (field.type == Field.IDENTITY)
        return field.useName();
      if (Table.isSequence(field))
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
    if (proc.outputs.size() == 0 || proc.isSProc == true)
      outData.println(indent + "  public void " + procName + "() throws Exception");
    else if (proc.isSingle)
      outData.println(indent + "  public boolean " + procName + "() throws Exception");
    else
      outData.println(indent + "  public Query " + procName + "() throws Exception");
    outData.println(indent + "  {");
    String parmField = "";
    if (proc.hasReturning || (proc.isMultipleInput && proc.isInsert))
    {
      parmField = getParmField(proc);
      String parms = "\"" + proc.table.useName() + "\", \"" + parmField + "\"";
      outData.println(indent + "    Connector.Returning _ret = connector.getReturning(" + parms + ");");
    }
    outData.println(indent + "    String _stmt = ");
    int phIndex = 0;
    String plus = "     ";
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = (Line)proc.lines.elementAt(i);
      if (l.isVar)
      {
        outData.println(plus + " " + l.line);
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
    String prep = "_prep";
    if (proc.isSProc == true)
    {
      prep = "_call";
      outData.println(indent + "    CallableStatement " + prep + " = connector.prepareCall(_stmt);");
    }
    else if (proc.hasReturning)
      outData.println(indent + "    PreparedStatement " + prep + " = connector.prepareStatement(_stmt, _ret.doesGeneratedKeys);");
    else
      outData.println(indent + "    PreparedStatement " + prep + " = connector.prepareStatement(_stmt);");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      if (proc.hasReturning)
      {

      }
      else if (proc.isInsert)
      {
        if (field.isSequence)
        {
          if (field.type == Field.BIGSEQUENCE)
            outData.println(indent + "    " + field.useLowerName() + " = connector.getBigSequence(\"" + proc.table.name + "\", \"" + field.useName() + "\");");
          else
            outData.println(indent + "    " + field.useLowerName() + " = connector.getSequence(\"" + proc.table.name + "\", \"" + field.useName() + "\");");
        }
      }
      if (field.type == Field.TIMESTAMP)
        outData.println(indent + "    " + field.useLowerName() + " = DataHandler.timeStamp(connector.getTimestamp());");
      if (field.type == Field.USERSTAMP)
        outData.println(indent + "    " + field.useLowerName() + " = connector.getUserstamp();");
    }
    if (proc.placeHolders.size() > 0)
    {
      for (int ph = 0; ph < proc.placeHolders.size(); ph++)
      {
        String placeHolder = (String)proc.placeHolders.elementAt(ph);
        int i = proc.indexOf(placeHolder);
        Field field = (Field)proc.inputs.elementAt(i);
        emitInputBinds(proc, field, prep, indent, ph + 1, outData);
      }
    }
    else
    {
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        emitInputBinds(proc, field, prep, indent, i + 1, outData);
      }
    }
    if (proc.outputs.size() > 0 && proc.isSProc == false)
    {
      outData.println(indent + "    ResultSet _result;");
      if (proc.hasReturning)
      {
        outData.println(indent + "    if (_ret.doesGeneratedKeys == false)");
        outData.println(indent + "      _result = " + prep + ".executeQuery();");
        outData.println(indent + "    else");
        outData.println(indent + "    {");
        outData.println(indent + "      " + prep + ".executeUpdate();");
        outData.println(indent + "      _result = " + prep + ".getGeneratedKeys();");
        outData.println(indent + "    }");
      }
      else
        outData.println(indent + "    _result = " + prep + ".executeQuery();");
      if (proc.outputs.size() > 0)
      {
        outData.println(indent + "    ResultSetMetaData _rsmd_ = _result.getMetaData();");
        outData.println(indent + "    int _columns_ = _rsmd_.getColumnCount();");
        outData.println(indent + "    if (_columns_ != " + proc.outputs.size() + ")");
        outData.println(indent + "      throw new Exception(\"Columns Read=\"+_columns_+\" != Expected=" + proc.outputs.size() + "\");");
      }
      if (!proc.isSingle)
      {
        outData.println(indent + "    Query _query = new Query(" + prep + ", _result);");
        outData.println(indent + "    return _query;");
        outData.println(indent + "  }");
        outData.println(indent + "  /**");
        outData.println(indent + "  * Returns the next record in a result set.");
        outData.println(indent + "  * @param result The result set for the query.");
        outData.println(indent + "  * @return true while records are found.");
        outData.println(indent + "  * @exception Exception is passed through");
        outData.println(indent + "  */");
        outData.println(indent + "  public boolean " + procName + "(Query query) throws Exception");
        outData.println(indent + "  {");
        outData.println(indent + "    if (!query.result.next())");
        outData.println(indent + "    {");
        outData.println(indent + "      query.close();");
        outData.println(indent + "      return false;");
        outData.println(indent + "    }");
        outData.println(indent + "    ResultSet _result = query.result;");
      }
      else
      {
        outData.println(indent + "    if (!_result.next())");
        outData.println(indent + "    {");
        outData.println(indent + "      _result.close();");
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
          if (field.isEmptyOrAnsiAsNull() == false)
          {
            outData.println(indent + "    " + field.useLowerName() + "IsNull = _result.getObject("
              + (i + 1) + ") == null;");
            outData.println(indent + "    if (" + field.useLowerName() + "IsNull == false" + possibleAnd(field, i + 1) + ")");
          }
          else
            outData.println(indent + "    if (_result.getObject(" + (i + 1) + ") != null" + possibleAnd(field, i + 1) + ")");
          pad = "  ";
        }
        if (field.type == Field.BLOB)
        {
          int pos = i + 1;
          outData.println(indent + "    " + field.useLowerName() + ".setBlob(new ByteArrayBlob(_result.getBlob(" + pos + ")));");
        }
        else
        {
          outData.print(indent + pad + "    " + field.useLowerName() + " = ");
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
          outData.print("_result.get");
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
          outData.println(indent + "      " + initJavaVar(field));
        }
      }
      if (proc.isSingle)
      {
        outData.println(indent + "    _result.close();");
        outData.println(indent + "    " + prep + ".close();");
      }
      outData.println(indent + "    return true;");
    }
    else
    {
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
      outData.println("    public " + extendsName + "[] " + procName + "Load() throws Exception");
      outData.println("    {");
      outData.println("      Vector recs = new Vector();");
      outData.println("      Query query = " + procName + "();");
      outData.println("      while (" + procName + "(query) == true)");
      outData.println("      {");
      outData.println("        " + extendsName + " rec = new " + extendsName + "();");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        if (field.type == Field.BLOB)
          outData.println("        rec." + field.useLowerName() + ".setBytes(" + field.useLowerName() + ".getBytes());");
        else
          outData.println("        rec." + field.useLowerName() + " = " + field.useLowerName() + ";");
        if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
          outData.println("        rec." + field.useLowerName() + "IsNull = " + field.useLowerName() + "IsNull;");
      }
      outData.println("        recs.addElement(rec);");
      outData.println("      }");
      outData.println("      " + extendsName + "[] result = new " + extendsName + "[recs.size()];");
      outData.println("      for (int i=0; i<recs.size();i++)");
      outData.println("        result[i] = (" + extendsName + ")recs.elementAt(i); ");
      outData.println("      return result;");
      outData.println("    }");
    }
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
    {
      outData.println(indent + "  /**");
      if (proc.outputs.size() == 0)
        outData.println(indent + "  * Returns no records.");
      else if (proc.isSingle)
      {
        outData.println(indent + "  * Returns at most one record.");
        outData.println(indent + "  * @return true if a record is returned.");
      }
      else
      {
        outData.println(indent + "  * Returns any number of records.");
        outData.println(indent + "  * @return result set of records found");
      }
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (!field.isPrimaryKey)
          continue;
        outData.println(indent + "  * @param " + field.useLowerName() + " key input.");
      }
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (field.isPrimaryKey)
          continue;
        if (proc.isSProc == true && field.isIn == false)
          continue;
        outData.println(indent + "  * @param " + field.useLowerName() + " input.");
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
        outData.println(indent + "  * @param " + proc.name + " dynamic input.");
      outData.println(indent + "  * @exception Exception is passed through");
      outData.println(indent + "  */");
      if (proc.outputs.size() == 0)
        outData.println(indent + "  public void " + procName + "(");
      else if (proc.isSingle)
        outData.println(indent + "  public boolean " + procName + "(");
      else
        outData.println(indent + "  public Query " + procName + "(");
      String comma = "    ";
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (!field.isPrimaryKey)
          continue;
        if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        {
          outData.println("indent+comma+boolean " + field.useLowerName() + "IsNull;");
          comma = "  , ";
        }
        if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        {
          outData.println(indent + comma + "boolean " + field.useLowerName() + "IsNull");
          comma = "  , ";
        }
        outData.println(indent + comma + javaVar(field));
        comma = "  , ";
      }
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (field.isPrimaryKey)
          continue;
        if (proc.isSProc == true && field.isIn == false)
          continue;
        if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
        {
          outData.println(indent + comma + "boolean " + field.useLowerName() + "IsNull");
          comma = "  , ";
        }
        outData.println(indent + comma + javaVar(field));
        comma = "  , ";
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        String name = (String)proc.dynamics.elementAt(i);
        outData.println(indent + comma + "String " + name);
        comma = "  , ";
      }
      outData.println(indent + "  ) throws Exception");
      outData.println(indent + "  {");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (proc.isSProc == true && field.isIn == false)
          continue;
        String usename = field.useLowerName();
        if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
          outData.println(indent + "    this." + usename + "IsNull = " + usename + "IsNull;");
        outData.println(indent + "    this." + usename + " = " + usename + ";");
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        String name = (String)proc.dynamics.elementAt(i);
        outData.println(indent + "    this." + name + " = " + name + ";");
      }
      if (proc.outputs.size() > 0)
        outData.println(indent + "    return " + procName + "();");
      else
        outData.println(indent + "    " + procName + "();");
      outData.println(indent + "  }");
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    if (table.fields.size() > 0)
      generateStdProcStruct(table, output, outLog);
    generateOtherProcStructs(table, output, outLog);
  }
  private static void generateStdProcStruct(Table table, String output, PrintWriter outLog)
  {
    try
    {
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
        outData.println("import java.io.Serializable;");
        outData.println("import java.sql.*;");
        outData.println("import bbd.jportal.*;");
        outData.println("import bbd.jportal.util.*;");
        outData.println("import bbd.crackle.util.*;");
        outData.println();
        outData.println("/**");
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println(" *" + s);
        }
        outData.println(" */");
        outData.println("public class " + table.useName() + "Rec implements Serializable");
        outData.println("{");
        outData.println("  private static final long serialVersionUID = 1L;");
        //generateEnumOrdinals(table, outData);
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
          if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
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
          if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
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
  private static void generateOtherProcStruct(Table table, Proc proc, String output, PrintWriter outLog)
  {
    try
    {
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
        outData.println("import java.io.Serializable;");
        outData.println("import java.sql.*;");
        outData.println("import bbd.jportal.*;");
        outData.println("import bbd.jportal.util.*;");
        outData.println("import bbd.crackle.util.*;");
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
        outData.println("public class " + table.useName() + proc.upperFirst() + "Rec " + extendsStd + "implements Serializable");
        outData.println("{");
        outData.println("  private static final long serialVersionUID = 1L;");
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
          if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
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
            if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
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
          if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
            outData.println("    " + field.useLowerName() + "IsNull = false;");
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field)proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
            outData.println("    " + initJavaVar(field));
          if (field.isNull == true && field.isEmptyOrAnsiAsNull() == false)
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
        String ret = "    return ";
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          outData.print(ret);
          ret = "         + ";
          Field field = (Field)proc.inputs.elementAt(j);
          int no = maxSize - field.useLowerName().length();
          outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field)proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
          {
            outData.print(ret);
            ret = "         + ";
            int no = maxSize - field.useLowerName().length();
            outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
          }
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.print(ret);
          ret = "         + ";
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
        return "@FieldAnnotate(size=" + (field.length + 1) + ")String " + name;
      case Field.DATE:
        return "@FieldAnnotate(size=9,type=FieldType.DATE)String " + name;
      case Field.DATETIME:
        return "@FieldAnnotate(size=15,type=FieldType.DATETIME)String " + name;
      case Field.TIME:
        return "@FieldAnnotate(size=7,type=FieldType.TIME)String " + name;
      case Field.TIMESTAMP:
        return "@FieldAnnotate(size=15,type=FieldType.TIMESTAMP)String " + name;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "@FieldAnnotate(size=" + (field.precision + 3) + ",type=FieldType.MONEY)String " + name;
        else
          return "double " + name;
      case Field.BLOB:
        return "JPBlob " + name;
      case Field.TLOB:
        return "@FieldAnnotate(size=" + (field.length + 1) + ",type=FieldType.TLOB)String " + name;
      case Field.MONEY:
        return "@FieldAnnotate(size=21,type=FieldType.MONEY)String " + name;
      case Field.USERSTAMP:
        return "@FieldAnnotate(size=" + (field.length + 1) + ",type=FieldType.USERSTAMP)String " + name;
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
