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

public class BCPCode extends Generator
{
  private static ObjectInputStream in;
private static PrintWriter outData;
private static PrintWriter outData2;
private static PrintWriter outData3;
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
        outLog.println(args[i] + ": Generate bulk loader for MS SQL Server Code");
        in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
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
    return "Generate bulk loader for MS SQL Server Code";
  }
  public static String documentation()
  {
    return "Generate bulk loader for MS SQL Server Code";
  }
  /**
   * Generates the loader code for each table present.
   */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i = 0; i < database.tables.size(); i++)
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
      outLog.println("Code: " + output + table.useName() + ".bat");
      OutputStream outFile = new FileOutputStream(output + table.name + ".bat");
      try
      {
        String x;
        String fileName;
        outData = new PrintWriter(outFile);
        if (table.database.output.length() > 0)
        {
          fileName = table.database.output;
          x = table.database.output + ".TXT";
        }
        else
        {
          fileName = table.database.name;
          x = "%1";
        }
        outData.println("call cleartab " + fileName + ".sql");
        outData.println("filter " + table.name + ".flt " + x + " > " + table.name + ".err");
        outData.println("bcp " + table.database.name + ".dbo." + table.name + " in filtered.txt /f" + table.name + ".fmt -b200 /Usa /P /S" + table.database.server + " /o" + table.name + ".log");
        outData.flush();
      }
      finally
      {
        outFile.close();
      }
      outLog.println("Code: " + output + table.useName() + ".fmt");
      outFile = new FileOutputStream(output + table.name + ".fmt");
      try
      {
        outData2 = new PrintWriter(outFile);
        outData2.println("6.0");
        outData2.println("" + table.fields.size());
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);
          String r;
          if (i == table.fields.size() - 1)
            r = "\"\\r\\n\"";
          else
            r = "\"\\t\"";
          outData2.println("" + (i + 1) +
              " " + varType(field) +
              " 0" +
              " " + fieldLength(field) +
              " " + r +
              " " + (i + 1) +
              " " + field.name);
        }
        outData2.flush();
      }
      finally
      {
        outFile.close();
      }
      outLog.println("Code: " + output + table.useName() + ".flt");
      outFile = new FileOutputStream(output + table.name + ".flt");
      try
      {
        outData3 = new PrintWriter(outFile);
        for (int i = 0; i < table.fields.size(); i++)
        {
          String s;
          Field field = (Field) table.fields.elementAt(i);
          s = (field.isNull) ? "Y" : "N";
          outData3.println(field.name + "\t" +
              filterType(field) + "\t" +
              fieldLength(field) + "\t" +
              s + "\t" +
              fieldSize(field));
        }
        outData3.flush();
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
  static String varType(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "SQLCHAR";
    }
    return "unsupported";
  }
  static String filterType(Field field)
  {
    switch (field.type)
    {
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return "A";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
      return "D";
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "N";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "F";
    }
    return "unsupported";
  }
  static int fieldLength(Field field)
  {
    switch (field.type)
    {
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return field.length;
    case Field.TLOB:
    case Field.BLOB:
      return 3000;
    case Field.DATE:
    case Field.TIME:
      return 10;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 19;
    case Field.BOOLEAN:
    case Field.BYTE:
      return 3;
    case Field.SHORT:
      return 5;
    case Field.INT:
      return 9;
    case Field.LONG:
      return 17;
    case Field.SEQUENCE:
      return 9;
    case Field.IDENTITY:
      return 9;
    case Field.FLOAT:
      return 17;
    case Field.DOUBLE:
      return 17;
    case Field.MONEY:
      return 30;
    }
    return field.length;
  }
  static int fieldSize(Field field)
  {
    switch (field.type)
    {
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return field.length;
    case Field.TLOB:
    case Field.BLOB:
      return 2048;
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 8;
    case Field.BOOLEAN:
    case Field.BYTE:
      return 1;
    case Field.SHORT:
      return 2;
    case Field.INT:
      return 4;
    case Field.LONG:
      return 4;
    case Field.SEQUENCE:
      return 4;
    case Field.IDENTITY:
      return 4;
    case Field.FLOAT:
      return 8;
    case Field.DOUBLE:
      return 8;
    case Field.MONEY:
      return 8;
    }
    return field.length;
  }
}

