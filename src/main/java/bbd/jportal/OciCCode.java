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

public class OciCCode extends Generator
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
        outLog.println(args[i] + ": Generate OCI C++ Code");
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
    return "Generate OCI C++ Code";
  }
  public static String documentation()
  {
    return "Generate OCI C++ Code";
  }
  /**
   * Padder function
   */
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  protected static Vector<Flag> flagsVector;
  static boolean          aix;
  static boolean          lowercase;
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
      flagsVector.addElement(new Flag("aix", new Boolean(aix),
          "Generate for AIX"));
      flagsVector.addElement(new Flag("lowercase", new Boolean(aix),
          "Generate lowercase"));
    }
    return flagsVector;
  }
  static void setFlags(Database database, PrintWriter outLog)
  {
    if (flagsVector != null)
    {
      aix = toBoolean(((Flag) flagsVector.elementAt(0)).value);
      lowercase = toBoolean(((Flag) flagsVector.elementAt(1)).value);
    } else
      flagDefaults();
    for (int i = 0; i < database.flags.size(); i++)
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
  public static void generate(Database database, String output,
      PrintWriter outLog)
  {
    setFlags(database, outLog);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output, outLog);
    }
  }
  static String fileName(String output, String node, String ext)
  {
    int p = output.indexOf('\\');
    if (p == -1 && aix == true && ext.equals(".cpp"))
      ext = ".C";
    if (lowercase == true)
      node = node.toLowerCase();
    return output + node + ext;
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
          outData
              .println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("#ifndef _" + table.useName().toLowerCase() + "SH");
          outData.println("#define _" + table.useName().toLowerCase() + "SH");
          outData.println();
          outData.println("#include <stddef.h>");
          outData.println("#include \"padgen.h\"");
          outData.println("#include \"ociapi.h\"");
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
  static String structName = "";
  static void generateStdOutputRec(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
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
      Field field = (Field) table.fields.elementAt(i);
      if (field.type == Field.BLOB)
        canExtend = false;
      if (field.comments.size() > 0)
      {
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  //" + s);
        }
      }
      outData.println("  " + padder(cppVar(field) + ";", 48)
          + generatePadding(field, filler++));
      if (isNull(field))
      {
        outData.println("  "
            + padder("int16  " + field.useName() + "IsNull;", 48)
            + generatePadding(filler++));
        filler++;
      }
    }
    outData.println();
    headerSwaps(outData, "", fields);
    String useName = table.useName();
    if (canExtend == true)
      extendHeader(outData, "", fields, useName);
    else
      extendDataBuildHeader(outData, "", fields, useName);
    outData.println("};");
    outData.println();
    outData.println("typedef D" + table.useName() + " O" + table.useName()
        + ";");
    outData.println();
  }
  private static void headerSwaps(PrintWriter outData, String baseClass,
      Vector<Field> inputs)
  {
    outData.println("  void Clear()");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::Clear();");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field)inputs.elementAt(j);
      outData.println("    " + cppInit(field));
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
      Field field = (Field) inputs.elementAt(j);
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
  private static void extendHeader(PrintWriter outData, String baseClass,
      Vector<Field> inputs, String useName)
  {
    outData.println("  #if defined(_TBUFFER_H_)");
    outData.println("  void _toXML(TBAmp &XRec)");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::_toXML(XRec);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field) inputs.elementAt(j);
      outData.println("    " + toXMLFormat(field));
    }
    outData.println("  }");
    outData.println("  void ToXML(TBAmp &XRec, char* Attr=0, char* Outer=\""
        + useName + "\")");
    outData.println("  {");
    outData
        .println("    XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\\n\");");
    outData.println("    _toXML(XRec);");
    outData
        .println("    XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\\n\");");
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
      Field field = (Field) inputs.elementAt(j);
      outData.print("    msg.GetValue(\"" + field.useName() + "\", work);");
      outData.println(fromXMLFormat(field));
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
    extendDataBuildHeader(outData, baseClass, inputs, useName);
  }
  private static void extendDataBuildHeader(PrintWriter outData,
      String baseClass, Vector<Field> inputs, String useName)
  {
    outData.println("  #if defined(_DATABUILD_H_)");
    if (baseClass.length() > 0)
      outData.println("  static int NoBuildFields() {return " + baseClass + "::NoBuildFields()+" + inputs.size() + ";}");
    else
      outData.println("  static int NoBuildFields() {return " + inputs.size() + ";}");
    outData.println("  void _buildAdds(DataBuilder &dBuild)");
    outData.println("  {");
    if (baseClass.length() > 0)
      outData.println("    " + baseClass + "::_buildAdds(dBuild);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = (Field) inputs.elementAt(j);
      if (field.type == Field.BLOB)
        outData.println("    dBuild.add(\"" + field.useName() + "\", "
            + field.useName() + ".len, " + field.useName() + ".data);");
      else
        outData.println("    dBuild.add(\"" + field.useName() + "\", "
            + field.useName() + ");");

    }
    outData.println("  }");
    outData.println("  void BuildData(DataBuilder &dBuild, char *name=\""
        + useName + "\")");
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
      Field field = (Field) inputs.elementAt(j);
      if (field.type == Field.BLOB)
        outData.println("    dBuild.set(\"" + field.useName() + "\", "
            + field.useName() + ".len, " + field.useName() + ".data, sizeof("
            + field.useName() + ".data));");
      else
        outData.println("    dBuild.set(\"" + field.useName() + "\", "
            + field.useName() + ", sizeof(" + field.useName() + "));");
    }
    outData.println("  }");
    outData.println("  void SetData(DataBuilder &dBuild, char *name=\""
        + useName + "\")");
    outData.println("  {");
    outData.println("    dBuild.name(name);");
    outData.println("    _buildSets(dBuild);");
    outData.println("  }");
    outData.println("  #endif");
  }
  /**
   * Build of output data rec for user procedures
   */
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
      String work2 = "";
      boolean canExtend = true;
      Vector<Field> fields = proc.outputs;
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field) fields.elementAt(j);
        if (field.type == Field.BLOB)
          canExtend = false;
      }
      fields = proc.inputs;
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = (Field) fields.elementAt(j);
        if (field.type == Field.BLOB)
          canExtend = false;
      }
      if (proc.outputs.size() > 0)
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
        work2 = typeChar + table.useName() + proc.upperFirst();
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
          outData.println("  " + padder(cppVar(field) + ";", 48)
              + generatePadding(field, filler++));
          if (isNull(field))
            outData.println("  "
                + padder("int16  " + field.useName() + "IsNull;", 48)
                + generatePadding(filler++));
        }
        outData.println();
        headerSwaps(outData, "", fields);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader(outData, "", fields, useName);
        else
          extendDataBuildHeader(outData, "", fields, useName);
        outData.println("};");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        structName = "D" + table.useName() + proc.upperFirst();
        outData.println("struct D" + table.useName() + proc.upperFirst() + work);
        outData.println("{");
        int filler = 0;
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  //" + s);
          }
          outData.println("  " + padder(cppVar(field) + ";", 48)
              + generatePadding(field, filler++));
          if (isNull(field))
            outData.println("  "
                + padder("int16  " + field.useName() + "IsNull;", 48)
                + generatePadding(field, filler++));
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          Integer n = (Integer) proc.dynamicSizes.elementAt(j);
          outData.println("  char " + s + "[" + n.intValue() + "];");
        }
        extendHeader(outData, work2, proc.inputs, table.useName()
            + proc.upperFirst());
        outData.println("};");
        outData.println();
      } else if (proc.outputs.size() > 0)
      {
        outData.println("typedef D" + table.useName() + proc.upperFirst()
            + " O" + table.useName() + proc.upperFirst() + ";");
        outData.println();
      }
    }
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
          + "(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
      outData.println("  : q_(conn)");
      outData.println("  {q_.FileAndLine(aFile,aLine);}");
      outData.println("};");
      outData.println();
    } else
    {
      if (proc.isStd)
        dataStruct = "D" + table.useName();
      else
        dataStruct = "D" + table.useName() + proc.upperFirst();
      outData.println("struct T" + table.useName() + proc.upperFirst()
          + " : public " + dataStruct);
      outData.println("{");
      if (proc.isMultipleInput)
        generateArrayInterface(table, proc, dataStruct, outData);
      else
        generateInterface(table, proc, dataStruct, outData);
      outData.println("};");
      outData.println();
    }
  }
  static void generateArrayInterface(Table table, Proc proc, String dataStruct,
      PrintWriter outData)
  {
    outData.println("  enum");
    Field field = (Field) proc.inputs.elementAt(0);
    String thisOne = field.useName().toUpperCase() + "_OFFSET";
    String lastOne = thisOne;
    String lastSize = cppLength(field);
    outData.println("  { " + padder(thisOne, 24) + "= 0");
    int lobNo = 0;
    for (int j = 1; j < proc.inputs.size(); j++)
    {
      field = (Field) proc.inputs.elementAt(j);
      if (isLob(field))
      {
        outData
            .println("  , "
                + padder(field.useName().toUpperCase() + "_LOB", 24) + "= "
                + lobNo);
        outData.println("  , "
            + padder(field.useName().toUpperCase() + "_LOB_TYPE", 24) + "= "
            + (field.type == Field.TLOB ? "SQLT_CLOB" : "SQLT_BLOB"));
        lobNo++;
      }
      thisOne = field.useName().toUpperCase() + "_OFFSET";
      outData.println("  , " + padder(thisOne, 24) + "= (" + lastOne + "+"
          + lastSize + ")");
      lastOne = thisOne;
      lastSize = cppLength(field);
    }
    outData.println("  , " + padder("ROWSIZE", 24) + "= (" + lastOne + "+"
        + lastSize + ")");
    if (proc.noRows > 0)
      outData.println("  , " + padder("NOROWS", 24) + "= " + proc.noRows);
    else
      outData
          .println("  , " + padder("NOROWS", 24) + "= (24576 / ROWSIZE) + 1");
    outData.println("  , " + padder("NOBINDS", 24) + "= " + proc.inputs.size());
    outData.println("  , " + padder("NOLOBS", 24) + "= " + noOfLobs(proc));
    field = (Field) proc.inputs.elementAt(0);
    thisOne = field.useName().toUpperCase();
    outData.println("  , " + padder(thisOne + "_POS", 24) + "= 0");
    for (int j = 1; j < proc.inputs.size(); j++)
    {
      field = (Field) proc.inputs.elementAt(j);
      thisOne = field.useName().toUpperCase();
      outData.println("  , " + padder(thisOne + "_POS", 24) + "= "
          + padder(thisOne + "_OFFSET", 24) + "* NOROWS");
    }
    outData.println("  };");
    outData.println("  TJQuery q_;");
    outData.println("  void Clear() {memset(this, 0, sizeof(" + dataStruct
        + "));}");
    outData
        .println("  void Init(int Commit=1); // Commit after each block inserted");
    outData.println("  void Fill();");
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("  void Fill(" + dataStruct
          + "& Rec) {*DRec() = Rec;Fill();}");
      outData.println("  void Fill(");
      generateWithArrayParms(proc, outData, "  ");
      outData.println("  );");
    }
    outData.println("  void Done();");
    outData.println("  T" + table.useName() + proc.upperFirst()
        + "(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
    outData.println("  : q_(conn)");
    outData.println("  {Clear();q_.FileAndLine(aFile,aLine);}");
    outData.println("  " + dataStruct + "* DRec() {return this;}");
  }
  static int noOfInputLobs(Proc proc)
  {
    int result = 0;
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = (Field) proc.inputs.elementAt(i);
      if (isLob(field))
        result++;
    }
    return result;
  }
  static int noOfOutputLobs(Proc proc)
  {
    int result = 0;
    for (int i = 0; i < proc.outputs.size(); i++)
    {
      Field field = (Field) proc.outputs.elementAt(i);
      if (isLob(field))
        result++;
    }
    return result;
  }
  static int noOfLobs(Proc proc)
  {
    return noOfInputLobs(proc) + noOfOutputLobs(proc);
  }
  static void generateInterface(Table table, Proc proc, String dataStruct,
      PrintWriter outData)
  {
    int inputLobs = noOfInputLobs(proc);
    int outputLobs = noOfOutputLobs(proc);
    if (proc.outputs.size() > 0 || inputLobs + outputLobs > 0)
      outData.println("  enum");
    String front = "  { ";
    int lobNo = 0;
    if (inputLobs > 0)
    {
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        if (isLob(field))
        {
          outData.println(front
              + padder(field.useName().toUpperCase() + "_LOB", 24) + "= "
              + lobNo);
          outData.println("  , "
              + padder(field.useName().toUpperCase() + "_LOB_TYPE", 24) + "= "
              + (field.type == Field.TLOB ? "SQLT_CLOB" : "SQLT_BLOB"));
          lobNo++;
          front = "  , ";
        }
      }
    }
    if (outputLobs > 0)
    {
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);
        if (isLob(field))
        {
          outData.println(front
              + padder(field.useName().toUpperCase() + "_LOB", 24) + "= "
              + lobNo);
          outData.println("  , "
              + padder(field.useName().toUpperCase() + "_LOB_TYPE", 24) + "= "
              + (field.type == Field.TLOB ? "SQLT_CLOB" : "SQLT_BLOB"));
          lobNo++;
          front = "  , ";
        }
      }
    }
    if (proc.outputs.size() > 0)
    {
      Field field = (Field) proc.outputs.elementAt(0);
      String thisOne = field.useName().toUpperCase() + "_OFFSET";
      String lastOne = thisOne;
      String lastSize = cppLength(field);
      outData.println(front + padder(thisOne, 24) + "= 0");
      front = "  , ";
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = (Field) proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase() + "_OFFSET";
        outData.println("  , " + padder(thisOne, 24) + "= (" + lastOne + "+"
            + lastSize + ")");
        lastOne = thisOne;
        lastSize = cppLength(field);
      }
      outData.println("  , " + padder("ROWSIZE", 24) + "= (" + lastOne + "+"
          + lastSize + ")");
      if (proc.isSingle)
        outData.println("  , " + padder("NOROWS", 24) + "= 1");
      else if (proc.noRows > 0)
        outData.println("  , " + padder("NOROWS", 24) + "= " + proc.noRows);
      else
        outData.println("  , " + padder("NOROWS", 24)
            + "= (24576 / ROWSIZE) + 1");
      outData.println("  , " + padder("NOBINDS", 24) + "= "
          + proc.inputs.size());
      outData.println("  , " + padder("NODEFINES", 24) + "= "
          + proc.outputs.size());
      outData.println("  , " + padder("NOLOBS", 24) + "= " + noOfLobs(proc));
      field = (Field) proc.outputs.elementAt(0);
      thisOne = field.useName().toUpperCase();
      outData.println("  , " + padder(thisOne + "_POS", 24) + "= 0");
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = (Field) proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase();
        outData.println("  , " + padder(thisOne + "_POS", 24) + "= "
            + padder(thisOne + "_OFFSET", 24) + "* NOROWS");
      }
    }
    if (proc.outputs.size() > 0 || inputLobs + outputLobs > 0)
      outData.println("  };");
    outData.println("  TJQuery q_;");
    outData.println("  void Clear() {memset(this, 0, sizeof(" + dataStruct
        + "));}");
    outData.println("  void Exec();");
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("  void Exec(" + dataStruct
          + "& Rec) {*DRec() = Rec;Exec();}");
      outData.println("  void Exec(");
      generateWithParms(proc, outData, "  ");
      outData.println("  );");
    }
    if (proc.outputs.size() > 0)
      outData.println("  bool Fetch();");
    outData.println("  T" + table.useName() + proc.upperFirst()
        + "(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
    outData.println("  : q_(conn)");
    outData.println("  {Clear();q_.FileAndLine(aFile,aLine);}");
    outData.println("  " + dataStruct + "* DRec() {return this;}");
    if (proc.outputs.size() > 0)
      outData.println("  O" + dataStruct.substring(1)
          + "* ORec() {return this;}");
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
      if (proc.isMultipleInput)
        generateArrayImplementation(table, proc, outData);
      else
        generateImplementation(table, proc, outData);
    }
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    int size = 1;
    questionsSeen = 0;
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        size += 256;
      else
        size += question(proc, l.line).length();
    }
    outData.println("  if (q_.command == 0)");
    outData.println("    q_.command = new char [" + size + "];");
    outData.println("  memset(q_.command, 0, sizeof(q_.command));");
    if (proc.lines.size() > 0)
    {
      String strcat = "  strcat(q_.command, ";
      String tail = "";
      questionsSeen = 0;
      for (int i = 0; i < proc.lines.size(); i++)
      {
        Line l = (Line) proc.lines.elementAt(i);
        if (l.isVar)
        {
          tail = ");";
          if (i != 0)
            outData.println(tail);
          outData.print("  strcat(q_.command, " + l.line + "");
          strcat = "  strcat(q_.command, ";
        } else
        {
          if (i != 0)
            outData.println(tail);
          tail = "";
          outData.print(strcat + "\"" + question(proc, l.line) + "\"");
          strcat = "                      ";
        }
      }
      outData.println(");");
    }
  }
  /**
   * Emits class method for processing the database activity
   */
  static void generateArrayImplementation(Table table, Proc proc,
      PrintWriter outData)
  {
    String fullName = table.useName() + proc.name;
    outData.println("void T" + fullName + "::Init(int Commit)");
    outData.println("{");
    generateCommand(proc, outData);
    outData
        .println("  q_.OpenArray(q_.command, NOBINDS, NOROWS, NOLOBS, ROWSIZE);");
    outData.println("  q_.SetCommit(Commit);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      outData.println("  q_.BindArray("
          + padder("\":" + field.name + "\",", 24) + padder("" + j + ",", 4)
          + cppBindArray(field, table.name) + ");");
    }
    outData.println("}");
    outData.println();
    outData.println("void T" + fullName + "::Fill()");
    outData.println("{");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if (field.type == Field.SEQUENCE && proc.isInsert)
        outData.println("  q_.Sequence(" + field.useName() + ", \""
            + table.name + "Seq\");");
      if (field.type == Field.TIMESTAMP)
        outData.println("  q_.conn.TimeStamp(" + field.useName() + ");");
      if (field.type == Field.USERSTAMP)
        outData.println("  q_.UserStamp(" + field.useName() + ");");
      outData.println("  q_.Put(" + cppPut(field) + ");");
      if (isNull(field))
        outData.println("  q_.PutNull(" + field.useName() + "IsNull, " + j
            + ");");
    }
    outData
        .println("  q_.Deliver(0); // 0 indicates defer doing it if not full");
    outData.println("}");
    outData.println();
    outData.println("void T" + fullName + "::Fill(");
    generateWithArrayParms(proc, outData, "");
    outData.println(")");
    outData.println("{");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE && proc.isInsert)
          || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
          || field.type == Field.USERSTAMP)
        continue;
      outData.println("  " + cppCopy(field));
    }
    outData.println("  Fill();");
    outData.println("}");
    outData.println();
    outData.println("void T" + fullName + "::Done()");
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
    String fullName = table.useName() + proc.name;
    outData.println("void T" + fullName + "::Exec()");
    outData.println("{");
    generateCommand(proc, outData);
    if (proc.outputs.size() > 0)
      outData
          .println("  q_.Open(q_.command, NOBINDS, NODEFINES, NOLOBS, NOROWS, ROWSIZE);");
    else if (proc.inputs.size() > 0)
      outData.println("  q_.Open(q_.command, " + proc.inputs.size() + ");");
    else
      outData.println("  q_.Open(q_.command);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      generateCppBind(field, outData);
    }
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      outData.println("  q_.Bind("
          + padder("\":" + field.name + "\",", 24)
          + padder("" + j + ",", 4)
          + cppBind(field, table.name, proc.isInsert)
          + ((isNull(field)) ? ", &" + field.useName()
              + "IsNull);" : ");"));
    }
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field) proc.outputs.elementAt(j);
      outData.println("  q_.Define(" + padder("" + j + ",", 4)
          + cppDefine(field) + ");");
    }
    outData.println("  q_.Exec();");
    outData.println("}");
    outData.println();
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("void T" + fullName + "::Exec(");
      generateWithParms(proc, outData, "");
      outData.println(")");
      outData.println("{");
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        if ((field.type == Field.SEQUENCE && proc.isInsert)
            || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
            || field.type == Field.USERSTAMP)
          continue;
        outData.println("  " + cppCopy(field));
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String) proc.dynamics.elementAt(j);
        outData.println("  strncpy(" + s + ", a" + s + ", sizeof(" + s
            + ")-1);");
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
        Field field = (Field) proc.outputs.elementAt(j);
        outData.println("  q_.Get(" + cppGet(field) + ");");
        if (isNull(field))
          outData.println("  q_.GetNull(" + field.useName() + "IsNull, " + j
              + ");");
      }
      outData.println("  return true;");
      outData.println("}");
      outData.println();
    }
  }
  static void generateWithArrayParms(Proc proc, PrintWriter outData, String pad)
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
  /**
   * Translates field type to cpp data member type
   */
  static String cppLength(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
      // return "sizeof(bool)";
    case Field.BYTE:
      // return "sizeof(signed char)";
    case Field.SHORT:
      return "sizeof(int16)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "sizeof(int32)";
    case Field.LONG:
      return "sizeof(int32)";
    case Field.CHAR:
    case Field.ANSICHAR:
      return "" + (field.length + 1);
    case Field.USERSTAMP:
      return "51";
    case Field.BLOB:
    case Field.TLOB:
      return "sizeof(OCILobLocator *)";
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
        return field.useName() + " = 0;";
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
      case Field.MONEY:
        return field.useName() + " = 0.0;";
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
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int32  " + field.useName();
    case Field.LONG:
      return "int32  " + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.USERSTAMP:
      return "char   " + field.useName() + "[51]";
    case Field.BLOB:
    case Field.TLOB:
      return "TJLob  " + field.useName();
    case Field.DATE:
      return "char   " + field.useName() + "[9]";
    case Field.TIME:
      return "char   " + field.useName() + "[7]";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char   " + field.useName() + "[15]";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double " + field.useName();
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
      return "int32  a" + field.useName();
    case Field.LONG:
      return "int32  a" + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char*  a" + field.useName();
    case Field.USERSTAMP:
      return "char*  a" + field.useName();
    case Field.BLOB:
    case Field.TLOB:
      return "TJLob  a" + field.useName();
    case Field.DATE:
      return "char*  a" + field.useName();
    case Field.TIME:
      return "char*  a" + field.useName();
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char*  a" + field.useName();
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double a" + field.useName();
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
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
    case Field.SEQUENCE:
      return field.useName() + " = a" + field.useName() + ";";
    case Field.CHAR:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
      return "strncpy(" + field.useName() + ", a" + field.useName()
          + ", sizeof(" + field.useName() + ")-1);";
    case Field.ANSICHAR:
      return "memcpy(" + field.useName() + ", a" + field.useName()
          + ", sizeof(" + field.useName() + "));";
    case Field.BLOB:
    case Field.TLOB:
      return field.useName() + " = a" + field.useName() + ";";
    case Field.USERSTAMP:
    case Field.IDENTITY:
    case Field.TIMESTAMP:
      return "// " + field.useName() + " -- generated";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * generate Holding variables
   */
  static void generateCppBind(Field field, PrintWriter outData)
  {
    switch (field.type)
    {
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      outData.println("  TJOCIDate " + field.useName() + "_OCIDate;");
    }
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
    case Field.INT:
    case Field.LONG:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName();
    case Field.SEQUENCE:
      if (isInsert)
        return "q_.Sequence(" + field.useName() + ", \"" + tableName + "Seq\")";
      else
        return field.useName();
    case Field.CHAR:
      return field.useName() + ", " + (field.length + 1);
    case Field.ANSICHAR:
      return field.useName() + ", " + (field.length + 1) + ", 1";
    case Field.BLOB:
    case Field.TLOB:
      return "q_.LobLocator(q_.ociLobs[" + field.useName().toUpperCase()
          + "_LOB], " + field.useName() + "), " + field.useName().toUpperCase()
          + "_LOB_TYPE";
    case Field.USERSTAMP:
      return "q_.UserStamp(" + field.useName() + "), 51";
    case Field.DATE:
      return "q_.Date(" + field.useName() + "_OCIDate, " + field.useName()
          + ")";
    case Field.TIME:
      return "q_.Time(" + field.useName() + "_OCIDate, " + field.useName()
          + ")";
    case Field.DATETIME:
      return "q_.DateTime(" + field.useName() + "_OCIDate, " + field.useName()
          + ")";
    case Field.TIMESTAMP:
      return "q_.TimeStamp(" + field.useName() + "_OCIDate, " + field.useName()
          + ")";
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
      return "(int32*)   (q_.data+" + field.useName().toUpperCase() + "_POS)";
    case Field.CHAR:
      return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
          + (field.length + 1);
    case Field.ANSICHAR:
      return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
          + (field.length + 1) + ", 1";
    case Field.USERSTAMP:
      return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), 51";
    case Field.BLOB:
    case Field.TLOB:
      return "q_.LobLocator(q_.ociLobs[" + field.useName().toUpperCase()
          + "_LOB*NOROWS], " + field.useName() + "), "
          + field.useName().toUpperCase() + "_LOB_TYPE";
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "(TJOCIDate*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "(double*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
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
      return "(int16*)  (q_.data+" + field.useName().toUpperCase() + "_POS)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "(int32*)    (q_.data+" + field.useName().toUpperCase() + "_POS)";
    case Field.LONG:
      return "(int32*)   (q_.data+" + field.useName().toUpperCase() + "_POS)";
    case Field.CHAR:
      return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
          + (field.length + 1);
    case Field.ANSICHAR:
      return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
          + (field.length + 1) + ", 1";
    case Field.USERSTAMP:
      return "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), 51";
    case Field.BLOB:
    case Field.TLOB:
      return "(OCILobLocator*) (q_.data+" + field.useName().toUpperCase()
          + "_POS), " + field.useName().toUpperCase() + "_LOB_TYPE";
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "(TJOCIDate*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "(double*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
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
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return padder(field.useName() + ",", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS";
    case Field.CHAR:
    case Field.ANSICHAR:
      return padder(field.useName() + ",", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS, " + (field.length + 1);
    case Field.USERSTAMP:
      return padder(field.useName() + ",", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS, 51";
    case Field.BLOB:
    case Field.TLOB:
      return padder(field.useName() + ",", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS";
    case Field.DATE:
      return padder("TJDate(" + field.useName() + "),", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS";
    case Field.TIME:
      return padder("TJTime(" + field.useName() + "),", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return padder("TJDateTime(" + field.useName() + "),", 32) + " q_.data+"
          + field.useName().toUpperCase() + "_POS";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static String cppPut(Field field)
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
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + field.useName() + ", " + (field.length + 1);
    case Field.USERSTAMP:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + field.useName() + ", 51";
    case Field.BLOB:
    case Field.TLOB:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + field.useName();
    case Field.DATE:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + "TJDate(" + field.useName() + ")";
    case Field.TIME:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + "TJTime(" + field.useName() + ")";
    case Field.DATETIME:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + "TJDateTime(" + field.useName() + ")";
    case Field.TIMESTAMP:
      return padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
          + "TJDateTime(" + field.useName() + ")";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   */
  static boolean isStruct(Field field)
  {
    return field.type == Field.TLOB || field.type == Field.BLOB;
  }
  static boolean isLob(Field field)
  {
    return field.type == Field.TLOB || field.type == Field.BLOB;
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
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.IDENTITY:
    case Field.SEQUENCE:
    case Field.TLOB:
      return true;
    }
    return false;
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
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
      return field.length + 1;
    case Field.TLOB:
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
    case Field.MONEY:
      return 8;
    }
    return 4;
  }
  static String fromXMLFormat(Field field)
  {
    switch (field.type)
    {
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.BLOB:
    case Field.TLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "memcpy(" + field.useName() + ", work.data, sizeof("
          + field.useName() + ")-1);";
    case Field.BOOLEAN:
    case Field.BYTE:
      return field.useName() + " = (uchar)atoi(work.data);";
    case Field.SHORT:
      return field.useName() + " = (short)atoi(work.data);";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " = atoi(work.data);";
    case Field.LONG:
      return field.useName() + " = atol(work.data);";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " = atof(work.data);";
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
    case Field.BLOB:
    case Field.TLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return front + "XRec.ampappend(" + field.useName() + ");" + back;
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.LONG:
      return front + "XRec.ampappend(JP_XML_FORMAT((int32)" + field.useName()
          + ").result);" + back;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return front + "XRec.ampappend(JP_XML_FORMAT((double)" + field.useName()
          + ").result);" + back;
    }
    return "// " + field.useName() + " <unsupported>";
  }
  public static String generatePadding(Field field, int fillerNo)
  {
    int n;
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "IDL2_INT16_PAD(" + fillerNo + ");";
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "IDL2_INT32_PAD(" + fillerNo + ");";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      n = 8 - ((field.length + 1) % 8);
      if (n != 8)
        return "IDL2_CHAR_PAD(" + fillerNo + "," + n + ");";
    }
    return "";
  }
  public static String generatePadding(int fillerNo)
  {
    return "IDL2_INT16_PAD(" + fillerNo + ");";
  }
}
