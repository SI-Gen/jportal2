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

/// Field.XML is not handled here

package bbd.jportal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class AdoVBCode3 extends Generator
{
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i <args.length; i++)
      {
        outLog.println(args[i]+": Generate VB Code for ADO");
        in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database)in.readObject();
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
    return "Generates Code3 Style VB5/6 ADO Code";
  }
  public static String documentation()
  {
    return "Generates Code3 Style VB5/6 ADO Code";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output, outLog);
    }
  }
  static void generate(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + ".bas");
      OutputStream outFile = new FileOutputStream(output+table.name + ".bas");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        generateStructs(table, outData);
        generateCode(table, outData);
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
  public static void generateStructs(Table table, PrintWriter outData)
  {
    outData.println("ATTRIBUTE VB_NAME=\""+table.name+"\"");
    outData.println("Option explicit");
    outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
    for (int i=0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      outData.println("'"+s);
    }
    outData.println();
    outData.println("Public Type D"+table.name);
    for (int i=0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("  "+varType(field));
      if (field.comments.size() > 0)
      {
        for (int c=0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  '"+s);
        }
      }
      if (field.isNull)
      {
        outData.println("  "+field.useName()+"IsNull As Boolean");
      }
    }
    outData.println("End Type");
    outData.println();
    for (int i=0; i< table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd  || proc.hasNoData())
        continue;
      if (proc.outputs.size() > 0)
      {
        for (int j=0; j<proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println("'"+comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput() || proc.inputs.size() == 0)
          typeChar = "O";
        outData.println("Public Type "+typeChar+table.useName()+proc.upperFirst());
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  '"+s);
          }
          outData.println("  "+varType(field));
          if (field.isNull)
            outData.println("  "+field.useName()+"IsNull As Boolean");
        }
        outData.println("End Type");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        outData.println("Public Type I"+table.useName()+proc.upperFirst());
        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  '"+s);
          }
          outData.println("  "+varType(field));
          if (field.isNull)
            outData.println("  "+field.useName()+"IsNull As Boolean");
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData.println("  "+s+" As String");
        }
        outData.println("End Type");
        outData.println();
      }
    }
  }
  static void generateCode(Table table, PrintWriter outData)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateCode(table, proc, outData);
    }
  }
  static int questionsSeen;
private static ObjectInputStream in;
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
  static String dropString(String sub, String command)
  {
    int p = command.indexOf(sub);
    while (p != -1)
    {
      String part1 = command.substring(0, p);
      String part2 = command.substring(p + sub.length());
      command = part1 + part2;
      p = command.indexOf("\" & \"");
    }
    return command;
  }
  static void generateCode(Table table, Proc proc, PrintWriter outData)
  {
    if (proc.comments.size() > 0)
    {
      for (int i=0; i<proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("  '"+comment);
      }
    }
    String inDataStruct;
    String outDataStruct = "D";
    String inTypeChar = "D";
    String outTypeChar = "D";
    if (proc.isStd)
    {
      inDataStruct = "D"+table.useName();
      outDataStruct = "D"+table.useName();
    }
    else
    {
      if (proc.outputs.size() > 0
      && (proc.hasDiscreteInput() || proc.inputs.size() == 0))
        outTypeChar = "O";
      if (proc.hasDiscreteInput())
        inTypeChar = "I";
      outDataStruct = outTypeChar+table.useName()+proc.upperFirst();
      inDataStruct = inTypeChar+table.useName()+proc.upperFirst();
    }
    String qryMaybe = "";
    if (proc.outputs.size() > 0)
      qryMaybe = ", Qry As ADODB.RecordSet";
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      outData.println("Public Sub "+table.useName()+proc.upperFirst()+"Exec(Conn As ADODB.Connection"+qryMaybe+", InRec As "+inDataStruct+")");
    else
      outData.println("Public Sub "+table.useName()+proc.upperFirst()+"Exec(Conn As ADODB.Connection"+qryMaybe+")");
    outData.println("  Dim Cmd As New ADODB.Command");
    outData.println("  Cmd.ActiveConnection = Conn");
    String command = "\"";
    questionsSeen = 0;
    for (int j=0; j < proc.lines.size(); j++)
    {
      Line l = (Line) proc.lines.elementAt(j);
      if (l.isVar)
      {
        command = command + "\" & InRec." + l.line + " & \"";
      }
      else
      {
        command = command + question(proc, l.line);
      }
    }
    command = command + "\"";
    outData.print("  Cmd.CommandText = ");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      int p = command.indexOf(":"+field.name);
      if (p != -1)
      {
        String part1 = command.substring(0, p) + "\" & ";
        String part2 = wrapNull(field, adoCreateParameter(field));
        String part3 = " & \"" + command.substring(p+field.name.length()+1);
        if (part1.length() > 500)
        {
          outData.println(part1+"_");
          outData.print("    ");
          part1 = "";
        }
        command = part1 + part2 + part3;
      }
      command = dropString("\" & \"", command);
      command = dropString("& \"\"", command);
      command = dropString("& \" \"", command);
    }
    outData.println(command);
    if (proc.outputs.size() > 0)
      outData.print("  Set Qry = ");
    else
      outData.print("  ");
    outData.println("Cmd.Execute");
    outData.println("End Sub");
    outData.println();
    if (proc.outputs.size() > 0)
    {
      String common = table.useName()+proc.upperFirst();
      boolean isDiscrete = true;
      if (proc.isStd || proc.hasDiscreteInput() == false)
        isDiscrete = false;
      outData.println("Public Function "+common+"Fetch(Qry As ADODB.RecordSet, OutRec As "+outDataStruct+") As Boolean");
      outData.println("  If Qry.EOF = True Then");
      outData.println("    "+common+"Fetch = False");
      outData.println("    Exit Function");
      outData.println("  End If");
      for (int j=0; j<proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        if (field.isNull)
        {
          outData.println("  OutRec."+field.useName()+"IsNull = IsNull(Qry!"+field.name+")");
          outData.println("  If Not OutRec."+field.useName()+"IsNull Then OutRec."+field.useName()+" = Qry!"+field.name
                          +" Else OutRec."+field.useName()+" = "+getNullInit(field));
        }
        else
          outData.println("  OutRec."+field.useName()+" = Qry!"+field.name);
      }
      if (!proc.isSingle)
        outData.println("  Qry.MoveNext");
      outData.println("  "+common+"Fetch = True");
      outData.println("End Function");
      outData.println();
      if (proc.isSingle)
      {
        if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
        {
          if (isDiscrete == false)
          {
            outData.println("Public Function "+common+"ReadOne(Conn As ADODB.Connection, IORec As "+inDataStruct+") As Boolean");
            outData.println("  Dim Qry As ADODB.RecordSet");
            outData.println("  "+common+"Exec Conn, Qry, IORec");
            outData.println("  "+common+"ReadOne = "+common+"Fetch(Qry, IORec)");
            outData.println("End Function");
            outData.println();
          }
          else
          {
            outData.println("Public Function "+common+"ReadOne(Conn As ADODB.Connection, InRec As "+inDataStruct+", OutRec As "+outDataStruct+") As Boolean");
            outData.println("  Dim Qry As ADODB.RecordSet");
            outData.println("  "+common+"Exec Conn, Qry, InRec");
            outData.println("  "+common+"ReadOne = "+common+"Fetch(Qry, OutRec)");
            outData.println("End Function");
            outData.println();
          }
        }
        else
        {
          outData.println("Public Function "+common+"ReadOne(Conn As ADODB.Connection, OutRec As "+outDataStruct+") As Boolean");
          outData.println("  Dim Qry As ADODB.RecordSet");
          outData.println("  "+common+"Exec Conn, Qry");
          outData.println("  "+common+"ReadOne = "+common+"Fetch(Qry, OutRec)");
          outData.println("End Function");
          outData.println();
        }
      }
      else
      {
        if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
        {
          outData.println("Public Sub "+common+"Load(Conn As ADODB.Connection, InRec As "+inDataStruct
                                       +", OutArray() As "+outDataStruct+")");
          outData.println("  Dim Qry As ADODB.RecordSet");
          outData.println("  Dim i   As Long");
          outData.println("  Dim OutRec As "+outDataStruct);
          outData.println("  Redim OutArray(0 To 64)");
          outData.println("  "+common+"Exec Conn, Qry, InRec");
          outData.println("  i = 0");
          outData.println("  Do");
          outData.println("    If Not "+common+"Fetch(Qry, OutRec) Then Exit Do");
          outData.println("    i = i + 1");
          outData.println("    If i > Ubound(OutArray) Then Redim Preserve OutArray(0 to i * 2)");
          outData.println("    OutArray(i) = OutRec");
          outData.println("  Loop");
          outData.println("  Redim Preserve OutArray(0 to i)");
          outData.println("End Sub");
          outData.println();
        }
        else
        {
          outData.println("Public Sub "+common+"Load(Conn As ADODB.Connection, OutArray() As "+outDataStruct+")");
          outData.println("  Dim Qry As ADODB.RecordSet");
          outData.println("  Dim i   As Long");
          outData.println("  Dim OutRec As "+outDataStruct);
          outData.println("  Redim OutArray(0 To 64)");
          outData.println("  "+common+"Exec Conn, Qry");
          outData.println("  i = 0");
          outData.println("  Do");
          outData.println("    If Not "+common+"Fetch(Qry, OutRec) Then Exit Do");
          outData.println("    i = i + 1");
          outData.println("    If i > Ubound(OutArray) Then Redim Preserve OutArray(0 to i * 2)");
          outData.println("    OutArray(i) = OutRec");
          outData.println("  Loop");
          outData.println("  Redim Preserve OutArray(0 to i)");
          outData.println("End Sub");
          outData.println();
        }
      }
    }
  }
  static boolean notString(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.BLOB:
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return true;
    }
    return false;
  }
  static String varType(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return field.useName() + " As Integer";
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " As Long";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return field.useName() + " As String";
    case Field.TLOB:
    case Field.BLOB:
      return field.useName() + " As String";
    case Field.DATE:
      return field.useName() + " As Date";
    case Field.TIME:
      return field.useName() + " As Date";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return field.useName() + " As Date";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " As Double";
    }
    return "As unsupported";
  }
  static String adoCreateParameter(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.LONG:
    case Field.IDENTITY:
      return "Format$(InRec." + field.useName() + ")";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return "\"'\" & InRec." +field.useName() + " & \"'\"";
    case Field.DATE:
      return "\"'\" & Format$(InRec." +field.useName() + ", \"yyyymmdd\") & \"'\"";
    case Field.TIME:
      return "\"'\" & Format$(InRec." +field.useName() + ", \"hhnnss\") & \"'\"";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "\"'\" & Format$(InRec." +field.useName() + ", \"yyyymmddhhnnss\") & \"'\"";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "Format$(InRec." + field.useName() + ")";
    }
    return "InRec." + field.useName();
  }
  static String getNullInit(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "0";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "0.0";
    }
    return "\"\"";
  }
  static String wrapNull(Field field, String string)
  {
    if (field.isNull)
      return "iif(InRec."+field.useName()+"IsNull, \"NULL\", "+string+")";
    return string;
  }
}

