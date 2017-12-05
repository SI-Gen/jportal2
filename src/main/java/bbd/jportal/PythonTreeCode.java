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

/**
 * @author vince
 */
public class PythonTreeCode extends Generator
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
        outLog.println(args[i] + ": Generate Python Tree Code");
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
    return "Generates Python Tree Code";
  }
  public static String documentation()
  {
    return "Generates Python Tree Code";
  }
  public static String stringTrip(String var, String value, boolean trip)
  {
    return var + " = _escape(''' " + value + " ''')";
  }
  public static String string(String var, String value)
  {
    if (value.length() > 0 && value.charAt(0) == '\'')
      return var + " = \"" + value + "\"";
    if (value.length() > 0)
      return var + " = '" + value + "'";
    return var + " = ''";
  }
  public static String string(String var, boolean value)
  {
    if (value == true)
      return var + " = True";
    return var + " = False";
  }
  public static String string(String var, int value)
  {
    return var + " = " + value;
  }
  public static String string(String var, long value)
  {
    if (value <= 999999999)
      return var + " = " + value;
    return var + " = long(" + value + ")";
  }
  public static void out(PrintWriter outData, String data)
  {
    if (data.length() > 0)
      outData.println(data);
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    database = database.doImports(outLog);
    try
    {
      outLog.println("Code: " + output + database.output + ".py");
      OutputStream outFile = new FileOutputStream(output + database.output + ".py");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("# This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("# " + database.output + ".py");
          outData.println("class _class: pass");
          outData.println("def _strings(data):");
          outData.println("  return data.strip().splitlines() if isinstance(data, str) else []");
          outData.println("def _integers(data):");
          outData.println("  return data.strip().splitlines() if isinstance(data, str) else []");
          outData.println("def _escape(data):");
          outData.println("  return data[1:-1] if isinstance(data, str) and len(data) > 1 else ''");
          outData.println("database = _db = _class()");
          out(outData, string("_db.name", database.name));
          out(outData, string("_db.output", database.output));
          out(outData, string("_db.server", database.server));
          out(outData, string("_db.userid", database.userid));
          out(outData, string("_db.password", database.password));
          out(outData, string("_db.packageName", database.packageName));
          if (database.flags.size() > 0)
          {
            outData.print("_db.flags = ");
            generateString(database.flags, outData, outLog);
          }
          else
            outData.println("_db.flags = ''");
          if (database.imports.size() > 0)
          {
            outData.print("_db.imports = ");
            generateString(database.imports, outData, outLog);
          }
          else
            outData.println("_db.imports = ''");
          outData.println("_db.tables = []");
          for (int i = 0; i < database.tables.size(); i++)
          {
            Table table = (Table) database.tables.elementAt(i);
            generateTable(table, outData, outLog);
            //outData.println();
            outData.println(lowerFirst(table.useName()) + " = _tb");
            outData.println("_db.tables.append(_tb)");
          }
          outData.println("_db.views = []");
          for (int i = 0; i < database.views.size(); i++)
          {
            View view = (View) database.views.elementAt(i);
            generateView(view, outData, outLog);
            //outData.println();
            outData.println("_db.views.append(_vw)");
          }
          outData.println("_db.sequences = []");
          for (int i = 0; i < database.sequences.size(); i++)
          {
            Sequence sequence = (Sequence) database.sequences.elementAt(i);
            generateSequence(sequence, outData, outLog);
            //outData.println();
            outData.println("_db.sequences.append(_sq)");
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
  static void generateTable(Table table, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_tb = _class()");
    out(outData, string("_tb.name", table.name));
    out(outData, string("_tb.alias", table.alias));
    out(outData, string("_tb.check", table.check));
    out(outData, string("_tb.hasPrimaryKey", table.hasPrimaryKey));
    out(outData, string("_tb.hasSequence", table.hasSequence));
    out(outData, string("_tb.hasTimeStamp", table.hasTimeStamp));
    out(outData, string("_tb.hasUserStamp", table.hasUserStamp));
    out(outData, string("_tb.hasExecute", table.hasExecute));
    out(outData, string("_tb.hasSelect", table.hasSelect));
    out(outData, string("_tb.hasInsert", table.hasInsert));
    out(outData, string("_tb.hasDelete", table.hasDelete));
    out(outData, string("_tb.hasUpdate", table.hasUpdate));
    out(outData, string("_tb.hasStdProcs", table.hasStdProcs));
    out(outData, string("_tb.hasIdentity", table.hasIdentity));
    out(outData, string("_tb.start", table.start));
    if (table.comments.size() > 0)
    {
      outData.print("_tb.comments = ");
      generateString(table.comments, outData, outLog);
    }
    else
      outData.println("_tb.comments = ''");
    if (table.options.size() > 0)
    {
      outData.print("_tb.options = ");
      generateString(table.options, outData, outLog);
    }
    else
      outData.println("_tb.options = ''");
    if (table.allUsers.size() > 0)
    {
      outData.print("_tb.allUsers = ");
      generateString(table.allUsers, outData, outLog);
    }
    else
      outData.println("_tb.allUsers = ''");
    outData.println("_tb.fields = []");
    for (int i = 0; i < table.fields.size(); i++)
    {
      generateField((Field) table.fields.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_tb.fields.append(_fd)");
    }
    outData.println("_tb.keys = []");
    for (int i = 0; i < table.keys.size(); i++)
    {
      generateKey((Key) table.keys.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_tb.keys.append(_ky)");
    }
    outData.println("_tb.links = []");
    for (int i = 0; i < table.links.size(); i++)
    {
      generateLink((Link) table.links.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_tb.links.append(_ln)");
    }
    outData.println("_tb.grants = []");
    for (int i = 0; i < table.grants.size(); i++)
    {
      generateGrant((Grant) table.grants.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_tb.grants.append(_gr)");
    }
    outData.println("_tb.views = []");
    for (int i = 0; i < table.views.size(); i++)
    {
      generateView((View) table.views.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_tb.views.append(_vw)");
    }
    outData.println("_tb.procs = []");
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData == true)
        continue;
      generateProc(proc, outData, outLog);
      //outData.println();
      outData.println(lowerFirst(table.useName()) + proc.name + " = _pr");
      outData.println("_tb.procs.append(_pr)");
    }
  }
  static String lowerFirst(String value)
  {
    return value.substring(0, 1).toLowerCase() + value.substring(1);
  }
  static void generateProc(Proc proc, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_pr = _class()");
    out(outData, string("_pr.name", proc.name));
    out(outData, string("_pr.noRows", proc.noRows));
    out(outData, string("_pr.isData", proc.isData));
    out(outData, string("_pr.isSql", proc.isSql));
    out(outData, string("_pr.isSingle", proc.isSingle));
    out(outData, string("_pr.isStd", proc.isStd));
    out(outData, string("_pr.useStd", proc.useStd));
    out(outData, string("_pr.useKey", proc.useKey));
    out(outData, string("_pr.hasImage", proc.hasImage));
    out(outData, string("_pr.isInsert", proc.isInsert));
    out(outData, string("_pr.isSProc", proc.isSProc));
    out(outData, string("_pr.isMultipleInput", proc.isMultipleInput));
    out(outData, string("_pr.hasReturning", proc.hasReturning));
    out(outData, string("_pr.start", proc.start));
    if (proc.comments.size() > 0)
    {
      outData.print("_pr.comments = ");
      generateString(proc.comments, outData, outLog);
    }
    else
      outData.println("_pr.comments = ''");
    if (proc.options.size() > 0)
    {
      outData.print("_pr.options = ");
      generateString(proc.options, outData, outLog);
    }
    else
      outData.println("_pr.options = ''");
    outData.println("_pr.inputs = []");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      generateField((Field) proc.inputs.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_pr.inputs.append(_fd)");
    }
    outData.println("_pr.outputs = []");
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      generateField((Field) proc.outputs.elementAt(i), outData, outLog);
      //outData.println();
      outData.println("_pr.outputs.append(_fd)");
    }
    PlaceHolder holder = new PlaceHolder(proc, PlaceHolder.COLON, "&");
    Vector<String> lines = holder.getLines();
    if (lines.size() > 0)
    {
      outData.print("_pr.lines = ");
      generateString(lines, outData, outLog);
    }
    else
      outData.println("_pr.lines = ''");
    if (proc.dynamics.size() > 0)
    {
      outData.print("_pr.dynamics = ");
      generateString(proc.dynamics, outData, outLog);
    }
    else
      outData.println("_pr.dynamics = ''");
    if (proc.dynamicSizes.size() > 0)
    {
      outData.print("_pr.dynamicSizes = ");
      generateInteger(proc.dynamicSizes, outData, outLog);
    }
    else
      outData.println("_pr.dynamicSizes = ''");
  }
  static String fieldType(byte type)
  {
    switch (type)
    {
      case Field.BIGIDENTITY:
        return "BIGIDENTITY";
      case Field.BIGSEQUENCE:
        return "BIGSEQUENCE";
      case Field.BLOB:
        return "BLOB";
      case Field.BOOLEAN:
        return "BOOLEAN";
      case Field.BYTE:
        return "BYTE";
      case Field.CHAR:
        return "CHAR";
      case Field.DATE:
        return "DATE";
      case Field.DATETIME:
        return "DATETIME";
      case Field.DOUBLE:
        return "DOUBLE";
      case Field.DYNAMIC:
        return "DYNAMIC";
      case Field.FLOAT:
        return "FLOAT";
      case Field.IDENTITY:
        return "IDENTITY";
      case Field.INT:
        return "INT";
      case Field.LONG:
        return "LONG";
      case Field.MONEY:
        return "MONEY";
      case Field.SEQUENCE:
        return "_sq";
      case Field.SHORT:
        return "SHORT";
      case Field.STATUS:
        return "STATUS";
      case Field.TIME:
        return "TIME";
      case Field.TIMESTAMP:
        return "TIMESTAMP";
      case Field.TLOB:
        return "TLOB";
      case Field.USERSTAMP:
        return "USERSTAMP";
      case Field.ANSICHAR:
        return "ANSICHAR";
      case Field.UID:
        return "UID";
      case Field.XML:
        return "XML";
    }
    return "0";
  }
  static void generateField(Field field, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_fd = _class()");
    out(outData, string("_fd.name", field.name));
    out(outData, string("_fd.alias", field.alias));
    out(outData, stringTrip("_fd.checkValue", field.checkValue, true));
    out(outData, stringTrip("_fd.defaultValue", field.defaultValue, true));
    out(outData, string("_fd.enumLink", field.enumLink));
    out(outData, string("_fd.type", fieldType(field.type)));
    out(outData, string("_fd.length", field.length));
    out(outData, string("_fd.precision", field.precision));
    out(outData, string("_fd.scale", field.scale));
    out(outData, string("_fd.bindPos", field.bindPos));
    out(outData, string("_fd.definePos", field.definePos));
    out(outData, string("_fd.isPrimaryKey", field.isPrimaryKey));
    out(outData, string("_fd.isSequence", field.isSequence));
    out(outData, string("_fd.isNull", field.isNull));
    out(outData, string("_fd.isIn", field.isIn));
    out(outData, string("_fd.isOut", field.isOut));
    if (field.comments.size() > 0)
    {
      outData.print("_fd.comments = ");
      generateString(field.comments, outData, outLog);
    }
    else
      outData.println("_fd.comments = ''");
    outData.println("_fd.enums = []");
    for (int i = 0; i < field.enums.size(); i++)
    {
      generateEnum((Enum) field.enums.elementAt(i), outData, outLog);
      outData.println("_fd.enums.append(_en)");
    }
    outData.println("_fd.valueList = []");
    for (int i = 0; i < field.valueList.size(); i++)
    {
      out(outData, string("_val", (String)field.valueList.elementAt(i)));
      outData.println("_fd.valueList.append(_val)");
    }
  }
  static void generateLine(Line line, PrintWriter outData, PrintWriter outLog)
  {
    if (line.line.trim().length() > 0)
      outData.println((line.isVar ? "&" : "") + line.line);
  }
  static void generateEnum(Enum entry, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_en = _class()");
    out(outData, string("_en.name", entry.name));
    out(outData, string("_en.value", entry.value));
  }
  static void generateGrant(Grant grant, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_gr = _class()");
    if (grant.perms.size() > 0)
    {
      outData.print("_gr.perms = ");
      generateString(grant.perms, outData, outLog);
    }
    else
      outData.println("_gr.perms = ''");
    if (grant.users.size() > 0)
    {
      outData.print("_gr.users = ");
      generateString(grant.users, outData, outLog);
    }
    else
      outData.println("_gr.users = ''");
  }
  static void generateKey(Key key, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_ky = _class()");
    out(outData, string("_ky.name", key.name));
    out(outData, string("_ky.isPrimary", key.isPrimary));
    out(outData, string("_ky.isUnique", key.isUnique));
    if (key.fields.size() > 0)
    {
      outData.print("_ky.fields = ");
      generateString(key.fields, outData, outLog);
    }
    else
      outData.println("_ky.fields = ''");
    if (key.options.size() > 0)
    {
      outData.print("_ky.options = ");
      generateString(key.options, outData, outLog);
    }
    else
      outData.println("_ky.options = ''");
  }
  static void generateLink(Link link, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_ln = _class()");
    out(outData, string("_ln.name", link.name));
    out(outData, string("_ln.linkName", link.linkName));
    if (link.fields.size() > 0)
    {
      outData.print("_ln.fields = ");
      generateString(link.fields, outData, outLog);
    }
    else
      outData.println("_ln.fields = ");
  }
  static void generateFlag(Flag flag, PrintWriter outData, PrintWriter outLog)
  {
    outData.println("_fg = _class()");
    out(outData, string("_fg.name", flag.name));
    out(outData, string("_fg.value", toBoolean(flag.value)));
    outData.println("_fg.description = '" + flag.description + "'");
  }
  static void generateString(Vector<String> allUsers, PrintWriter outData, PrintWriter outLog)
  {
    outData.println("_strings('''\\");
    for (int i = 0; i < allUsers.size(); i++)
    {
      String string = (String) allUsers.elementAt(i);
      if (string.length() > 2 && string.charAt(0) == '"')
        outData.println(string.substring(1, string.length() - 1));
      else
        outData.println(string);
    }
    outData.println("''')");
  }
  static void generateInteger(Vector<Integer> integers, PrintWriter outData, PrintWriter outLog)
  {
    outData.println("_integers('''\\");
    for (int i = 0; i < integers.size(); i++)
    {
      Integer integer = (Integer) integers.elementAt(i);
      outData.println(integer.intValue());
    }
    outData.println("''')");
  }
  static void generateSequence(Sequence sequence, PrintWriter outData, PrintWriter outLog)
  {
    outData.println("_sq = _class()");
    out(outData, string("_sq.name", sequence.name));
    out(outData, string("_sq.minValue", sequence.minValue));
    out(outData, string("_sq.maxValue", sequence.maxValue));
    out(outData, string("_sq.increment", sequence.increment));
    out(outData, string("_sq.cycleFlag", sequence.cycleFlag));
    out(outData, string("_sq.orderFlag", sequence.orderFlag));
    out(outData, string("_sq.startWith", sequence.startWith));
    out(outData, string("_sq.start", sequence.start));
  }
  static void generateView(View view, PrintWriter outData, PrintWriter outLog)
  {
    //outData.println();
    outData.println("_vw = _class()");
    out(outData, string("_vw.name", view.name));
    out(outData, string("_vw.start", view.start));
    if (view.aliases.size() > 0)
    {
      outData.print("_vw.aliases = ");
      generateString(view.aliases, outData, outLog);
    }
    else
      outData.println("_vw.aliases = ''");
    if (view.lines.size() > 0)
    {
      outData.print("_vw.lines = ");
      generateString(view.lines, outData, outLog);
    }
    else
      outData.println("_vw.lines = ''");
    if (view.users.size() > 0)
    {
      outData.print("_vw.users = ");
      generateString(view.users, outData, outLog);
    }
    else
      outData.println("_vw.users = ''");
  }
}