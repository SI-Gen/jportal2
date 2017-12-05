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

public class JavaCSCode extends Generator
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
        outLog.println(args[i]+": generate Java CS wrapper code");
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
    return "generate Java CS wrapper code";
  }
  public static String documentation()
  {
    return "generate Java CS wrapper code";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    boolean hasMulti = false;
    boolean useStandard = false;
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      for (int j=0; j<table.procs.size(); j++)
      {
        Proc proc = (Proc) table.procs.elementAt(j);
        if (proc.isData)
          continue;
        if (proc.outputs.size() > 0)
        {
          if (proc.isSingle)
            continue;
          else
          {
            hasMulti = true;
            if (proc.isStdExtended())
              useStandard = true;
          }
        }
      }
    }
    if (hasMulti == true)
    {
      for (int i=0; i<database.tables.size(); i++)
      {
        Table table = (Table) database.tables.elementAt(i);
        generateStructs(table, useStandard, output, outLog);
      }
    }
  }
  static void generateStructs(Table table, boolean useStandard, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName() + "Arrayed.cs");
      OutputStream outFile = new FileOutputStream(output+table.useName() + "Arrayed.cs");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        if (table.database.packageName.length() > 0)
        {
          outData.println("using " + table.database.packageName+";");
          outData.println("namespace " + table.database.packageName);
          outData.println("{");
        }
        else
        {
          outData.println("using bbd.utility;");
          outData.println("namespace bbd.utility");
          outData.println("{");
        }
        if (useStandard == true)
          generateStdProcStruct(table, outData);
        generateOtherProcStructs(table, outData);
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
  static void generateStdProcStruct(Table table, PrintWriter outData)
  {
    outData.println("  public class "+table.useName()+"Arrayed : IArrayRecs");
    outData.println("  {");
    outData.println("    private "+table.useName()+"Rec[] recs;");
    outData.println("    public "+table.useName()+"Arrayed("+table.useName()+"Rec[] recs)");
    outData.println("    {");
    outData.println("      this.recs = recs;");
    outData.println("    }");
    int no = 0;
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("    public const int "+field.useUpperName()+" = "+no+";");
      no++;
    }
    outData.println("    public int RowCount { get { return recs.Length; } }");
    outData.println("    public int ColumnCount { get { return "+no+"; } }");
    outData.println("    public "+table.useName()+"Rec this [int row] { get { return recs[row]; } }");
    outData.println("    public object GetTag(int row) { return recs[row]._tag; }");
    outData.println("    public void SetTag(int row, object value) { recs[row]._tag = value; }");
    outData.println("    public object this [int row, int col]");
    outData.println("    {");
    outData.println("      get");
    outData.println("      {");
    outData.println("        switch(col)");
    outData.println("        {");
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("        case "+field.useUpperName()+": return recs[row]."+field.useLowerName()+";");
    }
    outData.println("        }");
    outData.println("        return null;");
    outData.println("      }");
    outData.println("      set");
    outData.println("      {");
    outData.println("        switch(col)");
    outData.println("        {");
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("        case "+field.useUpperName()+": recs[row]."+field.useLowerName()+" = ("+csVarType(field)+") value; break;");
    }
    outData.println("        }");
    outData.println("      }");
    outData.println("    }");
    outData.println("  }");
  }
  static void generateOtherProcStructs(Table table, PrintWriter outData)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStdExtended() == true)
        continue;
      if (proc.outputs.size() == 0)
        continue;
      if (proc.hasNoData() == true)
        continue;
      if (proc.isSingle)
        continue;
      generateOtherProcStruct(table, proc, outData);
    }
  } 
  static void generateOtherProcStruct(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("  public class "+table.useName()+proc.upperFirst()+"Arrayed : IArrayRecs");
    outData.println("  {");
    outData.println("    private "+table.useName()+proc.upperFirst()+"Rec[] recs;");
    outData.println("    public "+table.useName()+proc.upperFirst()+"Arrayed("+table.useName()+proc.upperFirst()+"Rec[] recs)");
    outData.println("    {");
    outData.println("      this.recs = recs;");
    outData.println("    }");
    int no = 0;
    for (int i=0; i<proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      outData.println("    public const int "+field.useUpperName()+" = "+no+";");
      no++;
    }
    for (int i=0; i<proc.outputs.size(); i++)
    {
      Field field = (Field) proc.outputs.elementAt(i);
      if (proc.hasInput(field.name))
        continue;
      outData.println("    public const int "+field.useUpperName()+" = "+no+";");
      no++;
    }
    for (int i=0; i<proc.dynamics.size(); i++)
    {
      String s = (String) proc.dynamics.elementAt(i);
      outData.println("    public const int __"+s.toUpperCase()+"__ = "+no+";");
      no++;
    }
    outData.println("    public int RowCount { get { return recs.Length; } }");
    outData.println("    public int ColumnCount { get { return "+no+"; } }");
    outData.println("    public "+table.useName()+proc.upperFirst()+"Rec this [int row] { get { return recs[row]; } }");
    outData.println("    public object GetTag(int row) { return recs[row]._tag; }");
    outData.println("    public void SetTag(int row, object value) { recs[row]._tag = value; }");
    outData.println("    public object this [int row, int col]");
    outData.println("    {");
    outData.println("      get");
    outData.println("      {");
    outData.println("        switch(col)");
    outData.println("        {");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      outData.println("        case "+field.useUpperName()+": return recs[row]."+field.useLowerName()+";");
    }
    for (int j=0; j<proc.outputs.size(); j++)
    {
      Field field = (Field) proc.outputs.elementAt(j);
      if (!proc.hasInput(field.name))
        outData.println("        case "+field.useUpperName()+": return recs[row]."+field.useLowerName()+";");
    }
    for (int j=0; j<proc.dynamics.size(); j++)
    {
      String s = (String) proc.dynamics.elementAt(j);
      outData.println("        case __"+s.toUpperCase()+"__: return recs[row]."+s+";");
    }
    outData.println("        }");
    outData.println("        return null;");
    outData.println("      }");
    outData.println("      set");
    outData.println("      {");
    outData.println("        switch(col)");
    outData.println("        {");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      outData.println("        case "+field.useUpperName()+": recs[row]."+field.useLowerName()+" = ("+csVarType(field)+") value; break;");
    }
    for (int j=0; j<proc.outputs.size(); j++)
    {
      Field field = (Field) proc.outputs.elementAt(j);
      if (!proc.hasInput(field.name))
        outData.println("        case "+field.useUpperName()+": recs[row]."+field.useLowerName()+" = ("+csVarType(field)+") value; break;");
    }
    for (int j=0; j<proc.dynamics.size(); j++)
    {
      String s = (String) proc.dynamics.elementAt(j);
      outData.println("        case __"+s.toUpperCase()+"__: recs[row]."+s+" = (string) value; break;");
    }
    outData.println("        }");
    outData.println("      }");
    outData.println("    }");
    outData.println("  }");
  }
  /**
   * Translates field type to java data member type
   */
  static String csVar(Field field)
  {
    String props = " { get {return rec."+field.useLowerName()+";} set {rec."+field.useLowerName()+" = value;}}";
    String name=field.useUpperName() + props;
    switch(field.type)
    {
      case Field.BYTE:
        return "byte "+ name;
      case Field.SHORT:
        return "short "+ name;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "int "+ name;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "long " + name;
      case Field.ANSICHAR:
        if (field.length == 1)
          return "char "+ name;
      case Field.CHAR:
        return "string "+ name;
      case Field.DATE:
        return "string "+ name;
      case Field.DATETIME:
        return "string "+ name;
      case Field.TIME:
        return "string "+ name;
      case Field.TIMESTAMP:
        return "string "+ name;
      case Field.FLOAT:
      case Field.DOUBLE:
        return "double "+ name;
      case Field.BLOB:
      case Field.TLOB:
        return "string "+ name;
      case Field.MONEY:
        return "double "+ name;
      case Field.USERSTAMP:
        return "string "+ name;
    }
    return "/// unknown " +name;
  }
  /**
   * Translates field type to java data member type
   */
  static String csVarType(Field field)
  {
    switch(field.type)
    {
      case Field.BYTE:
        return "byte";
      case Field.SHORT:
        return "short";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "int";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "long";
      case Field.ANSICHAR:
        if (field.length == 1)
          return "char";
      case Field.CHAR:
        return "string";
      case Field.DATE:
        return "string";
      case Field.DATETIME:
        return "string";
      case Field.TIME:
        return "string";
      case Field.TIMESTAMP:
        return "string";
      case Field.FLOAT:
      case Field.DOUBLE:
        return "double";
      case Field.BLOB:
      case Field.TLOB:
        return "string";
      case Field.MONEY:
        return "double";
      case Field.USERSTAMP:
        return "string";
    }
    return "unknown";
  }
}
