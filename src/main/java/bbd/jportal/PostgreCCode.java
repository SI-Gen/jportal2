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

public class PostgreCCode extends Generator
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
        outLog.println(args[i] + ": Generate C++ Code for PostgreSQL");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
        in.close();
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
    return "Generate C++ Code for PostgreSQL";
  }
  public static String documentation()
  {
    return "Generate C++ Code for PostgreSQL";
  }
  static PlaceHolder placeHolder;
  /**
   * Generates the procedure classes for each table present.
   */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i = 0; i < database.tables.size(); i++)
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
      outLog.println("Code: " + fileName(output, table.useName().toLowerCase(), ".sh"));
      OutputStream outFile = new FileOutputStream(fileName(output, table.useName().toLowerCase(), ".sh"));
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("#ifndef _" + table.useName().toLowerCase() + "SH");
          outData.println("#define _" + table.useName().toLowerCase() + "SH");
          outData.println();
          outData.println("#include <stddef.h>");
          outData.println("#include \"padgen.h\"");
          outData.println("#include \"pgapi.h\"");
          outData.println();
          if (table.hasStdProcs)
            generateStdOutputRec(table, outData);
          generateUserOutputRecs(table, outData);
          generateInterface(table, outData);
          outData.println("#endif");
          outData.flush();
        } finally
        {
          outData.flush();
        }
        outFile.close();
        outLog.println("Code: " + fileName(output, table.useName().toLowerCase(), ".cpp"));
        outFile = new FileOutputStream(
            fileName(output, table.useName().toLowerCase(), ".cpp"));
        outData = new PrintWriter(outFile);
        try
        {
          outData
              .println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println();
          outData.println("#include \"" + fileName("", table.useName().toLowerCase(), ".sh")
              + "\"");
          outData.println();
          generateImplementation(table, outData);
        } finally
        {
          outData.flush();
        }
      } finally
      {
        outFile.close();
      }
    } catch (IOException e1)
    {
      outLog.println("Generate Procs IO Error");
    }
  }
  /**
   * Build of output data rec for standard procedures
   */
  static Vector<String> nullVector = new Vector<String>();
  static String structName = "";
  static void generateStdOutputRec(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = (String)table.comments.elementAt(i);
      outData.println("//" + s);
    }
    int filler = 0;
    structName = "D" + table.useName();
    outData.println("struct D" + table.useName());
    outData.println("{");
    boolean canExtend = true;
    Vector<Field> fields = table.fields;
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      if (field.type == Field.BLOB)
        canExtend = false;
      if (field.comments.size() > 0)
      {
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("  //" + s);
        }
      }
      outData.println("  " + padder(cppVar(field) + ";", 48) + generatePadding(field, filler++));
      if (isNull(field))
      {
        outData.println("  " + padder("int16  " + field.useName() + "IsNull;", 48) + generatePadding(filler++));
        filler++;
      }
    }
    outData.println();
    headerSwaps(outData, "", fields, null);
    String useName = table.useName();
    if (canExtend == true)
      extendHeader(outData, "", fields, useName, nullVector, null);
    else
      extendDataBuildHeader(outData, "", fields, useName, nullVector, null);
    outData.println("};");
    outData.println();
    outData.println("typedef D" + table.useName() + " O" + table.useName() + ";");
    outData.println();
    generateEnumOrdinals(table, outData);
  }
  static void generateUserOutputRecs(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      if (proc.isStdExtended())
        continue;
      String work = "";
      String baseClass = "";
      boolean canExtend = true;
      Vector<Field> fields = proc.outputs;
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field)fields.elementAt(j);
        if (field.type == Field.BLOB)
          canExtend = false;
      }
      fields = proc.inputs;
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field)fields.elementAt(j);
        if (field.type == Field.BLOB)
          canExtend = false;
      }
      fields = proc.outputs;
      if (fields.size() > 0)
      {
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println("//" + comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput())
          typeChar = "O";
        work = " : public " + typeChar + table.useName() + proc.upperFirst();
        baseClass = typeChar + table.useName() + proc.upperFirst();
        structName = typeChar + table.useName() + proc.upperFirst();
        outData.println("struct " + typeChar + table.useName()
            + proc.upperFirst());
        outData.println("{");
        int filler = 0;
        for (int j = 0; j < fields.size(); j++)
        {
          Field field = (Field) fields.elementAt(j);
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  //" + s);
          }
          outData.println("  " + padder(cppVar(field) + ";", 48) + generatePadding(field, filler++));
          if (isNull(field))
            outData.println("  " + padder("int16  " + field.useName() + "IsNull;", 48) + generatePadding(filler++));
        }
        outData.println();
        headerSwaps(outData, "", fields, null);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader(outData, "", fields, useName, nullVector, null);
        else
          extendDataBuildHeader(outData, "", fields, useName, nullVector, null);
        outData.println("};");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        structName = "D" + table.useName() + proc.upperFirst();
        outData.println("struct D" + table.useName() + proc.upperFirst() + work);
        outData.println("{");
        int filler = 0;
        Vector<Field> inputs = proc.inputs;
        for (int j = 0; j < inputs.size(); j++)
        {
          Field field = (Field)inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String)field.comments.elementAt(c);
            outData.println("  //" + s);
          }
          outData.println("  " + padder(cppVar(field) + ";", 48) + generatePadding(field, filler++));
          if (isNull(field))
            outData.println("  " + padder("int16  " + field.useName() + "IsNull;", 48) + generatePadding(filler++));
        }
        outData.println();
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          Integer n = (Integer)proc.dynamicSizes.elementAt(j);
          outData.println("  " + padder("char " + s + "[" + (n.intValue()+1) + "];", 48) + charPadding(n.intValue()+1, filler++));
        }
        headerSwaps(outData, baseClass, inputs, proc);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader(outData, baseClass, inputs, useName, proc.dynamics, proc);
        else
          extendDataBuildHeader(outData, baseClass, inputs, useName, proc.dynamics, proc);
        outData.println("};");
        outData.println();
      }
      else if (fields.size() > 0)
      {
        outData.println("typedef D" + table.useName() + proc.upperFirst()
            + " O" + table.useName() + proc.upperFirst() + ";");
        outData.println();
      }
    }
  }
  private static void headerSwaps(PrintWriter outData, String baseClass, Vector<Field> inputs, Proc proc)
  {
    outData.println("  void Clear()");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::Clear();");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      outData.println("    "+cppInit(field));
    }
    if (proc != null)
    {
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        outData.println("    memset(" + s + ", 0, sizeof(" + s + "));");
      }
    }
    outData.println("  }");
    outData.println("  " + structName + "() { Clear(); }");
    outData.println("  #ifdef swapbytesH");
    outData.println("  void Swaps()");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::Swaps();");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      if (notString(field) == false)
        continue;
      if (isStruct(field) == false)
        outData.println("    SwapBytes(" + field.useName() + ");");
      else
        outData.println("    " + field.useName() + ".Swaps();");
      if (isNull(field))
        outData.println("    SwapBytes(" + field.useName() + "IsNull);");
    }
    outData.println("  }");
    outData.println("  #endif");
  }
  private static void extendHeader(PrintWriter outData, String baseClass, Vector<Field> inputs, String useName, Vector<String> dynamics, Proc proc)
  {
    outData.println("  #if defined(_TBUFFER_H_)");
    outData.println("  void _toXML(TBAmp &XRec)");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::_toXML(XRec);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      outData.println("    " + toXMLFormat(field));
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = (String)dynamics.elementAt(j);
      String front = "XRec.append(\"  <" + str + ">\");";
      String back = "XRec.append(\"</" + str + ">\");";
      outData.println("    " + front + "XRec.ampappend(" + str + ");" + back);
    }
    outData.println("  }");
    outData.println("  void ToXML(TBAmp &XRec, char* Attr=0, char* Outer=\"" + useName + "\")");
    outData.println("  {");
    outData.println("    XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\\n\");");
    outData.println("    _toXML(XRec);");
    outData.println("    XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\\n\");");
    outData.println("  }");
    outData.println("  #endif");
    outData.println("  #if defined(__XMLRECORD_H__)");
    outData.println("  void _fromXML(TBAmp &XRec, TXMLRecord &msg)");
    outData.println("  {");
    outData.println("    TBAmp work;");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::_fromXML(XRec, msg);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      outData.print("    msg.GetValue(\"" + field.useName() + "\", work);");
      outData.println(fromXMLFormat(field));
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = (String)dynamics.elementAt(j);
      outData.print("    msg.GetValue(\"" + str + "\", work);");
      outData.println("memcpy(" + str + ", work.data, sizeof(" + str + ")-1);");
    }
    outData.println("  }");
    outData.println("  void FromXML(TBAmp &XRec)");
    outData.println("  {");
    outData.println("    TXMLRecord msg;");
    outData.println("    msg.Load(XRec);");
    outData.println("    memset(this, 0, sizeof(*this));");
    outData.println("    _fromXML(XRec, msg);");
    outData.println("  }");
    outData.println("  #endif");
    extendDataBuildHeader(outData, baseClass, inputs, useName, dynamics, proc);
  }
  private static String nullAdd(Field field)
  {
    if (isNull(field))
      return ", "+field.useName()+"IsNull";
    return "";
  }
  private static String nullSet(Field field)
  {
    if (isNull(field))
      return ", " + field.useName() + "IsNull";
    return "";
  }
  private static void extendDataBuildHeader(PrintWriter outData, String baseClass, Vector<Field> inputs, String useName, Vector<String> dynamics, Proc proc)
  {
    outData.println("  #if defined(_DATABUILD_H_)");
    int inputNo = 0;
    if (baseClass.length() > 0)
    {
      for (int j = 0; j < inputs.size(); j++)
      {
        Field field = (Field)inputs.elementAt(j);
        if (proc != null && proc.hasOutput(field.name))
          continue;
        inputNo++;
      }
      outData.println("  static int NoBuildFields() {return " + baseClass + "::NoBuildFields()+" + (inputNo + dynamics.size()) + ";}");
    }
    else
      outData.println("  static int NoBuildFields() {return " + (inputs.size() + dynamics.size()) + ";}");
    outData.println("  void _buildAdds(DataBuilder &dBuild)");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::_buildAdds(dBuild);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      if (field.type == Field.BLOB)
        outData.println("    dBuild.add(\"" + field.useName() + "\", " + field.useName() + ".len, " + field.useName() + ".data" + nullAdd(field) + ");");
      else
        outData.println("    dBuild.add(\"" + field.useName() + "\", " + field.useName() + nullAdd(field) + ");");
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = (String)dynamics.elementAt(j);
      outData.println("    dBuild.add(\"" + str + "\", " + str + ");");
    }
    outData.println("  }");
    outData.println("  void BuildData(DataBuilder &dBuild, char *name=\"" + useName + "\")");
    outData.println("  {");
    outData.println("    dBuild.name(name);");
    outData.println("    _buildAdds(dBuild);");
    outData.println("  }");
    outData.println("  void _buildSets(DataBuilder &dBuild)");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::_buildSets(dBuild);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      if (field.type == Field.BLOB)
        outData.println("    dBuild.set(\"" + field.useName() + "\", " + field.useName() + ".len, " + field.useName() + ".data, sizeof(" + field.useName() + ".data)" + nullSet(field) + ");");
      else
        outData.println("    dBuild.set(\"" + field.useName() + "\", " + field.useName() + ", sizeof(" + field.useName() + ")" + nullSet(field) + ");");
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = (String)dynamics.elementAt(j);
      outData.println("    dBuild.set(\"" + str + "\", " + str + ", sizeof(" + str + "));");
    }
    outData.println("  }");
    outData.println("  void SetData(DataBuilder &dBuild, char *name=\"" + useName + "\")");
    outData.println("  {");
    outData.println("    dBuild.name(name);");
    outData.println("    _buildSets(dBuild);");
    outData.println("  }");
    outData.println("  #endif");
  }
  /**
   * Build of output data rec for standard procedures
   */
  static void generateInterface(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateInterface(table, proc, outData);
    }
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateInterface(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.comments.size() > 0)
    {
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("  //" + comment);
      }
    }
    if (proc.hasNoData())
    {
      outData.println("struct T" + table.useName() + proc.upperFirst());
      outData.println("{");
      outData.println("  TJQuery q_;");
      outData.println("  void Exec();");
      outData.println("  T" + table.useName() + proc.upperFirst()
          + "(TJConnector &conn, const char *aFile=__FILE__, long aLine=__LINE__)");
      outData.println("  : q_(conn)");
      outData.println("  {q_.FileAndLine(aFile,aLine);}");
      outData.println("};");
      outData.println();
    } else
    {
      if (proc.isStdExtended() || proc.isStd)
        dataStruct = "D" + table.useName();
      else
        dataStruct = "D" + table.useName() + proc.upperFirst();
      outData.println("struct T" + table.useName() + proc.upperFirst()
          + " : public " + dataStruct);
      outData.println("{");
      generateInterface(table, proc, dataStruct, outData);
      outData.println("};");
      outData.println();
    }
  }
  /**
   * 
   */
  static void generateImplementation(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateImplementation(table, proc, outData);
    }
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, PlaceHolder.DOLLAR_NO, "");
    String fullName = table.useName() + proc.name;
    outData.println("void T" + fullName + "::Exec()");
    outData.println("{");
    generateCommand(proc, outData);
    outData.println("  q_.Open(q_.command, " + proc.inputs.size() + ", " + proc.outputs.size() + ");");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      generateCppBind(field, outData);
    }
    for (int j = 0; j < placeHolder.pairs.size(); j++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(j);
      Field field = pair.field;
      outData.println("  q_.Bind"+(field.type == Field.BLOB ? "Blob" : "")+"("
          + padder("" + j + ",", 4)
          + cppBind(field, table.name, proc.isInsert)
          + ((isNull(field)) ? ", &" + field.useName() + "IsNull" : "")
          + ((field.type == Field.ANSICHAR) ? ", 1" : "")
          + ");");
    }
    //for (int j = 0; j < proc.outputs.size(); j++)
    //{
    //  Field field = (Field) proc.outputs.elementAt(j);
    //  outData.println("  q_.Define" + (field.type == Field.BLOB ? "Blob" : "") + "(" + padder("" + j + ",", 4)
    //      + cppDefine(field) + ");");
    //}
    outData.println("  q_.Exec();");
    outData.println("}");
    outData.println();
    boolean skipExecWithParms = false;
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (field.type == Field.BLOB)
      {
        skipExecWithParms = true;
        break;
      }
    }
    if (skipExecWithParms == false)
    {
      if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      {
        outData.println("void T" + fullName + "::Exec(");
        generateWithParms(proc, outData, "");
        outData.println(")");
        outData.println("{");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field)proc.inputs.elementAt(j);
          if ((field.type == Field.SEQUENCE && proc.isInsert)
              || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
              || field.type == Field.USERSTAMP)
            continue;
          outData.println("  " + cppCopy(field));
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.println("  strncpy(" + s + ", a" + s + ", sizeof(" + s
              + ")-1);");
        }
        outData.println("  Exec();");
        outData.println("}");
        outData.println();
      }
    }
    if (proc.outputs.size() > 0)
    {
      outData.println("bool T" + fullName + "::Fetch()");
      outData.println("{");
      outData.println("  if (q_.Fetch() == false)");
      outData.println("    return false;");
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        outData.println("  q_.Get("+ j + ", " + cppGet(field) + ");");
        if (isNull(field))
          outData.println("  q_.GetNull(" + j + ", " +field.useName() + "IsNull);");
      }
      outData.println("  return true;");
      outData.println("}");
      outData.println();
    }
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    Vector<String> lines = placeHolder.getLines();
    int size = 1;
    for (int i = 0; i < lines.size(); i++)
    {
      String l = (String)lines.elementAt(i);
      if (l.charAt(0) == '"')
        size += (l.length()+2);
      else
      {
        String var = l.trim();
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          if (var.compareTo(s) == 0)
          {
            Integer n = (Integer)proc.dynamicSizes.elementAt(j);
            size += (n.intValue()+2);
          }
        }
      }
    }
    outData.println("  if (q_.command == 0)");
    outData.println("    q_.command = new char [" + size + "];");
    outData.println("  memset(q_.command, 0, sizeof(q_.command));");
    String strcat = "  strcat(q_.command, ";
    String terminate = "";
    if (lines.size() > 0)
    {
      for (int i = 0; i < lines.size(); i++)
      {
        String l = (String)lines.elementAt(i);
        if (l.charAt(0) != '"')
        {
          terminate = ");";
          strcat = "  strcat(q_.command, ";
          outData.println(terminate);
        }
        else if (i != 0)
          outData.println(terminate);
        outData.print(strcat + l);
        if (l.charAt(0) == '"')
        {
          terminate = "\"\\n\"";
          strcat = "                     ";
        }
      }
      outData.println(");");
    }
  }
  /**
   * generate Holding variables
   */
  static void generateCppBind(Field field, PrintWriter outData)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        outData.println("  int16 " + field.useName() + "_INT16;");
        break;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        outData.println("  int32 " + field.useName() + "_INT32;");
        break;
      case Field.LONG:
        outData.println("  int64 " + field.useName() + "_INT64;");
        break;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision <= 15)
          outData.println("  double " + field.useName() + "_DOUBLE;");
        break;
      case Field.DATE:
        outData.println("  TPGDate " + field.useName() + "_PGDate;");
        break;
      case Field.TIME:
        outData.println("  TPGTime " + field.useName() + "_PGTime;");
        break;
      case Field.DATETIME:
        outData.println("  TPGDateTime " + field.useName() + "_PGDateTime;");
        break;
      case Field.TIMESTAMP:
        outData.println("  TPGDateTime " + field.useName() + "_PGTimeStamp;");
        break;
    }
  }
  static void generateWithParms(Proc proc, PrintWriter outData, String pad)
  {
    String comma = "  ";
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE && proc.isInsert)
          || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
          || field.type == Field.USERSTAMP)
        continue;
      outData.println(pad + comma + "const " + cppParm(field));
      comma = ", ";
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String) proc.dynamics.elementAt(j);
      outData.println(pad + comma + "const char*   a" + s);
      comma = ", ";
    }
  }
  static void generateInterface(Table table, Proc proc, String dataStruct,
      PrintWriter outData)
  {
    outData.println("  TJQuery q_;");
    outData.println("  void Exec();");
    outData.println("  void Exec(" + dataStruct
        + "& Rec) {*DRec() = Rec;Exec();}");
    boolean skipExecWithParms = false;
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (field.type == Field.BLOB)
      {
        skipExecWithParms = true;
        break;
      }
    }
    if (skipExecWithParms == false)
    {
      if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      {
        outData.println("  void Exec(");
        generateWithParms(proc, outData, "  ");
        outData.println("  );");
      }
    }
    if (proc.outputs.size() > 0)
      outData.println("  bool Fetch();");
    outData.println("  T" + table.useName() + proc.upperFirst()
        + "(TJConnector &conn, const char *aFile=__FILE__, long aLine=__LINE__)");
    outData.println("  : q_(conn)");
    outData.println("  {Clear();q_.FileAndLine(aFile,aLine);}");
    outData.println("  " + dataStruct + "* DRec() {return this;}");
    if (proc.outputs.size() > 0)
      outData.println("  O" + dataStruct.substring(1)
          + "* ORec() {return this;}");
  }
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  public static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("enum e" + table.useName() + field.useName());
        String start = "{";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = (Enum)field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)element.value + "'";
          outData.println(start + " " + table.useName() + field.useName() + element.name + " = " + evalue);
          start = ",";
        }
        outData.println("};");
        outData.println();
        outData.println("inline const char *" + table.useName() + field.useName() + "Lookup(int no)");
        outData.println("{");
        outData.println("  switch(no)");
        outData.println("  {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = (Enum)field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)element.value + "'";
          outData.println("  case "+ evalue+": return \""+element.name+"\";");
        }
        outData.println("  default: return \"<unknown value>\";");
        outData.println("  }");
        outData.println("}");
      }
    }
  }
  static String fileName(String output, String node, String ext)
  {
    return output + node + ext;
  }
  private static String charPadding(int no, int fillerNo)
  {
    int n = 8 - (no % 8);
    if (n != 8)
      return "IDL2_CHAR_PAD(" + fillerNo + "," + n + ");";
    return "";
  }
  private static String generatePadding(Field field, int fillerNo)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "IDL2_INT16_PAD(" + fillerNo + ");";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "IDL2_INT32_PAD(" + fillerNo + ");";
      //case Field.LONG:
      //  return "// IDL2_INT64_PAD(" + fillerNo + ");";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.TLOB:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
        return charPadding(field.length + 1, fillerNo);
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return charPadding(field.precision + 3, fillerNo);
        break;
      case Field.MONEY:
        return charPadding(21, fillerNo);
    }
    return "";
  }
  public static String generatePadding(int fillerNo)
  {
    return "IDL2_INT16_PAD(" + fillerNo + ");";
  }
  static int getLength(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.LONG:
      return 8;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
      return field.length + 1;
    case Field.BLOB:
      return 8;
    case Field.DATE:
      return 9;
    case Field.TIME:
      return 7;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 15;
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return field.precision + 3; // allow for - . and null terminator
      return 8;
    case Field.MONEY:
      return 21;
  }
    return 4;
  }
  static boolean isNull(Field field)
  {
    if (field.isNull == false)
      return false;
    switch (field.type)
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
      case Field.BLOB:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.TIME:
        return true;
    }
    return false;
  }
  static boolean notString(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.IDENTITY:
    case Field.SEQUENCE:
    case Field.BLOB:
      return true;
    case Field.FLOAT:
    case Field.DOUBLE:
      return field.precision <= 15;
  }
    return false;
  }
  static boolean isStruct(Field field)
  {
    return field.type == Field.BLOB;
  }
  static boolean isLob(Field field)
  {
    return field.type == Field.BLOB;
  }
  static String cppInit(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
      case Field.LONG:
        return field.useName()+" = 0;";
      //return "long   " + field.useName();
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
        return "memset(" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.BLOB:
        return "memset(&" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision <= 15)
          return field.useName()+" = 0.0;";
        return "memset(" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.MONEY:
        return "memset(" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppVar(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "int16  " + field.useName();
    case Field.INT:
    case Field.IDENTITY:
    case Field.SEQUENCE:
      return "int32  " + field.useName();
    case Field.LONG:
      return "int64  " + field.useName();
      //return "long   " + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.TLOB:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.USERSTAMP:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.BLOB:
      return "TJBlob<" + field.length + "> " + field.useName();
    case Field.DATE:
      return "char   " + field.useName() + "[9]";
    case Field.TIME:
      return "char   " + field.useName() + "[7]";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char   " + field.useName() + "[15]";
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return "char   " + field.useName() + "[" + (field.precision+3) + "]";
      return "double " + field.useName();
    case Field.MONEY:
      return "char   " + field.useName() + "[21]";
  }
    return field.useName() + " <unsupported>";
  }
  static String cppLength(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "sizeof(int16)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "sizeof(int32)";
    case Field.LONG:
      return "sizeof(int64)";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.TLOB:
      return "" + (field.length + 1);
    case Field.BLOB:
      return "sizeof(TJBlob<" + field.length + ">)";
    case Field.USERSTAMP:
      return "51";
    case Field.DATE:
      return "sizeof(TPGDate)";
    case Field.TIME:
      return "sizeof(TPGTime)";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "sizeof(TPGDateTime)";
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return "" + (field.precision + 3);
      return "sizeof(double)";
    case Field.MONEY:
      return "21";
  }
    return "0";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppBind(Field field, String tableName, boolean isInsert)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return field.useName() + ", " + field.useName() + "_INT16";
    case Field.INT:
      return field.useName() + ", " + field.useName() + "_INT32";
    case Field.LONG:
      return field.useName() + ", " + field.useName() + "_INT64";
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return field.useName() + ", " + (field.precision) + ", " + (field.scale);
      return field.useName() + ", " + field.useName() + "_DOUBLE, " + (field.precision) + ", " + (field.scale);
    case Field.MONEY:
      return field.useName() + ", 18, 2";
    case Field.SEQUENCE:
      if (isInsert)
        return "q_.Sequence(" + field.useName() + ", \"" + tableName + "_" + field.useName() + "_seq\"), " + field.useName() + "_INT32";
      else
        return field.useName() + ", " + field.useName() + "_INT32";
    case Field.TLOB:
      return field.useName() + ", " + (field.length);
    case Field.CHAR:
      return field.useName() + ", " + (field.length);
    case Field.ANSICHAR:
      return field.useName() + ", " + (field.length);
    case Field.USERSTAMP:
      return "q_.UserStamp(" + field.useName() + ", sizeof(" + field.useName() + ")), 50";
    case Field.DATE:
      return "q_.Date(" + field.useName() + "_PGDate, " + field.useName()
          + ")";
    case Field.TIME:
      return "q_.Time(" + field.useName() + "_PGTime, " + field.useName()
          + ")";
    case Field.DATETIME:
      return "q_.DateTime(" + field.useName() + "_PGDateTime, " + field.useName()
          + ")";
    case Field.TIMESTAMP:
      return "q_.TimeStamp(" + field.useName() + "_PGTimeStamp, " + field.useName()
          + ")";
    case Field.BLOB:
      return "(char*)&" + field.useName() + ", sizeof("+ field.useName() + ".data)";
  }
    return field.useName() + ", <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppBindArray(Field field, String tableName)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "(int16*)  (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return "(int32*)    (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.LONG:
        return "(int64*)   (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.CHAR:
        return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
            + (field.length + 1);
      case Field.ANSICHAR:
        return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
            + (field.length + 1) + ", 1";
      case Field.USERSTAMP:
        return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), 51";
      case Field.DATE:
        return "(TPGDate*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.TIME:
        return "(TPGTime*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
        return "(TPGDateTime*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.precision + 3);
        return "(double*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.MONEY:
        return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
            + (field.precision + 3);
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppDefine(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "(int16*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        return "(int32*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.LONG:
        return "(int64*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.CHAR:
      case Field.TLOB:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), "
            + (field.length + 1);
      case Field.ANSICHAR:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), "
            + (field.length + 1) + ", 1";
      case Field.USERSTAMP:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), 51";
      case Field.BLOB:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), sizeof(" + field.useName() + ")";
      case Field.DATE:
        return "(TPGDate*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.TIME:
        return "(TPGTime*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
        return "(TPGDateTime*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.precision + 3);
        return "(double*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.MONEY:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), 21";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppGet(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.LONG:
      return field.useName();
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return field.useName() + ", " + (field.precision + 3);
      return field.useName();
    case Field.MONEY:
      return field.useName() + ", 21";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.TLOB:
      return field.useName() + ", " + (field.length + 1);
    case Field.USERSTAMP:
      return field.useName() + ", 51";
    case Field.BLOB:
      return field.useName() + ".len, " + field.useName()+".data, sizeof(" + field.useName() + ")";
    case Field.DATE:
      return "TJDate(" + field.useName() + ")";
    case Field.TIME:
      return "TJTime(" + field.useName() + ")";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "TJDateTime(" + field.useName() + ")";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppCopy(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
      return field.useName() + " = a" + field.useName() + ";";
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return "strncpy(" + field.useName() + ", a" + field.useName()
            + ", sizeof(" + field.useName() + ")-1);";
      return field.useName() + " = a" + field.useName() + ";";
    case Field.MONEY:
      return "strncpy(" + field.useName() + ", a" + field.useName()
          + ", sizeof(" + field.useName() + ")-1);";
    case Field.CHAR:
    case Field.TLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
      return "strncpy(" + field.useName() + ", a" + field.useName()
          + ", sizeof(" + field.useName() + ")-1);";
    case Field.ANSICHAR:
      return "memcpy(" + field.useName() + ", a" + field.useName()
          + ", sizeof(" + field.useName() + "));";
    case Field.BLOB:
      return field.useName() + " = a" + field.useName() + ";";
    case Field.USERSTAMP:
    case Field.IDENTITY:
    case Field.TIMESTAMP:
      return "// " + field.useName() + " -- generated";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppParm(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "int16  a" + field.useName();
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int32   a" + field.useName();
    case Field.LONG:
      return "int64  a" + field.useName();
    case Field.CHAR:
    case Field.TLOB:
    case Field.ANSICHAR:
      return "char*  a" + field.useName();
    case Field.USERSTAMP:
      return "char*  a" + field.useName();
    case Field.DATE:
      return "char*  a" + field.useName();
    case Field.TIME:
      return "char*  a" + field.useName();
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char*  a" + field.useName();
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return "char*  a" + field.useName();
      return "double a" + field.useName();
    case Field.MONEY:
      return "char*  a" + field.useName();
    }
    return field.useName() + " <unsupported>";
  }
  static String fromXMLFormat(Field field)
  {
    switch (field.type)
    {
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.TLOB:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
        return "memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
      case Field.BOOLEAN:
      case Field.BYTE:
        return field.useName() + " = (int8)atol(work.data);";
      case Field.SHORT:
        return field.useName() + " = (int16)atol(work.data);";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return field.useName() + " = (int32)atol(work.data);";
      case Field.LONG:
        return field.useName() + " = (int64)atol(work.data);";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
        return field.useName() + " = atof(work.data);";
      case Field.MONEY:
        return "memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
    }
    return "// " + field.useName() + " <unsupported>";
  }
  static String toXMLFormat(Field field)
  {
    String front = "XRec.append(\"  <" + field.useName() + ">\");";
    String back = "XRec.append(\"</" + field.useName() + ">\");";
    switch (field.type)
    {
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return front+"XRec.ampappend("+field.useName()+");"+back;
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
      return front + "XRec.ampappend(JP_XML_FORMAT((int32)" + field.useName() + ").result);" + back;
    case Field.LONG:
      return front + "XRec.ampappend(JP_XML_FORMAT((int64)" + field.useName() + ").result);" + back;
    case Field.FLOAT:
    case Field.DOUBLE:
      if (field.precision > 15)
        return front + "XRec.ampappend(" + field.useName() + ");" + back;
      return front + "XRec.ampappend(JP_XML_FORMAT((double)" + field.useName() + ").result);" + back;
    case Field.MONEY:
      return front + "XRec.ampappend(" + field.useName() + ");" + back;
  }
    return "// "+field.useName() + " <unsupported>";
  }
}
