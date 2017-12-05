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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

public class AdoCSCode extends Generator
{
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i < args.length; i++)
      {
        outLog.println(args[i] + ": Generate C# Code for ADO.NET via IDbConnection Version 2");
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
    return "Generate C# Code for ADO.NET via IDbConnection Version 2";
  }
  public static String documentation()
  {
    return "Generate C# Code for ADO.NET via IDbConnection Version 2"
    + "\r\nDATABASE name FLAGS flag"
    + "\r\n- \"mssql storedprocs\" generate stored procedures for MSSql"
    + "\r\n- \"use separate\" generate classes in separate files"
      ;
  }
  protected static Vector<Flag> flagsVector;
  static boolean mSSqlStoredProcs;
  static boolean useSeparate;
  private static void flagDefaults()
  {
    mSSqlStoredProcs = false;
    useSeparate = false;
  }
  public static Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<Flag>();
      flagDefaults();
      flagsVector.addElement(new Flag("mssql storedprocs", new Boolean(mSSqlStoredProcs), "Generate MSSql Stored Procedures"));
      flagsVector.addElement(new Flag("use separate", new Boolean(useSeparate), "Generate Separate Files"));
    }
    return flagsVector;
  }
  /**
   * Sets generation flags.
   */
  static void setFlags(Database database, PrintWriter outLog)
  {
    if (flagsVector != null)
    {
      mSSqlStoredProcs = toBoolean(((Flag)flagsVector.elementAt(0)).value);
      useSeparate = toBoolean(((Flag)flagsVector.elementAt(1)).value);
    }
    else
      flagDefaults();
    for (int i = 0; i < database.flags.size(); i++)
    {
      String flag = (String)database.flags.elementAt(i);
      if (flag.equalsIgnoreCase("mssql storedprocs"))
        mSSqlStoredProcs = true;
      else if (flag.equalsIgnoreCase("use separate"))
        useSeparate = true;
    }
    if (mSSqlStoredProcs)
      outLog.println(" (mssql storedprocs)");
    if (useSeparate)
      outLog.println(" (use separate)");
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    setFlags(database, outLog);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table)database.tables.elementAt(i);
      generate(table, output, outLog);
    }
  }
  static OutputStream procFile;
  static PrintWriter procData;
  static void generate(Table table, String output, PrintWriter outLog)
  {
    OutputStream outFile;
    try
    {
      String added = "";
      if (useSeparate == true)
        added = "Structs";
      outFile = openOutputStream(table, output, outLog, added);
      if (mSSqlStoredProcs == true)
      {
        outLog.println("DDL: " + output + table.useName() + ".sproc.sql");
        procFile = new FileOutputStream(output + table.name + ".sproc.sql");
        procData = new PrintWriter(procFile);
        procData.println("use " + table.database.name);
        procData.println();
      }
      try
      {
        PrintWriter outData = openWriterPuttingTop(table, outFile);
        generateStructs(table, outData);
        if (useSeparate == true)
        {
          outData.println("}");
          outData.flush();
          outFile.close();
          outFile = openOutputStream(table, output, outLog, "Tables");
          outData = openWriterPuttingTop(table, outFile);
        }
        if (useSeparate == true)
        {
          outData.println("}");
          outData.flush();
          outFile.close();
          outFile = openOutputStream(table, output, outLog, "");
          outData = openWriterPuttingTop(table, outFile);
        }
        generateCode(table, outData);
        outData.println("}");
        outData.flush();
        if (mSSqlStoredProcs == true)
          procData.flush();
      }
      finally
      {
        outFile.close();
        if (mSSqlStoredProcs == true)
          procFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate Procs IO Error");
    }
  }
  private static PrintWriter openWriterPuttingTop(Table table, OutputStream outFile)
  {
    PrintWriter outData = new PrintWriter(outFile);
    String packageName = table.database.packageName;
    if (packageName.length() == 0)
      packageName = "bbd.jportal";
    outData.println("using System;");
    outData.println("using System.Collections.Generic;");
    outData.println("using System.Data;");
    outData.println("using bbd.jportal;");
    outData.println("");
    outData.println("namespace " + packageName);
    outData.println("{");
    return outData;
  }
  private static OutputStream openOutputStream(Table table, String output, PrintWriter outLog, String added) throws FileNotFoundException, IOException
  {
    OutputStream outFile;
    String outFileName = output + table.name + added + ".cs";
    outLog.println("Code: " + outFileName);
    outFile = new FileOutputStream(outFileName);
    return outFile;
  }
  public static void generateStructPairs(Vector<Field> fields, Vector<String> dynamics, String mainName, PrintWriter outData)
  {
    outData.println("  [Serializable()]");
    outData.println("  public partial class " + mainName + "Rec");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("    " + fieldDef(field));
    }
    outData.println("  }");
  }
  public static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("  public class " + table.useName() + field.useUpperName() + "Ord");
        outData.println("  {");
        String datatype = "int";
        if (field.type == Field.ANSICHAR && field.length == 1)
          datatype = "string";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "\"" + (char)en.value + "\"";
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
            evalue = "\"" + (char)en.value + "\"";
          outData.println("      case " + evalue + ": return \"" + en.name + "\";");
        }
        outData.println("      }");
        outData.println("      return \"unknown ordinal: \"+ordinal;");
        outData.println("    }");
        outData.println("  }");
      }
    }
  }
  public static void generateStructs(Table table, PrintWriter outData)
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
      generateStructPairs(table.fields, null, table.useName(), outData);
      generateEnumOrdinals(table, outData);
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc = (Proc)table.procs.elementAt(i);
        if (proc.isData || proc.isStd || proc.hasNoData())
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
        Vector<Field> fields = new Vector<Field>();
        for (int j = 0; j < proc.outputs.size(); j++)
          fields.addElement(proc.outputs.elementAt(j));
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field)proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name) == false)
            fields.addElement(field);
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          Integer n = (Integer)proc.dynamicSizes.elementAt(j);
          Field field = new Field();
          field.name = s;
          field.type = Field.DYNAMIC;
          field.length = n.intValue();
          fields.addElement(field);
        }
        generateStructPairs(fields, proc.dynamics, table.useName() + proc.upperFirst(), outData);
      }
    }
  }
  public static void generateCode(Table table, PrintWriter outData)
  {
    boolean firsttime = true;
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData == true || proc.isStd == false)
        continue;
      generateStdCode(table, proc, outData, firsttime);
      firsttime = false;
    }
    if (firsttime == false)
      outData.println("  }");
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData == true || proc.isStd == true)
        continue;
      generateCode(table, proc, outData);
    }
  }
  static PlaceHolder placeHolder;
  static void generateStoredProc(Proc proc, String storedProcName, Vector<String> lines)
  {
    procData.println("if exists (select * from sysobjects where id = object_id('dbo." + storedProcName + "') and sysstat & 0xf = 4)");
    procData.println("drop procedure dbo." + storedProcName);
    procData.println("GO");
    procData.println("");
    procData.println("CREATE PROCEDURE dbo." + storedProcName);
    String comma = "(";
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      procData.println(comma + " @P" + i + " " + varType(field) + " -- " + field.name);
      comma = ",";
    }
    if (placeHolder.pairs.size() > 0)
      procData.println(")");
    procData.println("AS");
    for (int i = 0; i < lines.size(); i++)
    {
      String line = (String)lines.elementAt(i);
      procData.println(line.substring(1, line.length() - 1));
    }
    if (proc.isInsert && proc.hasReturning && proc.table.hasIdentity)
    {
      procData.println("; SELECT CAST(SCOPE_IDENTITY() AS INT)");
    }
    procData.println("GO");
    procData.println("");
  }
  static void generateStoredProcCommand(Proc proc, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    placeHolder = new PlaceHolder(proc, PlaceHolder.AT, "");
    String storedProcName = proc.table.useName() + upperFirst;
    Vector<String> lines = placeHolder.getLines();
    generateStoredProc(proc, storedProcName, lines);
    outData.println("    public string Command" + upperFirst + "()");
    outData.println("    {");
    for (int i = 0; i < lines.size(); i++)
    {
      String line = (String)lines.elementAt(i);
      outData.println("      // " + line.substring(1, line.length() - 1));
    }
    outData.println("      return \"" + storedProcName + "\";");
    outData.println("    }");
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    if (proc.hasReturning)
    {
      placeHolder = new PlaceHolder(proc, PlaceHolder.CURLY, "");
      outData.println("    public string Command" + upperFirst + "(Connect connect, string aTable, string aField)");
    }
    else if (proc.isMultipleInput == true && proc.isInsert == true)
    {
      placeHolder = new PlaceHolder(proc, PlaceHolder.CURLY, "");
      outData.println("    public string Command" + upperFirst + "(Connect connect, string aTable, string aField)");
    }
    else
    {
      placeHolder = new PlaceHolder(proc, PlaceHolder.CURLY, "Rec.");
      outData.println("    public string Command" + upperFirst + "()");
    }
    Vector<String> lines = placeHolder.getLines();
    outData.println("    {");
    if (proc.hasReturning)
      outData.println("      Returning _ret = new Returning(connect.TypeOfVendor, aTable, aField);");
    else if (proc.isMultipleInput == true && proc.isInsert == true)
      outData.println("      Returning _ret = new Returning(connect.TypeOfVendor, aTable, aField);");
    outData.println("      return ");
    String plus = "        ";
    for (int i = 0; i < lines.size(); i++)
    {
      String line = (String)lines.elementAt(i);
      if (proc.hasReturning)
      {
        String retName = returningField(proc);
        int b = line.indexOf(retName);
        if (b > 0 && line.charAt(b - 1) == ' ')
        {
          int e = line.indexOf(',');
          if (e == -1) e = line.indexOf('"');
          if (e - b == retName.length())
            line = "_ret.checkUse(" + line + ")";
        }
      }
      outData.println(plus + line);
      plus = "      + ";
    }
    outData.println("      ;");
    outData.println("    }");
  }
  static void generateNonQueryProc(Proc proc, String mainName, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    outData.println("    public void " + upperFirst + "(Connect connect)");
    outData.println("    {");
    outData.println("      using (Cursor wCursor = new Cursor(connect))");
    outData.println("      {");
    if (doMSSqlStoredProcs(proc))
      outData.println("        wCursor.Procedure(Command" + upperFirst + "());");
    else
      outData.println("        wCursor.Format(Command" + upperFirst + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      String member = "";
      if (field.type == Field.BLOB)
        member = ".getBlob()";
      String tail = "";
      if (isNull(field))
        tail = ", true";
      if (proc.isInsert && field.isSequence)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetSequence(\"" + proc.table.name + "\",\"" + field.name + "\", ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useName() + " = " + field.useLowerName() + ";");
			}
      else if (field.type == Field.TIMESTAMP)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetTimeStamp(ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useName() + " = " + field.useLowerName() + ";");
			}
      else if (field.type == Field.USERSTAMP)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetUserStamp(ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useName() + " = " + field.useLowerName() + ";");
			}
      else
        outData.println("        wCursor.Parameter(" + i + ", mRec._" + field.useName() + member + tail + ");");
    }
    outData.println("        wCursor.Exec();");
    outData.println("      }");
    outData.println("    }");
  }
  static void generateReturningProc(Proc proc, String mainName, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    Field identity = null;
    for (int i = 0; i < proc.table.fields.size(); i++)
    {
      Field field = (Field)proc.table.fields.elementAt(i);
      if (field.isSequence)
      {
        identity = field;
        break;
      }
    }
    if (identity == null)
    {
      generateNonQueryProc(proc, mainName, outData);
      return;
    }
    outData.println("    public bool " + upperFirst + "(Connect connect)");
    outData.println("    {");
    outData.println("      using (Cursor wCursor = new Cursor(connect))");
    outData.println("      {");
    if (doMSSqlStoredProcs(proc))
			outData.println("        wCursor.Procedure(Command" + upperFirst + "());");
    else
      outData.println("        wCursor.Format(Command" + upperFirst + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      String member = "";
      if (field.type == Field.BLOB)
        member = ".getBlob()";
      String tail = "";
      if (isNull(field))
        tail = ", true";
      if (field.type == Field.TIMESTAMP)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetTimeStamp(ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useName() + " = " + field.useLowerName() + ";");
			}
      else if (field.type == Field.USERSTAMP)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetUserStamp(ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useName() + " = " + field.useLowerName() + ";");
			}
      else
        outData.println("        wCursor.Parameter(" + i + ", mRec._" + field.useName() + member + tail + ");");
    }
    outData.println("        wCursor.Run();");
    outData.println("        bool wResult = (wCursor.HasReader() && wCursor.Read());");
    outData.println("        if (wResult == true)");
    outData.println("          mRec._" + identity.useName() + " = " + castOf(identity) + "wCursor." + cursorGet(identity, 0) + ";");
    outData.println("        if (wCursor.HasReader())");
    outData.println("          wCursor.Close();");
    outData.println("        return wResult;");
    outData.println("      }");
    outData.println("    }");
	}
  static void generateReadOneProc(Proc proc, String mainName, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    outData.println("    public bool " + upperFirst + "(Connect connect)");
    outData.println("    {");
    outData.println("      using (Cursor wCursor = new Cursor(connect))");
    outData.println("      {");
    if (doMSSqlStoredProcs(proc))
			outData.println("        wCursor.Procedure(Command" + upperFirst + "());");
    else
      outData.println("        wCursor.Format(Command" + upperFirst + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      String member = "";
      if (field.type == Field.BLOB)
        member = ".getBlob()";
      String tail = "";
      if (isNull(field))
        tail = ", true";
      if (field.type == Field.TIMESTAMP)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useUpperName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetTimeStamp(ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useUpperName() + " = " + field.useLowerName() + ";");
			}
      else if (field.type == Field.USERSTAMP)
			{
				outData.println("        var " + field.useLowerName() + " = mRec._" + field.useUpperName() + ";");
				outData.println("        wCursor.Parameter(" + i + ", wCursor.GetUserStamp(ref " + field.useLowerName() + "));");
				outData.println("        mRec._" + field.useUpperName() + " = " + field.useLowerName() + ";");
			}
      else
        outData.println("        wCursor.Parameter(" + i + ", mRec._" + field.useName() + member + tail + ");");
    }
    outData.println("        wCursor.Run();");
    outData.println("        bool wResult = (wCursor.HasReader() && wCursor.Read());");
    outData.println("        if (wResult == true)");
    outData.println("        {");
    boolean isNullDone = false;
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      String member = "";
      if (field.type == Field.BLOB)
        member = ".Buffer";
      if (isNull(field) == true && isNullDone == false)
      {
        outData.println("          bool isNull = false;");
        isNullDone = true;
      }
      outData.println("          mRec._" + field.useName() + member + " = " + castOf(field) + "wCursor." + cursorGet(field, i) + ";");
      if (isNull(field) == true)
      {
        outData.println("          if (isNull == true)");
        outData.println("            mRec._" + field.useName() + " = null;");
      }
    }
    outData.println("        }");
    outData.println("        if (wCursor.HasReader())");
    outData.println("          wCursor.Close();");
    outData.println("        return wResult;");
    outData.println("      }");
    outData.println("    }");
    if (proc.inputs.size() > 0)
    {
      outData.println("    public bool " + upperFirst + "(Connect connect");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        outData.println("    , " + fieldCastNo(field) + " _" + field.useName());
      }
      outData.println("    )");
      outData.println("    {");
      String comma = "{ ";
      outData.println("      mRec = new " + mainName + "Rec");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        outData.println("      " + comma + field.useName() + " = _" + field.useName());
        comma = ", ";
      }
      outData.println("      };");
      outData.println("      return " + upperFirst + "(connect);");
      outData.println("    }");
    }
  }
  static String returning(Proc proc)
  {
    if (proc.hasReturning == false && (proc.isMultipleInput == false || proc.isInsert == false))
      return "";
    String tableName = proc.table.useName();
    String fieldName = "";
    for (int i = 0; i < proc.table.fields.size(); i++)
    {
      Field field = (Field)proc.table.fields.elementAt(i);
      if (field.isSequence == true && proc.isInsert == true)
      {
        fieldName = field.useName();
        return "connect, \"" + tableName + "\", \"" + fieldName + "\"";
      }
    }
    return "";
  }
  static String returningField(Proc proc)
  {
    if (proc.hasReturning == false)
      return "";
    String fieldName = "";
    for (int i = 0; i < proc.table.fields.size(); i++)
    {
      Field field = (Field)proc.table.fields.elementAt(i);
      if (field.isSequence == true)
      {
        fieldName = field.useName();
        break;
      }
    }
    return fieldName;
  }
  static void generateFetchProc(Proc proc, String mainName, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    outData.println("    public void " + upperFirst + "(Connect connect)");
    outData.println("    {");
    outData.println("      mCursor = new Cursor(connect);");
    if (doMSSqlStoredProcs(proc))
			outData.println("      mCursor.Procedure(Command" + upperFirst + "());");
    else
      outData.println("      mCursor.Format(Command" + upperFirst + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      String member = "";
      if (field.type == Field.BLOB)
        member = ".getBlob()";
      String tail = "";
      if (isNull(field))
        tail = ", true";
      outData.println("      mCursor.Parameter(" + i + ", mRec._" + field.useName() + member + tail + ");");
    }
    outData.println("      mCursor.Run();");
    outData.println("    }");
    outData.println("    public DataTable " + upperFirst + "DataTable(Connect connect)");
    outData.println("    {");
    outData.println("      " + upperFirst + "(connect);");
    outData.println("      if (mCursor.HasReader())");
    outData.println("      {");
    outData.println("        DataTable table = new DataTable();");
    outData.println("        table.Load(mCursor.Reader);");
    outData.println("        return table;");
    outData.println("      }");
    outData.println("      else return null;");
    outData.println("    }");
    outData.println("    public void Set" + upperFirst + "Rec(DataTable table, int row) // used to be " + mainName);
    outData.println("    {");
    outData.println("      mRec = new " + mainName + "Rec();");
    outData.println("      DataRowCollection rows = table.Rows;");
    outData.println("      if (row < rows.Count)");
    outData.println("      {");
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      if (field.type == Field.BLOB)
        continue;
      if (useConvertType(field))
      {      
        String dtConvertType = dataTableConvertType(field);
        outData.println("        mRec._" + field.useName() + " = Convert." + dtConvertType + "(rows[row][\"" + field.useName() + "\"]);");
      }
      else
      {
        String dtType = dataTableType(field);
        outData.println("        mRec._" + field.useName() + " = (" + dtType + ")rows[row][\"" + field.useName() + "\"];");
      }
    }
    outData.println("      }");
    outData.println("    }");
    outData.println("    public bool " + upperFirst + "Fetch()");
    outData.println("    {");
    outData.println("      bool wResult = (mCursor.HasReader() && mCursor.Read());");
    outData.println("      if (wResult == true)");
    outData.println("      {");
    boolean isNullDone = false;
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field)proc.outputs.elementAt(i);
      String member = "";
      if (field.type == Field.BLOB)
        member = ".Buffer";
      if (isNull(field) == true && isNullDone == false)
      {
        outData.println("          bool isNull = false;");
        isNullDone = true;
      }
      outData.println("        mRec._" + field.useName() + member + " = " + castOf(field) + "mCursor." + cursorGet(field, i) + ";");
      if (isNull(field) == true)
      {
        outData.println("        if (isNull == true)");
        outData.println("          mRec._" + field.useName() + " = null;");
      }
    }
    outData.println("      }");
    outData.println("      else if (mCursor.HasReader())");
    outData.println("        mCursor.Close();");
    outData.println("      return wResult;");
    outData.println("    }");
    outData.println("    public void " + upperFirst + "Load(Connect connect)");
    outData.println("    {");
    outData.println("      " + upperFirst + "(connect);");
    outData.println("      while (" + upperFirst + "Fetch())");
    outData.println("      {");
    outData.println("        mList.Add(mRec);");
    outData.println("        mRec = new " + mainName + "Rec();");
    outData.println("      }");
    outData.println("    }");
    outData.println("    public IEnumerable<" + mainName + "Rec> " + upperFirst + "Yield(Connect connect)");
    outData.println("    {");
    outData.println("      try");
    outData.println("      {");
    outData.println("        " + upperFirst + "(connect);");
    outData.println("        while (" + upperFirst + "Fetch())");
    outData.println("          yield return mRec;");
    outData.println("      }");
    outData.println("      finally");
    outData.println("      {");
    outData.println("        mCursor.Close();");
    outData.println("      }");
    outData.println("    }");
    if (proc.inputs.size() > 0)
    {
      outData.println("    public IEnumerable<" + mainName + "Rec> " + upperFirst + "Yield(Connect connect");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        outData.println("    , " + fieldCastNo(field) + " _" + field.useName());
      }
      outData.println("    )");
      outData.println("    {");
      String comma = "{ ";
      outData.println("      mRec = new " + mainName + "Rec");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        outData.println("      " + comma + field.useName() + " = _" + field.useName());
        comma = ", ";
      }
      outData.println("      };");
      outData.println("      return " + upperFirst + "Yield(connect);");
      outData.println("    }");
    }
    outData.println("    public List<" + mainName + "Rec> Loaded { get { return mList; } }");
  }
  static void generateProcFunctions(Proc proc, String name, PrintWriter outData)
  {
    if (proc.outputs.size() > 0 && !proc.isSingle)
      generateFetchProc(proc, name, outData);
    else if (proc.outputs.size() > 0)
      generateReadOneProc(proc, name, outData);
    else if (proc.isInsert && proc.hasReturning && proc.table.hasIdentity)
      generateReturningProc(proc, name, outData);
    else
      generateNonQueryProc(proc, name, outData);
  }
  static void generateCClassTop(Proc proc, String mainName, PrintWriter outData, boolean doCursor)
  {
    outData.println("  [Serializable()]");
    outData.println("  public partial class " + mainName);
    outData.println("  {");
    if (doCursor == true || proc.hasNoData() == false)
    {
      outData.println("    private " + mainName + "Rec mRec;");
      outData.println("    public " + mainName + "Rec Rec { get { return mRec; } set { mRec = value; } }");
      if (doCursor == true || (proc.outputs.size() > 0 && !proc.isSingle))
      {
        outData.println("    private List<" + mainName + "Rec> mList;");
        outData.println("    public int Count { get { return mList.Count; } }");
        outData.println("    public Cursor mCursor;");
        outData.println("    public " + mainName + "Rec this[int i]");
        outData.println("    {");
        outData.println("      get");
        outData.println("      {");
        outData.println("        if (i >= 0 && i < mList.Count)");
        outData.println("          return mList[i];");
        outData.println("        throw new JPortalException(\"m index out of range\");");
        outData.println("      }");
        outData.println("      set");
        outData.println("      {");
        outData.println("        if (i < mList.Count)");
        outData.println("          mList.RemoveAt(i);");
        outData.println("        mList.Insert(i, value);");
        outData.println("      }");
        outData.println("    }");
      }
      outData.println("    public void Clear()");
      outData.println("    {");
      if (doCursor == true || (proc.outputs.size() > 0 && !proc.isSingle))
        outData.println("      mList = new List<" + mainName + "Rec>();");
      outData.println("      mRec = new " + mainName + "Rec();");
      outData.println("    }");
      outData.println("    public " + mainName + "()");
      outData.println("    {");
      outData.println("      Clear();");
      outData.println("    }");
    }
  }
  static boolean doMSSqlStoredProcs(Proc proc)
  {
    return mSSqlStoredProcs == true && proc.dynamics.size() == 0;
  }
  static void generateCode(Table table, Proc proc, PrintWriter outData)
  {
    String upperFirst = proc.upperFirst();
    if (proc.comments.size() > 0)
    {
      outData.println("  /// <summary>");
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = (String)proc.comments.elementAt(i);
        outData.println("  /// " + comment);
      }
      outData.println("  /// </summary>");
    }
    generateCClassTop(proc, table.useName() + upperFirst, outData, false);
    if (doMSSqlStoredProcs(proc) == true)
      generateStoredProcCommand(proc, outData);
    else
      generateCommand(proc, outData);
    generateProcFunctions(proc, table.useName() + upperFirst, outData);
    outData.println("  }");
  }
  static void generateStdCode(Table table, Proc proc, PrintWriter outData, boolean firsttime)
  {
    if (firsttime == true)
      generateCClassTop(proc, table.useName(), outData, table.hasCursorStdProc());
    if (proc.comments.size() > 0)
    {
      outData.println("    /// <summary>");
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = (String)proc.comments.elementAt(i);
        outData.println("    /// " + comment);
      }
      outData.println("    /// </summary>");
    }
    if (doMSSqlStoredProcs(proc) == true)
      generateStoredProcCommand(proc, outData);
    else
      generateCommand(proc, outData);
    generateProcFunctions(proc, table.useName(), outData);
  }
  static String castOf(Field field)
  {
    switch (field.type)
    {
      case Field.BYTE:
        return "(byte)";
      case Field.SHORT:
        return "(short)";
    }
    return "";
  }
  static String fullCastOf(Field field)
  {
    switch (field.type)
    {
      case Field.BYTE:
        return "(byte)";
      case Field.SHORT:
        return "(short)";
    }
    return "";
  }
  static String validNull(Field field)
  {
    switch (field.type)
    {
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.TIME:
        return "DateTime.MinValue";
      case Field.BOOLEAN:
        return "false";
      case Field.BYTE:
      case Field.DOUBLE:
      case Field.FLOAT:
      case Field.IDENTITY:
      case Field.INT:
      case Field.LONG:
      case Field.SEQUENCE:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.SHORT:
      case Field.STATUS:
        return "0";
    }
    return "null";
  }
  static String cursorGet(Field field, int occurence)
  {
    String tail = ")";
    if (isNull(field))
      tail = ", out isNull)";
    switch (field.type)
    {
      case Field.ANSICHAR:
        return "GetString(" + occurence + tail;
      case Field.BLOB:
        return "GetBlob(" + occurence + ", " + field.length + tail;
      case Field.BOOLEAN:
        return "GetBoolean(" + occurence + tail;
      case Field.BYTE:
        return "GetByte(" + occurence + tail;
      case Field.CHAR:
        return "GetString(" + occurence + tail;
      case Field.DATE:
        return "GetDateTime(" + occurence + tail;
      case Field.DATETIME:
        return "GetDateTime(" + occurence + tail;
      case Field.DYNAMIC:
        return "GetString(" + occurence + tail;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return "GetDecimal(" + occurence + tail;
        return "GetDouble(" + occurence + tail;
      case Field.IDENTITY:
        return "GetInt(" + occurence + tail;
      case Field.INT:
        return "GetInt(" + occurence + tail;
      case Field.LONG:
        return "GetLong(" + occurence + tail;
      case Field.BIGSEQUENCE:
        return "GetLong(" + occurence + tail;
      case Field.BIGIDENTITY:
        return "GetLong(" + occurence + tail;
      case Field.MONEY:
        return "GetDecimal(" + occurence + tail;
      case Field.SEQUENCE:
        return "GetInt(" + occurence + tail;
      case Field.SHORT:
        return "GetShort(" + occurence + tail;
      case Field.TIME:
        return "GetDateTime(" + occurence + tail;
      case Field.TIMESTAMP:
        return "GetDateTime(" + occurence + tail;
      case Field.TLOB:
        return "GetString(" + occurence + tail;
      case Field.XML:
        return "GetString(" + occurence + tail;
      case Field.USERSTAMP:
        return "GetString(" + occurence + tail;
      default:
        break;
    }
    return "Get(" + occurence + tail;
  }
  static boolean useConvertType(Field field)
  {
    switch (field.type)
    {
      case Field.IDENTITY:
      case Field.BIGIDENTITY:
      case Field.SEQUENCE:
      case Field.BIGSEQUENCE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
        return true;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision <= 15)
          return true;
    }
    return false;
  }
  static String dataTableConvertType(Field field)
  {
    switch (field.type)
    {
      case Field.DOUBLE:
      case Field.FLOAT:
        return "Double";
      case Field.IDENTITY:
      case Field.INT:
      case Field.SEQUENCE:
        return "Int32";
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.LONG:
        return "Int64";
      case Field.SHORT:
        return "Int16";
    }
    return "dataTableConvertType";
  }
  static String dataTableType(Field field)
  {
    switch (field.type)
    {
      case Field.ANSICHAR:
        return "string";
      case Field.BLOB:
        return "byte[]";
      case Field.BOOLEAN:
        return "bool";
      case Field.BYTE:
        return "byte)(short";
      case Field.CHAR:
        return "string";
      case Field.DATE:
        return "DateTime";
      case Field.DATETIME:
        return "DateTime";
      case Field.DYNAMIC:
        return "string";
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return "Decimal";
        return "Double";
      case Field.IDENTITY:
        return "int";
      case Field.BIGIDENTITY:
        return "long";
      case Field.INT:
        return "int";
      case Field.LONG:
        return "long";
      case Field.MONEY:
        return "Decimal";
      case Field.SEQUENCE:
        return "int";
      case Field.BIGSEQUENCE:
        return "long";
      case Field.SHORT:
        return "short";
      case Field.TIME:
        return "DateTime";
      case Field.TIMESTAMP:
        return "DateTime";
      case Field.TLOB:
        return "string";
      case Field.XML:
        return "string";
      case Field.USERSTAMP:
        return "string";
      default:
        break;
    }
    return "dataTableType";
  }
  static String fieldDef(Field field)
  {
    String result;
    //String maker = "";
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.USERSTAMP:
        result = "string";
        break;
      case Field.MONEY:
        result = "decimal";
        break;
      case Field.BLOB:
        result = "JPBlob";
        //maker = " = new JPBlob()";
        break;
      case Field.TLOB:
        result = "string";
        break;
      case Field.XML:
        result = "string";
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
      case Field.SEQUENCE:
      case Field.IDENTITY:
        result = "int";
        break;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        result = "long";
        break;
      case Field.SHORT:
        result = "short";
        break;
      case Field.DYNAMIC:
        result = "string";
        break;
      default:
        result = "whoknows";
        break;
    }
    if (isNull(field)) result += "?";
    return "public " + result + " " + field.useName() 
         + " { get { return _" + field.useName() + "; }"
         + " set { _" + field.useName() + " = value; } }" 
         + " internal " + result + " _" + field.useName() + ";";
  }
  static boolean isNull(Field field)
  {
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.DYNAMIC:
      case Field.IDENTITY:
      case Field.BIGIDENTITY:
      case Field.SEQUENCE:
      case Field.BIGSEQUENCE:
      case Field.USERSTAMP:
      case Field.BLOB:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
      case Field.TLOB:
      case Field.XML:
        return false;
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.DOUBLE:
      case Field.FLOAT:
      case Field.INT:
      case Field.LONG:
      case Field.MONEY:
      case Field.SHORT:
        return field.isNull;
    }
    return false;
  }
  static String fieldCastNo(Field field)
  {
    String result;
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        result = "string";
        break;
      case Field.MONEY:
        result = "decimal";
        break;
      case Field.BLOB:
        result = "JPBlob";
        break;
      case Field.TLOB:
        result = "string";
        break;
      case Field.XML:
        result = "string";
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
      case Field.SEQUENCE:
      case Field.IDENTITY:
        result = "int";
        break;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        result = "long";
        break;
      case Field.SHORT:
        result = "short";
        break;
      default:
        result = "whoknows";
        break;
    }
    return result;
  }
  static String fieldCast(Field field)
  {
    return "(" + fieldCastNo(field) + ")";
  }
  /**
   * Translates field type to SQLServer SQL column types
   */
  static String varType(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
        return "bit";
      case Field.BYTE:
        return "tinyint";
      case Field.SHORT:
        return "smallint";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "integer";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "longint";
      case Field.CHAR:
        return "varchar(" + String.valueOf(field.length) + ")";
      case Field.ANSICHAR:
        return "char(" + String.valueOf(field.length) + ")";
      case Field.DATE:
        return "datetime";
      case Field.DATETIME:
        return "datetime";
      case Field.TIME:
        return "datetime";
      case Field.TIMESTAMP:
        return "datetime";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "decimal";
        return "float";
      case Field.BLOB:
        return "image";
      case Field.TLOB:
        return "text";
      case Field.XML:
        return "xml";
      case Field.MONEY:
        return "decimal";
      case Field.USERSTAMP:
        return "varchar(24)";
      default:
        break;
    }
    return "unknown";
  }
}
