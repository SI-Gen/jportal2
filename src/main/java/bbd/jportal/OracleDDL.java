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
///    Dieter Rosch
/// ------------------------------------------------------------------

package bbd.jportal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class OracleDDL extends Generator
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
        outLog.println(args[i] + ": generating Oracle DDL");
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
  * Generates the SQL for ORACLE Table creation.
  */
  public static String description()
  {
    return "Generate Oracle DDL";
  }
  public static String documentation()
  {
    return "Generate Oracle DDL.";
  }
  private static boolean addExit = false;
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    try
    {
      String tableOwner = "";
      String fileName;
      for (String flag : database.flags)
      {
    	if (flag.equals("addExit") == true)
    		addExit = true;
      }
      if (addExit == false)
      	outLog.println("-- No addExit flag detected.");
      if (database.output.length() > 0)
        fileName = database.output;
      else
        fileName = database.name;
      outLog.println("DDL: " + output + fileName + ".sql");
      OutputStream outFile = new FileOutputStream(output + fileName + ".sql");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        if (database.schema.length() > 0)
          tableOwner = database.schema + "";
        else if (database.userid.length() > 0)
          tableOwner = database.userid + "";
        if (database.password.length() > 0)
        {
          if (addExit == false)
        	outLog.println("-- An exit added because a CONNECT is generated.");
          outData.println("CONNECT " + database.userid + "/" + database.password + "@" + database.server);
          outData.println();
          addExit = true;
        }
        for (int i = 0; i < database.tables.size(); i++)
          generateTable(database.tables.elementAt(i), outData);
        for (int i = 0; i < database.views.size(); i++)
          generateView(database.views.elementAt(i), outData, "", tableOwner);
        for (int i = 0; i < database.sequences.size(); i++)
          generateSequence(database.sequences.elementAt(i), outData, tableOwner);
        if (addExit == true)
        {
          outData.println("exit");
          outData.println();
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
      outLog.println("Generate Oracle SQL IO Error");
    }
  }
  static String bSO(int i)
  {
    String x = "" + (101 + i);
    return x.substring(1);
  }
  static void generateTable(Table table, PrintWriter outData)
  {
    String tableOwner = "";
    if (table.database.userid.length() > 0)
      tableOwner = table.database.userid + "";
    String comma = "( ";
    boolean hasNotNull = false;
    boolean bigSequence = false;
    if (table.fields.size() > 0)
    {
      outData.println("DROP TABLE " + tableOwner + table.name + " CASCADE CONSTRAINTS;");
      outData.println();
      outData.println("CREATE TABLE " + tableOwner + table.name);
      for (int i = 0; i < table.fields.size(); i++, comma = ", ")
      {
        Field field = (Field)table.fields.elementAt(i);
        outData.println(comma + field.name + " " + varType(field));
        if (field.defaultValue.length() > 0)
          hasNotNull = true;
        if (field.checkValue.length() > 0)
          hasNotNull = true;
        else if (!field.isNull)
          hasNotNull = true;
        if (field.type == Field.BIGSEQUENCE)
          bigSequence = true;	
      }
      outData.print(")");
      if (table.options.size() > 0)
      {
        for (int i = 0; i < table.options.size(); i++)
        {
          String option = table.options.elementAt(i);
          if (option.toUpperCase().startsWith("TABLESPACE"))
          {
            outData.println();
            outData.print(option);
          }
        }
      }
      outData.println(";");
      outData.println();
      outData.println("DROP PUBLIC SYNONYM " + table.name + ";");
      outData.println();
      outData.println("CREATE PUBLIC SYNONYM " + table.name + " FOR " + tableOwner + table.name + ";");
      outData.println();
      for (int i = 0; i < table.grants.size(); i++)
      {
        Grant grant = table.grants.elementAt(i);
        generateGrant(grant, outData, tableOwner + table.name);
      }
      if (table.hasSequence)
      {
        outData.println("DROP SEQUENCE " + tableOwner + table.name + "Seq;");
        outData.println();
        outData.println("CREATE SEQUENCE " + tableOwner + table.name + "Seq");
        outData.println("  MINVALUE 1");
        if (bigSequence == true)
          outData.println("  MAXVALUE 999999999999999999");
        else
          outData.println("  MAXVALUE 999999999");
        outData.println("  CYCLE");
        outData.println("  ORDER;");
        outData.println();
        outData.println("DROP PUBLIC SYNONYM " + table.name + "SEQ;");
        outData.println();
        outData.println("CREATE PUBLIC SYNONYM " + table.name + "SEQ FOR " + tableOwner + table.name + "SEQ;");
        outData.println();
        if (table.grants.size() > 0)
        {
          Grant grant = table.grants.elementAt(0);
          for (int j = 0; j < grant.users.size(); j++)
          {
            String user = grant.users.elementAt(j);
            outData.println("GRANT SELECT ON " + tableOwner + table.name + "SEQ TO " + user + ";");
            outData.println();
          }
        }
      }
      for (int i = 0; i < table.keys.size(); i++)
      {
        Key key = table.keys.elementAt(i);
        if (!key.isPrimary && !key.isUnique)
          generateIndex(table, key, outData);
      }
    }
    for (int i = 0; i < table.views.size(); i++)
    {
      View view = (View)table.views.elementAt(i);
      generateView(view, outData, table.name, tableOwner);
    }
    if (hasNotNull == true)
    {
      outData.println("ALTER TABLE " + tableOwner + table.name);
      outData.println("MODIFY");
      comma = "( ";
      for (int i = 0; i < table.fields.size(); i++, comma = ", ")
      {
        Field field = table.fields.elementAt(i);
        if (field.isNull && field.defaultValue.length() == 0 && field.checkValue.length() == 0)
          continue;
        outData.print(comma + field.name + " CONSTRAINT " + table.name + "_NN" + bSO(i));
        if (field.defaultValue.length() > 0)
          outData.print(" DEFAULT " + field.defaultValue);
        if (field.checkValue.length() > 0)
          outData.print(" CHECK (" + field.checkValue + ")");
        else
          outData.print(" NOT NULL");
        outData.println();
      }
      outData.println(");");
      outData.println();
    }
    if (table.keys.size() > 0)
    {
      String mComma = "( ";
      outData.println("ALTER TABLE " + tableOwner + table.name);
      outData.println("ADD");
      for (int i = 0; i < table.keys.size(); i++)
      {
        Key key = table.keys.elementAt(i);
        if (key.isPrimary)
          generatePrimary(table, key, outData, mComma);
        else if (key.isUnique)
          generateUnique(table, key, outData, mComma);
        mComma = ", ";
      }
      outData.println(");");
      outData.println();
    }
    if (table.links.size() > 0)
    {
      String mComma = "( ";
      outData.println("ALTER TABLE " + tableOwner + table.name);
      outData.println("ADD");
      for (int i = 0; i < table.links.size(); i++)
      {
        Link link = table.links.elementAt(i);
        if (link.linkName.length() == 0)
          link.linkName = table.name + "_FK" + bSO(i);
        generateLink(link, outData, mComma);
        mComma = ", ";
      }
      outData.println(");");
      outData.println();
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = table.procs.elementAt(i);
      if (proc.isData)
        generateData(proc, outData);
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
      keyname = table.name.toUpperCase() + "_" + keyname;
    outData.println(mcomma + "CONSTRAINT " + keyname + " PRIMARY KEY");
    for (int i = 0; i < key.fields.size(); i++, comma = "  , ")
    {
      String name = (String)key.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.println("  )");
    for (int i = 0; i < key.options.size(); i++)
    {
      String option = (String)key.options.elementAt(i);
      if (option.toUpperCase().startsWith("TABLESPACE") || option.toUpperCase().startsWith("USING INDEX"))
        outData.println("  " + option);
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
      keyname = table.name.toUpperCase() + "_" + keyname;
    outData.println(mcomma + "CONSTRAINT " + keyname + " UNIQUE");
    for (int i = 0; i < key.fields.size(); i++, comma = "  , ")
    {
      String name = (String)key.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.println("  )");
    for (int i = 0; i < key.options.size(); i++)
    {
      String option = (String)key.options.elementAt(i);
      if (option.toUpperCase().startsWith("TABLESPACE") || option.toUpperCase().startsWith("USING INDEX"))
          outData.println("  " + option);
    }
  }
  /**
  * Generates SQL code for ORACLE Index create
  */
  static void generateIndex(Table table, Key key, PrintWriter outData)
  {
    String tableOwner = "";
    if (table.database.userid.length() > 0)
      tableOwner = table.database.userid + "";
    String comma = "( ";
    String keyname = key.name.toUpperCase();
    if (keyname.indexOf(table.name.toUpperCase()) == -1)
      keyname = table.name.toUpperCase() + "_" + keyname;
    outData.println("DROP INDEX " + keyname + ";");
    outData.println("");
    outData.println("CREATE INDEX " + keyname + " ON " + tableOwner + table.name);
    for (int i = 0; i < key.fields.size(); i++, comma = ", ")
    {
      String name = (String)key.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.print(")");
    for (int i = 0; i < key.options.size(); i++)
    {
      outData.println();
      String option = (String)key.options.elementAt(i);
      if (option.toUpperCase().startsWith("TABLESPACE") || option.toUpperCase().startsWith("USING INDEX"))
        outData.print(option);
    }
    outData.println(";");
    outData.println();
  }
  /**
  * Generates foreign key SQL Code for Oracle
  */
  static void generateLink(Link link, PrintWriter outData, String mComma)
  {
    String comma = "  ( ";
    outData.println(mComma + "CONSTRAINT " + link.linkName + " FOREIGN KEY");
    for (int i = 0; i < link.fields.size(); i++, comma = "  , ")
    {
      String name = (String)link.fields.elementAt(i);
      outData.println(comma + name);
    }
    outData.println("  ) REFERENCES " + link.name);
    if (link.linkFields.size() > 0)
    {
      comma = "(";
      for (int i = 0; i < link.linkFields.size(); i++)
      {
        String name = (String)link.linkFields.elementAt(i);
        outData.print(comma + name);
        comma = ", ";
      }
      outData.print(")");
    }
    if (link.isDeleteCascade)
      outData.print(" ON DELETE CASCADE");
    outData.println(";");
    outData.println();
  }
  /**
  * Generates grants for Oracle
  */
  static void generateGrant(Grant grant, PrintWriter outData, String object)
  {
    for (int i = 0; i < grant.perms.size(); i++)
    {
      String perm = (String)grant.perms.elementAt(i);
      for (int j = 0; j < grant.users.size(); j++)
      {
        String user = (String)grant.users.elementAt(j);
        outData.println("GRANT " + perm + " ON " + object + " TO " + user + ";");
        outData.println();
      }
    }
  }
  /**
  * Generates views for Oracle
  */
  static void generateView(View view, PrintWriter outData, String tableName, String tableOwner)
  {
    outData.println("CREATE OR REPLACE FORCE VIEW " + tableName + view.name);
    String comma = "( ";
    for (int i = 0; i < view.aliases.size(); i++)
    {
      String alias = (String)view.aliases.elementAt(i);
      outData.println(comma + alias);
      comma = ", ";
    }
    outData.println(") AS");
    outData.println("(");
    for (int i = 0; i < view.lines.size(); i++)
    {
      String line = (String)view.lines.elementAt(i);
      outData.println(line);
    }
    outData.println(");");
    outData.println();
    for (int i = 0; i < view.users.size(); i++)
    {
      String user = (String)view.users.elementAt(i);
      outData.println("GRANT SELECT ON " + tableName + view.name + " TO " + user + ";");
    }
    outData.println();
    outData.println("DROP PUBLIC SYNONYM " + tableName + view.name + ";");
    outData.println();
    outData.println("CREATE PUBLIC SYNONYM " + tableName + view.name + " FOR " + tableOwner + tableName + view.name + ";");
    outData.println();
  }
  /**
  * Generates pass through data for Oracle
  */
  static void generateData(Proc proc, PrintWriter outData)
  {
    for (int i = 0; i < proc.lines.size(); i++)
    {
      String l = proc.lines.elementAt(i).line;
      if (l.toUpperCase().trim().startsWith("START"))
        continue;
      outData.println(l);
    }
    outData.println();
  }
  /**
  * Generates pass through data for Oracle
  */
  static void generateSequence(Sequence sequence, PrintWriter outData, String tableOwner)
  {
    outData.println("DROP SEQUENCE " + sequence.name + ";");
    outData.println();
    outData.println("CREATE SEQUENCE " + sequence.name);
    outData.println("  MINVALUE  " + sequence.minValue);
    outData.println("  MAXVALUE  " + sequence.maxValue);
    outData.println("  INCREMENT BY " + sequence.increment);
    if (sequence.cycleFlag)
      outData.println("  CYCLE");
    if (sequence.orderFlag)
      outData.println("  ORDER");
    outData.println("  START WITH " + sequence.startWith + ";");
    outData.println();
    outData.println("DROP PUBLIC SYNONYM " + sequence.name + ";");
    outData.println();
    outData.println("CREATE PUBLIC SYNONYM " + sequence.name + " FOR " + tableOwner + sequence.name + ";");
    outData.println();
  }
  /**
  * Translates field type to Oracle SQL column types
  */
  static String varType(Field field)
  {
    switch (field.type)
    {
      case Field.BYTE:
        return "NUMBER(3)";
      case Field.SHORT:
        return "NUMBER(5)";
      case Field.INT:
      case Field.SEQUENCE:
        return "NUMBER(10)";
      case Field.LONG:
      case Field.BIGSEQUENCE:
        return "NUMBER(18)";
      case Field.CHAR:
        return "VARCHAR2(" + String.valueOf(field.length) + ")";
      case Field.ANSICHAR:
        return "CHAR(" + String.valueOf(field.length) + ")";
      case Field.DATE:
        return "DATE";
      case Field.DATETIME:
        return "DATE";
      case Field.TIME:
        return "DATE";
      case Field.TIMESTAMP:
        return "DATE";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.scale != 0)
          return "NUMBER(" + String.valueOf(field.precision) + ", " + String.valueOf(field.scale) + ")";
        else if (field.precision != 0)
          return "NUMBER(" + String.valueOf(field.precision) + ")";
        return "NUMBER";
      case Field.BLOB:
        return "BLOB";
      case Field.TLOB:
        return "CLOB";
      case Field.MONEY:
        return "NUMBER(15,2)";
      case Field.USERSTAMP:
        return "VARCHAR2(" + String.valueOf(field.length) + ")";
      case Field.IDENTITY:
        return "<not supported>";
    }
    return "unknown";
  }
}
