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

public class Access2DDL extends Generator
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
      for (int i = 0; i < args.length; i++)
      {
        outLog.println(args[i] + ": Generate Access 2 DDL");
        in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
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
    return "Generates the SQL for Access Table creation";
  }
  public static String documentation()
  {
    return "Generates the SQL for Access Table creation that is "
          +"easy enough to use through isql or portalview.";
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
        if (database.server.length() > 0)
          outData.println("Server=" + database.server);
        if (database.userid.length() > 0)
          outData.println("UserID=" + database.userid);
        if (database.password.length() > 0)
          outData.println("Password=" + database.password);
        outData.println();
        for (int i = 0; i < database.tables.size(); i++)
          generate((Table) database.tables.elementAt(i), outData);
        outData.flush();
      } finally
      {
        outFile.close();
      }
    } catch (IOException e1)
    {
      outLog.println("Generate Access SQL IO Error");
    }
  }
  /**
   * Kinder bizarre
   */
  static void generate(Table table, PrintWriter outData)
  {
    String comma = "  ";
    outData.println("drop table " + table.name);
    outData.println("go");
    outData.println();
    outData.println("create table " + table.name);
    outData.println("(");
    for (int i = 0; i < table.fields.size(); i++, comma = ", ")
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.print(comma + varType(field));
      if (!field.isNull)
        outData.println(" not null");
    }
    outData.println(")");
    outData.println("go");
    outData.println();
    for (int i = 0; i < table.keys.size(); i++)
    {
      Key key = (Key) table.keys.elementAt(i);
      generate(key, outData, table.name, i);
    }
    for (int i = 0; i < table.links.size(); i++)
    {
      Link link = (Link) table.links.elementAt(i);
      generate(link, outData, table.name, i);
    }
  }
  /**
   * Generates SQL code for Access Index
   */
  static void generate(Key key, PrintWriter outData, String table, int n)
  {
    String comma = "  ";
    if (key.isPrimary)
      outData.println(
        "alter table "
          + table
          + " add constraint "
          + table
          + "_PK"
          + n
          + "  primary key");
    else if (key.isUnique)
      outData.println(
        "alter table " + table + " add constraint unique " + table + "_UK" + n);
    else
      outData.println("create index " + table + "_IX" + n + " on " + table);
    outData.println("(");
    for (int i = 0; i < key.fields.size(); i++, comma = ", ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.println(")");
    outData.println("go");
    outData.println();
  }
  /**
  * Generates foreign key SQL Code appended to table
  */
  static void generate(Link link, PrintWriter outData, String table, int n)
  {
    String comma = "    ";
    outData.println(
      "alter table "
        + table
        + " add constraint "
        + table
        + link.name
        + "_FK"
        + n
        + " foreign key (");
    for (int i = 0; i < link.fields.size(); i++, comma = "   ,")
    {
      String name = (String) link.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.println("  )");
    outData.println("  references " + link.name);
    outData.println("go");
  }
  /**
   * Translates field type to Access SQL column types
   */
  static String varType(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN :
        return field.name + " bit";
      case Field.BYTE :
        return field.name + " byte";
      case Field.SHORT :
        return field.name + " short";
      case Field.INT :
      case Field.LONG :
        return field.name + " long";
      case Field.SEQUENCE :
        return field.name + " counter";
      case Field.IDENTITY :
        return field.name + " counter";
      case Field.CHAR :
      case Field.ANSICHAR :
        if (field.length > 255)
          return field.name + " longtext(" + String.valueOf(field.length) + ")";
        else
          return field.name + " text(" + String.valueOf(field.length) + ")";
      case Field.DATE :
        return field.name + " date";
      case Field.DATETIME :
        return field.name + " datetime";
      case Field.TIME :
        return field.name + " time";
      case Field.TIMESTAMP :
        return field.name + " datetime";
      case Field.FLOAT :
        return field.name + " double";
      case Field.DOUBLE :
        return field.name + " double";
      case Field.BLOB :
        return field.name + " longbinary";
      case Field.TLOB :
        return field.name + " longtext";
      case Field.MONEY :
        return field.name + " currency";
      case Field.USERSTAMP :
        return field.name + " text(8)";
    }
    return "unknown";
  }
}
