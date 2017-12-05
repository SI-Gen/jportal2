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

public class AccessJetDDL extends Generator
{
  private static ObjectInputStream in;
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
        outLog.println(args[i]+": generating Access Jet DDL");
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
    return "Generates the SQL for Access Table Jet creation";
  }
  public static String documentation()
  {
    return "Generates the SQL for Access Table creation using Jet";
  }
  /**
   * Generates the SQL for Access Table creation.
   */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    try
    {
      String fileName;
      if (database.output.length() > 0)
        fileName = database.output;
      else
        fileName = database.name;
      outLog.println("DDL: " + output + fileName + ".sql");
      OutputStream outFile = new FileOutputStream(output + fileName + ".sql");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        for (int i=0; i < database.tables.size(); i++)
          generate((Table) database.tables.elementAt(i), outData);
        outData.flush();
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
  static String bSO(int i)
  {
    String x=""+(101+i);
    return x.substring(1);
  }
  static void generate(Table table, PrintWriter outData)
  {
    String comma = "( ";
    outData.println("DROP TABLE "+table.name+";");
    outData.println();
    outData.println("CREATE TABLE "+table.name);
    for (int i = 0; i < table.fields.size(); i++, comma = ", ")
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.print(comma+field.name+" "+varType(field));
      if (!field.isNull)
        outData.print(" NOT NULL");
      outData.println();
    }
    outData.println(");");
    outData.println();
    boolean hasAlter = false;
    for (int i=0; i < table.keys.size(); i++)
    {
      Key key = (Key) table.keys.elementAt(i);
      if (!key.isPrimary && !key.isUnique)
        generateIndex(table, key, outData);
      else
        hasAlter = true;
    }
    for (int i=0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        generate(proc, outData);
    }
    if (hasAlter == true)
    {
      String mComma = "  ";
      outData.println("ALTER TABLE "+table.name);
      outData.println("ADD");
      for (int i=0; i < table.keys.size(); i++)
      {
        Key key = (Key) table.keys.elementAt(i);
        if (key.isPrimary)
          generatePrimary(table, key, outData, mComma);
        else if (key.isUnique)
          generateUnique(table, key, outData, mComma);
        mComma = ", ";
      }
      outData.println(";");
      outData.println();
    }
    if (table.links.size() > 0)
    {
      String mComma = " ";
      outData.println("ALTER TABLE "+table.name);
      outData.println("ADD");
      for (int i=0; i < table.links.size(); i++)
      {
        Link link = (Link) table.links.elementAt(i);
        if (link.linkName.length() == 0)
          link.linkName = table.name+"_FK"+bSO(i);
        generate(link, outData, mComma);
        mComma = ", ";
      }
      outData.println(";");
      outData.println();
    }
  }
  /**
  * Generates SQL code for ORACLE Primary Key create
  */
  static void generatePrimary(Table table, Key key, PrintWriter outData, String mcomma)
  {
    String comma = "  ( ";
    String keyname = key.name.toUpperCase();
    if (keyname.indexOf(table.name.toUpperCase()) == -1)
      keyname = table.name.toUpperCase()+"_"+keyname;
    outData.println(mcomma+"CONSTRAINT "+keyname+" PRIMARY KEY");
    for (int i=0; i < key.fields.size(); i++, comma = "  , ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println("  )");
    for (int i=0; i < key.options.size(); i++)
    {
      String option = (String) key.options.elementAt(i);
        outData.println("  "+option);
    }
  }
  /**
  * Generates SQL code for ORACLE Unique Key create
  */
  static void generateUnique(Table table, Key key, PrintWriter outData, String mcomma)
  {
    String comma = "  ( ";
    String keyname = key.name.toUpperCase();
    if (keyname.indexOf(table.name.toUpperCase()) == -1)
      keyname = table.name.toUpperCase()+"_"+keyname;
    outData.println(mcomma+"CONSTRAINT "+keyname+" UNIQUE");
    for (int i=0; i<key.fields.size(); i++, comma = "  , ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println("  )");
    for (int i=0; i < key.options.size(); i++)
    {
      String option = (String) key.options.elementAt(i);
        outData.println("  "+option);
    }
  }
  /**
  * Generates SQL code for ORACLE Index create
  */
  static void generateIndex(Table table, Key key, PrintWriter outData)
  {
    String comma = "( ";
    String keyname = key.name.toUpperCase();
    if (keyname.indexOf(table.name.toUpperCase()) == -1)
      keyname = table.name.toUpperCase()+"_"+keyname;
    outData.println("DROP INDEX "+keyname+";");
    outData.println("");
    outData.println("CREATE INDEX "+keyname+" ON "+table.database.userid+ "" +table.name);
    for (int i=0; i<key.fields.size(); i++, comma = ", ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.print(")");
    for (int i=0; i < key.options.size(); i++)
    {
      outData.println();
      String option = (String) key.options.elementAt(i);
        outData.print(option);
    }
    outData.println(";");
    outData.println();
  }
  /**
  * Generates foreign key SQL Code for Oracle
  */
  static void generate(Link link, PrintWriter outData, String mComma)
  {
    String comma = "  ( ";
    outData.println(mComma+"CONSTRAINT " + link.linkName + " FOREIGN KEY");
    for (int i=0; i < link.fields.size(); i++, comma = "  , ")
    {
      String name = (String) link.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println("  ) REFERENCES "+link.name);
  }
  /**
  * Generates pass through data for Oracle
  */
  static void generate(Proc proc, PrintWriter outData)
  {
    for (int i=0; i < proc.lines.size(); i++)
    {
      //String l = (String) proc.lines.elementAt(i);
      Line line = 	proc.lines.elementAt(i);
      outData.println(line.line);
    }
    outData.println();
  }
  /**
  * Translates field type to Oracle SQL column types
  */
  static String varType(Field field)
  {
    switch(field.type)
    {
    case Field.BYTE:
      return "BYTE";
    case Field.SHORT:
      return "SHORT";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.LONG:
      return "LONG";
    case Field.CHAR:
    case Field.ANSICHAR:
      return "CHAR("+String.valueOf(field.length)+")";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
      return "DATETIME";
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.scale != 0)
        return "DOUBLE("+String.valueOf(field.precision)+", "+String.valueOf(field.scale)+")";
      else if (field.precision != 0)
        return "DOUBLE("+String.valueOf(field.precision)+")";
      return "DOUBLE";
    case Field.BLOB:
      return "LONGBINARY";
    case Field.TLOB:
      return "LONGTEXT";
    case Field.MONEY:
      return "DOUBLE(15,2)";
    case Field.USERSTAMP:
      return "VARCHAR(16)";
    case Field.IDENTITY:
      return "<not supported>";
    }
    return "unknown";
  }
}
