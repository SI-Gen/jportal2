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

public class OracleExportList extends Generator
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
        outLog.println(args[i]+": generating Oracle Export List");
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
    return "Generate Oracle Export List";
  }
  public static String documentation()
  {
    return "Generate Oracle Export List";
  }
  /**
  * Generates the SQL for ORACLE Table creation.
  */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("EXP: " + output + database.name + ".lst");
      OutputStream outFile = new FileOutputStream(output + database.name + ".Lst");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        String w1 = "TABLES = (";
        String w2 = ",";
        for (int i=0; i < database.tables.size(); i++)
        {
          Table table = (Table) database.tables.elementAt(i);
          if (i == database.tables.size()-1)
            w2 = ")";
          outData.println(w1 + table.name + w2);
          w1 = "          ";
        }
        outData.println("DIRECT=Y");
        outData.println("INDEXES=N");
        outData.println("GRANTS=N");
        outData.println("CONSTRAINTS=N");
        outData.println("LOG="+database.name+".log");
        outData.println("RECORDLENGTH=40000");
        outData.flush();
        outData.close();
      }
      finally
      {
        outFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate Oracle SQL IO Error");
    }
  }
}
