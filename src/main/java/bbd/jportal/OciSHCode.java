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

/**
 * @author vince
 */
public class OciSHCode extends Generator
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
				outLog.println(args[i]+": Generate OCI C++ SH Code");
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
		return "Generate OCI C++ SH Code";
	}
	public static String documentation()
	{
		return "Generate OCI C++ SH Code";
	}
	/**
	* Padder function
	*/
	static String padder(String s, int length)
	{
		for (int i = s.length(); i < length-1; i++)
			s = s + " ";
		return s + " ";
	}
  protected static Vector<Flag> flagsVector;
  static boolean aix;
  static boolean lowercase;
  private static void flagDefaults()
  {
    aix = false;
    lowercase = false;
  }
  public static Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<Flag>();
      flagDefaults();
      flagsVector.addElement(new Flag("aix", new Boolean (aix), "Generate for AIX"));
      flagsVector.addElement(new Flag("lowercase", new Boolean (aix), "Generate lowercase"));
    }
    return flagsVector;
  }
  static void setFlags(Database database, PrintWriter outLog)
  {
    if (flagsVector != null)
    {
      aix = toBoolean (((Flag)flagsVector.elementAt(0)).value);
      lowercase = toBoolean (((Flag)flagsVector.elementAt(1)).value);
    }
    else
      flagDefaults();
		for (int i=0; i < database.flags.size(); i++)
		{
			String flag = (String) database.flags.elementAt(i);
			if (flag.equalsIgnoreCase("lowercase"))
				lowercase = true;
			else if (flag.equalsIgnoreCase("aix"))
				aix = true;
		}
		if (lowercase)
			outLog.println(" (lowercase)");
		if (aix)
			outLog.println(" (aix)");
	}
	/**
	* Generates the procedure classes for each table present.
	*/
	public static void generate(Database database, String output, PrintWriter outLog)
	{
		setFlags(database, outLog);
		for (int i=0; i<database.tables.size(); i++)
		{
			Table table = (Table) database.tables.elementAt(i);
			generate(table, output, outLog);
		}
	}
	/**
	* Build of standard and user defined procedures
	*/
  static void generate(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output+table.useName()+".sh");
      OutputStream outFile = new FileOutputStream(output+table.useName()+".sh");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("#ifndef _"+table.useName().toUpperCase()+"_SH_");
          outData.println("#define _"+table.useName().toUpperCase()+"_SH_");
          outData.println();
          outData.println("#include <stddef.h>");
          outData.println("#include \"ociapi.h\"");
          outData.println();
          if (table.hasStdProcs)
            generateStdOutputRec(table, outData);
          generateUserOutputRecs(table, outData);
          generateInterface(table, outData);
          generateShInterface(table, outData);
          outData.println("#endif");
        }
        finally
        {
          outData.flush();
        }
        outFile.close();
        outLog.println("Code: "+output+table.useName()+".cpp");
        outFile = new FileOutputStream(output+table.useName()+".cpp");
        outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println();
          outData.println("#include \""+table.useName()+".sh"+"\"");
          outData.println();
          generateImplementation(table, outData);
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
  /**
   * Build of output data rec for standard procedures
   */
  static void generateStdOutputRec(Table table, PrintWriter outData)
  {
    for (int i=0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      outData.println("//"+s);
    }
    int pos = 0;
    int padding;
    int filler=0;
    outData.println("struct D"+table.useName());
    outData.println("{");
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (field.comments.size() > 0)
      {
        for (int c=0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  //"+s);
        }
      }
      padding = generatePadding(field, outData, pos, filler++);
      pos += padding;
      outData.println("  "+cppVar(field)+";");
      pos += getLength(field);
      if (field.isNull && notString(field))
      {
        padding = generatePadding(outData, pos, filler++);
        pos += padding;
        outData.println("  short  "+field.useName()+"IsNull;");
        pos += 2;
      }
    }
    outData.println("  #ifdef _NEDGEN_H_");
    outData.println("  void Swaps()");
    outData.println("  {");
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (notString(field) == false)
        continue;
      outData.println("    SwapBytes("+field.useName()+");");
      if (field.isNull)
        outData.println("    SwapBytes("+field.useName()+"IsNull);");
    }
    outData.println("  }");
    outData.println("  #endif");
    outData.println("};");
    outData.println();
    outData.println("typedef D"+table.useName()+" O"+table.useName()+";");
    outData.println();
  }
  /**
   * Build of output data rec for user procedures
   */
  static void generateUserOutputRecs(Table table, PrintWriter outData)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd  || proc.hasNoData())
        continue;
      String work = "";
      String work2 = "";
      if (proc.outputs.size() > 0)
      {
        for (int j=0; j<proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println("//"+comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput())
          typeChar = "O";
        work = " : public "+typeChar+table.useName()+proc.upperFirst();
        work2 = typeChar+table.useName()+proc.upperFirst();
        outData.println("struct "+typeChar+table.useName()+proc.upperFirst());
        outData.println("{");
        int pos = 0;
        int padding;
        int filler=0;
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  //"+s);
          }
          padding = generatePadding(field, outData, pos, filler++);
          pos += padding;
          outData.println("  "+cppVar(field)+";");
          pos += getLength(field);
          if (field.isNull && notString(field))
          {
            padding = generatePadding(outData, pos, filler++);
            pos += padding;
            outData.println("  short  "+field.useName()+"IsNull;");
            pos += 2;
          }
        }
        outData.println("  #ifdef _NEDGEN_H_");
        outData.println("  void Swaps()");
        outData.println("  {");
        for (int j=0; j<proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          if (notString(field) == false)
            continue;
          outData.println("    SwapBytes("+field.useName()+");");
          if (field.isNull)
            outData.println("    SwapBytes("+field.useName()+"IsNull);");
        }
        outData.println("  }");
        outData.println("  #endif");
        outData.println("};");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        outData.println("struct D"+table.useName()+proc.upperFirst()+work);
        outData.println("{");
        int pos = 0;
        int padding;
        int filler=0;
        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          for (int c=0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  //"+s);
          }
          padding = generatePadding(field, outData, pos, filler++);
          pos += padding;
          outData.println("  "+cppVar(field)+";");
          pos += getLength(field);
          if (field.isNull && notString(field))
          {
            padding = generatePadding(outData, pos, filler++);
            pos += padding;
            outData.println("  short  "+field.useName()+"IsNull;");
            pos += 2;
          }
        }
        for (int j=0; j<proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          Integer n = (Integer) proc.dynamicSizes.elementAt(j);
          outData.println("  char "+s+"["+n.intValue()+"];");
        }
        outData.println("  #ifdef _NEDGEN_H_");
        outData.println("  void Swaps()");
        outData.println("  {");
        if (work2.length() > 0)
          outData.println("    "+work2+".Swaps();");
        for (int j=0; j<proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (notString(field) == false)
            continue;
          outData.println("    SwapBytes("+field.useName()+");");
          if (field.isNull)
            outData.println("    SwapBytes("+field.useName()+"IsNull);");
        }
        outData.println("  }");
        outData.println("  #endif");
        outData.println("};");
        outData.println();
      }
      else if (proc.outputs.size() > 0)
      {
        outData.println("typedef D"+table.useName()+proc.upperFirst()
          +" O"+table.useName()+proc.upperFirst()+";");
        outData.println();
      }
    }
  }
  static boolean hasStd = false;
  /**
   * Build of output data rec for standard procedures
   */
  static void generateShInterface(Table table, PrintWriter outData)
  {
    if (hasStd == true)
    {
      outData.println("struct t"+table.useName()+" : public D"+table.useName());
      outData.println("{");
      for (int i=0; i<table.procs.size(); i++)
      {
        Proc proc = (Proc) table.procs.elementAt(i);
        if (proc.isStd == false || proc.isMultipleInput == true)
          continue;
        if (proc.isSingle)
          generateStdSingleCode(table, proc, outData);
        else if (proc.outputs.size() == 0)
          generateStdCommandCode(table, proc, outData);
      }
      outData.println("};");
      outData.println();
    }
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isStd && proc.isSingle == false && proc.outputs.size() > 0)
      {
        generateStdMultipleCode(table, proc, outData);
        continue;
      }
      if (proc.isData == true || proc.isStd)
        continue;
      if (proc.isSingle == false && proc.outputs.size() > 0)
      {
        generateUserMultipleCode(table, proc, outData);
        continue;
      }
      outData.println("struct t"+table.useName()+proc.upperFirst()+" : public D"+table.useName()+proc.upperFirst());
      outData.println("{");
      generateUserShCode(table, proc, outData);
      outData.println("};");
      outData.println();
    }
  }
  /**
   * Build of output data rec for standard procedures
   */
  static void generateInterface(Table table, PrintWriter outData)
  {
    hasStd = false;
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isStd && proc.isMultipleInput == false)
        hasStd = true;
      generateInterface(table, proc, outData);
    }
  }
  static void generateStdSingleCode(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("  bool "+proc.upperFirst()+"(TJConnector &conn, const char *file=__FILE__, long line=__LINE__)");
    outData.println("  {");
    outData.println("    bool result = false;");
    outData.println("    X"+table.useName()+proc.upperFirst()+" rec(conn, file, line);");
    outData.println("    rec.Exec(*DRecs());");
    outData.println("    result = rec.Fetch();");
    outData.println("    *DRecs() = rec.*DRecs();");
    outData.println("    return result;");
    outData.println("  }");
  }
  static void generateStdCommandCode(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("  void "+proc.upperFirst()+"(TJConnector &conn, const char *file=__FILE__, long line=__LINE__)");
    outData.println("  {");
    outData.println("    X"+table.useName()+proc.upperFirst()+" rec(conn, file, line);");
    outData.println("    rec.Exec(*DRecs());");
    outData.println("    this.*DRecs() = rec.*DRecs();");
    outData.println("  }");
  }
  static void generateStdMultipleCode(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("struct t"+table.useName()+proc.upperFirst()+"Query : public X"+table.useName()+proc.upperFirst());
    outData.println("{");
    outData.println("  t"+table.useName()+"* rec;");
    outData.println("  t"+table.useName()+proc.upperFirst()+"Query(TJConnector &conn, t"+table.useName()+"* d, bool doExec=true, const char *file=__FILE__, long line=__LINE__)");
    outData.println("  : X"+table.useName()+proc.upperFirst()+"(conn, file, line)");
    outData.println("  , rec(d)");
    outData.println("  {");
    outData.println("    if (doExec == true)");
    outData.println("      Exec();");
    outData.println("  }");
    outData.println("  bool Fetch()");
    outData.println("  {");
    outData.println("    bool result = X"+table.useName()+proc.upperFirst()+"::Fetch();");
    outData.println("    rec->*DRecs() = this.*DRecs();");
    outData.println("    return result;");
    outData.println("  }");
    outData.println("};");
    outData.println();
  }
  static void generateUserShCode(Table table, Proc proc, PrintWriter outData)
  {
    if (proc.isSingle)
      generateUserSingleCode(table, proc, outData);
    else if (proc.outputs.size() == 0)
      generateUserCommandCode(table, proc, outData);
  }
  static void generateUserSingleCode(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("  bool "+proc.upperFirst()+"(TJConnector &conn, const char *file=__FILE__, long line=__LINE__)");
    outData.println("  {");
    outData.println("    bool result = false;");
    outData.println("    X"+table.useName()+proc.upperFirst()+" rec(conn, file, line);");
    outData.println("    rec.Exec(*DRecs());");
    outData.println("    result = rec.Fetch();");
    outData.println("    *DRecs() = rec.*DRecs();");
    outData.println("    return result;");
    outData.println("  }");
  }
  static void generateUserCommandCode(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("  void "+proc.upperFirst()+"(TJConnector &conn, const char *file=__FILE__, long line=__LINE__)");
    outData.println("  {");
    outData.println("    X"+table.useName()+proc.upperFirst()+" rec(conn, file, line);");
    outData.println("    rec.Exec(*DRecs());");
    outData.println("    this.*DRecs() = rec.*DRecs();");
    outData.println("  }");
  }
  static void generateUserMultipleCode(Table table, Proc proc, PrintWriter outData)
  {
    outData.println("struct t"+table.useName()+proc.upperFirst()+"Query : public X"+table.useName()+proc.upperFirst());
    outData.println("{");
    outData.println("  t"+table.useName()+proc.upperFirst()+"* rec;");
    outData.println("  t"+table.useName()+proc.upperFirst()+"Query(TJConnector &conn, t"+table.useName()+proc.upperFirst()+"* d, bool doExec=true, const char *file=__FILE__, long line=__LINE__)");
    outData.println("  : X"+table.useName()+proc.upperFirst()+"(conn, file, line)");
    outData.println("  , rec(d)");
    outData.println("  {");
    outData.println("    if (doExec == true)");
    outData.println("      Exec();");
    outData.println("  }");
    outData.println("  bool Fetch()");
    outData.println("  {");
    outData.println("    bool result = X"+table.useName()+proc.upperFirst()+"::Fetch();");
    outData.println("    rec->*DRecs() = this.*DRecs();");
    outData.println("    return result;");
    outData.println("  }");
    outData.println("};");
    outData.println();
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateInterface(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.comments.size() > 0)
    {
      for (int i=0; i<proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("  //"+comment);
      }
    }
    if (proc.hasNoData())
    {
      outData.println("struct X"+table.useName()+proc.upperFirst());
      outData.println("{");
      outData.println("  TJQuery q_;");
      outData.println("  void Exec();");
      outData.println("  X"+table.useName()+proc.upperFirst()+"(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
      outData.println("  : q_(conn)");
      outData.println("  {q_.FileAndLine(aFile,aLine);}");
      outData.println("};");
      outData.println();
    }
    else
    {
      if (proc.isStd)
        dataStruct = "D"+table.useName();
      else
        dataStruct = "D"+table.useName()+proc.upperFirst();
      outData.println("struct X"+table.useName()+proc.upperFirst()+" : public "+dataStruct);
      outData.println("{");
      if (proc.isMultipleInput)
        generateArrayInterface(table, proc, dataStruct, outData);
      else
        generateInterface(table, proc, dataStruct, outData);
      outData.println("};");
      outData.println();
    }
  }
  static void generateArrayInterface(Table table, Proc proc, String dataStruct, PrintWriter outData)
  {
    outData.println("  enum");
    Field field = (Field) proc.inputs.elementAt(0);
    String thisOne = field.useName().toUpperCase()+"_OFFSET";
    String lastOne = thisOne;
    String lastSize = cppLength(field);
    outData.println("  { "+padder(thisOne, 24)+"= 0");
    for (int j=1; j<proc.inputs.size(); j++)
    {
      field = (Field) proc.inputs.elementAt(j);
      thisOne = field.useName().toUpperCase()+"_OFFSET";
      outData.println("  , "+padder(thisOne, 24)
        +"= ("+lastOne+"+"+lastSize+")");
      lastOne = thisOne;
      lastSize = cppLength(field);
    }
    outData.println("  , "+padder("ROWSIZE",24)+"= ("+lastOne+"+"+lastSize+")");
    if (proc.noRows > 0)
      outData.println("  , "+padder("NOROWS",24)+"= "+proc.noRows);
    else
      outData.println("  , "+padder("NOROWS",24)+"= (24576 / ROWSIZE) + 1");
    outData.println("  , "+padder("NOBINDS",24)+"= "+proc.inputs.size());
    field = (Field) proc.inputs.elementAt(0);
    thisOne = field.useName().toUpperCase();
    outData.println("  , "+padder(thisOne+"_POS", 24)+"= 0");
    for (int j=1; j<proc.inputs.size(); j++)
    {
      field = (Field) proc.inputs.elementAt(j);
      thisOne = field.useName().toUpperCase();
      outData.println("  , "+padder(thisOne+"_POS", 24)
        +"= "+padder(thisOne+"_OFFSET",24)+"* NOROWS");
    }
    outData.println("  };");
    outData.println("  TJQuery q_;");
    outData.println("  void Clear() {memset(this, 0, sizeof("+dataStruct+"));}");
    outData.println("  void Init(int Commit=1); // Commit after each block inserted");
    outData.println("  void Fill();");
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("  void Fill("+dataStruct+"& Rec) {*DRec() = Rec;Fill();}");
      outData.println("  void Fill(");
      generateWithArrayParms(proc, outData, "  ");
      outData.println("  );");
    }
    outData.println("  void Done();");
    outData.println("  X"+table.useName()+proc.upperFirst()+"(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
    outData.println("  : q_(conn)");
    outData.println("  {Clear();q_.FileAndLine(aFile,aLine);}");
    outData.println("  "+dataStruct+"* DRec() {return this;}");
  }
  static void generateInterface(Table table, Proc proc, String dataStruct, PrintWriter outData)
  {
    if (proc.outputs.size() > 0)
    {
      outData.println("  enum");
      Field field = (Field) proc.outputs.elementAt(0);
      String thisOne = field.useName().toUpperCase()+"_OFFSET";
      String lastOne = thisOne;
      String lastSize = cppLength(field);
      outData.println("  { "+padder(thisOne, 24)+"= 0");
      for (int j=1; j<proc.outputs.size(); j++)
      {
        field = (Field) proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase()+"_OFFSET";
        outData.println("  , "+padder(thisOne, 24)
          +"= ("+lastOne+"+"+lastSize+")");
        lastOne = thisOne;
        lastSize = cppLength(field);
      }
      outData.println("  , "+padder("ROWSIZE",24)+"= ("+lastOne+"+"+lastSize+")");
      if (proc.isSingle)
        outData.println("  , "+padder("NOROWS",24)+"= 1");
      else if (proc.noRows > 0)
        outData.println("  , "+padder("NOROWS",24)+"= "+proc.noRows);
      else
        outData.println("  , "+padder("NOROWS",24)+"= (24576 / ROWSIZE) + 1");
      outData.println("  , "+padder("NOBINDS",24)+"= "+proc.inputs.size());
      outData.println("  , "+padder("NODEFINES",24)+"= "+proc.outputs.size());
      field = (Field) proc.outputs.elementAt(0);
      thisOne = field.useName().toUpperCase();
      outData.println("  , "+padder(thisOne+"_POS", 24)+"= 0");
      for (int j=1; j<proc.outputs.size(); j++)
      {
        field = (Field) proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase();
        outData.println("  , "+padder(thisOne+"_POS", 24)
          +"= "+padder(thisOne+"_OFFSET",24)+"* NOROWS");
      }
      outData.println("  };");
    }
    outData.println("  TJQuery q_;");
    outData.println("  void Clear() {memset(this, 0, sizeof("+dataStruct+"));}");
    outData.println("  void Exec();");
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("  void Exec("+dataStruct+"& Rec) {*DRec() = Rec;Exec();}");
      outData.println("  void Exec(");
      generateWithParms(proc, outData, "  ");
      outData.println("  );");
    }
    if (proc.outputs.size() > 0)
      outData.println("  bool Fetch();");
    outData.println("  X"+table.useName()+proc.upperFirst()+"(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
    outData.println("  : q_(conn)");
    outData.println("  {Clear();q_.FileAndLine(aFile,aLine);}");
    outData.println("  "+dataStruct+"* DRec() {return this;}");
    if (proc.outputs.size() > 0)
      outData.println("  O"+dataStruct.substring(1)+"* ORec() {return this;}");
  }
  /**
   *
   */
  static void generateImplementation(Table table, PrintWriter outData)
  {
    for (int i=0; i<table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isMultipleInput)
        generateArrayImplementation(table, proc, outData);
      else
        generateImplementation(table, proc, outData);
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
      Field field = (Field) proc.inputs.elementAt(questionsSeen++);
      if (field.type == Field.IDENTITY && proc.isInsert)
        field = (Field) proc.inputs.elementAt(questionsSeen++);
      result = result + ":" + field.name;
      line = line.substring(1);
    }
    result = result + line;
    return result;
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    int size = 1;
    questionsSeen = 0;
    for (int i=0; i < proc.lines.size(); i++)
    {
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        size += 256;
      else
        size += question(proc, l.line).length();
    }
    outData.println("  if (q_.command == 0)");
    outData.println("    q_.command = new char ["+size+"];");
    outData.println("  memset(q_.command, 0, sizeof(q_.command));");
    if (proc.lines.size() > 0)
    {
      String strcat = "  strcat(q_.command, ";
      String tail = "";
      questionsSeen = 0;
      for (int i=0; i < proc.lines.size(); i++)
      {
        Line l = (Line) proc.lines.elementAt(i);
        if (l.isVar)
        {
          tail = ");";
          if (i != 0)
            outData.println(tail);
          outData.print("  strcat(q_.command, "+l.line+"");
          strcat = "  strcat(q_.command, ";
        }
        else
        {
          if (i != 0)
            outData.println(tail);
          tail = "";
          outData.print(strcat+"\""+question(proc, l.line)+"\"");
          strcat = "                      ";
        }
      }
      outData.println(");");
    }
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateArrayImplementation(Table table, Proc proc, PrintWriter outData)
  {
    String fullName = table.useName()+proc.name;
    outData.println("void X"+fullName+"::Init(int Commit)");
    outData.println("{");
    generateCommand(proc, outData);
    outData.println("  q_.OpenArray(q_.command, NOBINDS, NOROWS, ROWSIZE);");
    outData.println("  q_.SetCommit(Commit);");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      outData.println("  q_.BindArray("
        +padder("\":"+field.name+"\",", 24)
        +padder(""+j+",", 4)
        +cppBindArray(field, table.name)+");");
    }
    outData.println("}");
    outData.println();
    outData.println("void X"+fullName+"::Fill()");
    outData.println("{");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if (field.type == Field.SEQUENCE && proc.isInsert)
        outData.println("  q_.Sequence("+field.useName()+", \""+table.name+"Seq\");");
      if (field.type == Field.TIMESTAMP)
        outData.println("  q_.conn.TimeStamp("+field.useName()+");");
      if (field.type == Field.USERSTAMP)
        outData.println("  q_.UserStamp("+field.useName()+");");
      outData.println("  q_.Put("+cppPut(field)+");");
      if (field.isNull && notString(field))
        outData.println("  q_.PutNull("+field.useName()+"IsNull, "+j+");");
    }
    outData.println("  q_.Deliver(0); // 0 indicates defer doing it if not full");
    outData.println("}");
    outData.println();
    outData.println("void X"+fullName+"::Fill(");
    generateWithArrayParms(proc, outData, "");
    outData.println(")");
    outData.println("{");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE && proc.isInsert)
        ||   field.type == Field.IDENTITY
        ||   field.type == Field.TIMESTAMP
        ||   field.type == Field.USERSTAMP)
        continue;
      outData.println("  "+cppCopy(field));
    }
    outData.println("  Fill();");
    outData.println("}");
    outData.println();
    outData.println("void X"+fullName+"::Done()");
    outData.println("{");
    outData.println("  q_.Deliver(1); // 1 indicates doit now");
    outData.println("}");
    outData.println();
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    String fullName = table.useName()+proc.name;
    outData.println("void X"+fullName+"::Exec()");
    outData.println("{");
    generateCommand(proc, outData);
    if (proc.outputs.size() > 0)
      outData.println("  q_.Open(q_.command, NOBINDS, NODEFINES, NOROWS, ROWSIZE);");
    else if (proc.inputs.size() > 0)
      outData.println("  q_.Open(q_.command, "+proc.inputs.size()+");");
    else
      outData.println("  q_.Open(q_.command);");
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      generateCppBind(field, outData);
    }
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      outData.println("  q_.Bind("
        +padder("\":"+field.name+"\",", 24)
        +padder(""+j+",", 4)
        +cppBind(field, table.name, proc.isInsert)
        +((field.isNull && notString(field))?", &"+field.useName()+"IsNull);":");"));
    }
    for (int j=0; j<proc.outputs.size(); j++)
    {
      Field field = (Field) proc.outputs.elementAt(j);
      outData.println("  q_.Define("
        +padder(""+j+",", 4)
        +cppDefine(field)+");");
    }
    outData.println("  q_.Exec();");
    outData.println("}");
    outData.println();
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("void X"+fullName+"::Exec(");
      generateWithParms(proc, outData, "");
      outData.println(")");
      outData.println("{");
      for (int j=0; j<proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        if ((field.type == Field.SEQUENCE && proc.isInsert)
          ||   field.type == Field.IDENTITY
          ||   field.type == Field.TIMESTAMP
          ||   field.type == Field.USERSTAMP)
          continue;
        outData.println("  "+cppCopy(field));
      }
      for (int j=0; j<proc.dynamics.size(); j++)
      {
        String s = (String) proc.dynamics.elementAt(j);
        outData.println("  strncpy("+s+", a"+s+", sizeof("+s+"));");
      }
      outData.println("  Exec();");
      outData.println("}");
      outData.println();
    }
    if (proc.outputs.size() > 0)
    {
      outData.println("bool X"+fullName+"::Fetch()");
      outData.println("{");
      outData.println("  if (q_.Fetch() == false)");
      outData.println("    return false;");
      for (int j=0; j<proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        outData.println("  q_.Get("+cppGet(field)+");");
        if (field.isNull && notString(field))
          outData.println("  q_.GetNull("+field.useName()+"IsNull, "+j+");");
      }
      outData.println("  return true;");
      outData.println("}");
      outData.println();
    }
  }
  static void generateWithArrayParms(Proc proc, PrintWriter outData, String pad)
  {
    String comma = "  ";
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE && proc.isInsert)
        ||   field.type == Field.IDENTITY
        ||   field.type == Field.TIMESTAMP
        ||   field.type == Field.USERSTAMP)
        continue;
      outData.println(pad+comma+"const "+cppParm(field));
      comma = ", ";
    }
  }
  static void generateWithParms(Proc proc, PrintWriter outData, String pad)
  {
    String comma = "  ";
    for (int j=0; j<proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE && proc.isInsert)
        ||   field.type == Field.IDENTITY
        ||   field.type == Field.TIMESTAMP
        ||   field.type == Field.USERSTAMP)
        continue;
      outData.println(pad+comma+"const "+cppParm(field));
      comma = ", ";
    }
    for (int j=0; j<proc.dynamics.size(); j++)
    {
      String s = (String) proc.dynamics.elementAt(j);
      outData.println(pad+comma+"const char*   a"+s);
      comma = ", ";
    }
  }
  public static int generatePadding(Field field, PrintWriter outData, int pos, int fillerNo)
  {
    int n = pos % padSize(field);
    if (n > 0)
    {
      n = padSize(field)-n;
      outData.println("  char   filler"+fillerNo+"["+n+"];");
    }
    return n;
  }
  public static int generatePadding(PrintWriter outData, int pos, int fillerNo)
  {
    int n = pos % 2;
    if (n > 0)
    {
      n = 2-n;
      outData.println("  char   filler"+fillerNo+"["+n+"];");
    }
    return n;
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppLength(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
      //  return "sizeof(bool)";
    case Field.BYTE:
      //  return "sizeof(signed char)";
    case Field.SHORT:
      return "sizeof(short)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "sizeof(int)";
    case Field.LONG:
      return "sizeof(long)";
    case Field.CHAR:
    case Field.ANSICHAR:
      return ""+(field.length+1);
    case Field.USERSTAMP:
      return "51";
    case Field.BLOB:
    case Field.TLOB:
      return ""+(field.length+1);
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "8";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "sizeof(double)";
    }
    return "0";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppVar(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "short  "+field.useName();
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int    "+field.useName();
    case Field.LONG:
      return "long   "+field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char   "+field.useName()+"["+(field.length+1)+"]";
    case Field.USERSTAMP:
      return "char   "+field.useName()+"[51]";
    case Field.BLOB:
    case Field.TLOB:
      return "char   "+field.useName()+"["+(field.length+1)+"]";
    case Field.DATE:
      return "char   "+field.useName()+"[9]";
    case Field.TIME:
      return "char   "+field.useName()+"[7]";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char   "+field.useName()+"[15]";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double "+field.useName();
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppParm(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "short  a"+field.useName();
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int    a"+field.useName();
    case Field.LONG:
      return "long   a"+field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char*  a"+field.useName();
    case Field.USERSTAMP:
      return "char*  a"+field.useName();
    case Field.BLOB:
    case Field.TLOB:
      return "char*  a"+field.useName();
    case Field.DATE:
      return "char*  a"+field.useName();
    case Field.TIME:
      return "char*  a"+field.useName();
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char*  a"+field.useName();
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double a"+field.useName();
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppCopy(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
    case Field.SEQUENCE:
      return field.useName()+" = a"+field.useName()+";";
    case Field.CHAR:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
      return "strncpy("+field.useName()+", a"+field.useName()+", sizeof("+field.useName()+"));";
    case Field.ANSICHAR:
    case Field.BLOB:
    case Field.TLOB:
      return "memcpy("+field.useName()+", a"+field.useName()+", sizeof("+field.useName()+"));";
    case Field.USERSTAMP:
    case Field.IDENTITY:
    case Field.TIMESTAMP:
      return "// "+field.useName()+" -- generated";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * generate Holding variables
   */
  static void generateCppBind(Field field, PrintWriter outData)
  {
    switch(field.type)
    {
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      outData.println("  TJOCIDate "+field.useName()+"_OCIDate;");
    }
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppBind(Field field, String tableName, boolean isInsert)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName();
    case Field.SEQUENCE:
      //case Field.IDENTITY:
      if (isInsert)
        return "q_.Sequence("+field.useName()+", \""+tableName+"Seq\")";
      else
        return field.useName();
    case Field.CHAR:
      return field.useName()+", "+(field.length+1);
    case Field.ANSICHAR:
      return field.useName()+", "+(field.length+1)+", 1";
    case Field.BLOB:
    case Field.TLOB:
      return field.useName()+", "+(field.length+1);
    case Field.USERSTAMP:
      return "q_.UserStamp("+field.useName()+"), 51";
    case Field.DATE:
      return "q_.Date("+field.useName()+"_OCIDate, "+field.useName()+")";
    case Field.TIME:
      return "q_.Time("+field.useName()+"_OCIDate, "+field.useName()+")";
    case Field.DATETIME:
      return "q_.DateTime("+field.useName()+"_OCIDate, "+field.useName()+")";
    case Field.TIMESTAMP:
      return "q_.TimeStamp("+field.useName()+"_OCIDate, "+field.useName()+")";
    }
    return field.useName() + ", <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppBindArray(Field field, String tableName)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "(short*)  (q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "(int*)    (q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.LONG:
      return "(long*)   (q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.CHAR:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), "+(field.length+1);
    case Field.ANSICHAR:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), "+(field.length+1)+", 1";
    case Field.USERSTAMP:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), 51";
    case Field.BLOB:
    case Field.TLOB:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), "+(field.length+1);
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "(TJOCIDate*)(q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "(double*) (q_.data+"+field.useName().toUpperCase()+"_POS)";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppDefine(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "(short*)  (q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "(int*)    (q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.LONG:
      return "(long*)   (q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.CHAR:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), "+(field.length+1);
    case Field.ANSICHAR:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), "+(field.length+1)+", 1";
    case Field.USERSTAMP:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), 51";
    case Field.BLOB:
    case Field.TLOB:
      return "(char*)   (q_.data+"+field.useName().toUpperCase()+"_POS), "+(field.length+1);
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "(TJOCIDate*)(q_.data+"+field.useName().toUpperCase()+"_POS)";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "(double*) (q_.data+"+field.useName().toUpperCase()+"_POS)";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppGet(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.LONG:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return padder(field.useName()+",",32)+" q_.data+"+field.useName().toUpperCase()+"_POS";
    case Field.CHAR:
    case Field.ANSICHAR:
      return padder(field.useName()+",",32)+" q_.data+"+field.useName().toUpperCase()+"_POS, "+(field.length+1);
    case Field.USERSTAMP:
      return padder(field.useName()+",",32)+" q_.data+"+field.useName().toUpperCase()+"_POS, 51";
    case Field.BLOB:
    case Field.TLOB:
      return padder(field.useName()+",",32)+" q_.data+"+field.useName().toUpperCase()+"_POS, "+(field.length+1);
    case Field.DATE:
      return padder("TJDate("+field.useName()+"),",32)+" q_.data+"+field.useName().toUpperCase()+"_POS";
    case Field.TIME:
      return padder("TJTime("+field.useName()+"),",32)+" q_.data+"+field.useName().toUpperCase()+"_POS";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return padder("TJDateTime("+field.useName()+"),",32)+" q_.data+"+field.useName().toUpperCase()+"_POS";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppPut(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.LONG:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+field.useName()+", "+(field.length+1);
    case Field.USERSTAMP:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+field.useName()+", 51";
    case Field.BLOB:
    case Field.TLOB:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+field.useName()+", "+(field.length+1);
    case Field.DATE:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+"TJDate("+field.useName()+")";
    case Field.TIME:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+"TJTime("+field.useName()+")";
    case Field.DATETIME:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+"TJDateTime("+field.useName()+")";
    case Field.TIMESTAMP:
      return padder("q_.data+"+field.useName().toUpperCase()+"_POS,",32)+"TJDateTime("+field.useName()+")";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   */
  static boolean notString(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.IDENTITY:
    case Field.SEQUENCE:
      return true;
    }
    return false;
  }
  static String varVBType(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return field.useName() + " As Integer";
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " As Long";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return field.useName() + " As String * " + (field.length+1);
    case Field.TLOB:
    case Field.BLOB:
      return field.useName() + " As String * " + (field.length+1);
    case Field.DATE:
      return field.useName() + " As String * 9";
    case Field.TIME:
      return field.useName() + " As String * 7";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return field.useName() + " As String * 15";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " As Double";
    }
    return "As unsupported";
  }
  static int padSize(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return 1;
    case Field.DATE:
      return 1;
    case Field.TIME:
      return 1;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 1;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return 8;
    }
    return 4;
  }
  static int getLength(Field field)
  {
    switch(field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
      return field.length+1;
    case Field.DATE:
      return 9;
    case Field.TIME:
      return 7;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 15;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return 8;
    }
    return 4;
  }
}
