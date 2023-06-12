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
import bbd.jportal2.generators.Common.*;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public class MySqlCCode extends BaseGenerator implements IBuiltInSIProcessor
{

    private static boolean first = true;
    private static final boolean multiGeneration = true;
    public MySqlCCode() {
        super(MySqlCCode.class, multiGeneration, first);
        MySqlCCodeOutputOptions = JPortalTemplateOutputOptions.defaultBuiltInOptions();
    }

    /**
   * Reads input from stored repository
   */


  public String description()
  {
    return "Generate MySQL C++ Code ODBC";
  }
  public String documentation()
  {
    return "Generate MySQL C++ Code ODBC";
  }

  private Vector<Flag> flagsVector;

  private void flagDefaults() {}

  public Vector<Flag> getFlags()
{
  if (flagsVector == null)
  {
    flagDefaults();
    flagsVector = new Vector<>();
  }
  return flagsVector;
}
/**
 * Sets generation getFlags.
 */
void setFlags(Database database)
{
  flagDefaults();
}

  static PlaceHolder placeHolder;
  static JPortalTemplateOutputOptions MySqlCCodeOutputOptions;
  /**
   * Generates the procedure classes for each table present.
   */
  public void generate(Database database, String output) throws Exception {
    if (!canGenerate) return;
    setFlags(database);
    for (int i = 0; i < database.tables.size(); i++) {
      Table table = (Table) database.tables.elementAt(i);
      table.useBrackets = false;
      // This doesn't work as the insert is already generated at this point :/
      table.useReturningOutput = false;
      generate(table, output);
    }
    first = false;
  }
  void generate(Table table, String output) throws Exception {
    try (PrintWriter outData = this.openOutputFileForGeneration("sh", fileName(output, table.useName().toLowerCase(), ".sh"))) {
      outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
      outData.println("#ifndef _" + table.useName().toLowerCase() + "SH_MARSHAL_VERSION");
      outData.println("#define _" + table.useName().toLowerCase() + "SH_MARSHAL_VERSION");
      outData.println();
      outData.println("#include <stddef.h>");
      outData.println("#include \"padgen.h\"");
      outData.println("#include \"dbapi.h\"");
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
          outData.println("#include \"dbapi.h\"");
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
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String)field.comments.elementAt(c);
          outData.println("  //" + s);
        }
      outData.println("  " + CommonCCode.padder(CommonCCode.cppVar(field) + ";", 48) + CommonCCode.generatePadding(field, filler++));
      if (CommonCCode.isNull(field))
      {
        outData.println("  " + CommonCCode.padder("int16  " + field.useName() + "IsNull;", 48) + CommonCCode.generatePadding(filler++));
        filler++;
      }
    }
    outData.println();
    headerSwaps(outData, "", fields, null, null);
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
      Vector<Field> fields = proc.outputs;
      Map<String, Integer> duplicateFields = CommonCCode.GetDuplicatedFields(proc.inputs, proc.placeHolders);
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
          outData.println("  " + CommonCCode.padder(CommonCCode.cppVar(field) + ";", 48) + CommonCCode.generatePadding(field, filler++));
          if (CommonCCode.isNull(field))
            outData.println("  " + CommonCCode.padder("int16  " + field.useName() + "IsNull;", 48) + CommonCCode.generatePadding(filler++));

          if (!proc.hasDiscreteInput() && duplicateFields.size() > 0 && duplicateFields.containsKey(field.name))
          {
            int val = duplicateFields.get(field.name);
            if (val > 1)
            {
              for (int k = 0; k < val - 1; k++)
              {
                outData.println("  " + CommonCCode.padder(CommonCCode.cppVar(field.useName() + (k + 1), field.type, field.length, field.precision) + ";", 48) + CommonCCode.generatePadding(field, filler++));
              }
            }
          }
        }
        outData.println();
        headerSwaps(outData, "", fields, null, null);
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
          outData.println("  " + CommonCCode.padder(CommonCCode.cppVar(field) + ";", 48) + CommonCCode.generatePadding(field, filler++));
          int val = duplicateFields.get(field.name);
          if (val > 1)
          {
            for (int k = 0; k < val - 1; k++)
            {
              outData.println("  " + CommonCCode.padder(CommonCCode.cppVar(field.useName() + (k + 1), field.type, field.length, field.precision) + ";", 48) + CommonCCode.generatePadding(field, filler++));
            }
          }
          if (CommonCCode.isNull(field))
            outData.println("  " + CommonCCode.padder("int16  " + field.useName() + "IsNull;", 48) + CommonCCode.generatePadding(filler++));
        }
        outData.println();
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          Integer n = (Integer)proc.dynamicSizes.elementAt(j);
          outData.println("  " + CommonCCode.padder("char " + s + "[" + (n.intValue() + 1) + "];", 48) + CommonCCode.charPadding(n.intValue() + 1, filler++));
        }
        headerSwaps(outData, baseClass, inputs, proc, duplicateFields);
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
  private static void headerSwaps(PrintWriter outData, String baseClass, Vector<Field> inputs, Proc proc, Map<String, Integer> duplicateFields)
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
      outData.println("    " + CommonCCode.cppInit(field));
      if (duplicateFields != null)
        {
          int val = duplicateFields.get(field.name);
          if (val > 1)
          {
            for (int k = 0; k < val - 1; k++)
            {
              outData.println("    " + CommonCCode.cppInit(field.useName() + (k + 1), field.type, field.precision));
            }
          }
        }
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
      if (CommonCCode.notString(field) == false)
        continue;
      if (CommonCCode.isStruct(field) == false)
      {
        outData.println("    SwapBytes(" + field.useName() + ");");
        if (duplicateFields != null)
        {
          int val = duplicateFields.get(field.name);
          if (val > 1)
          {
            for (int k = 0; k < val - 1; k++)
            {
              outData.println("    SwapBytes(" + field.useName() + (k + 1) + ");");
            }
          }
        }
      }
      else
        outData.println("    " + field.useName() + ".Swaps();");
      if (CommonCCode.isNull(field))
      {
        outData.println("    SwapBytes(" + field.useName() + "IsNull);");
        if (duplicateFields != null)
        {
          int val = duplicateFields.get(field.name);
          if (val > 1)
          {
            for (int k = 0; k < val - 1; k++)
            {
              outData.println("    SwapBytes(" + field.useName() + (k + 1) + "IsNull);");
            }
          }
        }
      }
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
        outData.println("    dBuild.add(\"" + field.useName() + "\", " + field.useName() + ".len, " + field.useName() + ".data" + CommonCCode.nullAdd(field) + ");");
      else
        outData.println("    dBuild.add(\"" + field.useName() + "\", " + field.useName() + CommonCCode.nullAdd(field) + ");");
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
        outData.println("    dBuild.set(\"" + field.useName() + "\", " + field.useName() + ".len, " + field.useName() + ".data, sizeof(" + field.useName() + ".data)" + CommonCCode.nullSet(field) + ");");
      else
        outData.println("    dBuild.set(\"" + field.useName() + "\", " + field.useName() + ", sizeof(" + field.useName() + ")" + CommonCCode.nullSet(field) + ");");
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
  static void generateMultipleImplementation(Table table, Proc proc, PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, MySqlCCodeOutputOptions, PlaceHolder.QUESTION, "");
    String dataStruct;
    if (proc.isStdExtended() || proc.isStd)
      dataStruct = "D" + table.useName();
    else
      dataStruct = "D" + table.useName() + proc.upperFirst();
    placeHolder = new PlaceHolder(proc, MySqlCCodeOutputOptions, PlaceHolder.QUESTION, "");
    String fullName = table.useName() + proc.upperFirst();
    outData.println("void T" + fullName + "::Exec(int32 noOf, " + dataStruct + " *Recs)");
    outData.println("{");
    generateCommand(proc, outData);
    outData.println("  q_.OpenArray(q_.command, NOBINDS, NONULLS, noOf, ROWSIZE);");
    for (int i = 0, n = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field)proc.inputs.elementAt(i);
      outData.println("  " + CommonCCode.cppArrayPointer(field));
      if (CommonCCode.isNull(field))
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
      outData.println("    " + CommonCCode.cppArrayCopy(field));
      if (CommonCCode.isNull(field))
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
          outData.println("  q_.BindAnsiCharArray(" + i + ", " + field.useName() + ", " + size + CommonCCode.useNull(field));
          break;
        case Field.CHAR:
        case Field.TLOB:
        case Field.XML:
        case Field.USERSTAMP:
          outData.println("  q_.BindCharArray(" + i + ", " + field.useName() + ", " + size + CommonCCode.useNull(field));
          break;
        //case Field.BIGXML:
        //  break;
        case Field.LONG:
        case Field.BIGSEQUENCE:
        case Field.BIGIDENTITY:
          outData.println("  q_.BindInt64Array(" + i + ", " + field.useName() + CommonCCode.useNull(field));
          break;
        case Field.INT:
        case Field.SEQUENCE:
        case Field.IDENTITY:
          outData.println("  q_.BindInt32Array(" + i + ", " + field.useName() + CommonCCode.useNull(field));
          break;
        case Field.BOOLEAN:
        case Field.BYTE:
        case Field.SHORT:
          outData.println("  q_.BindInt16Array(" + i + ", " + field.useName() + CommonCCode.useNull(field));
          break;
        case Field.FLOAT:
        case Field.DOUBLE:
          if (field.precision <= 15)
            outData.println("  q_.BindDoubleArray(" + i + ", " + field.useName() + ", " + (field.precision) + ", " + (field.scale) + CommonCCode.useNull(field));
          else
            outData.println("  q_.BindMoneyArray(" + i + ", " + field.useName() + ", " + (field.precision) + ", " + (field.scale) + CommonCCode.useNull(field));
          break;
        case Field.MONEY:
          outData.println("  q_.BindMoneyArray(" + i + ", " + field.useName() + ", 18, 2" + CommonCCode.useNull(field));
          break;
        case Field.DATE:
          outData.println("  q_.BindDateArray(" + i + ", " + field.useName() + CommonCCode.useNull(field));
          break;
        case Field.TIME:
          outData.println("  q_.BindTimeArray(" + i + ", " + field.useName() + CommonCCode.useNull(field));
          break;
        case Field.DATETIME:
        case Field.TIMESTAMP:
          outData.println("  q_.BindDateTimeArray(" + i + ", " + field.useName() + CommonCCode.useNull(field));
          break;
        case Field.AUTOTIMESTAMP:
          outData.println("  //q_.BindDateTimeArray(" + i + ", " + field.useName() + CommonCCode.useNull(field));
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
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    placeHolder = new PlaceHolder(proc, MySqlCCodeOutputOptions, PlaceHolder.QUESTION, "");
    String fullName = table.useName() + proc.upperFirst();

    Field primaryKeyField = null;
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field tempfield = (Field)proc.outputs.elementAt(j);
      if (tempfield.isPrimaryKey() && tempfield.isSequence())
      {
        primaryKeyField = tempfield;
        break;
      }
    }

    if (proc.isInsert && primaryKeyField != null)
    {
      outData.println("struct T" + fullName + "_LastInsertID");
      outData.println("{");
      outData.println("  " + CommonCCode.cppVar(primaryKeyField) + ";");
      outData.println("  TJQuery q_;");
      outData.println("  void Exec()");
      outData.println("  {");
      outData.println("    const char idcommand[] = \"select LAST_INSERT_ID();\";");
      outData.println("    if (q_.command == 0)");
      outData.println("      q_.command = new char[25];");
      outData.println("    memset(q_.command, 0, 25);");
      outData.println("    strcpy(q_.command, idcommand);");
      outData.println("    q_.Open(q_.command, 0, 1, 1, " + CommonCCode.cppLength(primaryKeyField) + ");");
      outData.println("    q_.Define(" + CommonCCode.padder("0,", 4) + CommonCCode.cppDefineType(primaryKeyField) + " (q_.data));");
      outData.println("    q_.Exec();");
      outData.println("    if (q_.Fetch() == false)");
      outData.println("      return;");
      outData.println("    q_.Get(" + primaryKeyField.useName()+ ", q_.data);");
      outData.println("  }");
      outData.println("  T" + fullName + "_LastInsertID(TJConnector& conn) : q_(conn) { " + primaryKeyField.name + " = 0; }");
      outData.println("};");
      outData.println();
    }

    outData.println("void T" + fullName + "::Exec()");
    outData.println("{");
    generateCommand(proc, outData);

    Map<String, Integer> duplicateFields = new HashMap<String, Integer>();
    Map<String, Integer> usedFields = new HashMap<String, Integer>();
    Vector<Field> inputs = proc.inputs;
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      duplicateFields.putIfAbsent(field.name, 0);
      usedFields.putIfAbsent(field.name, 0);
    }
    for (int j = 0; j < proc.placeHolders.size(); j++)
    {
      String fieldName = proc.placeHolders.elementAt(j);
      duplicateFields.putIfAbsent(fieldName, 0);
      usedFields.putIfAbsent(fieldName, 0);
    }
    for (int j = 0; j < proc.placeHolders.size(); j++)
    {
      String fieldName = proc.placeHolders.elementAt(j);
      int val = duplicateFields.get(fieldName);
      duplicateFields.put(fieldName, val + 1);
    }

    int inputProcSize = proc.inputs.size();
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      int val = duplicateFields.get(field.name);
      if (val > 1)
      {
        inputProcSize = inputProcSize + val - 1;
      }
    }

    if (proc.outputs.size() > 0 && proc.isInsert && primaryKeyField != null)
      outData.println("  q_.Open(q_.command, NOBINDS, 0, 0, ROWSIZE);");
    else if (proc.outputs.size() > 0)
      outData.println("  q_.Open(q_.command, NOBINDS, NODEFINES, NOROWS, ROWSIZE);");
    else if (proc.inputs.size() > 0)
      outData.println("  q_.Open(q_.command, " + inputProcSize + ");");
    else
      outData.println("  q_.Open(q_.command);");

    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      CommonCCode.generateCppBind(field, outData);
      if (duplicateFields.containsKey(field.name))
      {
        int val = duplicateFields.get(field.name);
        if (val > 1)
        {
          for (int k = 0; k < val - 1; k++)
          {
            CommonCCode.generateCppBind(field.useName() + (k + 1), field.type, outData);
          }
        }
      }
    }
    
    int currentBindNo = 0;
    Vector<Field> blobs = new Vector<Field>();
    for (int j = 0; j < placeHolder.pairs.size(); j++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(j);
      Field field = pair.field;
      String tablename = table.tableNameWithSchema();
      String bind = "Bind";
      if (field.type == Field.BLOB) bind += "Blob";

      int val = duplicateFields.get(field.name);
      if (val > 1)
      {
        int usedNo = usedFields.get(field.name);
        if (usedNo == 0)
        {
          outData.println("  q_." + bind + "(" + CommonCCode.padder("" + currentBindNo + ",", 4) + CommonCCode.cppBind(field, tablename, proc.isInsert) + CommonCCode.padder(", " + CommonCCode.cppDirection(field), 4) + ((CommonCCode.isNull(field)) ? ", &" + field.useName() + "IsNull" : "") + CommonCCode.charFieldFlag(field) + ");");
        }
        else
        {
          outData.println("  " + CommonCCode.cppCopy(field.useName() + (usedNo), field));
          outData.println("  q_." + bind + "(" + CommonCCode.padder("" + currentBindNo + ",", 4) + CommonCCode.cppBind(field.useName() + (usedNo), field.type, field.length, field.scale, field.precision, tablename, proc.isInsert) + CommonCCode.padder(", " + CommonCCode.cppDirection(field), 4) + ((CommonCCode.isNull(field)) ? ", &" + field.useName() + "IsNull" : "") + CommonCCode.charFieldFlag(field) + ");");
        }
        usedFields.put(field.name, usedNo + 1);
      }
      else
      {
        outData.println("  q_." + bind + "(" + CommonCCode.padder("" + currentBindNo + ",", 4) + CommonCCode.cppBind(field, tablename, proc.isInsert) + CommonCCode.padder(", " + CommonCCode.cppDirection(field), 4) + ((CommonCCode.isNull(field)) ? ", &" + field.useName() + "IsNull" : "") + CommonCCode.charFieldFlag(field) + ");");
      }

      currentBindNo += 1;

      if (field.type == Field.BLOB)
        blobs.addElement(field);
    }
    if (!proc.isInsert)
    {
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field)proc.outputs.elementAt(j);
        String define = "Define";
        if (field.type == Field.BLOB) define += "Blob";
        //else if (field.type == Field.BIGXML) define += "BigXML";
        outData.println("  q_." + define +"(" + CommonCCode.padder("" + j + ",", 4) + CommonCCode.cppDefine(field) + ");");
      }
    }
    outData.println("  q_.Exec();");
    for (int j = 0; j < blobs.size(); j++)
    {
      Field field = (Field)blobs.elementAt(j);
      outData.println("  SwapBytes(" + field.useName() + ".len); // fixup len in data on intel type boxes");
    }
    if (proc.isInsert && primaryKeyField != null)
    {
      outData.println();
      outData.println("  T" + fullName + "_LastInsertID lastID(q_.conn);");
      outData.println("  lastID.Exec();");
      outData.println("  this->" + primaryKeyField.useName() + " = lastID." + primaryKeyField.useName() + ";");
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
          if ((CommonCCode.isSequence(field) && proc.isInsert)
          || (CommonCCode.isIdentity(field) && proc.isInsert)
          || field.type == Field.TIMESTAMP
          || field.type == Field.AUTOTIMESTAMP
          || field.type == Field.USERSTAMP)
            continue;
          outData.println("  " + CommonCCode.cppCopy(field));
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String)proc.dynamics.elementAt(j);
          outData.println("  strncpy(" + s + ", a" + s + ", sizeof(" + s + ")-1);");
        }
        outData.println("  Exec();");
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
        outData.println("  q_.Get(" + CommonCCode.cppGet(field) + ");");
        if (CommonCCode.isNull(field))
          outData.println("  q_.GetNull(" + field.useName() + "IsNull, " + j + ");");
      }
      outData.println("  return true;");
      outData.println("}");
      outData.println();
    }
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    boolean isReturning = false;
    String front = "", back = "", sequencer = "";
    Vector<String> lines = placeHolder.getLines();
    int size = 1;
    if (proc.isInsert == true && proc.hasReturning == true && proc.outputs.size() == 1)
    {
      Field field = (Field)proc.outputs.elementAt(0);
      if (field.isSequence == true)
      {
        isReturning = true;

        size += front.length();
        size += back.length();
        size += sequencer.length();
      }
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
    outData.println("    q_.command = new char [" + size + "];");
    outData.println("  memset(q_.command, 0, " + size + ");");
    if (isReturning == true)
    {
      outData.println("  struct cpp_ret {const char* head; const char *output; const char *sequence; const char* tail; cpp_ret(){head = output = sequence = tail = \"\";}} _ret;");
      if (!sequencer.isEmpty())
        outData.println("  _ret.sequence = \"" + sequencer + ",\";");
      if (!front.isEmpty())
        outData.println("  _ret.head = \"" + front + "\";");
      if (!back.isEmpty())
        outData.println("  _ret.tail = \"" + back + "\";");
    }
    String strcat = "  strcat(q_.command, ";
    String terminate = "";
    if (lines.size() > 0)
    {
      if (!front.isEmpty())
        outData.println(strcat + "_ret.head);");

      for (int i = 0; i < lines.size(); i++)
      {
        String l = (String)lines.elementAt(i);
        // What a fucking hack ... See above why this is here
        if (l.contains("output inserted"))
          continue;

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
          outData.print(strcat + CommonCCode.check(l));
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
  static void generateWithParms(Proc proc, PrintWriter outData, String pad)
  {
    String comma = "  ";
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      if ((CommonCCode.isSequence(field) && proc.isInsert) || (CommonCCode.isIdentity(field) && proc.isInsert) || field.type == Field.TIMESTAMP || field.type == Field.AUTOTIMESTAMP || field.type == Field.USERSTAMP)
        continue;
      outData.println(pad + comma + "const " + CommonCCode.cppParm(field));
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
    placeHolder = new PlaceHolder(proc, MySqlCCodeOutputOptions, PlaceHolder.QUESTION, "");
    String front = "  { ";
    boolean standardExec = true;
    if (proc.outputs.size() > 0)
    {
      outData.println("  enum");
      Field field = (Field)proc.outputs.elementAt(0);
      String thisOne = field.useName().toUpperCase() + "_OFFSET";
      String lastOne = thisOne;
      String lastSize = CommonCCode.cppLength(field);
      outData.println(front + CommonCCode.padder(thisOne, 24) + "= 0");
      front = "  , ";
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = (Field)proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase() + "_OFFSET";
        outData.println("  , " + CommonCCode.padder(thisOne, 24) + "= (" + lastOne + "+" + lastSize + ")");
        lastOne = thisOne;
        lastSize = CommonCCode.cppLength(field);
      }
      outData.println("  , " + CommonCCode.padder("ROWSIZE", 24) + "= (" + lastOne + "+" + lastSize + ")");
      if (proc.isSingle)
        outData.println("  , " + CommonCCode.padder("NOROWS", 24) + "= 1");
      else if (proc.noRows > 0)
        outData.println("  , " + CommonCCode.padder("NOROWS", 24) + "= " + proc.noRows);
      else
        outData.println("  , " + CommonCCode.padder("NOROWS", 24) + "= (24*1024 / ROWSIZE) + 1");
      outData.println("  , " + CommonCCode.padder("NOBINDS", 24) + "= " + placeHolder.pairs.size());
      outData.println("  , " + CommonCCode.padder("NODEFINES", 24) + "= " + proc.outputs.size());
      field = (Field)proc.outputs.elementAt(0);
      thisOne = field.useName().toUpperCase();
      outData.println("  , " + CommonCCode.padder(thisOne + "_POS", 24) + "= 0");
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = (Field)proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase();
        outData.println("  , " + CommonCCode.padder(thisOne + "_POS", 24) + "= " + CommonCCode.padder(thisOne + "_OFFSET", 24) + "* NOROWS");
      }
      outData.println("  };");
    }
    else if (proc.isMultipleInput)
    {
      int noNulls = 0;
      standardExec = false;
      outData.println("  enum");
      Field field = (Field)proc.inputs.elementAt(0);
      if (CommonCCode.isNull(field) || (field.type == Field.CHAR && field.isNull == true) || (field.type == Field.ANSICHAR && field.isNull == true))
        noNulls++;
      String thisOne = field.useName().toUpperCase() + "_OFFSET";
      String lastOne = thisOne;
      String thisSize = field.useName().toUpperCase() + "_SIZE";
      String lastSize = thisSize;
      outData.println(front + CommonCCode.padder(thisOne, 24) + "= 0");
      front = "  , ";
      outData.println(front + CommonCCode.padder(thisSize, 24) + "= " + CommonCCode.cppLength(field));
      for (int j = 1; j < proc.inputs.size(); j++)
      {
        field = (Field)proc.inputs.elementAt(j);
        if (CommonCCode.isNull(field) || (field.type == Field.CHAR && field.isNull == true) || (field.type == Field.ANSICHAR && field.isNull == true))
          noNulls++;
        thisOne = field.useName().toUpperCase() + "_OFFSET";
        thisSize = field.useName().toUpperCase() + "_SIZE";
        outData.println("  , " + CommonCCode.padder(thisOne, 24) + "= (" + lastOne + "+" + lastSize + ")");
        outData.println("  , " + CommonCCode.padder(thisSize, 24) + "= " + CommonCCode.cppLength(field));
        lastOne = thisOne;
        lastSize = thisSize;
      }
      outData.println("  , " + CommonCCode.padder("ROWSIZE", 24) + "= (" + lastOne + "+" + lastSize + ")");
      outData.println("  , " + CommonCCode.padder("NOBINDS", 24) + "= " + placeHolder.pairs.size());
      outData.println("  , " + CommonCCode.padder("NONULLS", 24) + "= " + noNulls);
      outData.println("  };");
      outData.println("  void Exec(int32 noOf, " + dataStruct + "* Recs);");
    }
    outData.println("  TJQuery q_;");
    if (standardExec == true)
    {
      if (proc.outputs.size() > 0)
          outData.println("  bool Fetch();");

      outData.println("  void Exec();");
      outData.println("  void Exec(" + dataStruct + "& Rec) {*DRec() = Rec;Exec();}");

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
                if ((CommonCCode.isSequence(field) && proc.isInsert) || (CommonCCode.isIdentity(field) && proc.isInsert) || field.type == Field.TIMESTAMP || field.type == Field.AUTOTIMESTAMP || field.type == Field.USERSTAMP)
                    continue;
                val = true;
            }
            for (int j = 0; j < proc.dynamics.size(); j++) {
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
  static String fromXMLFormat(Field field)
  {
    String front = "";
    if (CommonCCode.isNull(field))
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
    if (CommonCCode.isNull(field))
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
