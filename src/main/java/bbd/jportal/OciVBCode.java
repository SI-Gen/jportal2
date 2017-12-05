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

public class OciVBCode extends Generator
{
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i <args.length; i++)
      {
        outLog.println(args[i]+": Generate VB Code for OCI");
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
    return "Generate VB Code for OCI";
  }
  public static String documentation()
  {
    return "Generate VB Code for OCI";
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
  public static int generatePadding(Field field, PrintWriter outData, int pos, int fillerNo)
  {
    int n = pos % padSize(field);
    if (n > 0)
    {
      n = padSize(field)-n;
      outData.println("  Filler"+fillerNo+" As String * "+n);
    }
    return n;
  }
  public static int generatePadding(PrintWriter outData, int pos, int fillerNo)
  {
    int n = pos % 2;
    if (n > 0)
    {
      n = 2-n;
      outData.println("  Filler"+fillerNo+" As String * "+n);
    }
    return n;
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
    int pos = 0;
    int padding;
    int filler=0;
    outData.println("Public Type D"+table.name);
    for (int i=0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      padding = generatePadding(field, outData, pos, filler++);
      pos += padding;
      field.definePos = field.bindPos = pos;
      pos += getLength(field);
      outData.println("  "+varType(field));
      if (field.comments.size() > 0)
      {
        for (int c=0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  '"+s);
        }
      }
      if (field.isNull && notString(field))
      {
        padding = generatePadding(outData, pos, filler++);
        pos += padding;
        outData.println("  "+field.useName()+"IsNull As Integer");
        pos += 2;
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
        pos = 0;
        outData.println("Public Type "+typeChar+table.useName()+proc.upperFirst());
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  '"+s);
          }
          padding = generatePadding(field, outData, pos, filler++);
          pos += padding;
          field.definePos = pos;
          pos += getLength(field);
          outData.println("  "+varType(field));
          if (field.isNull && notString(field))
          {
            padding = generatePadding(outData, pos, filler++);
            pos += padding;
            outData.println("  "+field.useName()+"IsNull As Integer");
            pos += 2;
          }
        }
        outData.println("End Type");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        pos = 0;
        outData.println("Public Type I"+table.useName()+proc.upperFirst());
        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  '"+s);
          }
          padding = generatePadding(field, outData, pos, filler++);
          pos += padding;
          field.bindPos = pos;
          pos += getLength(field);
          outData.println("  "+varType(field));
          if (field.isNull && notString(field))
          {
            padding = generatePadding(outData, pos, filler++);
            pos += padding;
            outData.println("  "+field.useName()+"IsNull As Integer");
            pos += 2;
          }
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          Integer n = (Integer) proc.dynamicSizes.elementAt(j);
          outData.println("  "+s+" As String * "+n.intValue());
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
  static void sizePrint(String command, String endBit, PrintWriter outData)
  {
    String frontBit = "    ";
    for (;;)
    {
      if (command.length() < 1000)
      {
        outData.println(frontBit + command + endBit);
        return;
      }
      int n;
      for (n = 1000; command.charAt(n) != ','; n--)
        ;
      outData.println("    " + command.substring(0, n) + "\" & _");
      frontBit = "    \"";
      command = command.substring(n);
    }
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
    String bindList = "";
    String defineList = "";
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
    int rowSize = 0;
    String qryMaybe = "";
    if (proc.outputs.size() > 0)
      qryMaybe = ", Qry As Long";
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      outData.println("Public Sub "+table.useName()+proc.upperFirst()+"Exec(ByVal Conn As Long"+qryMaybe+", InRec As "+inDataStruct+")");
    else
      outData.println("Public Sub "+table.useName()+proc.upperFirst()+"Exec(ByVal Conn As Long"+qryMaybe+")");
    if (proc.outputs.size() > 0)
    {
      for (int j=0; j<proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        defineList = defineList+":"+field.definePos+","+ociOutputType(field);
        if (rowSize < field.definePos+getLength(field))
          rowSize = field.definePos+getLength(field);
        if (proc.hasInput(field.name))
          bindList = bindList+":"+field.name+","+field.definePos+","+ociInputType(table, proc, field);
      }
    }
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if (proc.hasOutput(field.name))
        continue;
      bindList = bindList+":"+field.name+","+field.bindPos+","+ociInputType(table, proc, field);
    }
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
    int noRows = 100;
    if (rowSize > 0)
      noRows = 24576 / (rowSize + 1);
    if (proc.noRows > 0)
      noRows = proc.noRows;
    if (proc.outputs.size() > 0)
    {
      if (proc.inputs.size() > 0)
      {
        if (proc.isSingle)
        {
          outData.println("  JP_CheckCOK JPVBSingleQueryWithInput(Qry, Conn, " // JPVB
                        + proc.inputs.size() + ", "
                        + proc.outputs.size() + ", "
                        + rowSize + ", _");
          sizePrint(command, ", _", outData);
          outData.println("    \"" + defineList + "\", _");
          outData.println("    \"" + bindList + "\", _");
          outData.println("    InRec), Conn");
        }
        else
        {
          outData.println("  JP_CheckCOK JPVBMultiQueryWithInput(Qry, Conn, "
                        + proc.inputs.size() + ", "
                        + proc.outputs.size() + ", "
                        + noRows + ", "
                        + rowSize + ", _");
          sizePrint(command, ", _", outData);
          outData.println("    \"" + defineList + "\", _");
          outData.println("    \"" + bindList + "\", _");
          outData.println("    InRec), Conn");
        }
      }
      else
      {
        if (proc.isSingle)
        {
          outData.println("  JP_CheckCOK JPVBSingleQuery(Qry, Conn, "
                        + proc.outputs.size() + ", "
                        + rowSize + ", _");
          sizePrint(command, ", _", outData);
          outData.println("    \"" + defineList + "\"), Conn");
        }
        else
        {
          outData.println("  JP_CheckCOK JPVBMultiQuery(Qry, Conn, "
                        + proc.outputs.size() + ", "
                        + noRows + ", "
                        + rowSize + ", _");
          sizePrint(command, ", _", outData);
          outData.println("    \"" + defineList + "\"), Conn");
        }
      }
    }
    else
    {
      if (proc.inputs.size() > 0)
      {
        outData.println("  JP_CheckCOK JPVBExecWithInput(Conn, "
                      + proc.inputs.size() + ", _");
        sizePrint(command, ", _", outData);
        outData.println("    \"" + bindList + "\", _");
        outData.println("    InRec), Conn");
      }
      else
      {
        outData.println("  JP_CheckCOK JPVBExec(Conn, _");
        sizePrint(command, "), Conn", outData);
      }
    }
    outData.println("End Sub");
    outData.println();
    if (proc.outputs.size() > 0)
    {
      String common = table.useName()+proc.upperFirst();
      boolean isDiscrete = true;
      if (proc.isStd || proc.hasDiscreteInput() == false)
        isDiscrete = false;
      outData.println("Public Function "+common+"Fetch(ByVal Qry As Long, OutRec As "+outDataStruct+") As Boolean");
      outData.println("  Dim Done As Long");
      outData.println("  JP_CheckQOK JPVBFetch(Qry, OutRec, Done), Qry");
      outData.println("  "+common+"Fetch = (Done = 1)");
      outData.println("End Function");
      outData.println();
      if (proc.isSingle)
      {
        if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
        {
          if (isDiscrete == false)
          {
            outData.println("Public Function "+common+"ReadOne(ByVal Conn As Long, IORec As "+inDataStruct+") As Boolean");
            outData.println("  Dim Qry As Long");
            outData.println("  "+common+"Exec Conn, Qry, IORec");
            outData.println("  "+common+"ReadOne = "+common+"Fetch(Qry, IORec)");
            outData.println("  JP_Close Qry");
            outData.println("End Function");
            outData.println();
          }
          else
          {
            outData.println("Public Function "+common+"ReadOne(ByVal Conn As Long, InRec As "+inDataStruct+", OutRec As "+outDataStruct+") As Boolean");
            outData.println("  Dim Qry As Long");
            outData.println("  "+common+"Exec Conn, Qry, InRec");
            outData.println("  "+common+"ReadOne = "+common+"Fetch(Qry, OutRec)");
            outData.println("  JP_Close Qry");
            outData.println("End Function");
            outData.println();
          }
        }
        else
        {
          outData.println("Public Function "+common+"ReadOne(ByVal Conn As Long, OutRec As "+outDataStruct+") As Boolean");
          outData.println("  Dim Qry As Long");
          outData.println("  "+common+"Exec Conn, Qry");
          outData.println("  "+common+"ReadOne = "+common+"Fetch(Qry, OutRec)");
          outData.println("  JP_Close Qry");
          outData.println("End Function");
          outData.println();
        }
      }
      else
      {
        if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
        {
          outData.println("Public Sub "+common+"Load(ByVal Conn As Long, InRec As "+inDataStruct
                                       +", OutArray() As "+outDataStruct+")");
          outData.println("  Dim Qry As Long");
          outData.println("  Dim i   As Long");
          outData.println("  Dim OutRec As "+outDataStruct);
          outData.println("  Redim OutArray(0 To 64)");
          outData.println("  "+common+"Exec Conn, Qry, InRec");
          outData.println("  i = 0");
          outData.println("  Do");
          outData.println("    If Not "+common+"Fetch(Qry, OutRec) Then Exit Do");
          outData.println("    i = i + 1");
          outData.println("    If i > Ubound(OutArray) Then Redim Preserve OutArray(0 to i + 63)");
          outData.println("    OutArray(i) = OutRec");
          outData.println("  Loop");
          outData.println("  Redim Preserve OutArray(0 to i)");
          outData.println("  JP_Close Qry");
          outData.println("End Sub");
          outData.println();
        }
        else
        {
          outData.println("Public Sub "+common+"Load(ByVal Conn As Long, OutArray() As "+outDataStruct+")");
          outData.println("  Dim Qry As Long");
          outData.println("  Dim i   As Long");
          outData.println("  Dim OutRec As "+outDataStruct);
          outData.println("  Redim OutArray(0 To 64)");
          outData.println("  "+common+"Exec Conn, Qry");
          outData.println("  i = 0");
          outData.println("  Do");
          outData.println("    If Not "+common+"Fetch(Qry, OutRec) Then Exit Do");
          outData.println("    i = i + 1");
          outData.println("    If i > Ubound(OutArray) Then Redim Preserve OutArray(0 to i + 64)");
          outData.println("    OutArray(i) = OutRec");
          outData.println("  Loop");
          outData.println("  Redim Preserve OutArray(0 to i)");
          outData.println("  JP_Close Qry");
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
      return field.useName() + " As String * " + (field.length+1);
    case Field.TLOB:
    case Field.BLOB:
      return field.useName() + " As String * " + (field.length+1);
    case Field.DATE:
      return field.useName() + " As String * 9";
    case Field.TIME:
      return field.useName() + " As String * 7";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return field.useName() + " As String * 15";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " As Double";
    }
    return "As unsupported";
  }
  static int padSize(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return 1;
    case Field.DATE:
      return 1;
    case Field.TIME:
      return 1;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 1;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return 8;
    }
    return 4;
  }
  static int getLength(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return field.length+1;
    case Field.DATE:
      return 9;
    case Field.TIME:
      return 7;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 15;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return 8;
    }
    return 4;
  }
  static String initVBVar(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
      return field.useName() + " = False";
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " = 0";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return field.useName() + " = \"\"";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
      return field.useName() + " = Now";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " = 0.0";
    }
    return "As unsupported";
  }
  static String ociInputType(Table table, Proc proc, Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return field.isNull ? "S" : "s";
    case Field.INT:
      return field.isNull ? "I" : "i";
    case Field.SEQUENCE:
      if (proc.isInsert)
        return "n="+table.name+"Seq";
    case Field.LONG:
    case Field.IDENTITY:
      return field.isNull ? "L" : "l";
    case Field.CHAR:
      return "c,"+(field.length+1);
    case Field.ANSICHAR:
      return "a,"+(field.length+1);
    case Field.USERSTAMP:
      return "u";
    case Field.TLOB:
    case Field.BLOB:
      return "c,"+(field.length+1);
    case Field.DATE:
      return field.isNull ? "0" : "1";
    case Field.TIME:
      return field.isNull ? "2" : "3";
    case Field.DATETIME:
      return field.isNull ? "4" : "5";
    case Field.TIMESTAMP:
      return "6";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.isNull ? "D" : "d";
    }
    return "?";
  }
  static String ociOutputType(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return field.isNull ? "S" : "s";
    case Field.INT:
      return field.isNull ? "I" : "i";
    case Field.SEQUENCE:
    case Field.LONG:
    case Field.IDENTITY:
      return field.isNull ? "L" : "l";
    case Field.CHAR:
      return "c,"+(field.length+1);
    case Field.ANSICHAR:
      return "a,"+(field.length+1);
    case Field.USERSTAMP:
      return "u";
    case Field.TLOB:
    case Field.BLOB:
      return "c,"+(field.length+1);
    case Field.DATE:
      return field.isNull ? "0" : "1";
    case Field.TIME:
      return field.isNull ? "2" : "3";
    case Field.DATETIME:
      return field.isNull ? "4" : "5";
    case Field.TIMESTAMP:
      return "6";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.isNull ? "D" : "d";
    }
    return "?";
  }
}


