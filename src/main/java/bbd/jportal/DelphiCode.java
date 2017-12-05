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

public class DelphiCode extends Generator
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
        outLog.println(args[i]+": Generate Delphi BDE Code");
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
    return "Generate Delphi BDE Code";
  }
  public static String documentation()
  {
    return "Generate Delphi BDE Code";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output, outLog);
    }
  }
  static void println(PrintWriter pw)
  {
    pw.println();
  }
  static void println(PrintWriter pw, String line)
  {
    String newline = line.replace("`", "    ");
    pw.println(newline);
  }
  /**
  * Build of standard and user defined procedures
  */
  static void generate(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + ".pas");
      OutputStream outFile = new FileOutputStream(output+table.useName() + ".pas");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        println(outData, "unit "+table.useName()+";");
        println(outData, "// This code was generated, do not modify it, modify it at source and regenerate it.");
        println(outData);
        println(outData, "interface");
        println(outData);
        println(outData, "uses  SysUtils, Db, DbTables, Connector;");
        println(outData);
        if (table.hasStdProcs)
          generateStdInterface(table, outData);
        generateOtherInterface(table, outData);
        for (int i=0; i<table.procs.size(); i++)
        {
          Proc proc = (Proc) table.procs.elementAt(i);
          if (proc.isData)
            continue;
          if (proc.dynamics.size() < 1)
          {
            println(outData, "var");
            println(outData, "`"+table.useName()+proc.upperFirst()+" : String =");
            generateSQLCode(proc, outData);
            println(outData);
          }
        }
        println(outData, "implementation");
        println(outData);
        if (table.hasStdProcs)
          generateStdImplementation(table, outData);
        generateOtherImplementation(table, outData);
        println(outData, "end.");
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
  static void generateStdInterface(Table table, PrintWriter outData)
  {
    for (int i=0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      println(outData, "//"+s);
    }
    println(outData, "type T"+table.useName()+" = Class");
    println(outData, "`Conn : TConnector;");
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (field.comments.size() > 0)
      {
        for (int c=0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          println(outData, "`//"+s);
        }
      }
      println(outData, "`"+delphiVar(field)+";");
      if (field.isNull) // && notString(field))
        println(outData, "`"+field.useName()+"IsNull : boolean;");
    }
    println(outData, "`constructor Create(const aConnector : TConnector);");
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStd || proc.hasNoData())
        generateInterface(proc, outData);
    }
    println(outData, "end;");
    println(outData);
  }
  /**
  * Build of all required standard procedures
  */
  static void generateStdImplementation(Table table, PrintWriter outData)
  {
    println(outData, "constructor T"+table.useName()+".Create(const aConnector : TConnector);");
    println(outData, "begin");
    println(outData, "`Conn := aConnector;");
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      println(outData, "`"+initDelphiVar(field));
      if (field.isNull) // && notString(field))
        println(outData, "`"+field.useName()+"IsNull := false;");
    }
    println(outData, "end;");
    println(outData);
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStd || proc.hasNoData())
        generateImplementation(proc, outData, table.useName(), table.useName());
    }
  }
  /**
  * Build of user defined procedures
  */
  static void generateOtherInterface(Table table, PrintWriter outData)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      for (int j=0; j<proc.comments.size(); j++)
      {
        String comment = (String) proc.comments.elementAt(j);
        println(outData, "//"+comment);
      }
      println(outData, "type T" + table.useName() + proc.upperFirst() + " = Class");
      println(outData, "`Conn : TConnector;");
      for (int j=0; j<proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        for (int c=0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          println(outData, "`//"+s);
        }
        println(outData, "`"+delphiVar(field)+";");
        if (field.isNull) // && notString(field))
          println(outData, "`"+field.useName()+"IsNull : boolean;");
      }
      for (int j=0; j<proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        if (!proc.hasInput(field.name))
        {
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            println(outData, "`//"+s);
          }
          println(outData, "`"+delphiVar(field)+";");
          if (field.isNull) // && notString(field))
            println(outData, "`"+field.useName()+"IsNull : boolean;");
        }
      }
      for (int j=0; j<proc.dynamics.size(); j++)
      {
        String s = (String) proc.dynamics.elementAt(j);
        println(outData, "`"+s+" : String;");
      }
      println(outData, "`constructor Create(const aConnector : TConnector);");
      generateInterface(proc, outData);
      println(outData, "end;");
      println(outData);
    }
  }
  static void generateOtherImplementation(Table table, PrintWriter outData)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      println(outData, "constructor T"+table.useName()+proc.name+".Create(const aConnector : TConnector);");
      println(outData, "begin");
      println(outData, "`Conn := aConnector;");
      for (int j=0; j<proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        println(outData, "`"+initDelphiVar(field));
        if (field.isNull) // && notString(field))
          println(outData, "`"+field.useName()+"IsNull := false;");
      }
      for (int j=0; j<proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        if (!proc.hasInput(field.name))
          println(outData, "`"+initDelphiVar(field));
        if (field.isNull) // && notString(field))
          println(outData, "`"+field.useName()+"IsNull := false;");
      }
      for (int j=0; j<proc.dynamics.size(); j++)
      {
        String s = (String) proc.dynamics.elementAt(j);
        println(outData, "`"+s+" := '';");
      }
      println(outData, "end;");
      println(outData);
      generateImplementation(proc, outData, table.useName(), table.useName()+proc.name);
    }
  }
  static void generateWithParms(Proc proc, PrintWriter outData)
  {
    String semicolon = "  ";
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      println(outData, "`"+semicolon+"const a"+delphiVar(field));
      semicolon = "; ";
      if (field.isNull) // && notString(field))
        println(outData, "`"+semicolon+"const a"+field.useName()+"IsNull : boolean");
    }
    for (int j=0; j<proc.dynamics.size(); j++)
    {
      String s = (String) proc.dynamics.elementAt(j);
      println(outData, "`"+semicolon+"const a"+s+" : String");
    }
  }
  /** Emits class method for processing the database activity */
  static void generateInterface(Proc proc, PrintWriter outData)
  {
    if (proc.comments.size() > 0)
    {
      for (int i=0; i<proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        println(outData, "`//"+comment);
      }
    }
    if (proc.hasNoData())
    {
      println(outData, "`class procedure "+proc.upperFirst()+"(const Conn : TConnector);");
    }
    else if (proc.outputs.size() == 0)
    {
      println(outData, "`procedure "+proc.upperFirst()+";");
      if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      {
        println(outData, "`procedure wp"+proc.upperFirst()+"(");
        generateWithParms(proc, outData);
        println(outData, "`);");
      }
    }
    else if (proc.isSingle)
    {
      println(outData, "`function "+proc.upperFirst()+" : Boolean;");
      if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      {
        println(outData, "`function wp"+proc.upperFirst()+"(");
        generateWithParms(proc, outData);
        println(outData, "`) : Boolean;");
      }
    }
    else
    {
      println(outData, "`function "+proc.upperFirst()+" : TQuery;");
      if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      {
        println(outData, "`function wp"+proc.upperFirst()+"(");
        generateWithParms(proc, outData);
        println(outData, "`) : tQuery;");
      }
      println(outData, "`function next"+proc.upperFirst()+"(const Query : TQuery) : Boolean;");
    }
  }
  static int questionsSeen;
  static String question(Proc proc, String line)
  {
    String result = "";
    int p;
    while ((p = line.indexOf("?")) > -1)
    {
      if (p > 0)
      {
        result = result + line.substring(0, p);
        line = line.substring(p);
      }
      Field field = (Field) proc.inputs.elementAt(questionsSeen++);
      if (field.type == Field.IDENTITY && proc.isInsert)
        field = (Field) proc.inputs.elementAt(questionsSeen++);
      result = result + ":" + field.name;
      line = line.substring(1);
    }
    result = result + line;
    return result;
  }
  /**
  * Emits class method for processing the database activity
  */
  static void generateImplementation(Proc proc, PrintWriter outData, String tableName, String fullName)
  {
    String with;
    if (proc.hasNoData())
      println(outData, "class procedure T"+fullName+ "" +proc.upperFirst()+";");
    else if (proc.outputs.size() == 0)
      println(outData, "procedure T"+fullName+ "" +proc.upperFirst()+";");
    else if (proc.isSingle)
      println(outData, "function T"+fullName+ "" +proc.upperFirst()+" : Boolean;");
    else
      println(outData, "function T"+fullName+ "" +proc.upperFirst()+" : TQuery;");
    if (proc.dynamics.size() > 0 || proc.outputs.size() == 0 || proc.isSingle)
      println(outData, "var");
    if (proc.outputs.size() == 0 || proc.isSingle)
      println(outData, "`Query : TQuery;");
    if (proc.dynamics.size() > 0)
      println(outData, "`"+tableName+proc.upperFirst()+" : String;");
    println(outData, "begin");
    if (proc.dynamics.size() > 0)
    {
      println(outData, "`"+tableName+proc.upperFirst()+" :=");
      generateSQLCode(proc, outData);
    }
    if (proc.outputs.size() == 0 || proc.isSingle)
    {
      println(outData, "`Query := TQuery.Create(nil);");
      println(outData, "`try");
      //println(outData, "``with Query do begin");
      with = "Query";
    }
    else
    {
      println(outData, "`result := TQuery.Create(nil);");
      println(outData, "`try");
      //println(outData, "``with result do begin");
      with = "result";
    }
    println(outData, "``"+with+".DatabaseName := Conn.DatabaseName;");
    println(outData, "``"+with+".SQL.Add("+tableName+proc.upperFirst()+");");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if (proc.isInsert)
      {
        if (field.isSequence)
          println(outData, "``"+field.useName()+" := Conn.getSequence('"+proc.table.name+"');");
      }
      if (field.type == Field.TIMESTAMP)
        println(outData, "``"+field.useName()+" := Conn.getTimeStamp;");
      if (field.type == Field.USERSTAMP)
        println(outData, "``"+field.useName()+" := Conn.getUserStamp;");
      if (field.isNull) // && notString(field))
      {
        println(outData, "``if not "+field.useName()+"IsNull then begin");
        println(outData, "```"+with+ "" +delphiInputs(field));
        println(outData, "``end else begin");
        println(outData, "```Query.Params.ParamByName('"+field.name+"').Clear;");
        println(outData, "```Query.Params.ParamByName('"+field.name+"').DataType := "+delphiDataType(field)+";");
        println(outData, "```Query.Params.ParamByName('"+field.name+"').Bound := true;");
        println(outData, "``end;");
      }
      else
        println(outData, "``"+with+ "" +delphiInputs(field));
    }
    if (proc.outputs.size() == 0)
      println(outData, "``"+with+".ExecSQL;");
    else
    {
      println(outData, "``"+with+".Open;");
      if (proc.isSingle)
      {
        println(outData, "``if not "+with+".eof then begin");
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          generateDelphiOutput(field, outData, "`", with);
        }
        println(outData, "```result := true;");
        println(outData, "``end");
        println(outData, "``else");
        println(outData, "```result := false;");
      }
    }
    //println(outData, "``end;");
    if (proc.outputs.size() == 0 || proc.isSingle)
    {
      println(outData, "`finally");
      println(outData, "``Query.free;");
    }
    else
    {
      println(outData, "`except");
      println(outData, "``result.free;");
    }
    println(outData, "`end;");
    println(outData, "end;");
    println(outData);
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
    {
      if (proc.outputs.size() == 0)
        println(outData, "procedure T"+fullName+".wp"+proc.upperFirst()+";");
      else if (proc.isSingle)
        println(outData, "function T"+fullName+".wp"+proc.upperFirst()+";");
      else
        println(outData, "function T"+fullName+".wp"+proc.upperFirst()+";");
      println(outData, "begin");
      for (int j=0; j<proc.inputs.size(); j++)
      {
        String Indent;
        Field field = (Field) proc.inputs.elementAt(j);
        Indent = "";
        if (field.isNull) // && notString(field))
        {
          println(outData, "`"+field.useName()+"IsNull := a"+field.useName()+"IsNull;");
          println(outData, "`if not "+field.useName()+"IsNull then");
          Indent = "  ";
        }
        println(outData, "`"+Indent+field.useName()+" := a"+field.useName()+";");
      }
      for (int j=0; j<proc.dynamics.size(); j++)
      {
        String s = (String) proc.dynamics.elementAt(j);
        println(outData, "`"+s+" := a"+s+";");
      }
      if (proc.outputs.size() == 0)
        println(outData, "`"+proc.upperFirst()+";");
      else
        println(outData, "`result := "+proc.upperFirst()+";");
      println(outData, "end;");
      println(outData);
    }
    if (proc.outputs.size() != 0 && !proc.isSingle)
    {
      println(outData, "function T"+fullName+".next"+proc.upperFirst()+"(const Query : TQuery) : Boolean;");
      println(outData, "begin");
      println(outData, "`if not Query.eof then begin");
      for (int j=0; j<proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        generateDelphiOutput(field, outData, "", "Query");
      }
      println(outData, "``result := true;");
      println(outData, "``Query.next;");
      println(outData, "`end");
      println(outData, "`else");
      println(outData, "``result := false;");
      println(outData, "end;");
      println(outData);
    }
  }
  /**
  * Emits SQL Code
  */
  static void generateDelphiOutput(Field field, PrintWriter outData, String gap, String with)
  {
    if (field.isNull) // && notString(field))
    {
      println(outData, gap+"``"+field.useName()+"IsNull := "+with+".FieldByName('"+field.name+"').isNull;");
      println(outData, gap+"``if not "+field.useName()+"IsNull then");
      println(outData, gap+"```"+delphiOutputs(field, with));
    }
    else
      println(outData, gap+"``"+delphiOutputs(field, with));
  }
  /**
  * Emits SQL Code
  */
  static void generateSQLCode(Proc proc, PrintWriter outData)
  {
    questionsSeen = 0;
    for (int i=0; i < proc.lines.size(); i++)
    {
      String x;
      if (i+1 < proc.lines.size())
        x = " +";
      else
        x = ";";
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        println(outData, "``"+l.line+x);
      else
      {
        String out = "``'"+question(proc, l.line)+"'"+x;
        println(outData, out);
      }
    }
  }
  /**
  * Translates field type to delphi data member type
  */
  static String delphiVar(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
      return field.useName() + " : Boolean";
    case Field.BYTE:
      return field.useName() + " : Shortint";
    case Field.SHORT:
      return field.useName() + " : Smallint";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " : Integer";
    case Field.LONG:
      return field.useName() + " : Longint";
    case Field.CHAR:
    case Field.ANSICHAR:
      return field.useName() + " : String";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
      return field.useName() + " : TDateTime";
    case Field.FLOAT:
    case Field.DOUBLE:
      return field.useName() + " : Double";
    case Field.BLOB:
    case Field.TLOB:
      return field.useName() + " : String";
    case Field.MONEY:
      return field.useName() + " : Double";
    case Field.USERSTAMP:
      return field.useName() + " : String";
    }
    return field.useName() + " : <unsupported>";
  }
  /**
  * returns the data member initialisation code (not always neccessary in java but
  * still we do it)
  */
  static String initDelphiVar(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
      return field.useName() +" := false;";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return field.useName() +" := Date;";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() +" := 0.0;";
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() +" := 0;";
    case Field.TIME:
      return field.useName() +" := Time;";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.BLOB:
    case Field.TLOB:
      return field.useName() +" := '';";
    }
    return field.useName() +"<unsupported>";
  }
  /**
  */
  static String delphiInputs(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
      return "Params.ParamByName('"+field.name+"').AsBoolean := "+field.useName()+";";
    case Field.DATE:
      return "Params.ParamByName('"+field.name+"').AsDateTime := "+field.useName()+";";
    case Field.DATETIME:
      return "Params.ParamByName('"+field.name+"').AsDateTime := "+field.useName()+";";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "Params.ParamByName('"+field.name+"').AsFloat := "+field.useName()+";";
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
      return "Params.ParamByName('"+field.name+"').AsInteger := "+field.useName()+";";
    case Field.IDENTITY:
    case Field.SEQUENCE:
      return "Params.ParamByName('"+field.name+"').AsInteger := "+field.useName()+";";
    case Field.TIME:
    case Field.TIMESTAMP:
      return "Params.ParamByName('"+field.name+"').AsDateTime := "+field.useName()+";";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return "Params.ParamByName('"+field.name+"').AsString := "+field.useName()+";";
    case Field.BLOB:
      return "Params.ParamByName('"+field.name+"').AsBlob := "+field.useName()+";";
    case Field.TLOB:
      return "Params.ParamByName('"+field.name+"').AsMemo := "+field.useName()+";";
    }
    return field.useName() +"<unsupported>";
  }
  /**
  */
//  static boolean notString(Field field)
//  {
//    switch(field.type)
//    {
//    case Field.BOOLEAN:
//    case Field.DATE:
//    case Field.DATETIME:
//    case Field.FLOAT:
//    case Field.DOUBLE:
//    case Field.MONEY:
//    case Field.BYTE:
//    case Field.SHORT:
//    case Field.INT:
//    case Field.LONG:
//    case Field.IDENTITY:
//    case Field.SEQUENCE:
//    case Field.TIME:
//    case Field.TIMESTAMP:
//    case Field.BLOB:
//    case Field.TLOB:
//      return true;
//    }
//    return false;
//  }
  /**
  */
  static String delphiOutputs(Field field, String with)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsBoolean;";
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.LONG:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsInteger;";
    case Field.DATE:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsDateTime;";
    case Field.DATETIME:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsDateTime;";
    case Field.FLOAT:
    case Field.DOUBLE:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsFloat;";
    case Field.MONEY:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsCurrency;";
    case Field.TIME:
    case Field.TIMESTAMP:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsDateTime;";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsString;";
    case Field.BLOB:
    case Field.TLOB:
      return field.useName() +" := "+with+".FieldByName('"+field.name+"').AsString;";
    }
    return field.useName() +"<unsupported>";
  }
  /**
  */
  static String delphiDataType(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.DATE:
      return "ftDate";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "ftDateTime";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "ftFloat";
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
      return "ftInteger";
    case Field.TIME:
      return "ftTime";
    }
    return "ftString";
  }
}

