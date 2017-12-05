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

public class DBPyCode extends Generator
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
        outLog.println(args[i]+": Generate IDL Code for 3 Tier Access");
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
    return "Generate DB Python Code";
  }
  public static String documentation()
  {
    return "Generate DB Python Code";
  }
  /**
   * Padding function
   */
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length-1; i++)
      s = s + " ";
    return s + " ";
  }
  /**
  * Generates the procedure classes for each table present.
  */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generatePython(table, output, outLog);
    }
  }
  private static void generatePython(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName() + "AnyDB.py");
      OutputStream outFile = new FileOutputStream(output + table.useName() + "AnyDB.py");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("# This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println();
          outData.println("def load(crackle, procs):");
          outData.println("  Proc = crackle.Proc");
          outData.println("  SINGLE   = crackle.SINGLE");
          outData.println("  MULTIPLE = crackle.MULTIPLE");
          outData.println("  ACTION   = crackle.ACTION");
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData)
              continue;
            if (proc.outputs.size() > 0)
              if (proc.isSingle)
                generatePythonSingle(table, proc, outData);
              else
                generatePythonMultiple(table, proc, outData);
            else
              generatePythonAction(table, proc, outData);
          }
          outData.println("  return procs");
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
  private static void generatePythonSingle(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    String extended = "True";
    if (proc.isStdExtended())
      dataStruct = table.useName();
    else
    {
      extended = "False";
      dataStruct = table.useName() + proc.upperFirst();
    }
    outData.println("  procs.append(Proc('" + proc.name + "', '" + dataStruct + "', '" + table.useName() + "', SINGLE, " + extended + ", True, True) )");
  }
  private static void generatePythonMultiple(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    String input = "False";
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
      input = "True";
    String extended = "True";
    if (proc.isStdExtended())
      dataStruct = table.useName();
    else
    {
      extended = "False";
      dataStruct = table.useName() + proc.upperFirst();
    }
    outData.println("  procs.append(Proc('" + proc.name + "', '" + dataStruct + "', '" + table.useName() + "', MULTIPLE, " + extended + ", " + input + ", True) )");
  }
  private static void generatePythonAction(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    String input = "False";
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
      input = "True";
    String output = "False";
    if (proc.hasModifieds())
      output = "True";
    String extended = "True";
    if (proc.isStdExtended())
      dataStruct = table.useName();
    else
    {
      extended = "False";
      dataStruct = table.useName() + proc.upperFirst();
    }
    outData.println("  procs.append(Proc('" + proc.name + "', '" + dataStruct + "', '" + table.useName() + "', ACTION, " + extended + ", " + input + ", " + output + ") )");
  }
}
