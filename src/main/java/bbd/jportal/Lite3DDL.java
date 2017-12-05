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

public class Lite3DDL extends Generator
{
  /**
   * Reads input from stored repository
   */
  public static void main(String[] args)
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i < args.length; i++)
      {
        outLog.println(args[i] + ": generating Lite3 DDL");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
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
    return "Generate Lite3 DDL";
  }
  public static String documentation()
  {
    return "Generate Lite3 DDL.";
  }
  private static String tableOwner;
  /**
   * Generates the SQL for Lite3 Table creation.
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
      if (database.schema.length() > 0) // does not have mutiple schemas - main or temp only
        tableOwner = "main.";
      else
        tableOwner = "";
      outLog.println("DDL: " + output + fileName + ".sql");
      OutputStream outFile = new FileOutputStream(output + fileName + ".sql");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        for (int i = 0; i < database.tables.size(); i++)
          generateTable(database, (Table) database.tables.elementAt(i), outData);
        outData.flush();
      }
      finally
      {
        outFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate LITE3 SQL IO Error");
    }
  }
  /**
   * @param database
   * @param table
   * @param outData
   */
  private static void generateTable(Database database, Table table, PrintWriter outData)
  {
    if (table.fields.size() > 0)
    {
      outData.println("DROP TABLE IF EXISTS " + tableOwner + table.name + ";");
      outData.println();
    }
    String comma = "( ";
    boolean primeDone = false;
    if (table.fields.size() > 0)
    {
      outData.println("CREATE TABLE " + tableOwner + table.name);
      for (int i = 0; i < table.fields.size(); i++, comma = ", ")
      {
        Field field = (Field) table.fields.elementAt(i);
        if (field.isSequence && field.isPrimaryKey)
          primeDone = true;
        outData.println(comma + field.name + " " + varType(field));
        comma = ", ";
      }
      for (int i = 0; i < table.keys.size(); i++)
      {
        Key key = (Key) table.keys.elementAt(i);
        if (key.isPrimary == true && primeDone == false)
          generatePrimary(table, key, outData);
        if (key.isUnique == true)
          generateUnique(table, key, outData);
      }
      if (table.links.size() > 0)
      {
        for (int i = 0; i < table.links.size(); i++)
        {
          Link link = (Link) table.links.elementAt(i);
          if (link.linkName.length() == 0)
            link.linkName = table.name + "_FK" + bSO(i);
          generateLink(link, outData, table.name, tableOwner, i);
        }
      }
      if (table.options.size() > 0)
      {
        for (int i = 0; i < table.options.size(); i++)
        {
          String option = (String) table.options.elementAt(i);
          if (option.toLowerCase().indexOf("constraint") == 0)
            outData.println(", " + option);
        }
      }
      outData.print(")");
      outData.println(";");
      outData.println();
      for (int i = 0; i < table.keys.size(); i++)
      {
        Key key = (Key) table.keys.elementAt(i);
        if (!key.isPrimary && !key.isUnique)
          generateIndex(table, key, outData);
      }
    }
    for (int i = 0; i < table.views.size(); i++)
    {
      View view = (View) table.views.elementAt(i);
      generateView(view, outData, table.name, tableOwner);
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        generateProc(proc, outData);
    }
  }
  /**
   * @param proc
   * @param outData
   */
  private static void generateProc(Proc proc, PrintWriter outData)
  {
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = proc.lines.elementAt(i);
      outData.println(l.line);
    }
    outData.println();
  }
  /**
   * @param table
   * @param key
   * @param outData
   * @param comma
   */
  private static void generateUnique(Table table, Key key, PrintWriter outData)
  {
    String comma = " (";
    outData.print(", UNIQUE");
    for (int i = 0; i < key.fields.size(); i++, comma = ", ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.print(comma + name);
    }
    outData.println(")");
  }
  /**
   * @param table
   * @param key
   * @param outData
   * @param comma
   */
  private static void generatePrimary(Table table, Key key, PrintWriter outData)
  {
    String comma = " (";
    outData.print(", PRIMARY KEY");
    for (int i = 0; i < key.fields.size(); i++, comma = ", ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.print(comma + name);
    }
    outData.println(")");
  }
  /**
   * Generates foreign key SQL Code for DB2
   */
  static void generateLink(Link link, PrintWriter outData,
      String tableName, String owner, int no)
  {
    String comma = "( ";
    String linkname = "FK" + no + link.linkName.toUpperCase();
    outData.println(", CONSTRAINT " + make18(linkname) + " FOREIGN KEY");
    for (int i = 0; i < link.fields.size(); i++, comma = "    , ")
    {
      String name = (String) link.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.print(") REFERENCES " + owner + link.name);
    if (link.linkFields.size() > 0)
    {
      comma = "(";
      for (int i = 0; i < link.linkFields.size(); i++)
      {
        String name = (String) link.linkFields.elementAt(i);
        outData.print(comma + name);
        comma = ", ";
      }
      outData.print(")");
    }
    if (link.isDeleteCascade)
      outData.print(" ON DELETE CASCADE");
    outData.println();
  }
  static String bSO(int i)
  {
    String x = "" + (101 + i);
    return x.substring(1);
  }
  static String make18(String data)
  {
    if (data.length() <= 18)
      return data;
    String x = "_UOIEAY";
    for (int i = 0; i < x.length(); i++)
    {
      char lookup = x.charAt(i);
      int n = data.indexOf(lookup);
      while (n != -1)
      {
        if (n == 0)
          data = data.substring(1);
        else if (n == data.length() - 1)
          data = data.substring(0, n);
        else
          data = data.substring(0, n) + data.substring(n + 1);
        if (data.length() <= 18)
          return data;
        n = data.indexOf(lookup);
      }
    }
    return data.substring(0, 18);
  }
  /**
   * @param view
   * @param outData
   * @param name
   * @param tableOwner
   */
  private static void generateView(View view, PrintWriter outData,
      String tableName, String tableOwner)
  {
    outData.println("DROP VIEW IF EXISTS " + tableOwner + tableName + view.name);
    outData.println("");
    outData.println("CREATE VIEW " + tableOwner + tableName + view.name);
    outData.println("AS (");
    for (int i = 0; i < view.lines.size(); i++)
    {
      String line = (String) view.lines.elementAt(i);
      outData.println(line);
    }
    outData.println(");");
    outData.println();
  }
  /**
   * @param table
   * @param key
   * @param outData
   */
  private static void generateIndex(Table table, Key key, PrintWriter outData)
  {
    String comma = "( ";
    String keyname = key.name.toUpperCase();
    if (keyname.indexOf(table.name.toUpperCase()) == -1)
      keyname = table.name.toUpperCase() + "_" + keyname;
    outData.println("DROP INDEX IF EXISTS " + tableOwner + table.name + keyname + ";");
    outData.println("");
    outData.print(
        "CREATE INDEX " + table.name + keyname + " ON " + tableOwner + table.name);
    for (int i = 0; i < key.fields.size(); i++, comma = ", ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.println(");");
    outData.println();
  }
  /**
   * @param field
   * @return
   */
  private static String varType(Field field)
  {
    // if (field.isNull == true)
    // return "NULL";
    String notNull = (field.isNull == true) ? "" : " NOT NULL";
    String primeKey = "";
    String autoInc = "";
    String defaultValue = "";
      
    if (field.defaultValue.length() > 0)
      defaultValue = " DEFAULT " + field.defaultValue;
    if (field.isSequence && field.isPrimaryKey)
    {
      primeKey = " PRIMARY KEY";
      autoInc = " AUTOINCREMENT";
      defaultValue = "";
    }
    String work = "unknown";
    switch (field.type)
    {
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.IDENTITY:
    case Field.SEQUENCE:
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      work = "INTEGER";
      break;
      case Field.BOOLEAN:
        work = "BOOLEAN";
        break;
    case Field.ANSICHAR:
    case Field.TLOB:
      case Field.XML:
      case Field.BIGXML:
      work = "TEXT";
      break;
      case Field.CHAR:
        work =  " VARCHAR(" + String.valueOf(field.length) + ")";
        break;
      case Field.USERSTAMP:
        work = " VARCHAR(50)";
        break;
      case Field.UID:
        work = " VARCHAR(36)";
        break;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      work = "REAL";
      break;
    case Field.BLOB:
        work = "BLOB";
        break;
      case Field.DATE:
        work = "DATE";
        break;
      case Field.DATETIME:
        work = "DATETIME";
        break;
      case Field.TIME:
        work = "TIME";
        break;
      case Field.TIMESTAMP:
        work = "DATETIME";
      break;
    }
    return work + notNull + defaultValue + primeKey + autoInc;
  }
}
