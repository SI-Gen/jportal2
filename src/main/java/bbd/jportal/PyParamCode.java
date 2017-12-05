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
import java.lang.*;
import java.util.Vector;

public class PyParamCode extends Generator
{
  public static void main(String args[])
  {
    try 
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i <args.length; i++) 
      {
        outLog.println(args[i]+": Generate Param Python Code");
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
    return "Generates Param Python Code";
  }
  public static String documentation()
  {
    return "Generates Param Python Code";
  }
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length-1; i++)
      s = s + " ";
    return s + " ";
  }
  static boolean hasParms(Database database)
  {
    for (int i=0; i<database.tables.size(); i++) 
    {
      Table table = (Table) database.tables.elementAt(i);
      for (int j=0; j<table.parameters.size(); j++)
      {
        Parameter param = (Parameter) table.parameters.elementAt(j);
        if (param.reader == null && param.cache == null)
          continue;
        return true;
      }
    }
    return false;
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    if (hasParms(database) == false)
      return;
    try 
    {
      outLog.println("Code: "+output+database.output+".py");
      OutputStream outFile = new FileOutputStream(output+database.output+".py");
      try 
      {
        PrintWriter outData = new PrintWriter(outFile);
        try 
        {
          output(database, outData);
          int count;
          for (int i=0; i<database.tables.size(); i++) 
          {
            Table table = (Table) database.tables.elementAt(i);
            count = 0;
            for (int j=0; j<table.parameters.size(); j++)
            {
              Parameter param = (Parameter) table.parameters.elementAt(j);
              if (param.reader == null && param.cache == null)
                continue;
              count++;
            }
            if (count > 0)
              generate(table, outData);
          }
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
  static void generate(Table table, PrintWriter outData)
  {
    output(table, outData);
    for (int i=0; i<table.parameters.size(); i++)
    {
      Parameter parameter = (Parameter) table.parameters.elementAt(i);
      if (parameter.reader == null && parameter.cache == null)
        continue;
      output(parameter, outData);
    }
    outData.flush();
  }
  static void output(Parameter parameter, PrintWriter outData)
  {
    if (parameter.reader != null || parameter.cache != null)
    {
      outData.println("parameter = pp.Parameter()");
      outData.println("table.parameters.append(parameter)");
      out(outData, string("parameter.title", parameter.title));
      out(outData, string("parameter.isViewOnly", parameter.isViewOnly));
      if (parameter.cache != null)
        output(parameter.cache, outData, "parameter.cache");
      if (parameter.reader != null)
        output(parameter.reader, outData, "parameter.reader");
      if (parameter.insert != null)
        output(parameter.insert, outData, "parameter.insert");
      if (parameter.update != null)
        output(parameter.update, outData, "parameter.update");
      if (parameter.delete != null)
        output(parameter.delete, outData, "parameter.delete");
      for (int i = 0; i < parameter.shows.size(); i++)
        output((Field) parameter.shows.elementAt(i), outData, "parameter.shows");
      for (int i = 0; i < parameter.supplied.size(); i++)
        output((Field) parameter.supplied.elementAt(i), outData, "parameter.supplied");
      for (int i = 0; i < parameter.cacheExtras.size(); i++)
        outData.println("parameter.cacheExtras.append('"+(String)parameter.cacheExtras.elementAt(i)+"')");
    }
  }
  static void output(Proc proc, PrintWriter outData, String owner)
  {
    outData.println("proc = pp.Proc()");
    outData.println(owner+" = proc");
    out(outData, string("proc.name", proc.name));
    out(outData, string("proc.isSingle", proc.isSingle));
    out(outData, string("proc.isStd", proc.isStd));
    out(outData, string("proc.useStd", proc.useStd));
    out(outData, string("proc.useKey", proc.useKey));
    out(outData, string("proc.isInsert", proc.isInsert));
    out(outData, string("proc.isSProc", proc.isSProc));
    out(outData, string("proc.isStdExtended", proc.isStdExtended()));
    out(outData, string("proc.extendsStd", proc.extendsStd));
    out(outData, string("proc.isMultipleInput", proc.isMultipleInput));
    for (int i = 0; i < proc.inputs.size(); i++)
      output((Field) proc.inputs.elementAt(i), outData, "proc.inputs");
    for (int i = 0; i < proc.outputs.size(); i++)
      output((Field) proc.outputs.elementAt(i), outData, "proc.outputs");
  }
  static void output(Enum entry, PrintWriter outData)
  {
    outData.println("enum = pp.Enum()");
    outData.println("field.enums.append(enum)");
    out(outData, string("enum.name", entry.name));
    out(outData, string("enum.value", entry.value));
  }
  static void output(Field field, PrintWriter outData, String owner)
  {
    outData.println("field = pp.Field()");
    outData.println(owner+".append(field)");
    out(outData, string("field.name", field.useName()));
    outData.println("field.type =" + fieldType(field.type));
    outData.println("field.length = ("+field.length+", "+field.precision+", "+field.scale+")");
    out(outData, string("field.isPrimaryKey", field.isPrimaryKey));
    out(outData, string("field.isSequence", field.isSequence));
    out(outData, string("field.isNull", field.isNull));
    for (int i = 0; i < field.enums.size(); i++)
      output((Enum) field.enums.elementAt(i), outData);
  }
  static void output(Database database, PrintWriter outData)
  {
    outData.println("import pyparam as pp");
    outData.println();
    outData.println("database = pp.Database()");
    outData.println("database.name = "+string(database.name));
    outData.println("database.packageName = "+string(database.packageName));
    if (database.views.size() > 0)
      for (int i=0; i<database.views.size();i++)
        output((View) database.views.elementAt(i), "database", outData);
  }
  static void output(View view, String owner, PrintWriter outData)
  {
    outData.println("view = pp.View()");
    outData.println(owner+".views.append(view)");
    out(outData, string("view.name", view.name));
    if (view.aliases.size() > 0)
    {
      outData.print  ("view.aliases = ");
      outputStrings(view.aliases, outData);
    }
  }
  static void output(Link link, PrintWriter outData)
  {
    outData.println("link = pp.Link()");
    outData.println("table.links.append(link)");
    out(outData, string("link.name", link.name));
    out(outData, string("link.linkName", link.linkName));
    if (link.fields.size() > 0)
    {
      outData.print  ("link.fields = ");
      outputStrings(link.fields, outData);
    }
  }
  static void output(Key key, PrintWriter outData)
  {
    outData.println("key = pp.Key()");
    outData.println("table.keys.append(key)");
    out(outData, string("key.name", key.name));
    out(outData, string("key.isPrimary", key.isPrimary));
    out(outData, string("key.isUnique", key.isUnique));
    if (key.fields.size() > 0)
    {
      outData.print("key.fields = ");
      outputStrings(key.fields, outData);
    }
    if (key.options.size() > 0)
    {
      outData.print("key.options = ");
      outputStrings(key.options, outData);
    }
  }
  static void output(Table table, PrintWriter outData)
  {
    outData.println("table = pp.Table()");
    outData.println("database.tables.append(table)");
    outData.println("table.name = "+string(table.name));
    outData.println("table.alias = "+string(table.alias));
    for (int i = 0; i < table.fields.size(); i++)
      output((Field) table.fields.elementAt(i), outData, "table.fields");
    for (int i = 0; i < table.keys.size(); i++)
      output((Key) table.keys.elementAt(i), outData);
    for (int i = 0; i < table.links.size(); i++)
      output((Link) table.links.elementAt(i), outData);
    for (int i=0; i<table.views.size();i++)
      output((View) table.views.elementAt(i), "table", outData);
  }
  static String string(String data)
  {
    return "'"+data+"'";
  }
  public static String string(String var, String value, boolean trip)
  {
    return var + " = escape(''' " + value + " ''')";
  }
  public static String string(String var, String value)
  {
    return var + " = '" + value + "'";
  }
  public static String string(String var, boolean value)
  {
    if (value == true)
      return var + " = True";
    return var + " = False";
  }
  public static String string(String var, int value)
  {
    if (value != 0)
      return var + " = " + value;
    return var + " = None";
  }
  static String lowerFirst(String data)
  {
    String x = data.substring(0,1);
    return x.toLowerCase()+data.substring(1);
  }
  public static void out(PrintWriter outData, String data)
  {
    outData.println(data);
  }
  static void outputStrings(Vector<String> strings, PrintWriter outData)
  {
    outData.println("pp.strings('''");
    for (int i=0; i<strings.size(); i++)
    {
      String string = (String) strings.elementAt(i);
      if (string.charAt(0) == '"')
        outData.println(string.substring(1,string.length()-2));
      else 
        outData.println(string);
    }
    outData.println("''')");
  }
  static String fieldType(byte type)
  {
    String as = "UNKNOWN";
    switch (type)
    {
      case Field.BLOB: as = "BLOB";break;
      case Field.BOOLEAN: as = "BOOLEAN";break;
      case Field.BYTE: as = "BYTE";break;
      case Field.CHAR: as = "CHAR";break;
      case Field.DATE: as = "DATE";break;
      case Field.DATETIME: as = "DATETIME";break;
      case Field.DOUBLE: as = "DOUBLE";break;
      case Field.DYNAMIC: as = "DYNAMIC";break;
      case Field.FLOAT: as = "FLOAT";break;
      case Field.IDENTITY: as = "IDENTITY";break;
      case Field.INT: as = "INT";break;
      case Field.LONG: as = "LONG";break;
      case Field.MONEY: as = "MONEY";break;
      case Field.SEQUENCE: as = "SEQUENCE";break;
      case Field.SHORT: as = "SHORT";break;
      case Field.STATUS: as = "STATUS";break;
      case Field.TIME: as = "TIME";break;
      case Field.TIMESTAMP: as = "TIMESTAMP";break;
      case Field.TLOB: as = "TLOB";break;
      case Field.USERSTAMP: as = "USERSTAMP";break;
      case Field.ANSICHAR: as = "ANSICHAR";break;
      case Field.UID: as = "UID";break;
      case Field.BIGSEQUENCE: as = "BIGSEQUENCE"; break;
      case Field.BIGIDENTITY: as = "BIGIDENTITY"; break;
    }
    return "(pp." + as+", "+type + ")";
  }
}
