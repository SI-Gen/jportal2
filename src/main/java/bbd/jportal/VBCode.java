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

public class VBCode extends Generator
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
        outLog.println(args[i]+": Generate VB DAO Code");
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
    return "Generate VB DAO Code";
  }
  public static String documentation()
  {
    return "Generate VB DAO Code";
  }
  protected static Vector<Flag> flagsVector;
  static boolean ioRoutines;
  static boolean handleErrors;
  private static void flagDefaults()
  {
    ioRoutines = false;
    handleErrors = true;
  }
  public static Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<Flag>();
      flagDefaults();
      flagsVector.addElement(new Flag("io routines", new Boolean (ioRoutines), "Generate IO Routines"));
      flagsVector.addElement(new Flag("handle errors", new Boolean (handleErrors), "Handle Errors"));
    }
    return flagsVector;
  }
  static void setFlags(Table table, PrintWriter outLog)
  {
    if (flagsVector != null)
    {
      ioRoutines = toBoolean (((Flag)flagsVector.elementAt(0)).value);
      handleErrors = toBoolean (((Flag)flagsVector.elementAt(1)).value);
    }
    else
      flagDefaults();
    for (int i=0; i < table.options.size(); i++)
    {
      String option = (String) table.options.elementAt(i);
      if (option.equalsIgnoreCase("io routines"))
        ioRoutines = true;
      if (option.equalsIgnoreCase("handle errors"))
        handleErrors = true;
    }
    if (ioRoutines)
      outLog.println(" (io routines)");
    if (handleErrors)
      outLog.println(" (handle errors)");
  }
  /**
  * Generates the procedure classes for each table present.
  */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      setFlags(table, outLog);
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
      outLog.println("Code: "+output+table.useName() + ".cls");
      OutputStream outFile = new FileOutputStream(output+table.name + ".cls");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        generateStd(table, outData);
        generateOther(table, output, outLog);
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
  public static void generateStd(Table table, PrintWriter outData)
  {
    outData.println("VERSION 1.0 CLASS");
    outData.println("BEGIN");
    outData.println("  MultiUse = -1");
    outData.println("END");
    outData.println("ATTRIBUTE VB_NAME=\"t"+table.name+"\"");
    outData.println("Option explicit");
    outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
    for (int i=0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      outData.println("'"+s);
    }
    outData.println();
    for (int i=0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("Public "+varType(field));
      if (field.comments.size() > 0)
      {
        for (int c=0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("'"+s);
        }
      }
      if (field.isNull && notString(field))
         outData.println("Public "+field.useName()+"IsNull as Boolean");
    }
    outData.println();
    outData.println("Private Sub Class_Initialize()");
    for (int i=0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("  "+initVBVar(field));
      if (field.isNull && notString(field))
         outData.println("  "+field.useName()+"IsNull = false");
    }
    outData.println("End Sub");
    outData.println();
    if (ioRoutines)
    {
      outData.println("Public Sub Class_PutFile(OutFile as Integer)");
      for (int i=0; i < table.fields.size(); i++)
      {
        Field field = (Field) table.fields.elementAt(i);
        outData.println("  Put OutFile, , "+field.useName());
        if (field.isNull && notString(field))
          outData.println("  Put OutFile, , "+field.useName()+"IsNull");
      }
      outData.println("End Sub");
      outData.println();
      outData.println("Public Sub Class_GetFile(InFile as Integer)");
      for (int i=0; i < table.fields.size(); i++)
      {
        Field field = (Field) table.fields.elementAt(i);
        outData.println("  Get InFile, , "+field.useName());
        if (field.isNull && notString(field))
          outData.println("  Get InFile, , "+field.useName()+"IsNull");
      }
      outData.println("End Sub");
      outData.println();
    }
    for (int i=0; i< table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStd || proc.hasNoData())
        emitCode(proc, outData, table.name);
    }
  }
  /**
  * Build of all required standard procedures
  */
  static void generateOther(Table table, String output, PrintWriter outLog)
  {
    for (int i=0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      try
      {
        outLog.println("Code: "+output+table.name+proc.upperFirst()+".cls");
        OutputStream outFile = new FileOutputStream(output+table.name+proc.upperFirst()+".cls");
        try
        {
          PrintWriter outData = new PrintWriter(outFile);
          outData.println("VERSION 1.0 CLASS");
          outData.println("BEGIN");
          outData.println("  MultiUse = -1");
          outData.println("END");
          outData.println("ATTRIBUTE VB_NAME=\"t"+table.name+proc.upperFirst()+"\"");
          outData.println("Option explicit");
          outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
          for (int j=0; j<proc.comments.size(); j++)
          {
            String comment = (String) proc.comments.elementAt(j);
            outData.println("'"+comment);
          }
          outData.println();
          for (int j=0; j<proc.inputs.size(); j++)
          {
            Field field = (Field) proc.inputs.elementAt(j);
            if (!proc.hasOutput(field.name))
              outData.println("' input");
            else
              outData.println("' input-output");
            outData.println("Public "+varType(field));
            for (int c=0; c < field.comments.size(); c++)
            {
              String s = (String) field.comments.elementAt(c);
              outData.println("'"+s);
            }
            if (field.isNull && notString(field))
              outData.println("Public "+field.useName()+"IsNull as Boolean");
          }
          for (int j=0; j<proc.outputs.size(); j++)
          {
            Field field = (Field) proc.outputs.elementAt(j);
            if (!proc.hasInput(field.name))
            {
              outData.println("' output");
              outData.println("Public "+varType(field));
              for (int c=0; c < field.comments.size(); c++)
              {
                String s = (String) field.comments.elementAt(c);
                outData.println("'"+s);
              }
            if (field.isNull && notString(field))
              outData.println("Public "+field.useName()+"IsNull as Boolean");
            }
          }
          for (int j=0; j<proc.dynamics.size(); j++)
          {
            String s = (String) proc.dynamics.elementAt(j);
            outData.println("' dynamic");
            outData.println("Public "+s+" As String");
          }
          outData.println();
          outData.println("Private Sub Class_Initialize()");
          for (int j=0; j<proc.inputs.size(); j++)
          {
            Field field = (Field) proc.inputs.elementAt(j);
            outData.println("  "+initVBVar(field));
            if (field.isNull && notString(field))
               outData.println("  "+field.useName()+"IsNull = false");
          }
          for (int j=0; j<proc.outputs.size(); j++)
          {
            Field field = (Field) proc.outputs.elementAt(j);
            if (proc.hasInput(field.name))
              continue;
            outData.println("  "+initVBVar(field));
            if (field.isNull && notString(field))
               outData.println("  "+field.useName()+"IsNull = false");
          }
          for (int j=0; j<proc.dynamics.size(); j++)
          {
            String s = (String) proc.dynamics.elementAt(j);
            outData.println("  "+s+" = \"\"");
          }
          outData.println("End Sub");
          outData.println();
          if (ioRoutines)
          {
            outData.println("Public Sub Class_PutFile(OutFile as Integer)");
            for (int j=0; j<proc.inputs.size(); j++)
            {
              Field field = (Field) proc.inputs.elementAt(j);
              outData.println("  Put OutFile, , "+field.useName());
              if (field.isNull && notString(field))
                outData.println("  Put OutFile, , "+field.useName()+"IsNull");
            }
            for (int j=0; j<proc.outputs.size(); j++)
            {
              Field field = (Field) proc.outputs.elementAt(j);
              if (proc.hasInput(field.name))
                continue;
              outData.println("  Put OutFile, , "+field.useName());
              if (field.isNull && notString(field))
                outData.println("  Put OutFile, , "+field.useName()+"IsNull");
            }
            for (int j=0; j<proc.dynamics.size(); j++)
            {
              String s = (String) proc.dynamics.elementAt(j);
              outData.println("  Put OutFile, , "+s);
            }
            outData.println("End Sub");
            outData.println();
            outData.println("Public Sub Class_GetFile(InFile as Integer)");
            for (int j=0; j<proc.inputs.size(); j++)
            {
              Field field = (Field) proc.inputs.elementAt(j);
              outData.println("  Get InFile, , "+field.useName());
              if (field.isNull && notString(field))
                outData.println("  Get InFile, , "+field.useName()+"IsNull");
            }
            for (int j=0; j<proc.outputs.size(); j++)
            {
              Field field = (Field) proc.outputs.elementAt(j);
              if (proc.hasInput(field.name))
                continue;
              outData.println("  Get InFile, , "+field.useName());
              if (field.isNull && notString(field))
                outData.println("  Get InFile, , "+field.useName()+"IsNull");
            }
            for (int j=0; j<proc.dynamics.size(); j++)
            {
              String s = (String) proc.dynamics.elementAt(j);
              outData.println("  Get InFile, , "+s);
            }
            outData.println("End Sub");
            outData.println();
          }
          emitCode(proc, outData, table.name);
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
  }
  /**
  * Emits functions for processing the database activity
  */
  static void emitCode(Proc proc, PrintWriter outData, String tableName)
  {
    if (proc.outputs.size() == 0)
    {
      if (handleErrors)
        outData.println("Public Function "+proc.upperFirst()+"(Connect as TConnect) as Boolean");
      else
        outData.println("Public Sub "+proc.upperFirst()+"(Connect as TConnect)");
    }
    else if (proc.isSingle)
      outData.println("Public Function "+proc.upperFirst()+"(Connect as TConnect) as Boolean");
    else
    {
      outData.println("Public Function "+proc.upperFirst()+"(Connect as TConnect) as TCursor");
    }
    if (proc.comments.size() > 0)
    {
      for (int i=0; i < proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("'"+comment);
      }
    }
    if (proc.outputs.size() == 0)
    {
      if (handleErrors)
      {
        outData.println("' Returns true if worked else false if failed.");
        outData.println("  "+proc.upperFirst()+" = True");
      }
      else
        outData.println("' Returns no output.");
    }
    else if (proc.isSingle)
    {
      outData.println("' Returns at most one record.");
      outData.println("' Returns true if a record is found");
      outData.println("  Dim RS as RecordSet");
      outData.println("  "+proc.upperFirst()+" = False");
    }
    else
    {
      outData.println("' Returns any number of records.");
      outData.println("' Returns result set of records found");
      outData.println("  Dim Cursor as new TCursor");
    }
    outData.println("  Connect.RoutineName = \""+proc.upperFirst()+"\"");
    outData.println("  Dim QD as QueryDef");
    if (handleErrors)
      outData.println("  On Error goto "+proc.upperFirst()+"Error");
    for (int i=0; i<proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      if (proc.isInsert)
      {
        if (field.isSequence)
          outData.println("  "+field.useName()+" = getSequence(\""+tableName+"\") ' User supplied Function for Sequences");
      }
      if (field.type == Field.TIMESTAMP)
        outData.println("  "+field.useName()+" = getTimeStamp ' User supplied Function for Time Stamp");
      if (field.type == Field.USERSTAMP)
        outData.println("  "+field.useName()+" = getUserStamp ' User supplied Function for User Stamp");
    }
    outData.print("  Set QD = Connect.DB.CreateQueryDef(\"\", ");
    String vbline = "";
    for (int i=0; i < proc.lines.size(); i++)
    {
      String x = "";
      if (i+1 < proc.lines.size())
        x = " & ";
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        vbline = vbline +l.line+x;
      else
        vbline = vbline + "\""+l.line+"\""+x;
    }
    int p;
    while ((p = vbline.indexOf("\" & \"")) > -1)
      vbline = vbline.substring(0, p) + vbline.substring(p+5);
    outData.println(vbline + ")");
    for (int i=0, n=0; i<proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      if (field.type == Field.IDENTITY && proc.isInsert)
        continue;
      if (field.isNull && notString(field))
      {
        outData.println("  QD!Parameter"+ (++n) + " = IIf("+field.useName()+"IsNull, Null, " + field.useName()+")");
      }
      else
        outData.println("  QD!Parameter"+ (++n) + " = " + field.useName());
    }
    if (proc.outputs.size() == 0)
    {
      outData.println("  QD.execute");
      outData.println("  QD.Close");
    }
    else if (proc.isSingle)
    {
      outData.println("  Set RS = QD.OpenRecordSet(dbOpenSnapShot, dbForwardOnly and dbReadOnly)");
      outData.println("  If Not RS.eof Then");
      for (int i=0; i < proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        if (field.isNull && notString(field))
        {
          outData.println("    "+field.useName()+"IsNull = isNull(RS!"+field.name+")");
          outData.println("    If Not "+field.useName()+"IsNull Then "+field.useName()+" = RS!"+field.name);
        }
        else
          outData.println("    "+field.useName()+" =  RS!"+field.name);
      }
      outData.println("    "+proc.upperFirst()+" = True");
      outData.println("  End If");
      outData.println("  RS.Close");
      outData.println("  QD.Close");
    }
    else
    {
      outData.println("  Set Cursor.Connect = Connect");
      outData.println("  Set Cursor.QD = QD");
      outData.println("  Set Cursor.RS = Cursor.QD.openRecordSet(dbOpenSnapShot, dbForwardOnly and dbReadOnly)");
      outData.println("  Set "+proc.upperFirst()+" = Cursor");
    }
    if (handleErrors)
    {
      outData.println(proc.upperFirst()+"Exit:");
      outData.println("  Exit Function");
      outData.println(proc.upperFirst()+"Error:");
      outData.println("  Connect.ErrorCode = Err");
      outData.println("  Connect.ErrorDescr = Error$");
      if (proc.outputs.size() == 0)
        outData.println("  "+proc.upperFirst()+" = False");
      outData.println("  Resume "+proc.upperFirst()+"Exit");
    }
    if (proc.outputs.size() == 0 && !handleErrors)
      outData.println("End Sub");
    else
      outData.println("End Function");
    outData.println();
    if (proc.outputs.size() > 0 && !proc.isSingle)
    {
      outData.println("Public Function next"+proc.upperFirst()+"(Cursor as TCursor) as Boolean");
      outData.println("' Returns true if a record is found");
      outData.println("  Cursor.Connect.RoutineName = \"next"+proc.upperFirst()+"\"");
      if (handleErrors)
      {
        outData.println("  Cursor.Connect.ErrorCode = 0");
        outData.println("  On Error Goto next"+proc.upperFirst()+"Error");
      }
      outData.println("  next"+proc.upperFirst()+" = false");
      outData.println("  if not Cursor.RS.eof then");
      for (int i=0; i<proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        if (field.isNull && notString(field))
        {
          outData.println("    "+field.useName()+"IsNull = isNull(Cursor.RS!"+field.name+")");
          outData.println("    If Not "+field.useName()+"IsNull Then "+field.useName()+" = Cursor.RS!"+field.name);
        }
        else
          outData.println("    "+field.useName()+" =  Cursor.RS!"+field.name);
      }
      outData.println("    next"+proc.upperFirst()+" = True");
      outData.println("    Cursor.RS.MoveNext");
      outData.println("  End If");
      if (handleErrors)
      {
        outData.println("next"+proc.upperFirst()+"Exit:");
        outData.println("  Exit Function");
        outData.println("next"+proc.upperFirst()+"Error:");
        outData.println("  Cursor.Connect.ErrorCode = Err");
        outData.println("  Cursor.Connect.ErrorDescr = Error$");
        outData.println("  Resume next"+proc.upperFirst()+"Exit");
      }
      outData.println("End Function");
      outData.println();
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
      return field.useName() + " as Boolean";
    case Field.BYTE:
      return field.useName() + " as Byte";
    case Field.SHORT:
      return field.useName() + " as Integer";
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " as Long";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return field.useName() + " as String";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
      return field.useName() + " as Date";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " as Double";
    }
    return "as unsupported";
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
    return "as unsupported";
  }
}


