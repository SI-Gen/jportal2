/// ------------------------------------------------------------------
/// Copyright (c) 1996, 2018 Vincent Risi in Association 
///                          with Barone Budge and Dominick 
/// All rights reserved. 
/// This program and the accompanying materials are made available 
/// under the terms of the Common Public License v1.0 
/// which accompanies this distribution and is available at 
/// http://www.eclipse.org/legal/cpl-v10.html 
/// Contributors:
///    Vincent Risi
/// ------------------------------------------------------------------
/// System : JPortal
/// ------------------------------------------------------------------
package bbd.jportal2.generators;
import bbd.jportal2.*;
import bbd.jportal2.Enum;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MSSqlCCode extends BaseGenerator implements IBuiltInSIProcessor
{
    public MSSqlCCode() {
        super(MSSqlCCode.class);
    }

    /**
   * Reads input from stored repository
   */

  public String description()
  {
    return "Generate MSSql C++ Code ODBC";
  }
  public String documentation()
  {
    return "Generate MSSql C++ Code ODBC";
  }
  static PlaceHolder placeHolder;
  /**
   * Generates the procedure classes for each table present.
   */
  public void generate(Database database, String output) throws Exception {
    for (int i = 0; i < database.tables.size(); i++) {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output);
    }
  }
  void generate(Table table, String output) throws Exception {
    try (PrintWriter outData = this.openOutputFileForGeneration("sh", fileName(output, table.useName().toLowerCase(), ".sh"))) {
      outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
      outData.println("#ifndef _" + table.useName().toLowerCase() + "SH_MARSHAL_VERSION");
      outData.println("#define _" + table.useName().toLowerCase() + "SH_MARSHAL_VERSION");
      outData.println();
      outData.println("#include <stddef.h>");
      outData.println("#include \"padgen.h\"");
      outData.println("#include \"mssapi.h\"");
      outData.println("#include \"swapbytes.h\"");
      outData.println();
      if (table.hasStdProcs)
        generateStdOutputRec(table, outData);
      generateUserOutputRecs(table, outData);
      generateInterface(table, outData);
      outData.println("#endif");
      outData.flush();
    }
    try (PrintWriter outData = this.openOutputFileForGeneration("cpp", fileName(output, table.useName().toLowerCase(), ".cpp"))) {
      outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
      outData.println();
      outData.println("#include \"" + fileName("", table.useName().toLowerCase(), ".sh") + "\"");
      outData.println();
      generateImplementation(table, outData);
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
          outData.println("#include \"mssapi.h\"");
          outData.println("#include \"swapbytes.h\"");
          outData.println();
          if (table.hasStdProcs)
            generateStdOutputRec(table, outData);
          generateUserOutputRecs(table, outData);
          generateInterface(table, outData);
          outData.println("#endif");
          outData.flush();
        }
        finally
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
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println();
          outData.println("#include \"" + fileName("", table.useName().toLowerCase(), ".sh") + "\"");
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
  private static void generateSnips(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName().toLowerCase() + "_snips.h");
      OutputStream outFile = new FileOutputStream(output + table.useName().toLowerCase() + "_snips.h");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("#ifndef _" + table.useName().toUpperCase() + "_SNIPS_H_");
          outData.println("#define _" + table.useName().toUpperCase() + "_SNIPS_H_");
          outData.println();
          outData.println("#include \"list.h\"");
          outData.println("#include \"" + table.useName().toLowerCase() + ".sh\"");
          outData.println();
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData)
              continue;
            //outData.println("#ifdef USE_" + table.useName().toUpperCase() + "_" + proc.name.toUpperCase());
            if (proc.isMultipleInput)
              generateSnipsBulkAction(table, proc, outData);
            else if (proc.isInsert && proc.hasReturning)
              generateSnipsAction(table, proc, outData);
            else if (proc.outputs.size() > 0)
              if (proc.isSingle)
                generateSnipsSingle(table, proc, outData);
              else
                generateSnipsMultiple(table, proc, outData);
            else
              generateSnipsAction(table, proc, outData);
            //outData.println("#endif");
            outData.println();
          }
          outData.println("#endif");
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
  private static void generateSnipsAction(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.print("inline void " + table.useName() + proc.upperFirst() + "(TJConnector *connect");
    if (hasInput || proc.hasModifieds())
      outData.print(", D" + dataStruct + " *rec");
    outData.println(")");
    outData.println("{");
    outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    if (hasInput || proc.hasModifieds())
      outData.println("  q.Exec(*rec);");
    else
      outData.println("  q.Exec();");
    if (proc.hasReturning && proc.outputs.size() > 0)
    {
      outData.println("  if (q.Fetch())");
      outData.println("    *rec = *q.DRec();");
    }
    else if (proc.hasModifieds())
      outData.println("  *rec = *q.DRec();");
    outData.println("}");
  }
  private static void generateSnipsBulkAction(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    outData.print("inline void " + table.useName() + proc.upperFirst() + "(TJConnector *connect");
    outData.print(", int noOf, D" + dataStruct + " *recs");
    outData.println(")");
    outData.println("{");
    outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    outData.println("  q.Exec(noOf, recs);");
    outData.println("}");
  }
  private static void generateSnipsSingle(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.println("inline bool " + table.useName() + proc.upperFirst()
                   + "(TJConnector *connect, D" + dataStruct + " *rec)");
    outData.println("{");
    outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    if (hasInput)
      outData.println("  q.Exec(*rec);");
    else
      outData.println("  q.Exec();");
    outData.println("  if (q.Fetch())");
    outData.println("  {");
    outData.println("    *rec = *q.DRec();");
    outData.println("    return true;");
    outData.println("  }");
    outData.println("  return false;");
    outData.println("}");
  }
  private static void generateSnipsMultiple(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.print("inline void " + table.useName() + proc.upperFirst() + "(TJConnector *connect");
    if (hasInput)
      outData.print(", D" + dataStruct + "* inRec");
    outData.println(", int32* noOf, O" + dataStruct + "*& outRecs)");
    outData.println("{");
    outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    if (hasInput)
      outData.println("  q.Exec(*inRec);");
    else
      outData.println("  q.Exec();");
    outData.println("  while (q.Fetch())");
    outData.println("    SnipAddList(outRecs, *noOf, *q.ORec(), (int32)q.NOROWS);");
    outData.println("}");
  }
  /**
   * Build of output data rec for standard procedures
   */
  static Vector nullVector = new Vector();
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
    Vector fields = table.fields;
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      if (field.type == Field.BLOB)
        canExtend = false;
      if (field.comments.size() > 0)
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("  //" + s);
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
    // we cannot put in a virtual destructor here as the data has to remaina POD for GNU
    //outData.println("  virtual ~D" + table.useName() + "() {}");
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
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      if (proc.isStdExtended())
        continue;
      String work = "";
      String baseClass = "";
      boolean canExtend = true;
      Vector fields = proc.outputs;
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
          String comment = (String)proc.comments.elementAt(j);
          outData.println("//" + comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput())
          typeChar = "O";
        work = " : public " + typeChar + table.useName() + proc.upperFirst();
        baseClass = typeChar + table.useName() + proc.upperFirst();
        structName = typeChar + table.useName() + proc.upperFirst();
        outData.println("struct " + typeChar + table.useName() + proc.upperFirst());
        outData.println("{");
        int filler = 0;
        for (int j = 0; j < fields.size(); j++)
        {
          Field field = (Field)fields.elementAt(j);
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
        headerSwaps(outData, "", fields, null);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader(outData, "", fields, useName, nullVector, null);
        else
          extendDataBuildHeader(outData, "", fields, useName, nullVector, null);
        // we cannot put in a virtual destructor here as the data has to remaina POD for GNU
        //outData.println("  virtual ~" + typeChar + table.useName() + proc.upperFirst() + "() {}");
        outData.println("};");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        structName = "D" + table.useName() + proc.upperFirst();
        outData.println("struct D" + table.useName() + proc.upperFirst() + work);
        outData.println("{");
        int filler = 0;
        Vector inputs = proc.inputs;
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
          outData.println("  " + padder("char " + s + "[" + (n.intValue() + 1) + "];", 48) + charPadding(n.intValue() + 1, filler++));
        }
        headerSwaps(outData, baseClass, inputs, proc);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader(outData, baseClass, inputs, useName, proc.dynamics, proc);
        else
          extendDataBuildHeader(outData, baseClass, inputs, useName, proc.dynamics, proc);
        // we cannot put in a virtual destructor here as the data has to remaina POD for GNU
        //outData.println("  virtual ~D" + table.useName() + proc.upperFirst() + "() {}");
        outData.println("};");
        outData.println();
      }
      else if (fields.size() > 0)
      {
        outData.println("typedef D" + table.useName() + proc.upperFirst() + " O" + table.useName() + proc.upperFirst() + ";");
        outData.println();
      }
    }
  }
  private static void headerSwaps(PrintWriter outData, String baseClass, Vector inputs, Proc proc)
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
      outData.println("    " + cppInit(field));
    }
    if (proc != null)
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        outData.println("    memset(" + s + ", 0, sizeof(" + s + "));");
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
  private static void extendHeader(PrintWriter outData, String baseClass, Vector inputs, String useName, Vector dynamics, Proc proc)
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
    outData.println("  void ToXML(TBAmp &XRec, const char* Attr, const char* Outer)");
    outData.println("  {");
    outData.println("    XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\\n\");");
    outData.println("    _toXML(XRec);");
    outData.println("    XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\\n\");");
    outData.println("  }");
    outData.println("  void ToXML(TBAmp &XRec, const char* Attr) {ToXML(XRec, Attr, \"" + useName + "\");}");
    outData.println("  void ToXML(TBAmp &XRec) {ToXML(XRec, 0);}");
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
      return ", " + field.useName() + "IsNull";
    return "";
  }
  private static String nullSet(Field field)
  {
    if (isNull(field))
      return ", " + field.useName() + "IsNull";
    return "";
  }
  private static void extendDataBuildHeader(PrintWriter outData, String baseClass, Vector inputs, String useName, Vector dynamics, Proc proc)
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
    outData.println("  void BuildData(DataBuilder &dBuild, const char* name)");
    outData.println("  {");
    outData.println("    dBuild.name(name);");
    outData.println("    _buildAdds(dBuild);");
    outData.println("  }");
    outData.println("  void BuildData(DataBuilder &dBuild) {BuildData(dBuild, \"" + useName + "\");}");
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
    outData.println("  void SetData(DataBuilder &dBuild, const char* name)");
    outData.println("  {");
    outData.println("    dBuild.name(name);");
    outData.println("    _buildSets(dBuild);");
    outData.println("  }");
    outData.println("  void SetData(DataBuilder &dBuild) {SetData(dBuild, \"" + useName + "\");}");
    outData.println("  #endif");
  }
  /**
   * Build of output data rec for standard procedures
   */
  static void generateInterface(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
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
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = (String)proc.comments.elementAt(i);
        outData.println("  //" + comment);
      }
    if (proc.hasNoData())
    {
      outData.println("struct T" + table.useName() + proc.upperFirst());
      outData.println("{");
      outData.println("  TJQuery q_;");
      outData.println("  void Exec();");
      outData.println("  T" + table.useName() + proc.upperFirst() + "(TJConnector &conn, const char* aFile=__FILE__, long aLine=__LINE__)");
      outData.println("  : q_(conn)");
      outData.println("  {q_.FileAndLine(aFile,aLine);}");
      outData.println("};");
      outData.println();
    }
    else
    {
      if (proc.isStdExtended() || proc.isStd)
        dataStruct = "D" + table.useName();
      else
        dataStruct = "D" + table.useName() + proc.upperFirst();
      outData.println("struct T" + table.useName() + proc.upperFirst() + " : public " + dataStruct);
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
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isMultipleInput)
        generateMultipleImplementation(table, proc, outData);
      else
        generateImplementation(table, proc, outData);
    }
  }
  static String useNull(Field field)
  {
    if (isNull(field) 
    || (field.type == Field.CHAR && field.isNull == true) 
|| (field.type == Field.ANSICHAR && field.isNull == true))
      return ", " + field.useName() + "IsNull);";
    return ");";
  }
  static void generateMultipleImplementation(Table table, Proc proc, PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    String dataStruct;
    if (proc.isStdExtended() || proc.isStd)
      dataStruct = "D" + table.useName();
    else
      dataStruct = "D" + table.useName() + proc.upperFirst();
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    String fullName = table.useName() + proc.upperFirst();
    outData.println("void T" + fullName + "::Exec(int32 noOf, " + dataStruct + " *Recs)");
    outData.println("{");
    generateCommand(proc, outData);
    outData.println("  q_.OpenArray(q_.command, NOBINDS, NONULLS, noOf, ROWSIZE);");
    for (int i = 0, n = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      outData.println("  " + cppArrayPointer(field));
      if (isNull(field))
        outData.println("  SQLINTEGER* " + field.useName() + "IsNull = &q_.indicators[noOf*" + n++ + "];");
      else if (field.type == Field.CHAR && field.isNull == true)
        outData.println("  SQLINTEGER* " + field.useName() + "IsNull = &q_.indicators[noOf*" + n++ + "];");
      else if (field.type == Field.ANSICHAR && field.isNull == true)
        outData.println("  SQLINTEGER* " + field.useName() + "IsNull = &q_.indicators[noOf*" + n++ + "];");

    }
    outData.println("  for (int i=0; i<noOf; i++)");
    outData.println("  {");
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      outData.println("    " + cppArrayCopy(field));
      if (isNull(field))
        outData.println("    " + field.useName() + "IsNull[i] = Recs[i]." + field.useName() + "IsNull;");
      else if (field.type == Field.CHAR && field.isNull == true)
        outData.println("    " + field.useName() + "IsNull[i] = strlen(Recs[i]." + field.useName() + ") == 0 ? JP_NULL : SQL_NTS;");
      else if (field.type == Field.ANSICHAR && field.isNull == true)
        outData.println("    " + field.useName() + "IsNull[i] = strlen(Recs[i]." + field.useName() + ") == 0 ? JP_NULL : SQL_NTS;");
    }
    outData.println("  }");
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      String size = field.useName().toUpperCase() + "_SIZE";
      switch (field.type)
      {
        case Field.ANSICHAR:
          outData.println("  q_.BindAnsiCharArray(" + i + ", " + field.useName() + ", " + size + useNull(field));
          break;
        case Field.CHAR:
        case Field.TLOB:
        case Field.XML:
        case Field.USERSTAMP:
          outData.println("  q_.BindCharArray(" + i + ", " + field.useName() + ", " + size + useNull(field));
          break;
        //case Field.BIGXML:
        //  break;
        case Field.LONG:
        case Field.BIGSEQUENCE:
        case Field.BIGIDENTITY:
          outData.println("  q_.BindInt64Array(" + i + ", " + field.useName() + useNull(field));
          break;
        case Field.INT:
        case Field.SEQUENCE:
        case Field.IDENTITY:
          outData.println("  q_.BindInt32Array(" + i + ", " + field.useName() + useNull(field));
          break;
        case Field.BOOLEAN:
        case Field.BYTE:
        case Field.SHORT:
          outData.println("  q_.BindInt16Array(" + i + ", " + field.useName() + useNull(field));
          break;
        case Field.FLOAT:
        case Field.DOUBLE:
          if (field.precision <= 15)
            outData.println("  q_.BindDoubleArray(" + i + ", " + field.useName() + ", " + (field.precision) + ", " + (field.scale) + useNull(field));
          else
            outData.println("  q_.BindMoneyArray(" + i + ", " + field.useName() + ", " + (field.precision) + ", " + (field.scale) + useNull(field));
          break;
        case Field.MONEY:
          outData.println("  q_.BindMoneyArray(" + i + ", " + field.useName() + ", 18, 2" + useNull(field));
          break;
        case Field.DATE:
          outData.println("  q_.BindDateArray(" + i + ", " + field.useName() + useNull(field));
          break;
        case Field.TIME:
          outData.println("  q_.BindTimeArray(" + i + ", " + field.useName() + useNull(field));
          break;
        case Field.DATETIME:
        case Field.TIMESTAMP:
          outData.println("  q_.BindDateTimeArray(" + i + ", " + field.useName() + useNull(field));
          break;
        case Field.AUTOTIMESTAMP:
          outData.println("  //q_.BindDateTimeArray(" + i + ", " + field.useName() + useNull(field));
          break;
      }
    }
    outData.println("  q_.Exec();");
    outData.println("}");
    outData.println();
  }
  /**
   * Emits class method for processing the database activity
   */
  static boolean isIdentity(Field field)
  {
    return field.type == Field.BIGIDENTITY || field.type == Field.IDENTITY;
  }
  static boolean isSequence(Field field)
  {
    return field.type == Field.BIGSEQUENCE || field.type == Field.SEQUENCE;
  }
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    String fullName = table.useName() + proc.upperFirst();
    outData.println("void T" + fullName + "::Exec()");
    outData.println("{");
    generateCommand(proc, outData);
    if (proc.outputs.size() > 0)
      outData.println("  q_.Open(q_.command, NOBINDS, NODEFINES, NOROWS, ROWSIZE);");
    else if (proc.inputs.size() > 0)
      outData.println("  q_.Open(q_.command, " + proc.inputs.size() + ");");
    else
      outData.println("  q_.Open(q_.command);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      generateCppBind(field, outData);
    }
    Vector blobs = new Vector();
    for (int j = 0; j < placeHolder.pairs.size(); j++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(j);
      Field field = pair.field;
      String tablename = table.tableName();
      String bind = "Bind";
      if (field.type == Field.BLOB) bind += "Blob";
      //else if (field.type == Field.BIGXML) bind += "BigXML";
      outData.println("  q_." + bind + "(" + padder("" + j + ",", 4) + cppBind(field, tablename, proc.isInsert) + padder(", " + cppDirection(field), 4) + ((isNull(field)) ? ", &" + field.useName() + "IsNull" : "") + charFieldFlag(field) + ");");
      if (field.type == Field.BLOB)
        blobs.addElement(field);
    }
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field)proc.outputs.elementAt(j);
      String define = "Define";
      if (field.type == Field.BLOB) define += "Blob";
      //else if (field.type == Field.BIGXML) define += "BigXML";
      outData.println("  q_." + define +"(" + padder("" + j + ",", 4) + cppDefine(field) + ");");
    }
    outData.println("  q_.Exec();");
    for (int j = 0; j < blobs.size(); j++)
    {
      Field field = (Field)blobs.elementAt(j);
      outData.println("  SwapBytes(" + field.useName() + ".len); // fixup len in data on intel type boxes");
    }
    outData.println("}");
    outData.println();
    boolean skipExecWithParms = false;
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if (field.type == Field.BLOB)// || field.type == Field.BIGXML)
      {
        skipExecWithParms = true;
        break;
      }
    }
    if (skipExecWithParms == false)
      if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
      {
        outData.println("void T" + fullName + "::Exec(");
        generateWithParms(proc, outData, "");
        outData.println(")");
        outData.println("{");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field)proc.inputs.elementAt(j);
          if ((isSequence(field) && proc.isInsert)
          || (isIdentity(field) && proc.isInsert)
          || field.type == Field.TIMESTAMP
          || field.type == Field.AUTOTIMESTAMP
          || field.type == Field.USERSTAMP)
            continue;
          outData.println("  " + cppCopy(field));
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.println("  strncpy(" + s + ", a" + s + ", sizeof(" + s + ")-1);");
        }
        outData.println("  Exec();");
        if (proc.outputs.size() > 0 && proc.isInsert)
            outData.println("  Fetch();");
        outData.println("}");
        outData.println();
      }
    if (proc.outputs.size() > 0)
    {
      outData.println("bool T" + fullName + "::Fetch()");
      outData.println("{");
      outData.println("  if (q_.Fetch() == false)");
      outData.println("    return false;");
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field)proc.outputs.elementAt(j);
        outData.println("  q_.Get(" + cppGet(field) + ");");
        if (isNull(field))
          outData.println("  q_.GetNull(" + field.useName() + "IsNull, " + j + ");");
      }
      outData.println("  return true;");
      outData.println("}");
      outData.println();
    }
  }
  static String check(String value)
  {
    return value;
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    boolean isReturning = false;
    boolean isBulkSequence = false;
    String front = "", back = "", sequencer = "", output = "";
    Vector lines = placeHolder.getLines();
    int size = 1;
    if (proc.isInsert == true && proc.hasReturning == true && proc.outputs.size() == 1)
    {
      Field field = (Field)proc.outputs.elementAt(0);
      if (field.isSequence == true)
      {
        isReturning = true;
        //front = "select " + field.useName() + " from new table(";
        output = field.useName();
        // back = ")";
        //sequencer = "nextval for " + proc.table.tableName() + "seq";
        size += front.length();
        size += back.length();
        size += sequencer.length();
      }
    }
    if (proc.isMultipleInput == true && proc.isInsert == true)
    {
      isBulkSequence = true;
      sequencer = "nextval for " + proc.table.tableName() + "seq";
      size += sequencer.length();
    }
    for (int i = 0; i < lines.size(); i++)
    {
      String l = (String)lines.elementAt(i);
      if (l.charAt(0) == '"')
        size += (l.length() + 2);
      else
      {
        String var = l.trim();
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          if (var.compareTo(s) == 0)
          {
            Integer n = (Integer)proc.dynamicSizes.elementAt(j);
            size += (n.intValue() + 2);
          }
        }
      }
    }
    outData.println("  if (q_.command == 0)");
    outData.println("    q_.command = new char [" + (size + 15) + "];");
    outData.println("  memset(q_.command, 0, " + (size + 15) + ");");
    if (isReturning == true)
    {
      outData.println("  struct cpp_ret {char* head; char *output; char *sequence; char* tail; cpp_ret(){head = output = sequence = tail = \"\";}} _ret;");
      if (!sequencer.isEmpty())
        outData.println("  _ret.sequence = \"" + sequencer + ",\";");
      if (!front.isEmpty())
        outData.println("  _ret.head = \"" + front + "\";");
      if (!back.isEmpty())
        outData.println("  _ret.tail = \"" + back + "\";");
      outData.println("  _ret.output = \"OUTPUT Inserted." + output + "\";\n");
    }
    if (isBulkSequence == true)
    {
      outData.println("  struct cpp_ret {const char* head; const char *output; const char *sequence; const char* tail; cpp_ret(){head = output = sequence = tail = \"\";}} _ret;");
      outData.println("  _ret.sequence = \"" + sequencer + ",\";");
    }
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
          if (i != 0)
            outData.println(terminate);
        }
        else if (i != 0)
          outData.println(terminate);
        if (l.charAt(0) != '"')
            outData.print(strcat + check(l));
        else
        {
            outData.print(strcat + l);
        }
        if (l.charAt(0) == '"')
        {
          terminate = "\"\\n\"";
          strcat = "                     ";
        }
      }
      outData.println(");");
    }
    //if (isReturning == true)
    //  outData.println("  strcat(q_.command, \"" + back + "\");");
  }
  /**
   * generate Holding variables
   */
  static void generateCppBind(Field field, PrintWriter outData)
  {
    switch (field.type)
    {
      case Field.DATE:
        outData.println("  DATE_STRUCT " + field.useName() + "_CLIDate;");
        break;
      case Field.TIME:
        outData.println("  TIME_STRUCT " + field.useName() + "_CLITime;");
        break;
      case Field.DATETIME:
        outData.println("  TIMESTAMP_STRUCT " + field.useName() + "_CLIDateTime;");
        break;
      case Field.TIMESTAMP:
        outData.println("  TIMESTAMP_STRUCT " + field.useName() + "_CLITimeStamp;");
        break;
      case Field.AUTOTIMESTAMP:
        outData.println("  //TIMESTAMP_STRUCT " + field.useName() + "_CLITimeStamp;");
        break;
    }
  }
  static void generateWithParms(Proc proc, PrintWriter outData, String pad)
  {
    String comma = "  ";
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if ((isSequence(field) && proc.isInsert) || (isIdentity(field) && proc.isInsert) || field.type == Field.TIMESTAMP || field.type == Field.AUTOTIMESTAMP || field.type == Field.USERSTAMP)
        continue;
      outData.println(pad + comma + "const " + cppParm(field));
      comma = ", ";
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      outData.println(pad + comma + "const char*   a" + s);
      comma = ", ";
    }
  }
  static void generateInterface(Table table, Proc proc, String dataStruct,
      PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    String front = "  { ";
    boolean standardExec = true;
    if (proc.outputs.size() > 0)
    {
      outData.println("  enum");
      Field field = (Field)proc.outputs.elementAt(0);
      String thisOne = field.useName().toUpperCase() + "_OFFSET";
      String lastOne = thisOne;
      String lastSize = cppLength(field);
      outData.println(front + padder(thisOne, 24) + "= 0");
      front = "  , ";
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = (Field)proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase() + "_OFFSET";
        outData.println("  , " + padder(thisOne, 24) + "= (" + lastOne + "+" + lastSize + ")");
        lastOne = thisOne;
        lastSize = cppLength(field);
      }
      outData.println("  , " + padder("ROWSIZE", 24) + "= (" + lastOne + "+" + lastSize + ")");
      if (proc.isSingle)
        outData.println("  , " + padder("NOROWS", 24) + "= 1");
      else if (proc.noRows > 0)
        outData.println("  , " + padder("NOROWS", 24) + "= " + proc.noRows);
      else
        outData.println("  , " + padder("NOROWS", 24) + "= (24*1024 / ROWSIZE) + 1");
      outData.println("  , " + padder("NOBINDS", 24) + "= " + placeHolder.pairs.size());
      outData.println("  , " + padder("NODEFINES", 24) + "= " + proc.outputs.size());
      field = (Field)proc.outputs.elementAt(0);
      thisOne = field.useName().toUpperCase();
      outData.println("  , " + padder(thisOne + "_POS", 24) + "= 0");
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = (Field)proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase();
        outData.println("  , " + padder(thisOne + "_POS", 24) + "= " + padder(thisOne + "_OFFSET", 24) + "* NOROWS");
      }
      outData.println("  };");
    }
    else if (proc.isMultipleInput)
    {
      int noNulls = 0;
      standardExec = false;
      outData.println("  enum");
      Field field = (Field)proc.inputs.elementAt(0);
      if (isNull(field) || (field.type == Field.CHAR && field.isNull == true) || (field.type == Field.ANSICHAR && field.isNull == true))
        noNulls++;
      String thisOne = field.useName().toUpperCase() + "_OFFSET";
      String lastOne = thisOne;
      String thisSize = field.useName().toUpperCase() + "_SIZE";
      String lastSize = thisSize;
      outData.println(front + padder(thisOne, 24) + "= 0");
      front = "  , ";
      outData.println(front + padder(thisSize, 24) + "= " + cppLength(field));
      for (int j = 1; j < proc.inputs.size(); j++)
      {
        field = (Field)proc.inputs.elementAt(j);
        if (isNull(field) || (field.type == Field.CHAR && field.isNull == true) || (field.type == Field.ANSICHAR && field.isNull == true))
          noNulls++;
        thisOne = field.useName().toUpperCase() + "_OFFSET";
        thisSize = field.useName().toUpperCase() + "_SIZE";
        outData.println("  , " + padder(thisOne, 24) + "= (" + lastOne + "+" + lastSize + ")");
        outData.println("  , " + padder(thisSize, 24) + "= " + cppLength(field));
        lastOne = thisOne;
        lastSize = thisSize;
      }
      outData.println("  , " + padder("ROWSIZE", 24) + "= (" + lastOne + "+" + lastSize + ")");
      outData.println("  , " + padder("NOBINDS", 24) + "= " + placeHolder.pairs.size());
      outData.println("  , " + padder("NONULLS", 24) + "= " + noNulls);
      outData.println("  };");
      outData.println("  void Exec(int32 noOf, " + dataStruct + "* Recs);");
    }
    outData.println("  TJQuery q_;");
    if (standardExec == true)
    {
      if (proc.outputs.size() > 0)
          outData.println("  bool Fetch();");

      outData.println("  void Exec();");
      if (proc.outputs.size() > 0 && proc.isInsert)
        outData.println("  void Exec(" + dataStruct + "& Rec) {*DRec() = Rec;Exec(); Fetch();}");
      else
        outData.println("  void Exec(" + dataStruct + "& Rec) {*DRec() = Rec;Exec(); }");

      boolean skipExecWithParms = false;
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field)proc.inputs.elementAt(j);
        if (field.type == Field.BLOB)// || field.type == Field.BIGXML)
        {
          skipExecWithParms = true;
          break;
        }
      }
      if (skipExecWithParms == false)
        if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
        {
            boolean val = false;

            for (int j = 0; j < proc.inputs.size(); j++)
            {
                Field field = (Field)proc.inputs.elementAt(j);
                if ((isSequence(field) && proc.isInsert) || (isIdentity(field) && proc.isInsert) || field.type == Field.TIMESTAMP || field.type == Field.AUTOTIMESTAMP || field.type == Field.USERSTAMP)
                    continue;
                val = true;
            }
            for (int j = 0; j < proc.dynamics.size(); j++) {
                String s = (String) proc.dynamics.elementAt(j);
                val=true;
            }
            if (val)
            {
                outData.println("  void Exec(");
                generateWithParms(proc, outData, "  ");
                outData.println("  );");
            }
        }
    }
    outData.println("  T" + table.useName() + proc.upperFirst() + "(TJConnector &conn, const char* aFile=__FILE__, long aLine=__LINE__)");
    outData.println("  : q_(conn)");
    outData.println("  {Clear();q_.FileAndLine(aFile,aLine);}");
    outData.println("  " + dataStruct + "* DRec() {return this;}");
    if (proc.outputs.size() > 0)
      outData.println("  O" + dataStruct.substring(1) + "* ORec() {return this;}");
    if (proc.isStdExtended() == false && proc.extendsStd == true)
    {
      outData.println("  D" + table.useName() + "* DStd() {return (D" + table.useName() + "*)this;}");
      if (proc.outputs.size() > 0)
        outData.println("  O" + table.useName() + "* OStd() {return (O" + table.useName() + "*)this;}");
    }
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
        outData.println("inline const char* " + table.useName() + field.useName() + "Lookup(int no)");
        outData.println("{");
        outData.println("  switch(no)");
        outData.println("  {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = (Enum)field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)element.value + "'";
          outData.println("  case " + evalue + ": return \"" + element.name + "\";");
        }
        outData.println("  default: return \"<unknown value>\";");
        outData.println("  }");
        outData.println("}");
        outData.println();
      }
      else if (field.valueList.size() > 0)
      {
        outData.println("enum e" + table.useName() + field.useName());
        String start = "{";
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String element = (String)field.valueList.elementAt(j);
          outData.println(start + " " + table.useName() + field.useName() + element);
          start = ",";
        }
        outData.println("};");
        outData.println();
        outData.println("inline const char *" + table.useName() + field.useName() + "Lookup(int no)");
        outData.println("{");
        outData.println("  switch(no)");
        outData.println("  {");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String element = (String)field.valueList.elementAt(j);
          outData.println("  case " + j + ": return \"" + element + "\";");
        }
        outData.println("  default: return \"<unknown value>\";");
        outData.println("  }");
        outData.println("}");
        outData.println();
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
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.XML:
      case Field.TLOB:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return charPadding(field.length + 1, fillerNo);
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return charPadding(field.precision + 3, fillerNo);
        break;
      case Field.MONEY:
        return charPadding(21, fillerNo);
      //case Field.BIGXML:
      //  break;
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
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return 8;
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.TLOB:
      case Field.XML:
        return field.length + 1;
      //case Field.BIGXML:
      case Field.BLOB:
        return 8;
      case Field.DATE:
        return 9;
      case Field.TIME:
        return 7;
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
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
  static String charFieldFlag(Field field)
  {
    if (field.type != Field.CHAR && field.type != Field.ANSICHAR && field.type != Field.TLOB && field.type != Field.XML)
      return "";
    if ((field.type == Field.CHAR || field.type == Field.TLOB || field.type == Field.XML) && field.isNull == true)
      return ", 0, 1";
    if (field.type == Field.ANSICHAR)
      if (field.isNull == true)
        return ", 1, 1";
      else
        return ", 0, 0";
    return ", 0, 0";
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
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BLOB:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
      case Field.TIME:
      //case Field.XML:
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
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
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
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
      case Field.LONG:
        return field.useName() + " = 0;";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "memset(" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.BLOB:
        return "memset(&" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision <= 15)
          return field.useName() + " = 0.0;";
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
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return "int64  " + field.useName();
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return "char   " + field.useName() + "[" + (field.length + 1) + "]";
      case Field.USERSTAMP:
        return "char   " + field.useName() + "[9]";
      case Field.BLOB:
        return "TJBlob<" + field.length + "> " + field.useName();
      case Field.DATE:
        return "char   " + field.useName() + "[9]";
      case Field.TIME:
        return "char   " + field.useName() + "[7]";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "char   " + field.useName() + "[15]";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "char   " + field.useName() + "[" + (field.precision + 3) + "]";
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
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "sizeof(int64)";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return "" + (field.length + 1);
      case Field.BLOB:
        return "sizeof(TJBlob<" + field.length + ">)";
      //case Field.BIGXML:
      //  return "sizeof(TJBigXML)";
      case Field.USERSTAMP:
        return "9";
      case Field.DATE:
        return "sizeof(DATE_STRUCT)";
      case Field.TIME:
        return "sizeof(TIME_STRUCT)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "sizeof(TIMESTAMP_STRUCT)";
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
  static String cppDirection(Field field)
  {
    if (field.isIn && field.isOut)
      return "SQL_PARAM_INPUT_OUTPUT";
    if (field.isOut)
      return "SQL_PARAM_OUTPUT";
    return "SQL_PARAM_INPUT";
  }
  static String cppArrayPointer(Field field)
  {
    String offset = field.useName().toUpperCase() + "_OFFSET";
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return "int16 *" + field.useName() + " = (int16 *)(q_.data + " + offset + " * noOf);";
      case Field.INT:
        return "int32 *" + field.useName() + " = (int32 *)(q_.data + " + offset + " * noOf);";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "int64 *" + field.useName() + " = (int64 *)(q_.data + " + offset + " * noOf);";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
        return "double *" + field.useName() + " = (double *)(q_.data + " + offset + " * noOf);";
      case Field.MONEY:
        return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
      case Field.SEQUENCE:
        return "int32 *" + field.useName() + " = (int32 *)(q_.data + " + offset + " * noOf);";
      case Field.TLOB:
      case Field.XML:
      case Field.CHAR:
      case Field.ANSICHAR:
        return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
      case Field.USERSTAMP:
        return "char *" + field.useName() + " = (char *)(q_.data + " + offset + " * noOf);";
      case Field.DATE:
        return "DATE_STRUCT* " + field.useName() + " = (DATE_STRUCT *)(q_.data + " + offset + " * noOf);";
      case Field.TIME:
        return "TIME_STRUCT* " + field.useName() + " = (TIME_STRUCT *)(q_.data + " + offset + " * noOf);";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "TIMESTAMP_STRUCT* " + field.useName() + " = (TIMESTAMP_STRUCT *)(q_.data + " + offset + " * noOf);";
      case Field.BLOB:
        return "// Blobs are not handled here";
      //case Field.BIGXML:
      //  return "// BigXMLs are not handled here";
    }
    return "// not handled here";
  }
  static String cppBind(Field field, String tableName, boolean isInsert)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.IDENTITY:
        return field.useName();
      case Field.FLOAT:
      case Field.DOUBLE:
        return field.useName() + ", " + (field.precision) + ", " + (field.scale);
      case Field.MONEY:
        return field.useName() + ", 18, 2";
      case Field.SEQUENCE:
      case Field.BIGSEQUENCE:
        if (isInsert)
          return "q_.Sequence(" + field.useName() + ", \"" + tableName + "Seq\")";
        else
          return field.useName();
      case Field.TLOB:
      case Field.XML:
        return field.useName() + ", " + (field.length);
      case Field.CHAR:
        return field.useName() + ", " + (field.length);
      case Field.ANSICHAR:
        return field.useName() + ", " + (field.length);
      case Field.USERSTAMP:
        return "q_.UserStamp(" + field.useName() + "), 8";
      case Field.DATE:
        return "q_.Date(" + field.useName() + "_CLIDate, " + field.useName() + ")";
      case Field.TIME:
        return "q_.Time(" + field.useName() + "_CLITime, " + field.useName() + ")";
      case Field.DATETIME:
        return "q_.DateTime(" + field.useName() + "_CLIDateTime, " + field.useName() + ")";
      case Field.TIMESTAMP:
        return "q_.TimeStamp(" + field.useName() + "_CLITimeStamp, " + field.useName() + ")";
      case Field.AUTOTIMESTAMP:
        return "q_.TimeStamp(" + field.useName() + "_CLITimeStamp, " + field.useName() + ")";
      case Field.BLOB:
        return "(char*)&" + field.useName() + ", sizeof(" + field.useName() + ".data)";
      //case Field.BIGXML:
      //  return field.useName()+".data, " + field.useName( )+ ".length";
    }
    return field.useName() + ", <unsupported>";
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
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return "(int64*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.length + 1);
      case Field.ANSICHAR:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.length + 1) + ", 1";
      case Field.USERSTAMP:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), 9";
      case Field.BLOB:
        return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), " + (field.length);
      //case Field.BIGXML:
      //  return "(char*)  (q_.data+" + field.useName().toUpperCase() + "_POS), sizeof(" + field.useName() + ")";
      case Field.DATE:
        return "(DATE_STRUCT*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.TIME:
        return "(TIME_STRUCT*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return "(TIMESTAMP_STRUCT*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
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
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
      case Field.LONG:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, " + (field.precision + 3);
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.MONEY:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, 21";
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, " + (field.length + 1);
      case Field.USERSTAMP:
        return padder(field.useName() + ",", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS, 9";
      case Field.BLOB:
        return padder(field.useName() + ".len, " + field.useName() + ".data,", 32) +
            " q_.data+" + field.useName().toUpperCase() + "_POS, sizeof(" + field.useName() + ")";
      //case Field.BIGXML:
      //  return field.useName() + ".setBigXML(" + field.useName().toUpperCase() + "_POS, " + field.length + ")";
      case Field.DATE:
        return padder("TJDate(" + field.useName() + "),", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.TIME:
        return padder("TJTime(" + field.useName() + "),", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return padder("TJDateTime(" + field.useName() + "),", 32) + " q_.data+" + field.useName().toUpperCase() + "_POS";
    }
    return field.useName() + " <unsupported>";
  }
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
      case Field.IDENTITY:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return field.useName() + " = a" + field.useName() + ";";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "strncpy(" + field.useName() + ", a" + field.useName() + ", sizeof(" + field.useName() + ")-1);";
        return field.useName() + " = a" + field.useName() + ";";
      case Field.MONEY:
        return "strncpy(" + field.useName() + ", a" + field.useName() + ", sizeof(" + field.useName() + ")-1);";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
        return "strncpy(" + field.useName() + ", a" + field.useName() + ", sizeof(" + field.useName() + ")-1);";
      case Field.ANSICHAR:
        return "memcpy(" + field.useName() + ", a" + field.useName() + ", sizeof(" + field.useName() + "));";
      case Field.BLOB:
        return field.useName() + " = a" + field.useName() + ";";
      case Field.USERSTAMP:
      case Field.TIMESTAMP:
        return "// " + field.useName() + " -- generated";
      case Field.AUTOTIMESTAMP:
        return "// " + field.useName() + " -- generated";
    }
    return field.useName() + " <unsupported>";
  }
  static String cppArrayCopy(Field field)
  {
    String size = field.useName().toUpperCase() + "_SIZE";
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.SEQUENCE:
      //case Field.IDENTITY:
      case Field.BIGSEQUENCE:
        //case Field.BIGIDENTITY:
        return field.useName() + "[i] = Recs[i]." + field.useName() + ";";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return "strncpy(&" + field.useName() + "[i*" + size + "], Recs[i]." + field.useName() + ", " + size + "-1);";
        return field.useName() + "[i] = Recs[i]." + field.useName() + ";";
      case Field.MONEY:
        return "strncpy(&" + field.useName() + "[i*" + size + "], Recs[i]." + field.useName() + ", " + size + "-1);";
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
        return "strncpy(&" + field.useName() + "[i*" + size + "], Recs[i]." + field.useName() + ", " + size + "-1);";
      case Field.DATE:
        return "q_.Date(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.TIME:
        return "q_.Time(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.DATETIME:
        return "q_.DateTime(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.ANSICHAR:
        return "memcpy(&" + field.useName() + "[i*" + size + "], a" + field.useName() + ", " + size + ");";
      case Field.BLOB:
        return field.useName() + "[i] = Recs[i]." + field.useName() + ";";
      case Field.USERSTAMP:
        return field.useName() + " -- generated";
      case Field.IDENTITY:
        return field.useName() + " -- generated";
      case Field.TIMESTAMP:
        return "q_.TimeStamp(" + field.useName() + "[i], Recs[i]." + field.useName() + ");";
      case Field.AUTOTIMESTAMP:
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
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return "int64  a" + field.useName();
      case Field.CHAR:
      case Field.TLOB:
      case Field.XML:
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
      case Field.AUTOTIMESTAMP:
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
    String front = "";
    if (isNull(field))
      front = "if (strlen(work.data) == 0) " + field.useName() + "IsNull = true; else ";
    switch (field.type)
    {
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.TLOB:
      //case Field.XML: (xml is xml is bizarro)
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return front+"memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
      case Field.BOOLEAN:
      case Field.BYTE:
        return front + field.useName() + " = (int8)atol(work.data);";
      case Field.SHORT:
        return front + field.useName() + " = (int16)atol(work.data);";
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return front + field.useName() + " = (int32)atol(work.data);";
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return front + field.useName() + " = (int64)atoint64(work.data);";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return front + "memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
        return front + field.useName() + " = atof(work.data);";
      case Field.MONEY:
        return front + "memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
    }
    return "// " + field.useName() + " <unsupported>";
  }
  static String toXMLFormat(Field field)
  {
    String front = "XRec.append(\"  <" + field.useName() + ">\");";
    String back = "XRec.append(\"</" + field.useName() + ">\");";
    if (isNull(field))
      front += "if (" + field.useName() + "IsNull == false) ";
    switch (field.type)
    {
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.USERSTAMP:
      case Field.TLOB:
      //case Field.XML: (xml is xml is bizarro)
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.AUTOTIMESTAMP:
        return front + "XRec.ampappend(" + field.useName() + ");" + back;
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return front + "XRec.ampappend(JP_XML_FORMAT((int32)" + field.useName() + ").result);" + back;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return front + "XRec.ampappend(JP_XML_FORMAT((int64)" + field.useName() + ").result);" + back;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return front + "XRec.ampappend(" + field.useName() + ");" + back;
        return front + "XRec.ampappend(JP_XML_FORMAT((double)" + field.useName() + ").result);" + back;
      case Field.MONEY:
        return front + "XRec.ampappend(" + field.useName() + ");" + back;
    }
    return "// " + field.useName() + " <unsupported>";
  }
}
