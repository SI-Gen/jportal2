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

public class IBDDL extends Generator
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
        outLog.println(args[i]+": generating Interbase DDL");
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
  /**
  * Generates the SQL for Interbase Table creation.
  */
  public static String description()
  {
    return "Generate Interbase DDL";
  }
  public static String documentation()
  {
    return "Generate Interbase DDL";
  }
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
        if (database.password.length() > 0)
        {
          outData.println("CONNECT \""+database.server+"\" USER \""+database.userid+"\" PASSWORD \""+database.password+"\";");
          outData.println();
        }
        for (int i=0; i < database.tables.size(); i++)
          generate((Table) database.tables.elementAt(i), outData);
        for (int i=0; i < database.views.size(); i++)
          generate((View) database.views.elementAt(i), outData, "");
        outData.flush();
      }
      finally
      {
        outFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate Interbase SQL IO Error");
    }
  }
  static void generate(Table table, PrintWriter outData)
  {
    int i;
    String comma;
    outData.println("DROP TABLE "+table.name+";");
    outData.println();
    outData.println("CREATE TABLE "+table.name+ " (");
    for (i = 0, comma = "  "; i < table.fields.size(); i++, comma = ", ")
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.print(comma+field.name+" "+varType(field));
      if (!field.isNull)
        outData.println(" NOT NULL");
      else
        outData.println();
    }
    for (i=0; i < table.keys.size(); i++)
    {
      Key key = (Key) table.keys.elementAt(i);
      if (key.isPrimary)
        generatePrimary(key, outData);
      else if (key.isUnique)
        generateUnique(key, outData);
    }
    for (i=0; i < table.links.size(); i++)
    {
      Link link = (Link) table.links.elementAt(i);
      generate(link, outData);
    }
    outData.println(");");
    outData.println();
    for (i=0; i < table.grants.size(); i++)
    {
      Grant grant = (Grant) table.grants.elementAt(i);
      generate(grant, outData, table.name);
    }
    if (table.hasSequence)
    {
      outData.println("CREATE GENERATOR "+table.name+"Seq;");
      outData.println();
      outData.println("SET GENERATOR "+table.name+"Seq TO 1;");
      outData.println();
      outData.println("DROP VIEW "+table.name+"NextSeq;");
      outData.println();
      outData.println("CREATE VIEW "+table.name+"NextSeq (nextNo) AS");
      outData.println("  SELECT GEN_ID("+table.name+"Seq, 1)");
      outData.println("    FROM RDB$GENERATORS");
      outData.println("   WHERE RDB$GENERATOR_NAME = '"+table.name.toUpperCase()+"SEQ';");
      outData.println();
      for (i=0; i < table.grants.size(); i++)
      {
        Grant grant = (Grant) table.grants.elementAt(i);
        generate(grant, outData, table.name+"NextSeq");
      }
    }
    for (i=0; i < table.keys.size(); i++)
    {
      Key key = (Key) table.keys.elementAt(i);
      if (!key.isPrimary && !key.isUnique)
        generateIndex(key, outData, table.name);
    }
    for (i=0; i < table.views.size(); i++)
    {
      View view = (View) table.views.elementAt(i);
      generate(view, outData, table.name);
    }
    outData.println("COMMIT;");
    outData.println();
    for (i=0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        generate(proc, outData);
    }
  }
  /**
  * Generates SQL code for Interbase Index create
  */
  static void generateIndex(Key key, PrintWriter outData, String table)
  {
    int i;
    String comma;
    outData.println("DROP INDEX "+key.name+";");
    outData.println("");
    outData.println("CREATE INDEX "+key.name+" ON "+table+ " (");
    for (i=0, comma = "  "; i<key.fields.size(); i++, comma = ", ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println(");");
    outData.println("");
  }
  /**
  * Generates SQL code for Interbase Primary Key create
  */
  static void generatePrimary(Key key, PrintWriter outData)
  {
    int i;
    String comma;
    outData.println(", PRIMARY KEY (");
    for (i=0, comma = "    "; i < key.fields.size(); i++, comma = "  , ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println("  )");
  }
  /**
  * Generates SQL code for Interbase Unique Key create
  */
  static void generateUnique(Key key, PrintWriter outData)
  {
    int i;
    String comma;
    outData.println(", UNIQUE (");
    for (i=0, comma = "    "; i < key.fields.size(); i++, comma = "  , ")
    {
      String name = (String) key.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println("  )");
  }
  /**
  * Generates foreign key SQL Code for Interbase
  */
  static void generate(Link link, PrintWriter outData)
  {
    int i;
    String comma;
    outData.println(", FOREIGN KEY (");
    for (i=0, comma = "    "; i < link.fields.size(); i++, comma = "   ,")
    {
      String name = (String) link.fields.elementAt(i);
      outData.println(comma+name);
    }
    outData.println("  )");
    outData.println("  REFERENCES "+link.name);
  }
  /**
  * Generates grants for Interbase
  */
  static void generate(Grant grant, PrintWriter outData, String object)
  {
    for (int i=0; i < grant.perms.size(); i++)
    {
      String perm = (String) grant.perms.elementAt(i);
      for (int j=0; j < grant.users.size(); j++)
      {
        String user = (String) grant.users.elementAt(j);
        outData.println("GRANT " + perm + " ON " + object + " TO " + user + ";");
        outData.println();
      }
    }
  }
  static void generate(View view, PrintWriter outData, String tableName)
  {
    outData.println("DROP VIEW "+tableName+view.name+";");
    outData.println();
    outData.println("CREATE VIEW "+tableName+view.name);
    outData.println("(");
    int i;
    String comma;
    for (i=0, comma = "  "; i < view.aliases.size(); i++)
    {
      String alias = (String) view.aliases.elementAt(i);
      outData.println(comma+alias);
      comma = ", ";
    }
    outData.println(") AS");
    outData.println("(");
    for (i=0; i < view.lines.size(); i++)
    {
      String line = (String) view.lines.elementAt(i);
      outData.println(line);
    }
    outData.println(");");
    outData.println();
    for (i=0; i < view.users.size(); i++)
    {
      String user = (String) view.users.elementAt(i);
      outData.println("GRANT SELECT ON "+tableName+view.name+" TO "+user+";");
    }
    outData.println();
  }
  /**
  * Generates pass through data for Interbase
  */
  static void generate(Proc proc, PrintWriter outData)
  {
    for (int i=0; i < proc.lines.size(); i++)
    {
      Line l = proc.lines.elementAt(i);
      outData.println(l.line);
    }
    outData.println();
  }
  /**
  * Translates field type to InterBase SQL column types
  */
  static String varType(Field field)
  {
    switch(field.type)
    {
    case Field.BYTE:
    case Field.SHORT:
      return "SHORTINT";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.LONG:
      return "INTEGER";
    case Field.CHAR:
      return "VARCHAR("+String.valueOf(field.length)+")";
    case Field.ANSICHAR:
      return "CHAR("+String.valueOf(field.length)+")";
    case Field.DATE:
    case Field.DATETIME:
    case Field.TIME:
    case Field.TIMESTAMP:
      return "DATE";
    case Field.FLOAT:
    case Field.DOUBLE:
      return "DOUBLE";
    case Field.BLOB:
    case Field.TLOB:
      return "BLOB";
    case Field.MONEY:
      return "NUMERIC(15,2)";
    case Field.USERSTAMP:
      return "VARCHAR(50)";
    case Field.IDENTITY:
      return "<not supported>";
    }
    return "unknown";
  }
}
