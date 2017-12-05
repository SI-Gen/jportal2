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
import java.util.Vector;

public class JavaJCCode
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
        outLog.println(args[i]+": generate Java code for jdbc and crackle consumption");
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
   * Generates the procedure classes for each table present.
   */
  public static String description()
  {
    return "generate Java code for jdbc and crackle consumption - separates structs and others from main";
  }
  public static String documentation()
  {
    return "generate Java code for jdbc and crackle consumption - separates structs and others from main";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generateStructs(table, output, outLog);
      generate(table, output, outLog);
    }
  }
  static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    generateStdProcStruct(table, output, outLog);
    generateOtherProcStructs(table, output, outLog);
  }
  static void generateStdProcStruct(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + "Struct.java");
      OutputStream outFile = new FileOutputStream(output+table.useName() + "Struct.java");
      try
      {
        outData = new PrintWriter(outFile);
        if (table.database.packageName.length() > 0)
        {
          outData.println("package " + table.database.packageName + ";");
          outData.println();
        }
        outData.println("import java.io.Serializable;");
        outData.println("import java.sql.*;");
        outData.println();
        outData.println("/**");
        for (int i=0; i < table.comments.size(); i++)
        {
          String s = (String) table.comments.elementAt(i);
          outData.println(" *"+s);
        }
        outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
        outData.println(" * Does not use inner public classes and separates structs out.");
        outData.println(" */");
        outData.println("public class "+table.useName()+"Struct implements Serializable");
        outData.println("{");
        for (int i=0; i<table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);
          if (field.comments.size() > 0)
          {
            outData.println("  /**");
            for (int c=0; c < field.comments.size(); c++)
            {
              String s = (String) field.comments.elementAt(c);
              outData.println("   *"+s);
            }
            outData.println("   */");
          }
          outData.println("  public "+javaVar(field)+";");
          outData.println("  public "+getterSetter(field));
        }
        outData.println("  public "+table.useName()+"Struct()");
        outData.println("  {");
        int maxSize = 0;
        for (int i=0; i<table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);
          if (field.useName().length() > maxSize)
            maxSize = field.useName().length();
          outData.println("    "+initJavaVar(field));
        }
        outData.println("  }");
        outData.println("  public String toString()");
        outData.println("  {");
        outData.println("    String CRLF = (String) System.getProperty(\"line.separator\");");
        for (int i=0; i<table.fields.size(); i++)
        {
          if (i==0)
            outData.print("    return ");
          else
            outData.print("         + ");
          Field field = (Field) table.fields.elementAt(i);
          int no = maxSize - field.useName().length();
          outData.println("\"  "+field.useName()+padded(no+1)+": \" + "+field.useName()+" + CRLF");
        }
        outData.println("    ;");
        outData.println("  }");
        outData.println("}");
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
  static void generateOtherProcStructs(Table table, String output, PrintWriter outLog)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.extendsStd || (!proc.isStd && !proc.hasNoData()))
        generateOtherProcStruct(table, proc, output, outLog);
    }
  } 
  static void generateOtherProcStruct(Table table, Proc proc, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName()+proc.upperFirst()+"Struct.java");
      OutputStream outFile = new FileOutputStream(output+table.useName()+proc.upperFirst()+"Struct.java");
      try
      {
        outData2 = new PrintWriter(outFile);
        if (table.database.packageName.length() > 0)
        {
          outData2.println("package " + table.database.packageName + ";");
          outData2.println();
        }
        outData2.println("import java.io.Serializable;");
        outData2.println("import java.sql.*;");
        outData2.println();
        outData2.println("/**");
        for (int j=0; j<proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData2.println(" *"+comment);
        }
        outData2.println(" */");
        if (proc.extendsStd)
          outData2.println("public class "+table.useName()+proc.upperFirst()+"Struct extends " + table.useName()+"Struct");
        else
          outData2.println("public class "+table.useName()+proc.upperFirst()+"Struct implements Serializable");

        outData2.println("{");
        int maxSize = 0;
        for (int t=0; t<table.fields.size(); t++)
        {
          Field field = (Field) table.fields.elementAt(t);
          if (field.useName().length() > maxSize)
            maxSize = field.useName().length();
        }

        for (int j=0; j<proc.inputs.size(); j++)
        {          
          Field field = (Field) proc.inputs.elementAt(j);
          
          //Skip if the field is in the standard proc
          if (proc.extendsStd && table.hasField(field.name))
            continue;

          if (field.useName().length() > maxSize)
            maxSize = field.useName().length();
          outData2.println("  /**");
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData2.println("   *"+s);
          }
          if (!proc.hasOutput(field.name))
            outData2.println("   * (input)");
          else
            outData2.println("   * (input/output)");
          outData2.println("   */");
          outData2.println("  public "+javaVar(field)+";");
          outData2.println("  public "+getterSetter(field));
        }
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);

          //Skip if the field is in the standard proc
          if (proc.extendsStd && table.hasField(field.name))
            continue;

          if (field.useName().length() > maxSize)
            maxSize = field.useName().length();
          if (!proc.hasInput(field.name))
          {
            outData2.println("  /**");
            for (int c=0; c < field.comments.size(); c++)
            {
              String s = (String) field.comments.elementAt(c);
              outData2.println("   *"+s);
            }
            outData2.println("   * (output)");
            outData2.println("   */");
            outData2.println("  public "+javaVar(field)+";");
            outData2.println("  public "+getterSetter(field));
          }
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          if (s.length() > maxSize)
            maxSize = s.length();
          outData2.println("  /**");
          outData2.println("   * (dynamic)");
          outData2.println("   */");
          outData2.println("  public String "+s+";");
        }
        outData2.println("  public "+table.useName()+proc.upperFirst()+"Struct()");
        outData2.println("  {");
        if (proc.extendsStd)
          outData2.println("    "+"super();");

        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          //Skip if the field is in the standard proc
          if (proc.extendsStd && table.hasField(field.name))
            continue;

          outData2.println("    "+initJavaVar(field));
        }
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          //Skip if the field is in the standard proc
          if (proc.extendsStd && table.hasField(field.name))
            continue;

          if (!proc.hasInput(field.name))
            outData2.println("    "+initJavaVar(field));
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData2.println("    "+s+" = \"\";");
        }
        outData2.println("  }");
        outData2.println("  public String toString()");
        outData2.println("  {");
        outData2.println("    String CRLF = (String) System.getProperty(\"line.separator\");");
        String ret = "    return ";
        if (proc.extendsStd)
        {
          ret += "super.toString() + CRLF";
          outData2.println(ret);
          ret = "         + ";
        }

        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          //Skip if the field is in the standard proc
          if (proc.extendsStd && table.hasField(field.name))
            continue;

          outData2.print(ret);
          ret = "         + ";
          int no = maxSize - field.useName().length();
          outData2.println("\"  "+field.useName()+padded(no+1)+": \" + "+field.useName()+" + CRLF");
        }
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          if (!proc.hasInput(field.name))
          {
            outData2.print(ret);
            ret = "         + ";
            int no = maxSize - field.useName().length();
            outData2.println("\"  "+field.useName()+padded(no+1)+": \" + "+field.useName()+" + CRLF");
          }
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData2.print(ret);
          ret = "         + ";
          int no = maxSize - s.length();
          outData2.println("\"  "+s+padded(no+1)+": \" + "+s+" + CRLF");
        }
        outData2.println("    ;");
        outData2.println("  }");
        outData2.println("}");
        outData2.flush();
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
  /**
   * Build of standard and user defined procedures
   */
  static void generate(Table table, String output, PrintWriter outLog)
  {
    generateStdProcs(table, output, outLog);
    generateOtherProcs(table, output, outLog);
  }
  /**
   * Build of all required standard procedures
   */
  static private String extendsName;
  static void generateStdProcs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + ".java");
      OutputStream outFile = new FileOutputStream(output+table.useName() + ".java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        if (table.database.packageName.length() > 0)
        {
          outData.println("package " + table.database.packageName + ";");
          outData.println("");
        }
        outData.println("import bbd.jportal.*;");
        outData.println("import java.sql.*;");
        outData.println("import java.util.*;");
        outData.println("");
        outData.println("/**");
        for (int i=0; i < table.comments.size(); i++)
        {
          String s = (String) table.comments.elementAt(i);
          outData.println(" *"+s);
        }
        outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
        outData.println(" */");
        extendsName = table.useName()+"Struct";
        outData.println("public class "+table.useName()+" extends "+extendsName);
        outData.println("{");
        outData.println("  Connector connector;");
        outData.println("  Connection connection;");
        outData.println("  /**");
        outData.println("   * @param Connector for specific database");
        outData.println("   */");
        outData.println("  public "+table.useName()+"()");
        outData.println("  {");
        outData.println("    super();");
        outData.println("  }");
        outData.println("  public void setConnector(Connector conn)");
        outData.println("  {");
        outData.println("    this.connector = conn;");
        outData.println("    connection = connector.connection;");
        outData.println("  }");

        outData.println("  public "+table.useName()+"(Connector connector)");
        outData.println("  {");
        outData.println("    super();");
        outData.println("    this.connector = connector;");
        outData.println("    connection = connector.connection;");
        outData.println("  }");
        for (int i=0; i<table.procs.size(); i++)
        {
          Proc proc = (Proc) table.procs.elementAt(i);
          if (proc.isData)
            continue;
          if (proc.isStd)
            emitProc(proc, outData);
          else if (proc.hasNoData())
            emitStaticProc(proc, outData);
        }
        outData.println("}");
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
  /**
   * Build of user defined procedures
   */
  static void generateOtherProcs(Table table, String output, PrintWriter outLog)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (!proc.isStd && !proc.hasNoData())
        generateOtherProc(table, proc, output, outLog);
    }
  }
  static void generateOtherProc(Table table, Proc proc, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName()+proc.upperFirst()+".java");
      OutputStream outFile = new FileOutputStream(output+table.useName()+proc.upperFirst()+".java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        if (table.database.packageName.length() > 0)
        {
          outData.println("package " + table.database.packageName + ";");
          outData.println("");
        }
        outData.println("import bbd.jportal.*;");
        outData.println("import java.sql.*;");
        outData.println("import java.util.*;");
        outData.println("");
        outData.println("/**");
        for (int j=0; j<proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println(" *"+comment);
        }
        outData.println(" */");
        extendsName = table.useName()+proc.upperFirst()+"Struct";
        outData.println("public class "+table.useName()+proc.upperFirst()+" extends "+extendsName);
        outData.println("{");
        outData.println("  Connector connector;");
        outData.println("  Connection connection;");

        outData.println("  public "+table.useName()+proc.upperFirst()+"()");
        outData.println("  {");
        outData.println("    super();");
        outData.println("  }");

        outData.println("  public void setConnector(Connector conn)");
        outData.println("  {");
        outData.println("    this.connector = conn;");
        outData.println("    connection = connector.connection;");
        outData.println("  }");

        outData.println("  public "+table.useName()+proc.upperFirst()+"(Connector connector)");
        outData.println("  {");
        outData.println("    super();");
        outData.println("    this.connector = connector;");
        outData.println("    connection = connector.connection;");
        outData.println("  }");
        emitProc(proc, outData);
        outData.println("}");
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
  /**
   *
   */
  static PlaceHolder placeHolders;
  /**
   * Emits a static or class method
   */
  static void emitStaticProc(Proc proc, PrintWriter outData)
  {
    outData.println("  /**");
    outData.println("   * class method as it has no input or output.");
    outData.println("   * @exception SQLException is passed through");
    outData.println("   */");
    outData.println("  public static void "+proc.lowerFirst()+"(Connector connector) throws SQLException");
    outData.println("  {");
    placeHolders = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    Vector<?> lines = placeHolders.getLines();
  	outData.println("    String statement = ");
    String plus = "    ";
    for (int i=0; i<lines.size(); i++)
    {
      outData.println(plus+(String)lines.elementAt(i));
      plus = "    +";
    }
 	  outData.println("    ;");
    outData.println("    PreparedStatement prep = connector.connection.prepareStatement(statement);");
    outData.println("    prep.executeUpdate();");
    outData.println("    prep.close();");
    outData.println("  }");
  }
  /** Emits class method for processing the database activity */
  static void emitProc(Proc proc, PrintWriter outData)
  {              //  12345
    boolean isBulkInsert = false;
    boolean isBulkUpdate = false;
    
    /** FIXME: The Proc class has no way of identifying a bulk update or insert, so these comparisons are required. */
    if (proc.name.toUpperCase().equals("BULKINSERT")) {
      isBulkInsert = true;
      outData.println("  protected List<"+proc.table.useName()+"Struct> inserts = new ArrayList<>();");
      outData.println("  public void setInserts(List<"+proc.table.useName()+"Struct> inserts) { this.inserts = inserts; }");
      outData.println("  public void addForInsert("+proc.table.useName()+"Struct struct) { inserts.add(struct); }");
    } else if (proc.name.toUpperCase().equals("BULKUPDATE")) {
      isBulkUpdate = true;
      outData.println("  protected List<"+proc.table.useName()+"Struct> updates = new ArrayList<>();");
      outData.println("  public void setUpdates(List<"+proc.table.useName()+"Struct> updates) { this.updates = updates; }");
      outData.println("  public void addForUpdate("+proc.table.useName()+"Struct struct) { updates.add(struct); }");
    }

    outData.println("  /**");
    if (proc.comments.size() > 0)
    {
      for (int i=0; i<proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("    *"+comment);
      }
    }
    if (isBulkInsert || isBulkUpdate) {
      outData.println("   * Returns a list of update counts for every operation performed.");
    } else if (!proc.extendsStd && proc.outputs.size() == 0)
      outData.println("   * Returns no output.");
    else if (proc.isSingle)
    {
      outData.println("   * Returns at most one record.");
      outData.println("   * @return true if a record is found");
    }
    else
    {
      outData.println("   * Returns any number of records.");
      outData.println("   * @return result set of records found");
    }
    outData.println("   * @exception SQLException is passed through");
    outData.println("   */");
    String procName = proc.lowerFirst();
    if (isBulkInsert || isBulkUpdate) {
      outData.println("  public int[] "+procName+"() throws SQLException");
    } else if (!proc.extendsStd && proc.outputs.size() == 0)
      outData.println("  public void "+procName+"() throws SQLException");
    else if (proc.isSingle)
      outData.println("  public boolean "+procName+"() throws SQLException");
    else
      outData.println("  public Query "+procName+"() throws SQLException");
    outData.println("  {");
		placeHolders = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
		Vector<?> lines = placeHolders.getLines();

    Field primaryKeyField = null;
    for (int i = 0; i < proc.table.fields.size() && primaryKeyField == null; i++) {
        Field fEval = proc.table.fields.get(i);
        if (fEval.isPrimaryKey)
            primaryKeyField = fEval;
    }    

    if (proc.hasReturning)
        outData.println("Connector.Returning _ret = connector.getReturning(\""+proc.table.name+"\",\""+ primaryKeyField.useName()+"\");");

		outData.println("    String statement = ");
    String plus = "      ";
    for (int i=0; i<lines.size(); i++)
    {
      outData.println(plus+(String)lines.elementAt(i));
      plus = "    + ";
    }
		outData.println("    ;");
    outData.println("    PreparedStatement prep = connection.prepareStatement(statement);");

    if (isBulkInsert) {
      outData.println("    for ("+proc.table.useName()+"Struct struct : inserts) {");
    } else if (isBulkUpdate) {
      outData.println("    for ("+proc.table.useName()+"Struct struct : updates) {");
    }

    for (int i=0; i<proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      if (proc.isInsert) {
        if (field.type == Field.BIGSEQUENCE) {
          if (isBulkInsert || isBulkUpdate) {
            outData.println("      struct."+field.useName()+" = connector.getBigSequence(\""+proc.table.name+"\");");
          } else {
            outData.println("    "+field.useName()+" = connector.getBigSequence(\""+proc.table.name+"\");");
         }
        } else if (field.type == Field.SEQUENCE) {
          if (isBulkInsert || isBulkUpdate) {
            outData.println("      struct."+field.useName()+" = connector.getSequence(\""+proc.table.name+"\");");      
          } else {
            outData.println("    "+field.useName()+" = connector.getSequence(\""+proc.table.name+"\");");                  
          }
        }
      }
      if (field.type == Field.TIMESTAMP) {
        if (isBulkInsert || isBulkUpdate) {
          outData.println("      struct."+field.useName()+" = connector.getTimestamp();");
        } else {
          outData.println("    "+field.useName()+" = connector.getTimestamp();");
        }
      }
      if (field.type == Field.USERSTAMP) {
        if (isBulkInsert || isBulkUpdate) {
          outData.println("      struct."+field.useName()+" = connector.getUserstamp();");
        } else {
          outData.println("    "+field.useName()+" = connector.getUserstamp();");
        }
      }
    }


    Vector<?> pairs = placeHolders.getPairs();
    for (int i=0; i<pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs) pairs.elementAt(i);
      Field field = pair.field;

      if (isBulkInsert || isBulkUpdate) {
        outData.print("      prep.set");
      } else {
        outData.print("    prep.set");
      }
      outData.print(setType(field));
      outData.print("(");
      outData.print(i+1);

      if (isBulkInsert || isBulkUpdate) {
        outData.println(", struct."+field.useName()+");");
      } else {
        outData.println(", "+field.useName()+");");
      }
    }
    
    if (isBulkInsert || isBulkUpdate) {
      outData.println("      prep.addBatch();");
      outData.println("    }");
    }

    if (isBulkInsert || isBulkUpdate) {
      outData.println("    int updateCounts[] = prep.executeBatch();");
      outData.println("    prep.close();");
      outData.println("    return updateCounts;");
    } else if (proc.extendsStd || proc.outputs.size() > 0) {
      outData.println("    ResultSet result = prep.executeQuery();");
      if (!proc.isSingle)
      {
        outData.println("    Query query = new Query(prep, result);");
        outData.println("    return query;");
        outData.println("  }");
        outData.println("  /**");
        outData.println("   * Returns the next record in a result set.");
        outData.println("   * @param result The result set for the query.");
        outData.println("   * @return true while records are found.");
        outData.println("   * @exception SQLException is passed through");
        outData.println("   */");
        outData.println("  public boolean "+procName+"(Query query) throws SQLException");
        outData.println("  {");
        outData.println("    if (!query.result.next())");
        outData.println("    {");
        outData.println("      query.close();");
        outData.println("      return false;");
        outData.println("    }");
        outData.println("    ResultSet result = query.result;");
      }
      else
      {
        outData.println("    if (!result.next())");
        outData.println("    {");
        outData.println("      result.close();");
        outData.println("      prep.close();");
        outData.println("      return false;");
        outData.println("    }");
      }
      if (proc.extendsStd)
      {
        for (int i=0; i<proc.table.fields.size(); i++)
        {
          Field field = (Field) proc.table.fields.elementAt(i);
          outData.print("    "+field.useName()+" =  result.get");
          outData.print(setType(field));
          outData.print("(");
          outData.print(i+1);
          outData.println(");");
        }        
      }
      for (int i=0; i<proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        outData.print("    "+field.useName()+" =  result.get");
        outData.print(setType(field));
        outData.print("(");
        outData.print(i+1);
        outData.println(");");
      }
      if (proc.isSingle)
      {
        outData.println("    result.close();");
        outData.println("    prep.close();");
      }
      outData.println("    return true;");
    }
    else
    {
      outData.println("    prep.executeUpdate();");
      outData.println("    prep.close();");
    }
    outData.println("  }");
    
    if (isBulkInsert || isBulkUpdate) {
      // Skip the generation of a secondary function.
      return;
    }

    if ((proc.extendsStd || (proc.outputs.size() > 0)) && !proc.isSingle)
    {
      outData.println("  /**");
      outData.println("   * Returns all the records in a result set as array of "+extendsName+ "");
      outData.println("   * @return array of "+extendsName+ "");
      outData.println("   * @exception SQLException is passed through");
      outData.println("   */");
      outData.println("  public "+extendsName+"[] "+procName+"Load() throws SQLException");
      outData.println("  {");
      outData.println("    Vector recs = new Vector();");
      outData.println("    Query query = "+procName+"();");
      outData.println("    while ("+procName+"(query) == true)");
      outData.println("    {");
      outData.println("      "+extendsName+" rec = new "+extendsName+"();");
      if (proc.extendsStd)
      {
        for (int i=0; i<proc.table.fields.size(); i++)
        {
          Field field = (Field) proc.table.fields.elementAt(i);
          outData.println("      rec."+field.useName()+" = "+field.useName()+";");
        }        
      }      
      for (int i=0; i<proc.outputs.size(); i++)
      {
        Field field = (Field) proc.outputs.elementAt(i);
        outData.println("      rec."+field.useName()+" = "+field.useName()+";");
      }
      outData.println("      recs.addElement(rec);");
      outData.println("    }");
      outData.println("    "+extendsName+"[] result = new "+extendsName+"[recs.size()];");
      outData.println("    for (int i=0; i<recs.size();i++)");
      outData.println("      result[i] = ("+extendsName+")recs.elementAt(i); ");
      outData.println("    return result;");
      outData.println("  }");
    }
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
    {
      outData.println("  /**");
      if (!proc.extendsStd && proc.outputs.size() == 0)
        outData.println("   * Returns no records.");
      else if (proc.isSingle)
      {
        outData.println("   * Returns at most one record.");
        outData.println("   * @return true if a record is returned.");
      }
      else
      {
        outData.println("   * Returns any number of records.");
        outData.println("   * @return result set of records found");
      }
      // if (proc.extendsStd)
      // {
      //   for (int i=0; i<proc.table.fields.size(); i++)
      //   {
      //     Field field = (Field) proc.table.fields.elementAt(i);
      //     if ((field.isSequence && proc.isInsert)
      //       || (field.type == Field.TIMESTAMP)
      //       || (field.type == Field.USERSTAMP))
      //       continue;
      //     if (!field.isPrimaryKey)
      //       continue;
      //     outData.println("   * @param "+field.useName()+" key input.");
      //   }
      // }      
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (!field.isPrimaryKey)
          continue;
        outData.println("   * @param "+field.useName()+" key input.");
      }
      // if (proc.extendsStd)
      // {      
      //   for (int i=0; i<proc.table.fields.size(); i++)
      //   {
      //     Field field = (Field) proc.table.fields.elementAt(i);
      //     if ((field.isSequence && proc.isInsert)
      //       || (field.type == Field.TIMESTAMP)
      //       || (field.type == Field.USERSTAMP))
      //       continue;
      //     if (field.isPrimaryKey)
      //       continue;
      //     outData.println("   * @param "+field.useName()+" input.");
      //   }      
      // }
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (field.isPrimaryKey)
          continue;
        outData.println("   * @param "+field.useName()+" input.");
      }
      for (int i=0; i<proc.dynamics.size(); i++)
        outData.println("   * @param "+proc.name+" dynamic input.");
      outData.println("   * @exception SQLException is passed through");
      outData.println("   */");
      if (!proc.extendsStd && proc.outputs.size() == 0)
        outData.println("  public void "+procName+"(");
      else if (proc.isSingle)
        outData.println("  public boolean "+procName+"(");
      else
        outData.println("  public Query "+procName+"(");
      String comma = "    ";
      // if (proc.extendsStd)
      // {
      //   for (int i=0; i<proc.table.fields.size(); i++)
      //   {
      //     Field field = (Field) proc.table.fields.elementAt(i);
      //     if ((field.isSequence && proc.isInsert)
      //       || (field.type == Field.TIMESTAMP)
      //       || (field.type == Field.USERSTAMP))
      //       continue;
      //     if (!field.isPrimaryKey)
      //       continue;
      //     outData.println(comma+javaVar(field));
      //     comma = "  , ";
      //   }
      //   for (int i=0; i<proc.table.fields.size(); i++)
      //   {
      //     Field field = (Field) proc.table.fields.elementAt(i);
      //     if ((field.isSequence && proc.isInsert)
      //       || (field.type == Field.TIMESTAMP)
      //       || (field.type == Field.USERSTAMP))
      //       continue;
      //     if (field.isPrimaryKey)
      //       continue;
      //     outData.println(comma+javaVar(field));
      //     comma = "  , ";
      //   }        
      // }
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (!field.isPrimaryKey)
          continue;
        outData.println(comma+javaVar(field));
        comma = "  , ";
      }
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        if (field.isPrimaryKey)
          continue;
        outData.println(comma+javaVar(field));
        comma = "  , ";
      }
      for (int i=0; i<proc.dynamics.size(); i++)
      {
        String name = (String) proc.dynamics.elementAt(i);
        outData.println(comma+"String "+name);
        comma = "  , ";
      }
      outData.println("  ) throws SQLException");
      outData.println("  {");
      for (int i=0; i<proc.inputs.size(); i++)
      {
        Field field = (Field) proc.inputs.elementAt(i);
        if ((field.isSequence && proc.isInsert)
          || (field.type == Field.TIMESTAMP)
          || (field.type == Field.USERSTAMP))
          continue;
        String usename = field.useName();
        outData.println("    this."+usename+" = "+usename+";");
      }
      for (int i=0; i<proc.dynamics.size(); i++)
      {
        String name = (String) proc.dynamics.elementAt(i);
        outData.println("    this."+name+" = "+name+";");
      }
      if (proc.extendsStd || proc.outputs.size() > 0)
        outData.println("    return "+procName+"();");
      else
        outData.println("    "+procName+"();");
      outData.println("  }");
    }
  }
  /**
   * Translates field type to java data member type
   */
  static String javaVar(Field field)
  {
    switch(field.type)
    {
    case Field.BYTE:
      return "byte "+ field.useName();
    case Field.SHORT:
      return "short "+ field.useName();
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int "+ field.useName();
    case Field.LONG:
    case Field.BIGSEQUENCE:
    case Field.BIGIDENTITY:    
      return "long "+ field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "String "+ field.useName();
    case Field.DATE:
      return "java.sql.Date "+ field.useName();
    case Field.DATETIME:
      return "Timestamp "+ field.useName();
    case Field.TIME:
      return "Time "+ field.useName();
    case Field.TIMESTAMP:
      return "Timestamp "+ field.useName();
    case Field.FLOAT:
    case Field.DOUBLE:
      return "double "+ field.useName();
    case Field.BLOB:
    case Field.TLOB:
      return "String "+ field.useName();
    case Field.MONEY:
      return "double "+ field.useName();
    case Field.USERSTAMP:
      return "String "+ field.useName();
    }
    return "unknown";
  }
  /**
   * Translates field type to java data member type
   */
  static String getterSetter(Field field)
  {
    String type = null;
    switch(field.type)
    {
      case Field.BYTE:
        type = "byte ";
        break;
      case Field.SHORT:
        type = "short ";
        break;
      case Field.BIGSEQUENCE:
        type = "long ";
        break;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        type = "int ";
        break;
      case Field.LONG:
        type = "long ";
        break;
      case Field.CHAR:
      case Field.ANSICHAR:
        type = "String ";
        break;
      case Field.DATE:
        type = "java.sql.Date ";
        break;
      case Field.DATETIME:
        type = "Timestamp ";
        break;
      case Field.TIME:
        type = "Time ";
        break;
      case Field.TIMESTAMP:
        type = "Timestamp ";
        break;
      case Field.FLOAT:
      case Field.DOUBLE:
        type = "double ";
        break;
      case Field.BLOB:
      case Field.TLOB:
        type = "String ";
        break;
      case Field.MONEY:
        type = "double ";
        break;
      case Field.USERSTAMP:
        type = "String ";
        break;
    }
    if(type == null){
      return "unknown";

    }else{
      return type+ "get"+ field.useName() +"(){ return "+field.useName()+"; } \n  public void set"+field.useName() +"(" +type +" "+field.useName() +"){ this."+field.useName() +" = "+field.useName() +"; }\n";

    }
  }
  /**
   * returns the data member initialisation code (not always neccessary in java but
   * still we do it)
   */
  static String initJavaVar(Field field)
  {
    switch(field.type)
    {
    case Field.BYTE:
      return field.useName() +" = 0;";
    case Field.CHAR:
    case Field.ANSICHAR:
      return field.useName() +" = \"\";";
    case Field.DATE:
      return field.useName() +" = new Date(0);";
    case Field.DATETIME:
      return field.useName() +" = new Timestamp(0);";
    case Field.FLOAT:
    case Field.DOUBLE:
      return field.useName() +" = 0.0;";
    case Field.BLOB:
    case Field.TLOB:
      return field.useName() +" = \"\";";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() +" = 0;";
    case Field.LONG:
    case Field.BIGSEQUENCE:
    case Field.BIGIDENTITY:        
      return field.useName() +" = 0;";
    case Field.MONEY:
      return field.useName() +" = 0.0;";
    case Field.SHORT:
      return field.useName() +" = 0;";
    case Field.TIME:
      return field.useName() +" = new Time(0);";
    case Field.TIMESTAMP:
      return field.useName() +" = new Timestamp(0);";
    case Field.USERSTAMP:
      return field.useName() +" = \"\";";
    }
    return "unknown";
  }
  /**
   * JDBC get and set type for field data transfers
   */
  static String setType(Field field)
  {
    switch(field.type)
    {
    case Field.BYTE:
      return "Byte";
    case Field.CHAR:
    case Field.ANSICHAR:
      return "String";
    case Field.DATE:
      return "Date";
    case Field.DATETIME:
      return "Timestamp";
    case Field.FLOAT:
    case Field.DOUBLE:
      return "Double";
    case Field.BLOB:
    case Field.TLOB:
      return "String";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "Int";
    case Field.LONG:
    case Field.BIGSEQUENCE:
    case Field.BIGIDENTITY:        
      return "Long";
    case Field.MONEY:
      return "Double";
    case Field.SHORT:
      return "Short";
    case Field.TIME:
      return "Time";
    case Field.TIMESTAMP:
      return "Timestamp";
    case Field.USERSTAMP:
      return "String";
    }
    return "unknown";
  }
static String padString = "                                                         ";
private static PrintWriter outData;
private static PrintWriter outData2; 
  private static String padded(int size)
  {
    if (size == 0)
      return "";
    if (size > padString.length())
      size = padString.length();
    return padString.substring(0, size);
  }
}
