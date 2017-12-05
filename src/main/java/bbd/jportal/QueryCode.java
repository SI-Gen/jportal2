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

import java.util.Vector;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class QueryCode extends Generator
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
        outLog.println(args[i]+": Generate SQL Query Code");
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
    return "Generate SQL Query Code";
  }
  public static String documentation()
  {
    return "Generate SQL Query Code";
  }
  /**
  * Generates the procedure classes for each table present.
  */
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
      outLog.println("Code: "+output+table.useName() + ".py");
      OutputStream outFile = new FileOutputStream(output+table.useName() + ".py");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        for (int i=0; i<table.procs.size(); i++)
        {
          Proc proc = (Proc) table.procs.elementAt(i);
          if (proc.isData)
            continue;
          generateSQLCode(proc, outData);
        }
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
  static Vector<String> parmsSeen;
  static void addToParms(String parm)
  {
    for (int i=0; i<parmsSeen.size(); i++)
    {
      String stored = (String)parmsSeen.elementAt(i);
      if (stored.compareTo(parm) == 0)
        return;
    }
    parmsSeen.addElement(parm);
  }
  static int questionsSeen;
  static String lowerFirst(String name)
  {
    String result = name.substring(0,1).toLowerCase()+name.substring(1);
    return result;
  }
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
      result = result + "%(" + lowerFirst(field.name) +")s";
      addToParms(lowerFirst(field.name));
      line = line.substring(1);
    }
    result = result + line;
    return python(result);
  }
  static final String VALID_NAME_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
private static ObjectInputStream in;
  static String python(String line)
  {
    String result = "";
    int p;
    while ((p = line.indexOf(":")) > -1)
    {
      if (p > 0)
      {
        result = result + line.substring(0, p);
        line = line.substring(p+1);
      }
      int n=0;
      for (;n<line.length();n++)
      {
        char ch = line.charAt(n);
        if (VALID_NAME_CHARS.indexOf(ch) == -1)
          break;
      }
      String parm = line.substring(0, n);
      result = result + "%(" + lowerFirst(parm) + ")s";
      addToParms(lowerFirst(parm));
      line = line.substring(n);
    }
    result = result + line;
    return result;
  }
  /**
  * Emits SQL Code
  */
  static void generateSQLCode(Proc proc, PrintWriter outData)
  {
    questionsSeen = 0;
    parmsSeen = new Vector<String>();

    outData.println("_"+lowerFirst(proc.name)+"_='''\\");
    for (int i=0; i < proc.lines.size(); i++)
    {
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        outData.println("%("+l.line+")s");
      else
        outData.println(question(proc, l.line));
    }
    outData.println("'''");
    outData.print  ("def "+lowerFirst(proc.name)+"(");
    String comma = "";
    for (int i=0; i < parmsSeen.size(); i++)
    {
      String parm = (String)parmsSeen.elementAt(i);
      outData.print(comma+parm+"=''");
      comma =", ";
    }
    outData.println("):");
    outData.println("    parms = {}");
    for (int i=0; i < parmsSeen.size(); i++)
    {
      String parm = (String)parmsSeen.elementAt(i);
      outData.println("    parms['"+parm+"'] = "+parm);
    }
    outData.println("    return _"+lowerFirst(proc.name)+"_ % parms");
    outData.println();
  }
}

