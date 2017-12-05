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
import java.util.Vector;

public class VBNetCode extends Generator
{
	public static void main(String args[])
	{
		try
		{
			PrintWriter outLog = new PrintWriter(System.out);
			for (int i = 0; i <args.length; i++)
			{
				outLog.println(args[i]+": Generate VB.NET Code for Ado.Net");
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
		return "Generate VB.NET Code for Ado.Net";
	}
	public static String documentation()
	{
		return "Generate VB.NET Code for Ado.Net";
	}
	public static void generate(Database database, String output, PrintWriter outLog)
	{
		for (int i=0; i < database.tables.size(); i++)
		{
			Table table = (Table) database.tables.elementAt(i);
			generate(table, output, outLog);
		}
	}
	static void generate(Table table, String output, PrintWriter outLog)
	{
		try
		{
			outLog.println("Code: "+output+table.useName() + ".vb");
			OutputStream outFile = new FileOutputStream(output+table.name + ".vb");
			try
			{
				String packageName = table.database.packageName;
				if (packageName.length() == 0)
				  packageName = "bbd.jportal"; 
				PrintWriter outData = new PrintWriter(outFile);
				outData.println("Import System");
				outData.println("Import System.Collections");
				outData.println("Import System.Data");
				outData.println("Import "+packageName);
				outData.println("");
				outData.println("NameSpace "+packageName);
				generateStructs(table, outData);
				generateCode(table, outData);
				outData.println("End NameSpace");
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
	public static void generateStructPairs(Vector<Field> fields, Vector<String> dynamics, String name, PrintWriter outData)
	{
		outData.println("  [Serializable()]");
		outData.println("  public class "+name+"Rec");
		outData.println("  {");
		for (int i=0; i < fields.size(); i++)
		{
			Field field = (Field) fields.elementAt(i);
			outData.println("    "+fieldDef(field)+";");
			if (field.isNull)
				outData.println("    public bool "+field.useLowerName()+"IsNull;");
		}
		outData.println("  }");
	}
	public static void generateStructs(Table table, PrintWriter outData)
	{
		if (table.fields.size() > 0)
		{
			if (table.comments.size() > 0)
			{
				outData.println("  /// <summary>");
				for (int i=0; i < table.comments.size(); i++)
				{
					String s = (String) table.comments.elementAt(i);
					outData.println("  /// "+s);
				}
				outData.println("  /// </summary>");
			}
			generateStructPairs(table.fields, null, table.useName(), outData);
			for (int i=0; i< table.procs.size(); i++)
			{
				Proc proc = (Proc) table.procs.elementAt(i);
				if (proc.isData || proc.isStd  || proc.hasNoData())
					continue;
				if (proc.comments.size() > 0)
				{
					outData.println("  /// <summary>");
					for (int j=0; j < proc.comments.size(); j++)
					{
						String s = (String) proc.comments.elementAt(j);
						outData.println("  /// "+s);
					}
					outData.println("  /// </summary>");
				}
				Vector<Field> fields = new Vector<Field>();
				for (int j=0; j<proc.outputs.size(); j++)
					fields.addElement(proc.outputs.elementAt(j));
				for (int j=0; j<proc.inputs.size(); j++)
				{
					Field field = (Field) proc.inputs.elementAt(j);
					if (proc.hasOutput(field.name) == false)
						fields.addElement(field);
				}
				for (int j=0; j<proc.dynamics.size(); j++)
				{
					String s = (String) proc.dynamics.elementAt(j);
					Integer n = (Integer) proc.dynamicSizes.elementAt(j);
					Field field = new Field();
					field.name = s;
					field.type = Field.DYNAMIC;
					field.length = n.intValue();
					fields.addElement(field);
				}
				generateStructPairs(fields, proc.dynamics, table.useName()+proc.upperFirst(), outData);
			}
		}
	}
	public static void generateCode(Table table, PrintWriter outData)
	{
		boolean firsttime=true;
		for (int i=0; i<table.procs.size(); i++)
		{
			Proc proc = (Proc) table.procs.elementAt(i);
			if (proc.isData == true || proc.isStd == false)
				continue;
			generateStdCode(table, proc, outData, firsttime);
			firsttime=false;
		}
		if (firsttime == false)
			outData.println("  }");
		for (int i=0; i<table.procs.size(); i++)
		{
			Proc proc = (Proc) table.procs.elementAt(i);
			if (proc.isData == true || proc.isStd == true)
				continue;
			generateCode(table, proc, outData);
		}
	}
	static int questionsSeen;
	static String question(Proc proc, String line)
	{
		String result = "";
		int p;
		while ((p = line.indexOf("?")) > -1)
		{
			if (p > 0)
			{
				result = result + line.substring(0, p);
				line = line.substring(p);
			}
			if (questionsSeen < proc.inputs.size())
			{
				Field field = (Field) proc.inputs.elementAt(questionsSeen++);
				if (field.type == Field.IDENTITY && proc.isInsert)
					field = (Field) proc.inputs.elementAt(questionsSeen++);
				result = result + ":" + field.name;
			}
			else
				result = result + ":<UNKNOWN("+questionsSeen+")>";
			line = line.substring(1);
		}
		result = result + line;
		return result;
	}
	static String dropString(String sub, String command)
	{
		int p = command.indexOf(sub);
		while (p != -1)
		{
			String part1 = command.substring(0, p);
			String part2 = command.substring(p + sub.length());
			command = part1 + part2;
			p = command.indexOf("\" + \"");
		}
		return command;
	}
	static int colonIndexOf(String command, Field field)
	{
		String C = command.toUpperCase();
		String F = field.name.toUpperCase();
		return C.indexOf(":"+F);
	}
	static class Pairs
	{
		Field field;
		int pos;
		Pairs(Field field, int pos)
		{
			this.field = field;
			this.pos = pos;
		}
	}
	static Vector<Pairs> pairs;
	static void generateCommand(Proc proc, PrintWriter outData)
	{
		String command = "\"";
		questionsSeen = 0;
		for (int j=0; j < proc.lines.size(); j++)
		{
			Line l = (Line) proc.lines.elementAt(j);
			if (l.isVar)
			{
				command = command + "\" + rec." + l.line + " + \"";
			}
			else
			{
				command = command + question(proc, l.line);
			}
		}
		command = command + "\"";
		outData.println("    public string Command"+proc.upperFirst());
		outData.println("    {");
		outData.print("      get { return ");
		StringBuffer command2 = new StringBuffer(command);
		pairs = new Vector<Pairs>();
		for (int j=0; j<proc.inputs.size();)
		{
			Field field = (Field) proc.inputs.elementAt(j);
			int p = colonIndexOf(command2.toString(), field);
			if (p != -1)
			{
				command2.setCharAt(p, '?');
				int k = 0;
				while(k < pairs.size())
				{
					Pairs kPair = (Pairs) pairs.elementAt(k);
					if (kPair.pos > p)
						break;
					else
						k++;
				}
				pairs.insertElementAt(new Pairs(field, p), k);
			}
			else
				j++;
		}
		for (int j=pairs.size()-1; j>=0; j--)
		{
			Pairs pair = (Pairs) pairs.elementAt(j);
			Field field = pair.field;
			int p = pair.pos;
			if (p != -1)
			{
				String part1 = command.substring(0, p) + "\" + ";
				String part2 = "\"?\"";
				String part3 = " + \"" + command.substring(p+field.name.length()+1);
				command = part1 + part2 + part3;
			}
			command = dropString("\" + \"", command);
			command = dropString("+ \"\"", command);
			command = dropString("+ \" \"", command);
		}
		outData.println(command+"; }");
		outData.println("    }");
	}
	static void generateNonQueryProc(Proc proc, String name, PrintWriter outData)
	{
		outData.println("    public void "+proc.upperFirst()+"(Connect connect)");
		outData.println("    {");
		outData.println("      Cursor cursor = new Cursor(connect, Command"+proc.upperFirst()+");");
		for (int i=0; i<pairs.size(); i++)
		{
			Pairs pair = (Pairs) pairs.elementAt(i); 
			Field field = pair.field;
			outData.println("      cursor.Command.Parameters.Add(new OleDbParameter()).Value = rec."+field.useLowerName()+";");
		}
		outData.println("      cursor.Exec();");
		outData.println("    }");
	}
	static void generateReadOneProc(Proc proc, String name, PrintWriter outData)
	{
		outData.println("    public bool "+proc.upperFirst()+"(Connect connect)");
		outData.println("    {");
		outData.println("      Cursor cursor = new Cursor(connect, Command"+proc.upperFirst()+");");
		for (int i=0; i<pairs.size(); i++)
		{
			Pairs pair = (Pairs) pairs.elementAt(i); 
			Field field = pair.field;
			outData.println("      cursor.Command.Parameters.Add(new OleDbParameter()).Value = rec."+field.useLowerName()+";");
		}
		outData.println("      cursor.Run();");
		outData.println("      bool result = (cursor.Reader != null) && cursor.Reader.Read();");
		outData.println("      if (result == true)");
		outData.println("      {");
		for (int i=0; i<proc.outputs.size(); i++)
		{
			Field field = (Field) proc.outputs.elementAt(i);
			outData.println("        rec."+field.useLowerName()+" = cursor."+cursorGet(field, i)+";");
		}
		outData.println("      }");
		outData.println("      if (cursor.Reader != null)");
		outData.println("        cursor.Reader.Close();");
		outData.println("      return result;");
		outData.println("    }");
	}
	static void generateFetchProc(Proc proc, String name, PrintWriter outData)
	{
		outData.println("    public void "+proc.upperFirst()+"(Connect connect)");
		outData.println("    {");
		outData.println("      cursor = new Cursor(connect, Command"+proc.upperFirst()+");");
		for (int i=0; i<pairs.size(); i++)
		{
			Pairs pair = (Pairs) pairs.elementAt(i); 
			Field field = pair.field;
			outData.println("      cursor.Command.Parameters.Add(new OleDbParameter()).Value = rec."+field.useLowerName()+";");
		}
		outData.println("      cursor.Run();");
		outData.println("    }");
		outData.println("    public bool "+proc.upperFirst()+"Fetch()");
		outData.println("    {");
		outData.println("      bool result = (cursor.Reader != null) && cursor.Reader.Read();");
		outData.println("      if (result == true)");
		outData.println("      {");
		for (int i=0; i<proc.outputs.size(); i++)
		{
			Field field = (Field) proc.outputs.elementAt(i);
			outData.println("        rec."+field.useLowerName()+" = cursor."+cursorGet(field, i)+";");
		}
		outData.println("      }");
		outData.println("      else if (cursor.Reader != null)");
		outData.println("        cursor.Reader.Close();");
		outData.println("      return result;");
		outData.println("    }");
		outData.println("    public void "+proc.upperFirst()+"Load(Connect connect)");
		outData.println("    {");
		outData.println("      "+proc.upperFirst()+"(connect);");
		outData.println("      while ("+proc.upperFirst()+"Fetch())");
		outData.println("      {");
		outData.println("        list.Add(rec);");
		outData.println("        rec = new "+name+"Rec();");
		outData.println("      }");
		outData.println("    }");
		outData.println("    public ArrayList Loaded { get { return list; } }");
		outData.println("    public DataTable "+proc.upperFirst()+"DataTable()");
		outData.println("    {");
		outData.println("      DataTable result = new DataTable();");
		for (int i=0; i<proc.inputs.size(); i++)
		{
			Field field = (Field) proc.inputs.elementAt(i);
			outData.println("      result.Columns.Add(new DataColumn(\""+field.useLowerName()+"\", typeof("+dataTableType(field)+")));");
		}
		for (int i=0; i<proc.outputs.size(); i++)
		{
			Field field = (Field) proc.outputs.elementAt(i);
			if (proc.hasInput(field.name))
				continue;
			outData.println("      result.Columns.Add(new DataColumn(\""+field.useLowerName()+"\", typeof("+dataTableType(field)+")));");
		}
		outData.println("      foreach ("+name+"Rec rec in Loaded)");
		outData.println("      {");
		outData.println("        DataRow dr = result.NewRow();");
		int noInDataSet=0;
		for (int i=0; i<proc.inputs.size(); i++)
		{
			Field field = (Field) proc.inputs.elementAt(i);
			outData.println("        dr["+noInDataSet+"] = rec."+field.useLowerName()+";");
			noInDataSet++;
		}
		for (int i=0; i<proc.outputs.size(); i++)
		{
			Field field = (Field) proc.outputs.elementAt(i);
			if (proc.hasInput(field.name))
				continue;
			outData.println("        dr["+noInDataSet+"] = rec."+field.useLowerName()+";");
			noInDataSet++;
		}
		outData.println("        result.Rows.Add(dr);");
		outData.println("      }");
		outData.println("      return result;");
		outData.println("    }");
		outData.println("    public DataTable "+proc.upperFirst()+"DataTable(Connect connect)");
		outData.println("    {");
		outData.println("      "+proc.upperFirst()+"Load(connect);");
		outData.println("      return "+proc.upperFirst()+"DataTable();");
		outData.println("    }");
	}
	static void generateProcFunctions(Proc proc, String name, PrintWriter outData)
	{
		if (proc.outputs.size() > 0 && !proc.isSingle)
			generateFetchProc(proc, name, outData);
		else if (proc.outputs.size() > 0)
			generateReadOneProc(proc, name, outData);
		else
			generateNonQueryProc(proc, name, outData);
	}
	static void generateCClassTop(Proc proc, String name, PrintWriter outData, boolean doCursor)
	{
		outData.println("  [Serializable()]");
		outData.println("  public class "+name);
		outData.println("  {");
		if (doCursor == true || proc.hasNoData() == false)
		{
			outData.println("    private "+name+"Rec rec;");
			outData.println("    public "+name+"Rec Rec { get { return rec; } set { rec = value; } }");
			if (doCursor == true || (proc.outputs.size() > 0 && !proc.isSingle))
			{
				outData.println("    private ArrayList list;");
				outData.println("    public int Count { get { return list.Count; } }");
				outData.println("    public Cursor cursor;");
				outData.println("    public "+name+"Rec this[int i]");
				outData.println("    {");
				outData.println("      get");
				outData.println("      {");
				outData.println("        if (i < list.Count)");
				outData.println("          return ("+name+"Rec)list[i];");
				outData.println("        return null;");
				outData.println("      }");
				outData.println("      set");
				outData.println("      {");
				outData.println("        if (i < list.Count)");
				outData.println("          list.RemoveAt(i);");
				outData.println("        list.Insert(i, value);");
				outData.println("      }");
				outData.println("    }");
			}
			outData.println("    public void Clear()");
			outData.println("    {");
			if (doCursor == true || (proc.outputs.size() > 0 && !proc.isSingle))
				outData.println("      list = new ArrayList();");
			outData.println("      rec = new "+name+"Rec();");
			outData.println("    }");
			outData.println("    public "+name+"()");
			outData.println("    {");
			outData.println("      Clear();");
			outData.println("    }");
		}
	}
	static void generateCode(Table table, Proc proc, PrintWriter outData)
	{
		if (proc.comments.size() > 0)
		{
			outData.println("  /// <summary>");
			for (int i=0; i<proc.comments.size(); i++)
			{
				String comment = (String) proc.comments.elementAt(i);
				outData.println("  /// "+comment);
			}
			outData.println("  /// </summary>");
		}
		generateCClassTop(proc, table.useName()+proc.upperFirst(), outData, false);
		generateCommand(proc, outData);
		generateProcFunctions(proc, table.useName()+proc.upperFirst(), outData);
		outData.println("  }");
	}
	static void generateStdCode(Table table, Proc proc, PrintWriter outData, boolean firsttime)
	{
		if (firsttime == true)
			generateCClassTop(proc, table.useName(), outData, table.hasCursorStdProc());
		if (proc.comments.size() > 0)
		{
			outData.println("    /// <summary>");
			for (int i=0; i<proc.comments.size(); i++)
			{
				String comment = (String) proc.comments.elementAt(i);
				outData.println("    /// "+comment);
			}
			outData.println("    /// </summary>");
		}
		generateCommand(proc, outData);
		generateProcFunctions(proc, table.useName(), outData);
	}
	static String cursorGet(Field field, int occurence)
	{
		switch (field.type)
		{
		case Field.ANSICHAR:
			return "GetString("+occurence+")";
		case Field.BLOB:
			return "GetString("+occurence+")";
		case Field.BOOLEAN:
			return "GetInt("+occurence+")";
		case Field.BYTE:
			return "GetInt("+occurence+")";
		case Field.CHAR:
			return "GetString("+occurence+")";
		case Field.DATE:
			return "GetString("+occurence+")";
		case Field.DATETIME:
			return "GetString("+occurence+")";
		case Field.DYNAMIC:
			return "GetString("+occurence+")";
		case Field.DOUBLE:
			return "GetDouble("+occurence+")";
		case Field.FLOAT:
			return "GetDouble("+occurence+")";
		case Field.IDENTITY:
			return "GetInt("+occurence+")";
		case Field.INT:
			return "GetInt("+occurence+")";
		case Field.LONG:
			return "GetInt("+occurence+")";
		case Field.MONEY:
			return "GetString("+occurence+")";
		case Field.SEQUENCE:
			return "GetInt("+occurence+")";
		case Field.SHORT:
			return "GetInt("+occurence+")";
		case Field.TIME:
			return "GetString("+occurence+")";
		case Field.TIMESTAMP:
			return "GetString("+occurence+")";
		case Field.TLOB:
			return "GetString("+occurence+")";
		case Field.USERSTAMP:
			return "GetString("+occurence+")";
		}
		return "Get("+occurence+")";
	}
	static String dataTableType(Field field)
	{
		switch (field.type)
		{
		case Field.ANSICHAR:
			return "String";
		case Field.BLOB:
			return "String";
		case Field.BOOLEAN:
			return "Int32";
		case Field.BYTE:
			return "Int32";
		case Field.CHAR:
			return "String";
		case Field.DATE:
			return "String";
		case Field.DATETIME:
			return "String";
		case Field.DYNAMIC:
			return "String";
		case Field.DOUBLE:
			return "Double";
		case Field.FLOAT:
			return "Double";
		case Field.IDENTITY:
			return "Int32";
		case Field.INT:
			return "Int32";
		case Field.LONG:
			return "Int32";
		case Field.MONEY:
			return "String";
		case Field.SEQUENCE:
			return "Int32";
		case Field.SHORT:
			return "Int32";
		case Field.TIME:
			return "String";
		case Field.TIMESTAMP:
			return "String";
		case Field.TLOB:
			return "String";
		case Field.USERSTAMP:
			return "String";
		}
		return "dataTableType";
	}
//	public static String fieldMakeUp(Field field)
//	{
//		switch (field.type)
//		{
//		case Field.ANSICHAR:
//		case Field.BLOB:
//		case Field.CHAR:
//		case Field.DATE:
//		case Field.DATETIME:
//		case Field.DYNAMIC:
//		case Field.MONEY:
//		case Field.TIME:
//		case Field.TIMESTAMP:
//		case Field.TLOB:
//		case Field.USERSTAMP:
//			return "C"+(field.length+1);
//		case Field.BOOLEAN:
//		case Field.BYTE:
//			return "I1";
//		case Field.DOUBLE:
//		case Field.FLOAT:
//			return "D8";
//		case Field.IDENTITY:
//		case Field.INT:
//		case Field.LONG:
//		case Field.SEQUENCE:
//			return "I4";
//		case Field.SHORT:
//			return "I2";
//		}
//		return "X0";
//	}
	static String fieldDef(Field field)
	{
		String result;
		switch (field.type)
		{
		case Field.ANSICHAR:
		case Field.BLOB:
		case Field.CHAR:
		case Field.DATE:
		case Field.DATETIME:
		case Field.DYNAMIC:
		case Field.MONEY:
		case Field.TIME:
		case Field.TIMESTAMP:
		case Field.TLOB:
		case Field.USERSTAMP:
			result = "string";
			break;
		case Field.BOOLEAN:
		case Field.BYTE:
		case Field.STATUS:
			result = "sbyte";
			break;
		case Field.DOUBLE:
		case Field.FLOAT:
			result = "double";
			break;
		case Field.IDENTITY:
		case Field.INT:
		case Field.LONG:
		case Field.SEQUENCE:
			result = "int";
			break;
		case Field.SHORT:
			result = "short";
			break;
		default:
			result = "whoknows";
			break;
		}
		return "public "+result+" "+field.useLowerName();
	}
	static String fieldInit(Field field)
	{
		String result;
		switch (field.type)
		{
		case Field.ANSICHAR:
		case Field.BLOB:
		case Field.CHAR:
		case Field.DATE:
		case Field.DATETIME:
		case Field.DYNAMIC:
		case Field.MONEY:
		case Field.TIME:
		case Field.TIMESTAMP:
		case Field.TLOB:
		case Field.USERSTAMP:
			result = "\"\"";
			break;
		case Field.BOOLEAN:
		case Field.BYTE:
		case Field.DOUBLE:
		case Field.FLOAT:
		case Field.IDENTITY:
		case Field.INT:
		case Field.LONG:
		case Field.SEQUENCE:
		case Field.SHORT:
		case Field.STATUS:
			result = "0";
			break;
		default:
			result = "whoknows";
			break;
		}
		return field.useLowerName()+" = "+result;
	}
}
