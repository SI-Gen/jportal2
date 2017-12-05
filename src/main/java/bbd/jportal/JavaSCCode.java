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

public class JavaSCCode extends Generator
{
  /**
   * Reads input from stored repository
   */
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i <args.length; i++)
      {
        outLog.println(args[i]+": generate Java Single Class code");
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
    return "generate Java Single Class code - with main class has connection and implementation uses Standard and User public inner classes";
  }
  public static String documentation()
  {
    return "generate Java Single Class code - with main class has connection and implementation uses Standard and User public inner classes";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output, outLog);
    }
  }
  /**
   * Build of standard and user defined procedures
   */
  static void generate(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + ".java");
      OutputStream outFile = new FileOutputStream(output+table.useName() + ".java");
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
  /**
   * Build of all required standard procedures
   */
  static void generateStdProcs(Table table, PrintWriter outData)
  {
    if (table.database.packageName.length() > 0)
      outData.println("package " + table.database.packageName + ";");
    outData.println("import java.sql.Connection;");
    outData.println("import java.sql.PreparedStatement;");
    outData.println("import java.sql.ResultSet;");
    outData.println("import java.sql.SQLException;");
    outData.println("import bbd.jportal.util.Connector;");
    outData.println("import bbd.jportal.util.Query;");
    outData.println("/**");
    for (int i=0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      outData.println("*"+s);
    }
    outData.println("* This code was generated, do not modify it, modify it at source and regenerate it.");
    outData.println("* With main class has connection");
    outData.println("* Implementation uses Standard and User public inner classes.");
    outData.println("*/");
    outData.println("public class "+table.useName());
    outData.println("{");
    outData.println("  Connector connector;");
    outData.println("  Connection connection;");
    outData.println("  public "+table.useName()+"(Connector connector)");
    outData.println("  {");
    outData.println("    this.connector = connector;");
    outData.println("    connection = connector.connection;");
    outData.println("  }");
    if (table.fields.size() > 0)
    {
      generateEnumOrdinals(table, outData);
      outData.println("  public class Standard");
      outData.println("  {");
      for (int i=0; i<table.fields.size(); i++)
      {
        Field field = (Field) table.fields.elementAt(i);
        if (field.comments.size() > 0)
        {
          outData.println("    /**");
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("    *"+s);
          }
          outData.println("    */");
        }
        if (field.isNull)
          outData.println("    public boolean "+field.useName()+"IsNull;");
        outData.println("    public "+javaVar(field)+";");
      }
      outData.println("    /**");
      outData.println("    * @param Connector for specific database");
      outData.println("    */");
      outData.println("    public Standard()");
      outData.println("    {");
      for (int i=0; i<table.fields.size(); i++)
      {
        Field field = (Field) table.fields.elementAt(i);
        outData.println("      "+initJavaVar(field));
      }
      outData.println("    }");
      outData.println("    public Standard getCopy()");
      outData.println("    {");
      outData.println("      Standard _result = new Standard();");
      for (int i=0; i<table.fields.size(); i++)
      {
        Field field = (Field) table.fields.elementAt(i);
        String name = field.useName();
        outData.println("      _result."+name+" = "+name+";");
      }
      outData.println("      return _result;");
      outData.println("    }");
      for (int i=0; i<table.procs.size(); i++)
      {
        Proc proc = (Proc) table.procs.elementAt(i);
        if (proc.isData)
          continue;
        if (proc.isStd)
          emitProc(proc, outData);
      }
      outData.println("  }");
      outData.println("  public Standard getStandard()");
      outData.println("  {");
      outData.println("    return new Standard();");
      outData.println("  }");
    }
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStd)
        continue;
      else if (proc.hasNoData())
        emitStaticProc(proc, outData);
    }
  }
  public static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i=0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("  public static class "+field.useUpperName()+"Ord");
        outData.println("  {");
        for (int j=0; j<field.enums.size(); j++)
        {
          Enum element = (Enum) field.enums.elementAt(j);
          String evalue = ""+element.value;
          if (field.type == Field.ANSICHAR &&  field.length == 1)
            evalue = "'"+(char)element.value+"'";
          outData.println("    public static "+javaVarType(field)+" "+element.name.toUpperCase()+" = "+evalue+";");
        }
        outData.println("    public static String toString("+javaVarType(field)+" ordinal)");
        outData.println("    {");
        outData.println("      switch (ordinal)");
        outData.println("      {");
        for (int j=0; j<field.enums.size(); j++)
        {
          Enum element = (Enum) field.enums.elementAt(j);
          String evalue = ""+element.value;
          if (field.type == Field.ANSICHAR &&  field.length == 1)
            evalue = "'"+(char)element.value+"'";
          outData.println("      case "+evalue+": return \""+element.name+"\";");
        }
        outData.println("      }");
        outData.println("      return \"<??\"+ordinal+\"??>\";");
        outData.println("    }");
        outData.println("  }");
      }
    }
  }
  /**
   * Build of user defined procedures
   */
  static void generateOtherProcs(Table table, PrintWriter outData, String output, PrintWriter outLog)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (!proc.isStd && !proc.hasNoData())
      {
        outData.println("  /**");
        for (int j=0; j<proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println("  *"+comment);
        }
        outData.println("  */");
        outData.println("  public class " + proc.upperFirst());
        outData.println("  {");
        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          outData.println("    /**");
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("    *"+s);
          }
          if (proc.hasOutput(field.name) == false || field.isOut == false)
            outData.println("    * (input)");
          else
            outData.println("    * (input/output)");
          outData.println("    */");
          if (field.isNull)
            outData.println("    public boolean "+field.useName()+"IsNull;");
          outData.println("    public "+javaVar(field)+";");
        }
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
          {
            outData.println("    /**");
            for (int c=0; c < field.comments.size(); c++)
            {
              String s = (String) field.comments.elementAt(c);
              outData.println("    *"+s);
            }
            outData.println("    * (output)");
            outData.println("    */");
            if (field.isNull)
              outData.println("    public boolean "+field.useName()+"IsNull;");
            outData.println("    public "+javaVar(field)+";");
          }
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData.println("    /**");
          outData.println("    * (dynamic)");
          outData.println("    */");
          outData.println("    public String "+s+";");
        }
        outData.println("    public " + proc.upperFirst()+"()");
        outData.println("    {");
        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          outData.println("      "+initJavaVar(field));
        }
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
            outData.println("      "+initJavaVar(field));
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData.println("      "+s+" = \"\";");
        }
        outData.println("    }");
        outData.println("    public "+proc.upperFirst()+" getCopy()");
        outData.println("    {");
        outData.println("      "+proc.upperFirst()+" _result = new "+proc.upperFirst()+"();");
        for (int jj=0; jj<proc.inputs.size(); jj++)
        {
          Field field = (Field) proc.inputs.elementAt(jj);
          String name = field.useName();
          outData.println("      _result."+name+" = "+name+";");
        }
        for (int jj=0; jj<proc.outputs.size(); jj++)
        {
          Field field = (Field) proc.outputs.elementAt(jj);
          if (proc.hasInput(field.name))
            continue;
          String name = field.useName();
          outData.println("      _result."+name+" = "+name+";");
        }
        for (int jj=0; jj<proc.dynamics.size(); jj++)
        {
          String s = (String) proc.dynamics.elementAt(jj);
          outData.println("      _result."+s+" = "+s+";");
        }
        outData.println("      return _result;");
        outData.println("    }");
        emitProc(proc, outData);
        outData.println("  }");
        outData.println("  public "+proc.upperFirst()+" get"+proc.upperFirst()+"()");
        outData.println("  {");
        outData.println("    return new "+proc.upperFirst()+"();");
        outData.println("  }");
        outData.flush();
      }
    }
  }
  /**
   *
   */
  static int checkPlaceHolders(Proc proc, PrintWriter outData, String l, int phIndex)
  {
    if (phIndex >= proc.placeHolders.size())
    {
      outData.println(l);
      return phIndex;
    }
    int n = 0;
    while (phIndex < proc.placeHolders.size())
    {
      String placeHolder = ":" + (String) proc.placeHolders.elementAt(phIndex);
      n = l.indexOf(placeHolder);
      if (n == -1)
        break;
      phIndex++;
      String work = "";
      if (n > 0)
        work = work + l.substring(0, n);
      work = work + "?";
      n += placeHolder.length();
      if (n < l.length());
      work = work + l.substring(n);
      l = work;
    }
    outData.println(l);
    return phIndex;
  }
  /**
   * Emits a static or class method
   */
  static void emitStaticProc(Proc proc, PrintWriter outData)
  {
    outData.println("  /**");
    outData.println("  * class method as it has no input or output.");
    outData.println("  * @exception SQLException is passed through");
    outData.println("  */");
    outData.println("  public static void "+proc.lowerFirst()+"(Connector connector) throws SQLException");
    outData.println("  {");
    outData.println("    String statement = ");
    int phIndex = 0;
    String plus = "     ";
    for (int i=0; i<proc.lines.size(); i++)
    {
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        outData.println(plus+l.line);
      else
        phIndex = checkPlaceHolders(proc, outData, plus+" \""+l.line+"\"", phIndex);
      plus = "    +";
    }
    outData.println("    ;");
    outData.println("    PreparedStatement prepared = connector.connection.prepareStatement(statement);");
    outData.println("    prepared.executeUpdate();");
    outData.println("    prepared.close();");
    outData.println("  }");
  }
  static void emitOutputBinds(Proc proc, Field field, String prep, String indent, int pos, PrintWriter outData)
  {
    if (field.isOut == true)
    {
      outData.print(indent+"    "+field.useName()+" = "+prep+".get");
      outData.print(setType(field));
      outData.print("(");
      outData.print(pos);
      outData.println(");");
    }
  }
  static void emitInputBinds(Proc proc, Field field, String prep, String indent, int pos, PrintWriter outData)
  {
    if (field.isIn == true || field.isOut == false)
    {
      String pad = "";
      if (field.isNull)
      {
        pad = "  ";
        outData.println(indent+"    if ("+field.useName()+"IsNull)");
        outData.println(indent+pad+"    "+prep+".setNull("+(pos)+", "+javaType(field)+");");
        outData.println(indent+"    else");
      }
      outData.print(indent+pad+"    "+prep+".set");
      outData.print(setType(field));
      outData.print("(");
      outData.print(pos);
      outData.println(", "+field.useName()+");");
    }
    if (field.isOut == true && proc.isSProc == true)
      outData.println(indent+"    "+prep+".registerOutParameter("+(pos)+", "+javaType(field)+");");
  }
  /** Emits class method for processing the database activity */
  static void emitProc(Proc proc, PrintWriter outData)
  {
    String indent = "  ";
    outData.println(indent+"  /**");
    if (proc.comments.size() > 0)
    {
      for (int i=0; i<proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println(indent+"  *"+comment);
      }
    }
    if (proc.outputs.size() == 0)
      outData.println(indent+"  * Returns no output.");
    else if (proc.isSingle)
    {
      outData.println(indent+"  * Returns at most one record.");
      outData.println(indent+"  * @return true if a record is found");
    }
    else
    {
      outData.println(indent+"  * Returns any number of records.");
      outData.println(indent+"  * @return result set of records found");
    }
    outData.println(indent+"  * @exception SQLException is passed through");
    outData.println(indent+"  */");
    String procName = proc.lowerFirst();
    if (proc.outputs.size() == 0 || proc.isSProc == true)
      outData.println(indent+"  public void "+procName+"() throws SQLException");
    else if (proc.isSingle)
      outData.println(indent+"  public boolean "+procName+"() throws SQLException");
    else
      outData.println(indent+"  public Query "+procName+"() throws SQLException");
    outData.println(indent+"  {");
    outData.println(indent+"    String statement = ");
    int phIndex = 0;
    String plus = "     ";
    for (int i=0; i<proc.lines.size(); i++)
    {
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        outData.println(plus+l.line);
      else
      {
        phIndex = checkPlaceHolders(proc, outData, plus+" \""+l.line+"\"", phIndex);
      }
      plus = "    +";
    }
    outData.println(indent+"    ;");
    String prep = "prepared";
    if (proc.isSProc == true)
    {
      prep = "callable";
      outData.println(indent+"    CallableStatement "+prep+" = connection.prepareCall(statement);");
    }
    else
      outData.println(indent+"    PreparedStatement "+prep+" = connection.prepareStatement(statement);");
    for (int i=0; i<proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      if (proc.isInsert)
      {
        if (field.isSequence)
          outData.println(indent+"    "+field.useName()+" = connector.getSequence(\""+proc.table.name+"\");");
      }
      if (field.type == Field.TIMESTAMP)
        outData.println(indent+"    "+field.useName()+" = connector.getTimestamp();");
      if (field.type == Field.USERSTAMP)
        outData.println(indent+"    "+field.useName()+" = connector.getUserstamp();");
    }
    if (proc.placeHolders.size() > 0)
    {
      for (int ph=0; ph<proc.placeHolders.size(); ph++)
      {
        String placeHolder = (String) proc.placeHolders.elementAt(ph);
        int i = proc.indexOf(placeHolder);
        Field field = (Field) proc.inputs.elementAt(i);
        emitInputBinds(proc, field, prep, indent, ph+1, outData);
      }
    }
    else
    {
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        emitInputBinds(proc, field, prep, indent, i+1, outData);
      }
    }
    if (proc.outputs.size() > 0 && proc.isSProc == false)
    {
      outData.println(indent+"    ResultSet result = "+prep+".executeQuery();");
      if (!proc.isSingle)
      {
        outData.println(indent+"    Query query = new Query("+prep+", result);");
        outData.println(indent+"    return query;");
        outData.println(indent+"  }");
        outData.println(indent+"  /**");
        outData.println(indent+"  * Returns the next record in a result set.");
        outData.println(indent+"  * @param result The result set for the query.");
        outData.println(indent+"  * @return true while records are found.");
        outData.println(indent+"  * @exception SQLException is passed through");
        outData.println(indent+"  */");
        outData.println(indent+"  public boolean "+procName+"(Query query) throws SQLException");
        outData.println(indent+"  {");
        outData.println(indent+"    if (!query.result.next())");
        outData.println(indent+"    {");
        outData.println(indent+"      query.close();");
        outData.println(indent+"      return false;");
        outData.println(indent+"    }");
        outData.println(indent+"    ResultSet result = query.result;");
      }
      else
      {
        outData.println(indent+"    if (!result.next())");
        outData.println(indent+"    {");
        outData.println(indent+"      result.close();");
        outData.println(indent+"      "+prep+".close();");
        outData.println(indent+"      return false;");
        outData.println(indent+"    }");
      }
      for (int i=0; i<proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        String pad = "";
        if (field.isNull)
        {
          outData.println(indent+"    "+field.useName()+"IsNull = result.getObject("
            +(i+1)+") == null;");
          outData.println(indent+"    if ("+field.useName()+"IsNull == false)");
          pad = "  ";
        }
        outData.print(indent+pad+"    "+field.useName()+" =  result.get");
        outData.print(setType(field));
        outData.print("(");
        outData.print(i+1);
        outData.println(");");
      }
      if (proc.isSingle)
      {
        outData.println(indent+"    result.close();");
        outData.println(indent+"    "+prep+".close();");
      }
      outData.println(indent+"    return true;");
    }
    else
    {
      outData.println(indent+"    "+prep+".executeUpdate();");
      if (proc.isSProc == true)
      {
        if (proc.placeHolders.size() > 0)
        {
          for (int ph=0; ph<proc.placeHolders.size(); ph++)
          {
            String placeHolder = (String) proc.placeHolders.elementAt(ph);
            int i = proc.indexOf(placeHolder);
            Field field = (Field) proc.inputs.elementAt(i);
            emitOutputBinds(proc, field, prep, indent, ph+1, outData);
          }
        }
        else
        {
          for (int i=0; i<proc.inputs.size(); i++)
          {
            Field field = (Field) proc.inputs.elementAt(i);
            emitOutputBinds(proc, field, prep, indent, i+1, outData);
          }
        }
      }
      outData.println(indent+"    "+prep+".close();");
    }
    outData.println(indent+"  }");
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
    {
      outData.println(indent+"  /**");
      if (proc.outputs.size() == 0)
        outData.println(indent+"  * Returns no records.");
      else if (proc.isSingle)
      {
        outData.println(indent+"  * Returns at most one record.");
        outData.println(indent+"  * @return true if a record is returned.");
      }
      else
      {
        outData.println(indent+"  * Returns any number of records.");
        outData.println(indent+"  * @return result set of records found");
      }
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (!field.isPrimaryKey)
          continue;
        outData.println(indent+"  * @param "+field.useName()+" key input.");
      }
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (field.isPrimaryKey)
          continue;
        if (proc.isSProc == true && field.isIn == false)
          continue;
        outData.println(indent+"  * @param "+field.useName()+" input.");
      }
      for (int i=0; i<proc.dynamics.size(); i++)
      {
        //String name = (String) proc.dynamics.elementAt(i);
        outData.println(indent+"  * @param "+proc.name+" dynamic input.");
      }
      outData.println(indent+"  * @exception SQLException is passed through");
      outData.println(indent+"  */");
      if (proc.outputs.size() == 0)
        outData.println(indent+"  public void "+procName+"(");
      else if (proc.isSingle)
        outData.println(indent+"  public boolean "+procName+"(");
      else
        outData.println(indent+"  public Query "+procName+"(");
      String comma = "    ";
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (!field.isPrimaryKey)
          continue;
        if (field.isNull)
        {
          outData.println("indent+comma+boolean "+field.useName()+"IsNull;");
          comma = "  , ";
        }
        if (field.isNull)
        {
          outData.println(indent+comma+"boolean "+field.useName()+"IsNull");
          comma = "  , ";
        }
        outData.println(indent+comma+javaVar(field));
        comma = "  , ";
      }
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (field.isPrimaryKey)
          continue;
        if (proc.isSProc == true && field.isIn == false)
          continue;
        if (field.isNull)
        {
          outData.println(indent+comma+"boolean "+field.useName()+"IsNull");
          comma = "  , ";
        }
        outData.println(indent+comma+javaVar(field));
        comma = "  , ";
      }
      for (int i=0; i<proc.dynamics.size(); i++)
      {
        String name = (String) proc.dynamics.elementAt(i);
        outData.println(indent+comma+"String "+name);
        comma = "  , ";
      }
      outData.println(indent+"  ) throws SQLException");
      outData.println(indent+"  {");
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (proc.isSProc == true && field.isIn == false)
          continue;
        String usename = field.useName();
        if (field.isNull)
          outData.println(indent+"    this."+usename+"IsNull = "+usename+"IsNull;");
        outData.println(indent+"    this."+usename+" = "+usename+";");
      }
      for (int i=0; i<proc.dynamics.size(); i++)
      {
        String name = (String) proc.dynamics.elementAt(i);
        outData.println(indent+"    this."+name+" = "+name+";");
      }
      if (proc.outputs.size() > 0)
        outData.println(indent+"    return "+procName+"();");
      else
        outData.println(indent+"    "+procName+"();");
      outData.println(indent+"  }");
    }
  }
  /**
   * Translates field type to java data member type
   */
  static String javaVar(Field field)
  {
    switch(field.type)
    {
      case Field.BYTE:
        return "byte "+ field.useName();
      case Field.SHORT:
        return "short "+ field.useName();
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "int "+ field.useName();
      case Field.LONG:
        return "long "+ field.useName();
      case Field.CHAR:
      case Field.ANSICHAR:
        return "String "+ field.useName();
      case Field.DATE:
        return "Date "+ field.useName();
      case Field.DATETIME:
        return "Date "+ field.useName();
      case Field.TIME:
        return "Time "+ field.useName();
      case Field.TIMESTAMP:
        return "Timestamp "+ field.useName();
      case Field.FLOAT:
      case Field.DOUBLE:
        return "double "+ field.useName();
      case Field.BLOB:
      case Field.TLOB:
        return "String "+ field.useName();
      case Field.MONEY:
        return "double "+ field.useName();
      case Field.USERSTAMP:
        return "String "+ field.useName();
    }
    return "unknown";
  }
  /**
   * returns the data member initialisation code (not always neccessary in java but
   * still we do it)
   */
  static String initJavaVar(Field field)
  {
    switch(field.type)
    {
      case Field.BYTE:
        return field.useName() +" = 0;";
      case Field.CHAR:
      case Field.ANSICHAR:
        return field.useName() +" = \"\";";
      case Field.DATE:
        return field.useName() +" = new Date(0);";
      case Field.DATETIME:
        return field.useName() +" = new Date(0);";
      case Field.FLOAT:
      case Field.DOUBLE:
        return field.useName() +" = 0.0;";
      case Field.BLOB:
      case Field.TLOB:
        return field.useName() +" = \"\";";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return field.useName() +" = 0;";
      case Field.LONG:
        return field.useName() +" = 0;";
      case Field.MONEY:
        return field.useName() +" = 0.0;";
      case Field.SHORT:
        return field.useName() +" = 0;";
      case Field.TIME:
        return field.useName() +" = new Time(0);";
      case Field.TIMESTAMP:
        return field.useName() +" = new Timestamp(0);";
      case Field.USERSTAMP:
        return field.useName() +" = \"\";";
    }
    return "unknown";
  }
  static String javaType(Field field)
  {
    switch(field.type)
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
      return "java.sql.Types.DATE";
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
  static String javaVarType(Field field)
  {
    switch(field.type)
    {
    case Field.BYTE:
      return "byte";
    case Field.CHAR:
      return "String";
    case Field.ANSICHAR:
      if (field.length == 1)
        return "char";
      return "String";
    case Field.DATE:
      return "Date";
    case Field.DATETIME:
      return "Date";
    case Field.FLOAT:
    case Field.DOUBLE:
      return "double";
    case Field.BLOB:
      return "byte[]";
    case Field.TLOB:
      return "char[]";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int";
    case Field.LONG:
      return "long";
    case Field.MONEY:
      return "double";
    case Field.SHORT:
      return "short";
    case Field.TIME:
      return "Time";
    case Field.TIMESTAMP:
      return "Date";
    case Field.USERSTAMP:
      return "String";
    }
    return "junket";
  }
  /**
   * JDBC get and set type for field data transfers
   */
  static String setType(Field field)
  {
    switch(field.type)
    {
      case Field.BYTE:
        return "Byte";
      case Field.CHAR:
      case Field.ANSICHAR:
        return "String";
      case Field.DATE:
        return "Date";
      case Field.DATETIME:
        return "Date";
      case Field.FLOAT:
      case Field.DOUBLE:
        return "Double";
      case Field.BLOB:
      case Field.TLOB:
        return "String";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "Int";
      case Field.LONG:
        return "Long";
      case Field.MONEY:
        return "Double";
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
}
