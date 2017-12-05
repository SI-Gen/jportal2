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

/**
 * @author vince
 */
public class VBScriptCode extends Generator
{
  public static void main(String args[])
  {
    try 
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i <args.length; i++) 
      {
        outLog.println(args[i]+": Generate VBScript Code");
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
    return "Generates ADO (OleDB) VBScript Code";
  }
  public static String documentation()
  {
    return "Generates ADO (OleDB) VBScript Code";
  }
  protected static Vector<Flag> flagsVector;
  static boolean mSSqlStoredProcs;
  private static void flagDefaults()
  {
    mSSqlStoredProcs = false;
  }
  public static Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<Flag>();
      flagDefaults();
      flagsVector.addElement(new Flag("mssql storedprocs", new Boolean (mSSqlStoredProcs), "Generate MSSql Stored Procedures"));
    }
    return flagsVector;
  }
  /**
   * Sets generation flags.
   */
  static void setFlags(Database database, PrintWriter outLog)
  {
    if (flagsVector != null)
      mSSqlStoredProcs = toBoolean (((Flag)flagsVector.elementAt(0)).value);
    else
      flagDefaults();
    for (int i=0; i < database.flags.size(); i++)
    {
      String flag = (String) database.flags.elementAt(i);
      if (flag.equalsIgnoreCase("mssql storedprocs"))
        mSSqlStoredProcs = true;
    }
    if (mSSqlStoredProcs)
      outLog.println(" (mssql storedprocs)");
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    setFlags(database, outLog);
    for (int i=0; i<database.tables.size(); i++) 
    {
      Table table = (Table) database.tables.elementAt(i);
      if (table.hasStdProcs)
        generateStd(table, output, outLog);
      generateProcs(table, output, outLog);
    }
  }
  private static void generateStd(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + ".vbs");
      OutputStream outFile = new FileOutputStream(output+table.useName() + ".vbs");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        outData.println("<%"); 
        outData.println(); 
        outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
        outData.println("class C"+table.useName()+"Rec");
        generateRec(table.fields, outData);
        outData.println("end class");
        outData.println(); 
        outData.println("%>"); 
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
  static void generateRec(Vector<?> fields, PrintWriter outData)
  {
    for (int i=0; i < fields.size(); i++)
    {
      Field field = (Field) fields.elementAt(i);
      generateField(field, outData);
    }
    outData.println("  private sub Class_Initialize()");
    outData.println("    Clear");
    outData.println("  end sub");
    outData.println("  public sub Clear()");
    for (int i=0; i < fields.size(); i++)
    {
      Field field = (Field) fields.elementAt(i);
      outData.println("    "+initVBVar(field));
    }
    outData.println("  end sub");
  }
  static void generateField(Field field, PrintWriter outData)
  {
    String appComment = "";
    if (field.comments.size() > 1)
    {
      for (int c=0; c < field.comments.size(); c++)
      {
        String s = (String) field.comments.elementAt(c);
        outData.println("  '"+s);
      }
    }
    else if (field.comments.size() == 1)
      appComment = " '"+(String) field.comments.elementAt(0);
    outData.println("  public "+field.useName()+appComment);
  }
  static void generateProcs(Table table, String output, PrintWriter outLog)
  {
    for (int i=0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      try
      {
        outLog.println("Code: "+output+table.useName()+proc.upperFirst()+".vbs");
        OutputStream outFile = new FileOutputStream(output+table.useName()+proc.upperFirst()+".vbs");
        try
        {
          PrintWriter outData = new PrintWriter(outFile);
          try
          {
            String recName = "";
            if (proc.isStd)
            {
              recName = "C"+table.useName()+"Rec";
              outData.println("<%"); 
              outData.println("' You must have one and only one <!--#includes file=\""+table.useName() + ".vbs\"-->");
              outData.println("' in order to support this generation.");
              outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
            }
            else if (proc.hasNoData() == false)
            {
              recName = "C"+table.useName()+proc.upperFirst()+"Rec";
              outData.println("<%"); 
              outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
              outData.println("class "+recName);
              generateRec(proc, outData);
              outData.println("end class");
            }
            else
            {
              outData.println("<%"); 
              outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
            }
            outData.println();
            boolean hasMulti = false;
            if (proc.outputs.size() > 0
              && proc.isSingle == false)
              hasMulti = true;
            String optZero = hasMulti ? "(0)" : "";
            outData.println("class C"+table.useName()+proc.upperFirst());
            outData.println("  public rec");
            outData.println("  public cursor");
            outData.println("  public rs");
            outData.println();
            outData.println("  private sub Class_Initialize()");
            if (hasMulti == true)
            {
              outData.println("    rec = Array(0)");
              outData.println("    Clear"); 
            }
            outData.println("    set rec"+optZero+" = new "+recName);
            outData.println("  end sub");
            outData.println();
            outData.println("  private sub Class_Terminate()");
            outData.println("    set cursor = nothing");
            outData.println("  end sub");
            outData.println();
            if (hasMulti)
            {
              outData.println("  public sub Clear()");
              outData.println("    redim preserve rec(0)");
              outData.println("  end sub");
              outData.println();
              outData.println("  public sub ReSize(aSize)");
              outData.println("    redim preserve rec(aSize)");
              outData.println("  end sub");
              outData.println();
            }
            emitCode(proc, outData, table.useName(), recName);
            outData.println("end class");
            outData.println();
            outData.println("%>"); 
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
  }
  static void generateRec(Proc proc, PrintWriter outData)
  {
    for (int i=0; i < proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      generateField(field, outData);
    }
    for (int i=0; i < proc.outputs.size(); i++)
    {
      Field field = (Field) proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      generateField(field, outData);
    }
    for (int i=0; i<proc.dynamics.size(); i++)
    {
      String s = (String) proc.dynamics.elementAt(i);
      outData.println("  public "+s+" 'dynamic");
    }
  }
  static void emitCode(Proc proc, PrintWriter outData, String tableName, String recName)
  {
    String optUseIndex = "";
    if (proc.outputs.size() == 0)
    {
      outData.println("  public sub Execute(connect)");
    }
    else if (proc.isSingle)
      outData.println("  public function Execute(connect)");
    else
    {
      outData.println("  public sub Execute(connect)");
      optUseIndex = "(0)";
    }
    if (proc.comments.size() > 0)
    {
      for (int i=0; i < proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("    '"+comment);
      }
    }
    if (proc.outputs.size() == 0)
    {
      outData.println("    ' Returns no output.");
    }
    else if (proc.isSingle)
    {
      outData.println("    ' Returns at most one record.");
      outData.println("    ' Returns true if a record is found");
      outData.println("    Execute = false");
    }
    else
    {
      outData.println("    ' Returns any number of records.");
    }
    outData.println("    set cursor = new CCursor");
    outData.println("    connect.RoutineName = \""+proc.upperFirst()+"\"");
    for (int index=0; index < proc.inputs.size(); index++)
    {
      Field field = (Field) proc.inputs.elementAt(index);
      if (proc.isInsert)
      {
        if (field.isSequence)
          outData.println("    rec"+optUseIndex+ "" +field.useName()+" = getSequence(\""+tableName+"\") ' User supplied Function for Sequences");
      }
      if (field.type == Field.TIMESTAMP)
        outData.println("    rec"+optUseIndex+ "" +field.useName()+" = getTimeStamp ' User supplied Function for Time Stamp");
      if (field.type == Field.USERSTAMP)
        outData.println("    rec"+optUseIndex+ "" +field.useName()+" = getUserStamp ' User supplied Function for User Stamp");
    }
    outData.println("    cursor.SetConnect connect");
    outData.print  ("    cursor.CommandText ");
    String vbline = "";
    for (int i=0; i < proc.lines.size(); i++)
    {
      String x = "";
      if (i+1 < proc.lines.size())
        x = " & ";
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        vbline = vbline +"rec"+optUseIndex+ "" +l.line+x;
      else
        vbline = vbline + "\""+l.line+"\""+x;
    }
    int p;
    while ((p = vbline.indexOf("\" & \"")) > -1)
      vbline = vbline.substring(0, p) + vbline.substring(p+5);
    if (proc.placeHolders.size() > 0)
    {
      for (int i = 0; i < proc.placeHolders.size(); i++)
      {
        String placeHolder = ":" + (String) proc.placeHolders.elementAt(i);
        String work = "";
        int n = vbline.indexOf(placeHolder);
        if (n == -1)
        {
          outData.println("Error with placeholders "+placeHolder);
          break;
        }
        if (n > 0)
          work = vbline.substring(0, n);
        work = work + "?";
        n += placeHolder.length();
        if (n < vbline.length());
        work = work + vbline.substring(n);
        vbline = work;
      }
      outData.println(vbline);
      for (int i=0; i < proc.placeHolders.size(); i++)
      {
        String placeHolder = (String) proc.placeHolders.elementAt(i);
        int index =  proc.indexOf(placeHolder);
        emitInputCode(proc, index, i, outData, optUseIndex);
      }
    }
    else
    {
      outData.println(vbline);
      for (int index=0; index < proc.inputs.size(); index++)
        emitInputCode(proc, index, index, outData, optUseIndex);
    }
    outData.println("    cursor.Execute");
    outData.println("    set rs = cursor.rs");
    if (proc.outputs.size() == 0)
      outData.println("    set cursor = nothing");
    else if (proc.isSingle)
    {
      outData.println("    if not rs.Eof then");
      for (int i=0; i < proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        outData.println("      rec"+optUseIndex+ "" +field.useName()+" = rs(\""+field.name+"\")");
      }
      outData.println("      Execute = true");
      outData.println("    end if");
    }
    if (proc.isSingle)
      outData.println("  end function");
    else
      outData.println("  end sub");
    outData.println();
    if (proc.outputs.size() > 0 && !proc.isSingle)
    {
      outData.println("  public function MoveNext()");
      outData.println("    ' Returns true if a record is found");
      outData.println("    if cursor is nothing then err.Raise 1998, \""+proc.upperFirst()+"\"");
      outData.println("    cursor.connect.RoutineName = \"MoveNext\"");
      outData.println("    MoveNext = false");
      outData.println("    if not rs.Eof then");
      for (int i=0; i<proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        outData.println("      rec"+optUseIndex+ "" +field.useName()+" = rs(\""+field.name+"\")");
      }
      outData.println("      MoveNext = true");
      outData.println("      rs.MoveNext");
      outData.println("    end if");
      outData.println("  end function");
      outData.println();
      outData.println("  public sub Cancel()");
      outData.println("    ' You must call call this when you terminate the fetch loop prematurly");
      outData.println("    ' but you must call this only if you do");
      outData.println("    if cursor.isOpen then cursor.Done");
      outData.println("  end sub");
      outData.println();
      outData.println("  public sub Load(Connect)");
      outData.println("    dim i");
      outData.println("    ReSize 32");
      outData.println("    Execute Connect");
      outData.println("    i = 0");
      outData.println("    do");
      outData.println("      if not MoveNext then exit do");
      outData.println("      if i >= ubound(rec) then ReSize i*2");
      outData.println("      i = i + 1");
      outData.println("      set rec(i) = rec(0)");
      outData.println("      set rec(0) = new "+recName);
      outData.println("    loop");
      outData.println("    ReSize i");
      outData.println("  end sub");
      outData.println();
    }
  }
  static void emitInputCode(Proc proc, int index, int pos, PrintWriter outData, String optUseIndex)
  {
    Field field = (Field) proc.inputs.elementAt(index);
    if (field.type == Field.IDENTITY && proc.isInsert)
      return;
    outData.println("    cursor.CreateParameter "+parmCreate(field, "rec"+optUseIndex+ "" +field.useName(), pos));
    return;
  }
  static String initVBVar(Field field)
  {
    if (field.isNull)
      return field.useName() + " = null";
    switch(field.type)
    {
    case Field.BOOLEAN:
      return field.useName() + " = false";
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
  static String parmCreate(Field field, String passed, int index)
  {
    String front = "\"_P"+index+"_\"";
    String type="adUnknown";
    switch(field.type)
    {
    case Field.BOOLEAN:
      type = "adBoolean";
      break;
    case Field.BYTE:
      type = "adTinyInt";
      break;
    case Field.SHORT:
      type = "adSmallInt";
      break;
    case Field.INT:
      type = "adInteger";
      break;
    case Field.LONG:
      type = "adBigInt";
      break;
    case Field.SEQUENCE:
      type = "adInteger";
      break;
    case Field.IDENTITY:
      type = "adInteger";
      break;
    case Field.CHAR:
    case Field.ANSICHAR:
      type = "adChar";
    case Field.USERSTAMP:
      type = "adVarChar";
      break;
    case Field.TLOB:
      type = "adLongVarChar";
      break;
    case Field.BLOB:
      type = "adLongVarBinary";
      break;
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      type = "adDate";
      break;
    case Field.TIME:
      type = "adTime";
      break;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      type = "adDouble";
      break;
    }
    return front+", "+type+", adParamInput, "+field.length+", "+passed;
  }
}
