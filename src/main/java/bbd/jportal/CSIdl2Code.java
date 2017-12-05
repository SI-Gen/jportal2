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

public class CSIdl2Code extends Generator
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
        outLog.println(args[i] + ": Generate C# IDL Code for 3 Tier Access");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
        in.close();
        generate(database, "", outLog);
      }
      outLog.flush();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  public static String description()
  {
    return "Generate C# IDL Code for 3 Tier Access";
  }
  public static String documentation()
  {
    return "Generate C# IDL Code for 3 Tier Access";
  }
  /**
   * Padder function
   */
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  /**
   * Generates the procedure classes for each table present.
   */
  public static void generate(Database database, String output,
      PrintWriter outLog)
  {
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      genTable(table, output, outLog);
    }
  }
  static String packName;
  /**
   * Build of standard and user defined procedures
   */
  static void genTable(Table table, String output, PrintWriter outLog)
  {
    boolean noIdl = table.hasOption("noIdl");
    if (noIdl == true)
    {
      boolean needIdl = false;
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc = (Proc) table.procs.elementAt(i);
        if (proc.hasOption("Idl") == true)
        {
          needIdl = true;
          break;
        }
      }
      if (needIdl == false)
        return;
    }
    if (table.database.packageName.length() > 0)
      packName = table.database.packageName;
    else
      packName = "bbd.jportal";
    try
    {
      outLog.println("Code: "+output+table.useName() + ".ii");
      OutputStream outFile = new FileOutputStream(output+table.useName() + ".ii");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("module  "+table.useName()+";");
          outData.println();
          outData.println("struct "+table.useName()+" \"using "+packName+";\"");
          if (table.hasStdProcs)
            outData.println("struct "+table.useName()+"Rec \"using "+packName+";\"");
          for (int i=0; i<table.procs.size(); i++)
          {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (noIdl == true && proc.hasOption("Idl") == false)
              continue;
            if (proc.isData || proc.isStdExtended()  || proc.hasNoData())
              continue;
            String procName = table.useName()+proc.upperFirst()+"Rec";
            outData.println("struct "+procName+" \"using "+packName+";\"");
          }
          outData.println();
          for (int i=0; i<table.procs.size(); i++)
          {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData)
              continue;
            if (noIdl == true && proc.hasOption("Idl") == false)
              continue;
            if (proc.outputs.size() > 0)
              if (proc.isSingle)
                genSingle(table, proc, outData);
              else
                genMultiple(table, proc, outData);
            else
              if (proc.isInsert && proc.hasReturning && proc.table.hasIdentity)
                genSingle(table, proc, outData);
              else
                genAction(table, proc, outData);
          }
          //for (int i=0; i<table.parameters.size(); i++)
          //{
          //  Parameter parameter = (Parameter)table.parameters.elementAt(i);
          //  if (parameter.cache != null)
          //  {
          //    Proc cache = (Proc) parameter.cache;
          //    if (cache != null)
          //      genCacheLoader(table, cache, parameter.cacheExtras, outData);
          //  }
          //}
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
  //private static void genCacheLoader(Table table, Proc proc, Vector extras, PrintWriter outData)
  //{
  //  String dataStruct;
  //  if (proc.isStdExtended())
  //    dataStruct = table.useName()+"Rec";
  //  else
  //    dataStruct = table.useName()+proc.upperFirst()+"Rec";
  //  String function = table.useName()+proc.upperFirst();
  //  outData.print(function+"Result "+function+"Cache(int reload");
  //  boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
  //  if (hasInput)
  //    outData.print(", "+dataStruct+" rec");
  //  outData.println(")");
  //  outData.println("{");
  //  outData.println("message: #");
  //  outData.println("input");
  //  if (hasInput)
  //    outData.println("  rec;");
  //  outData.println("  reload;");
  //  outData.println("code");
  //  outData.println("//  "+function+"Result result = new "+function+"Result();");
  //  outData.println("//  try");
  //  outData.println("//  {");
  //  outData.println("//    if (Cache.cached(\""+function+"\", reload) == false)");
  //  String tableList = "\""+table.name+"\"";
  //  if (extras.size() > 0)
  //  {
  //    String comma = "";
  //    tableList = "";
  //    for (int i=0; i<extras.size(); i++)
  //    {
  //      tableList += comma+"\""+(String)extras.elementAt(i)+"\"";
  //      comma = ",";
  //    }
  //  }
  //  outData.println("//      Cache.put(this, \""+function+"\", new String[] {"+tableList+"}, "+function+"(), reload);");
  //  outData.println("//    result = ("+function+"Result) Cache.get(\""+function+"\");");
  //  outData.println("//  }");
  //  outData.println("//  catch(Exception ex)");
  //  outData.println("//  {");
  //  outData.println("//    ex.printStackTrace();");
  //  outData.println("//    throw ex;");
  //  outData.println("//  }");
  //  outData.println("//  return result;");
  //  outData.println("endcode");
  //  outData.println("}");
  //  outData.println();
  //}
  private static void genAction(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStdExtended())
      dataStruct = table.useName()+"Rec";
    else
    {
      dataStruct = table.useName()+proc.upperFirst()+"Rec";
    }
    boolean hasMods  = proc.hasModifieds();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.print("void "+table.useName()+proc.upperFirst()+"(");
    if (hasInput)
      outData.print(dataStruct+" rec");
    outData.println(")");
    outData.println("{");
    outData.println("message: #");
    if (hasInput)
    {
      outData.println("input");
      outData.println("  rec;");
    }
    if (hasMods == true)
    {
      outData.println("output");
      outData.println("  rec;");
    }
    outData.println("code");
    outData.println("  try");
    outData.println("  {");
    if (proc.isStdExtended())
      outData.println("    " + table.useName() + " cursor = new " + table.useName() + "();");
    else
      outData.println("    " + table.useName() + "" + proc.upperFirst() + " cursor = new " + table.useName() + "." + proc.upperFirst() + "();");
    if (hasInput)
      outData.println("    cursor.Rec = rec;");
    outData.println("    cursor."+proc.upperFirst()+"(connector);");
    if (hasMods == true)
      outData.println("    rec = cursor.Rec;");
    outData.println("  }");
    outData.println("  catch(Exception ex)");
    outData.println("  {");
    outData.println("    connector.FlagRollback();");
    outData.println("    throw ex;");
    outData.println("  }");
    outData.println("endcode");
    outData.println("}");
    outData.println();
  }
  private static void genMultiple(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStdExtended())
      dataStruct = table.useName() + "Rec";
    else
      dataStruct = table.useName() + proc.upperFirst() + "Rec";
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.print("int " + table.useName() + proc.upperFirst() + "(");
    if (hasInput)
      outData.print("" + dataStruct + "* rec, ");
    outData.println("" + dataStruct + "* result)");
    outData.println("{");
    outData.println("message: #");
    if (hasInput)
    {
      outData.println("input");
      outData.println("  rec;");
    }
    outData.println("output");
    outData.println("  result;");
    outData.println("code");
    outData.println("  try");
    outData.println("  {");
    if (proc.isStdExtended())
      outData.println("    " + table.useName() + " cursor = new " + table.useName() + "();");
    else
      outData.println("    " + table.useName() + "" + proc.upperFirst() + " cursor = new(" + table.useName() + "." + proc.upperFirst() + "();");
    if (hasInput)
      outData.println("    cursor.Rec = rec;");
    outData.println("    cursor." + proc.upperFirst() + "Load();");
    outData.println("    result = cursor.List;");
    outData.println("  }");
    outData.println("  catch(Exception ex)");
    outData.println("  {");
    outData.println("    connector.FlagRollback();");
    outData.println("    throw ex;");
    outData.println("  }");
    outData.println("endcode");
    outData.println("}");
    outData.println();
  }
  private static void genSingle(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    String added = "";
    if (proc.isStdExtended())
      dataStruct = table.useName()+"Rec";
    else
    {
      dataStruct = table.useName()+proc.upperFirst()+"Rec";
      added = "Proc";
    }
    outData.println(dataStruct+" "+table.useName()+proc.upperFirst()
                   +"("+dataStruct+" rec)");
    outData.println("{");
    outData.println("message: #");
    outData.println("input");
    outData.println("  rec;");
    outData.println("output");
    outData.println("  rec;");
    outData.println("code");
    outData.println("//  "+dataStruct+" result = null;");
    outData.println("//  try");
    outData.println("//  {");
    outData.println("//    "+table.useName()+" table = new "+table.useName()+"();");
    if (proc.isStdExtended())
      outData.println("//    "+table.useName()+".Standard cursor = table.getStandard();");
    else
      outData.println("//    "+table.useName()+ "" +proc.upperFirst()+" cursor = table.get"+proc.upperFirst()+"();");
    outData.println("//    cursor.assign(rec);");
    outData.println("//    boolean has = cursor."+proc.lowerFirst()+"();");
    outData.println("//    if (has == true)");
    outData.println("//      result = cursor.get"+added+"Copy();");
    outData.println("//  }");
    outData.println("//  catch(Exception ex)");
    outData.println("//  {");
    outData.println("//    ex.printStackTrace();");
    outData.println("//    throw ex;");
    outData.println("//  }");
    outData.println("//  return result;");
    outData.println("endcode");
    outData.println("}");
    outData.println();
  }
}
