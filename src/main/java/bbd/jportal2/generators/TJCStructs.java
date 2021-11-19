/// ------------------------------------------------------------------
/// Copyright (c) 1996, 2007 Vincent Risi in Association 
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

import bbd.jportal.Enum;
import bbd.jportal.Field;
import bbd.jportal.Proc;
import bbd.jportal.Table;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import static bbd.jportal2.generators.Writer.*;

public class TJCStructs
{
  public static void generateSnips(Table table, String output, boolean checkBindO) throws Exception
  {
    //outLog.println("Code: " + output + table.useName().toLowerCase() + "_snips.h");
    try (PrintWriter outWriter = new PrintWriter(new FileOutputStream(output + table.useName().toLowerCase() + "_snips.h")))
    {
      writer = outWriter;
      indent_size = 4;
      writeln("// This code was generated, do not modify it, modify it at source and regenerate it.");
      writeln(format("#ifndef _%s_SNIPS_H_", table.useName().toUpperCase()));
      writeln(format("#define _%s_SNIPS_H_", table.useName().toUpperCase()));
      writeln();
      writeln("#include \"list.h\"");
      writeln(format("#include \"%s.sh\"", table.useName().toLowerCase()));
      writeln();
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc = table.procs.elementAt(i);
        if (proc.isData)
          continue;
        if (proc.isMultipleInput)
          generateSnipsBulkAction(table, proc);
        else if (proc.isInsert && proc.hasReturning)
          generateSnipsAction(table, proc, checkBindO);
        else if (proc.outputs.size() > 0)
          if (proc.isSingle)
            generateSnipsSingle(table, proc);
          else
            generateSnipsMultiple(table, proc);
        else
          generateSnipsAction(table, proc, false);
        writeln();
      }
      writeln("#endif");
      writer.flush();
    }
  }
  private static void generateSnipsAction(Table table, Proc proc, boolean checkBindO)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    write("inline void " + table.useName() + proc.upperFirst() + "(TJConnector *connect");
    if (hasInput || proc.hasModifieds())
      write(", D" + dataStruct + " *rec");
    writeln(")");
    writeln("{");
    writeln(1, "T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    if (hasInput || proc.hasModifieds())
      writeln(1, "q.Exec(*rec);");
    else
      writeln(1, "q.Exec();");
    if (proc.hasReturning && checkBindO == false)
    {
      writeln(1, "if (q.Fetch())");
      writeln(2, "*rec = *q.DRec();");
    }
    else if (proc.hasReturning && proc.isInsert && proc.outputs.size() == 1 && checkBindO)
      writeln(1, "*rec = *q.DRec();");
    else if (proc.hasModifieds())
      writeln(1, "*rec = *q.DRec();");
    writeln("}");
  }
  private static void generateSnipsBulkAction(Table table, Proc proc)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    write("inline void " + table.useName() + proc.upperFirst() + "(TJConnector *connect");
    write(", int noOf, D" + dataStruct + " *recs");
    writeln(")");
    writeln("{");
    writeln(1, "T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    writeln(1, "q.Exec(noOf, recs);");
    writeln("}");
  }
  private static void generateSnipsSingle(Table table, Proc proc)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    writeln("inline bool " + table.useName() + proc.upperFirst()
            + "(TJConnector *connect, D" + dataStruct + " *rec)");
    writeln("{");
    writeln(1, "T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    if (hasInput)
      writeln(1, "q.Exec(*rec);");
    else
      writeln(1, "q.Exec();");
    writeln(1, "if (q.Fetch())");
    writeln(1, "{");
    writeln(2, "*rec = *q.DRec();");
    writeln(2, "return true;");
    writeln(1, "}");
    writeln(1, "return false;");
    writeln("}");
  }
  private static void generateSnipsMultiple(Table table, Proc proc)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    write("inline void " + table.useName() + proc.upperFirst() + "(TJConnector *connect");
    if (hasInput)
      write(", D" + dataStruct + "* inRec");
    writeln(", int32* noOf, O" + dataStruct + "*& outRecs)");
    writeln("{");
    writeln(1, "T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
    if (hasInput)
      writeln(1, "q.Exec(*inRec);");
    else
      writeln(1, "q.Exec();");
    writeln(1, "while (q.Fetch())");
    writeln(2, "SnipAddList(outRecs, *noOf, *q.ORec(), (int32)q.NOROWS);");
    writeln("}");
  }
  static Vector<String> nullVector = new Vector<String>();
  static String structName = "";
  static
  public void generateStdOutputRec(Table table)
  {
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = table.comments.elementAt(i);
      writeln("//" + s);
    }
    int filler = 0;
    structName = "D" + table.useName();
    writeln("struct D" + table.useName());
    writeln("{");
    boolean canExtend = true;
    Vector<Field> fields = table.fields;
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.type == Field.BLOB)
        canExtend = false;
      if (field.comments.size() > 0)
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = field.comments.elementAt(c);
          writeln(1, "//" + s);
        }
      writeln(1, "" + padder(cppVar(field) + ";", 48) + generatePadding(field, filler++));
      if (isNull(field))
      {
        writeln(1, "" + padder("int16  " + field.useName() + "IsNull;", 48) + generatePadding(filler++));
        filler++;
      }
    }
    writeln();
    headerSwaps("", fields, null);
    String useName = table.useName();
    if (canExtend == true)
      extendHeader("", fields, useName, nullVector, null);
    else
      extendDataBuildHeader("", fields, useName, nullVector, null);
    writeln("};");
    writeln();
    writeln("typedef D" + table.useName() + " O" + table.useName() + ";");
    writeln();
    generateEnumOrdinals(table);
  }
  static
  public void generateUserOutputRecs(Table table)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = table.procs.elementAt(i);
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
        Field field = fields.elementAt(j);
        if (field.type == Field.BLOB)
          canExtend = false;
      }
      fields = proc.inputs;
      for (int j = 0; j < fields.size(); j++)
      {
        Field field = fields.elementAt(j);
        if (field.type == Field.BLOB)
          canExtend = false;
      }
      fields = proc.outputs;
      if (fields.size() > 0)
      {
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = proc.comments.elementAt(j);
          writeln("//" + comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput())
          typeChar = "O";
        work = " : public " + typeChar + table.useName() + proc.upperFirst();
        baseClass = typeChar + table.useName() + proc.upperFirst();
        structName = typeChar + table.useName() + proc.upperFirst();
        writeln("struct " + typeChar + table.useName() + proc.upperFirst());
        writeln("{");
        int filler = 0;
        for (int j = 0; j < fields.size(); j++)
        {
          Field field = fields.elementAt(j);
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = field.comments.elementAt(c);
            writeln(1, "//" + s);
          }
          writeln(1, "" + padder(cppVar(field) + ";", 48) + generatePadding(field, filler++));
          if (isNull(field))
            writeln(1, "" + padder("int16  " + field.useName() + "IsNull;", 48) + generatePadding(filler++));
        }
        writeln();
        headerSwaps("", fields, null);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader("", fields, useName, nullVector, null);
        else
          extendDataBuildHeader("", fields, useName, nullVector, null);
        writeln("};");
        writeln();
      }
      if (proc.hasDiscreteInput())
      {
        structName = "D" + table.useName() + proc.upperFirst();
        writeln("struct D" + table.useName() + proc.upperFirst() + work);
        writeln("{");
        int filler = 0;
        Vector<Field> inputs = proc.inputs;
        for (int j = 0; j < inputs.size(); j++)
        {
          Field field = inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = field.comments.elementAt(c);
            writeln(1, "//" + s);
          }
          writeln(1, "" + padder(cppVar(field) + ";", 48) + generatePadding(field, filler++));
          if (isNull(field))
            writeln(1, "" + padder("int16  " + field.useName() + "IsNull;", 48) + generatePadding(filler++));
        }
        writeln();
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = proc.dynamics.elementAt(j);
          Integer n = proc.dynamicSizes.elementAt(j);
          writeln(1, "" + padder("char " + s + "[" + (n + 1) + "];", 48) + charPadding(n + 1, filler++));
        }
        headerSwaps(baseClass, inputs, proc);
        String useName = table.useName() + proc.upperFirst();
        if (canExtend == true)
          extendHeader(baseClass, inputs, useName, proc.dynamics, proc);
        else
          extendDataBuildHeader(baseClass, inputs, useName, proc.dynamics, proc);
        writeln("};");
        writeln();
      } else if (fields.size() > 0)
      {
        writeln("typedef D" + table.useName() + proc.upperFirst() + " O" + table.useName() + proc.upperFirst() + ";");
        writeln();
      }
    }
  }
  private static void headerSwaps(String baseClass, Vector<Field> inputs, Proc proc)
  {
    writeln(1, "void Clear()");
    writeln(1, "{");
    if (baseClass.length() > 0)
      writeln(2, "" + baseClass + "::Clear();");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      writeln(2, "" + cppInit(field));
    }
    if (proc != null)
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = proc.dynamics.elementAt(j);
        writeln(2, "memset(" + s + ", 0, sizeof(" + s + "));");
      }
    writeln(1, "}");
    writeln(1, "" + structName + "() { Clear(); }");
    writeln(1, "#ifdef swapbytesH");
    writeln(1, "void Swaps()");
    writeln(1, "{");
    if (baseClass.length() > 0)
      writeln(2, "" + baseClass + "::Swaps();");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      if (notString(field) == false)
        continue;
      if (isStruct(field) == false)
        writeln(2, "SwapBytes(" + field.useName() + ");");
      else
        writeln(2, "" + field.useName() + ".Swaps();");
      if (isNull(field))
        writeln(2, "SwapBytes(" + field.useName() + "IsNull);");
    }
    writeln(1, "}");
    writeln(1, "#endif");
  }
  private static void extendHeader(String baseClass, Vector<Field> inputs, String useName, Vector<String> dynamics, Proc proc)
  {
    writeln(1, "#if defined(_TBUFFER_H_)");
    writeln(1, "void _toXML(TBAmp &XRec)");
    writeln(1, "{");
    if (baseClass.length() > 0)
      writeln(2, "" + baseClass + "::_toXML(XRec);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      writeln(2, "" + toXMLFormat(field));
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = dynamics.elementAt(j);
      String front = "XRec.append(\"  <" + str + ">\");";
      String back = "XRec.append(\"</" + str + ">\");";
      writeln(2, "" + front + "XRec.ampappend(" + str + ");" + back);
    }
    writeln(1, "}");
    writeln(1, "void ToXML(TBAmp &XRec, char* Attr, char* Outer)");
    writeln(1, "{");
    writeln(2, "XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\\n\");");
    writeln(2, "_toXML(XRec);");
    writeln(2, "XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\\n\");");
    writeln(1, "}");
    writeln(1, "void ToXML(TBAmp &XRec, char* Attr) {ToXML(XRec, Attr, \"" + useName + "\");}");
    writeln(1, "void ToXML(TBAmp &XRec) {ToXML(XRec, 0);}");
    writeln(1, "#endif");
    writeln(1, "#if defined(__XMLRECORD_H__)");
    writeln(1, "void _fromXML(TBAmp &XRec, TXMLRecord &msg)");
    writeln(1, "{");
    writeln(2, "TBAmp work;");
    if (baseClass.length() > 0)
      writeln(2, "" + baseClass + "::_fromXML(XRec, msg);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      write(2, "msg.GetValue(\"" + field.useName() + "\", work);");
      writeln(fromXMLFormat(field));
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = dynamics.elementAt(j);
      write(2, "msg.GetValue(\"" + str + "\", work);");
      writeln("memcpy(" + str + ", work.data, sizeof(" + str + ")-1);");
    }
    writeln(1, "}");
    writeln(1, "void FromXML(TBAmp &XRec)");
    writeln(1, "{");
    writeln(2, "TXMLRecord msg;");
    writeln(2, "msg.Load(XRec);");
    writeln(2, "memset(this, 0, sizeof(*this));");
    writeln(2, "_fromXML(XRec, msg);");
    writeln(1, "}");
    writeln(1, "#endif");
    extendDataBuildHeader(baseClass, inputs, useName, dynamics, proc);
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
  private static void extendDataBuildHeader(String baseClass, Vector<Field> inputs, String useName, Vector<String> dynamics, Proc proc)
  {
    writeln(1, "#if defined(_DATABUILD_H_)");
    int inputNo = 0;
    if (baseClass.length() > 0)
    {
      for (int j = 0; j < inputs.size(); j++)
      {
        Field field = inputs.elementAt(j);
        if (proc != null && proc.hasOutput(field.name))
          continue;
        inputNo++;
      }
      writeln(1, "static int NoBuildFields() {return " + baseClass + "::NoBuildFields()+" + (inputNo + dynamics.size()) + ";}");
    } else
      writeln(1, "static int NoBuildFields() {return " + (inputs.size() + dynamics.size()) + ";}");
    writeln(1, "void _buildAdds(DataBuilder &dBuild)");
    writeln(1, "{");
    if (baseClass.length() > 0)
      writeln(2, "" + baseClass + "::_buildAdds(dBuild);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      if (field.type == Field.BLOB)
        writeln(2, "dBuild.add(\"" + field.useName() + "\", " + field.useName() + ".len, " + field.useName() + ".data" + nullAdd(field) + ");");
      else
        writeln(2, "dBuild.add(\"" + field.useName() + "\", " + field.useName() + nullAdd(field) + ");");
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = dynamics.elementAt(j);
      writeln(2, "dBuild.add(\"" + str + "\", " + str + ");");
    }
    writeln(1, "}");
    writeln(1, "void BuildData(DataBuilder &dBuild, char *name)");
    writeln(1, "{");
    writeln(2, "dBuild.name(name);");
    writeln(2, "_buildAdds(dBuild);");
    writeln(1, "}");
    writeln(1, "void BuildData(DataBuilder &dBuild) {BuildData(dBuild, \"" + useName + "\");}");
    writeln(1, "void _buildSets(DataBuilder &dBuild)");
    writeln(1, "{");
    if (baseClass.length() > 0)
      writeln(2, "" + baseClass + "::_buildSets(dBuild);");
    for (int j = 0; j < inputs.size(); j++)
    {
      Field field = inputs.elementAt(j);
      if (proc != null && proc.hasOutput(field.name))
        continue;
      if (field.type == Field.BLOB)
        writeln(2, "dBuild.set(\"" + field.useName() + "\", " + field.useName() + ".len, " + field.useName() + ".data, sizeof(" + field.useName() + ".data)" + nullSet(field) + ");");
      else
        writeln(2, "dBuild.set(\"" + field.useName() + "\", " + field.useName() + ", sizeof(" + field.useName() + ")" + nullSet(field) + ");");
    }
    for (int j = 0; j < dynamics.size(); j++)
    {
      String str = dynamics.elementAt(j);
      writeln(2, "dBuild.set(\"" + str + "\", " + str + ", sizeof(" + str + "));");
    }
    writeln(1, "}");
    writeln(1, "void SetData(DataBuilder &dBuild, char *name)");
    writeln(1, "{");
    writeln(2, "dBuild.name(name);");
    writeln(2, "_buildSets(dBuild);");
    writeln(1, "}");
    writeln(1, "void SetData(DataBuilder &dBuild) {SetData(dBuild, \"" + useName + "\");}");
    writeln(1, "#endif");
  }
  public static String padder(String s, int length)
  {
    StringBuilder sBuilder = new StringBuilder(s);
    for (int i = sBuilder.length(); i < length; i++)
      sBuilder.append(" ");
    s = sBuilder.toString();
    return s;
  }
  public static void generateEnumOrdinals(Table table)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        writeln("enum e" + table.useName() + field.useName());
        String start = "{";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char) element.value + "'";
          writeln(start + " " + table.useName() + field.useName() + element.name + " = " + evalue);
          start = ",";
        }
        writeln("};");
        writeln();
        writeln("inline char *" + table.useName() + field.useName() + "Lookup(int no)");
        writeln("{");
        writeln(1, "switch(no)");
        writeln(1, "{");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char) element.value + "'";
          writeln(1, "case " + evalue + ": return \"" + element.name + "\";");
        }
        writeln(1, "default: return \"<unknown value>\";");
        writeln(1, "}");
        writeln("}");
        writeln();
      } else if (field.valueList.size() > 0)
      {
        writeln("enum e" + table.useName() + field.useName());
        String start = "{";
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String element = field.valueList.elementAt(j);
          writeln(start + " " + table.useName() + field.useName() + element);
          start = ",";
        }
        writeln("};");
        writeln();
        writeln("inline const char *" + table.useName() + field.useName() + "Lookup(int no)");
        writeln("{");
        writeln(1, "switch(no)");
        writeln(1, "{");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String element = field.valueList.elementAt(j);
          writeln(1, "case " + j + ": return \"" + element + "\";");
        }
        writeln(1, "default: return \"<unknown value>\";");
        writeln(1, "}");
        writeln("}");
        writeln();
      }
    }
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
  private static boolean isNull(Field field)
  {
    if (field.isNull == false)
      return false;
    return switch (field.type)
            {
              case Field.BOOLEAN, Field.FLOAT, Field.DOUBLE, Field.MONEY, Field.BYTE, Field.SHORT, Field.INT, Field.LONG, Field.IDENTITY, Field.SEQUENCE, Field.BIGIDENTITY, Field.BIGSEQUENCE, Field.BLOB, Field.DATE, Field.DATETIME, Field.TIMESTAMP, Field.AUTOTIMESTAMP, Field.TIME ->
                      //case Field.XML:
                      true;
              default -> false;
            };
  }
  private static boolean notString(Field field)
  {
    return switch (field.type)
            {
              case Field.BOOLEAN, Field.BYTE, Field.SHORT, Field.INT, Field.LONG, Field.IDENTITY, Field.SEQUENCE, Field.BIGIDENTITY, Field.BIGSEQUENCE, Field.BLOB -> true;
              case Field.FLOAT, Field.DOUBLE -> field.precision <= 15;
              default -> false;
            };
  }
  static
  private boolean isStruct(Field field)
  {
    return field.type == Field.BLOB;
  }
  static
  private String cppInit(Field field)
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
      case Field.MONEY:
        return "memset(" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.BLOB:
        return "memset(&" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision <= 15)
          return field.useName() + " = 0.0;";
        return "memset(" + field.useName() + ", 0, sizeof(" + field.useName() + "));";
    }
    return field.useName() + " <unsupported>";
  }
  /**
   * Translates field type to cpp data member type
   */
  static
  private String cppVar(Field field)
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
      case Field.DATE:
        return "char   " + field.useName() + "[9]";
      case Field.BLOB:
        return "TJBlob<" + field.length + "> " + field.useName();
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
  static
  private String fromXMLFormat(Field field)
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
      case Field.MONEY:
        return front + "memcpy(" + field.useName() + ", work.data, sizeof(" + field.useName() + ")-1);";
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
    }
    return "// " + field.useName() + " <unsupported>";
  }
  static
  private String toXMLFormat(Field field)
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
      case Field.MONEY:
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
    }
    return "// " + field.useName() + " <unsupported>";
  }
}
