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
import java.util.HashMap;

public class BinCSCode extends Generator
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
        outLog.println(args[i]+": Generate IDL Code for BinU 3 Tier Access");
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
    return "Generate IDL Code for Binu 3 Tier Access";
  }
  public static String documentation()
  {
    return "Generate IDL Code for BinU 3 Tier Access";
  }
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length-1; i++)
      s = s + " ";
    return s + " ";
  }
  /**
  * Generates the procedure classes for each table present.
  */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generateStructs(table, output, outLog);
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output + table.useName() + ".cs");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".cs");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// ###############################################################");
          outData.println("// # PLEASE BEWARE OF THE GENERATED CODE MODIFIER ATTACK MONSTER #");
          outData.println("// #                        'Vengeance is mine' sayeth The Beast #");
          outData.println("// ###############################################################");
          outData.println("using System;");
          outData.println("using System.IO;");
          //outData.println("using Bbd.Idl2;");
          outData.println("namespace bbd.jportal");
          outData.println("{");
          generateStructs(table, outData);
          //generateBinUsage(table, outData);
          outData.println("}");
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
  private static void generateStructs(Table table, PrintWriter outData)
  {
    if (table.fields.size() > 0)
    {
      generateEnumOrdinals(table, outData);
      generateStruct(table.fields, table.useName(), outData);
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc = (Proc)table.procs.elementAt(i);
        if (proc.isStd || proc.isStdExtended() || proc.hasNoData())
          generateBinCode(table, proc, table.fields, outData);
      }
      outData.println("  }");
      outData.println();
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      generateStructSetup(proc, table.useName() + proc.upperFirst(), outData);
      outData.println("  }");
      outData.println();
    }
  }
  private static int maxVarNameLen = 4;
  private static void setMaxVarNameLen(Vector<Field> fields, int minVarNameLen)
  {
    maxVarNameLen = minVarNameLen;
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      int len = field.useName().length();
      if (isNull(field)) len += 6;
      if (len > maxVarNameLen)
        maxVarNameLen = len;
    }
  }
  private static void generateStruct(Vector<Field> fields, String mainName, PrintWriter outData)
  {
    setMaxVarNameLen(fields, 4);
    outData.println("  public partial class " + mainName + " : ITJMarshal");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("    " + fieldDef(field));
      if (isNull(field))
        outData.println("    public bool " + field.useLowerName() + "IsNull;");
    }
    generateReader(fields, outData);
    generateWriter(fields, outData);
  }
  private static void generateWriter(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("    public void Write(TJWriter writer)");
    outData.println("    {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      writeCall(field, outData);
    }
    outData.println("    }");
  }
  private static void generateReader(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("    public void Read(TJReader reader)");
    outData.println("    {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      readCall(field, outData);
    }
    outData.println("    }");
  }
  private static void generateStructSetup(Proc proc, String mainName, PrintWriter outData)
  {
    Vector<Field> fields = new Vector<Field>();
    for (int i=0; i<proc.outputs.size(); i++)
      fields.addElement(proc.outputs.elementAt(i));
    if (proc.hasDiscreteInput())
    {
      Vector<?> inputs = proc.inputs;
      for (int j = 0; j < inputs.size(); j++)
      {
        Field field = (Field)inputs.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        fields.addElement(field);
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        Integer n = (Integer)proc.dynamicSizes.elementAt(j);
        Field field = new Field();
        field.name = s;
        field.type = Field.CHAR;
        field.length = n.intValue();
        fields.addElement(field);
      }
    }
    generateStruct(fields, mainName, outData);
    generateBinCode(proc.table, proc, fields, outData);
  }
  private static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("  public class " + table.useName() + field.useUpperName() + "Ord");
        outData.println("  {");
        String datatype = "Int32";
        if (field.type == Field.ANSICHAR && field.length == 1)
          datatype = "string";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "\"" + (char)en.value + "\"";
          outData.println("    public const " + datatype + " " + en.name + " = " + evalue + ";");
        }
        outData.println("    public static string ToString(" + datatype + " ordinal)");
        outData.println("    {");
        outData.println("      switch (ordinal)");
        outData.println("      {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "\"" + (char)en.value + "\"";
          outData.println("      case " + evalue + ": return \"" + en.name + "\";");
        }
        outData.println("      }");
        outData.println("      return \"unknown ordinal: \"+ordinal;");
        outData.println("    }");
        outData.println("  }");
      }
      else if (field.valueList.size() > 0)
      {
        outData.println("  public class " + table.useName() + field.useUpperName() + "Ord");
        outData.println("  {");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String en = (String)field.valueList.elementAt(j);
          outData.println("    public const int " + en + " = " + j + ";");
        }
        outData.println("    public static string ToString(int ordinal)");
        outData.println("    {");
        outData.println("      switch (ordinal)");
        outData.println("      {");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String en = (String)field.valueList.elementAt(j);
          outData.println("      case " + j + ": return \"" + en + "\";");
        }
        outData.println("      }");
        outData.println("      return \"unknown ordinal: \"+ordinal;");
        outData.println("    }");
        outData.println("  }");
      }
    }
  }
  private static void skip(int used, PrintWriter outData)
  {
    int size = used % 8;
    if (size == 0)
      outData.println();
    else
      outData.println("reader.Skip(" + (8 - size) + ");");
  }
  private static void filler(int used, PrintWriter outData)
  {
    int size = used % 8;
    if (size == 0)
      outData.println();
    else
      outData.println("writer.Filler(" + (8 - size) + ");");
  }
  private static void writeCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.print("      writer.PutString(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("      writer.PutDateTime(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("      writer.PutDecimal(" + var + ", 21);");
        filler(21, outData);
        break;
      case Field.BLOB:
        outData.print("      writer.PutJPBlob(" + var + ", " + field.length + ");");
        filler(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("      writer.PutInt16(" + var + ");writer.Filler(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("      writer.PutInt16(" + var + ");writer.Filler(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("      writer.PutDecimal(" + var + ", " + (field.precision + 3) + ");");
          filler(field.precision + 3, outData);
        }
        else
          outData.println("      writer.PutDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      writer.PutInt32(" + var + ");writer.Filler(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      writer.PutInt64(" + var + ");");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
    if (isNull(field))
      outData.println("      writer.PutBool(" + var + "IsNull);writer.Filler(6);");
  }
  private static void readCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.print("      reader.GetString(out " + var + ", " + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("      reader.GetDateTime(out " + var + ", " + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("      reader.GetDecimal(out " + var + ", " + (21) + ");");
        skip(21, outData);
        break;
      case Field.BLOB:
        outData.print("      reader.GetJPBlob(out " + var + ", " + field.length + ");");
        skip(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("      reader.GetInt16(out " + var + ");reader.Skip(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("      reader.GetInt16(out " + var + ");reader.Skip(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("      reader.GetDecimal(out " + var + ", " + (field.precision + 3) + ");");
          skip(field.precision + 3, outData);
        }
        else
          outData.println("      reader.GetDouble(out " + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      reader.GetInt32(out " + var + ");reader.Skip(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      reader.GetInt64(out " + var + ");");
        break;
      default:
        outData.println("    // " + var + " unsupported");
        break;
    }
    if (isNull(field))
      outData.println("      reader.GetBool(out " + field.useLowerName() + "IsNull);reader.Skip(6);");
  }
  private static String fieldDef(Field field)
  {
    String result;
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.XML:
        result = "string";
        break;
      case Field.MONEY:
        result = "decimal";
        break;
      case Field.BLOB:
        result = "JPBlob";
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        result = "DateTime";
        break;
      case Field.BOOLEAN:
        result = "bool";
        break;
      case Field.BYTE:
      case Field.STATUS:
        result = "short";
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          result = "decimal";
        else
          result = "double";
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        result = "int";
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        result = "long";
        break;
      case Field.SHORT:
        result = "short";
        break;
      case Field.DYNAMIC:
        return "public string " + field.useName();
      default:
        result = "whoknows";
        break;
    }
    return 
        padder("public " + result + " " + field.useLowerName() + ";", maxVarNameLen + 18)
      + padder("public " + result + " " + field.useUpperName(), maxVarNameLen + 18)
      + padder("{get {return this." + field.useLowerName() + ";}", maxVarNameLen + 20)
      + "set {this." + field.useLowerName() + " = value;}}";
  }
  private static boolean isNull(Field field)
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
      case Field.TIME:
        return true;
    }
    return false;
  }
  private static void generateBinUsageAction(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    outData.print("    public static void " + proc.upperFirst() + "(");
    if (hasInput)
      outData.print("ref " + dataStruct + " rec");
    outData.println(")");
    outData.println("    {");
    if (hasInput)
      outData.println("      TJGeneric.Action<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, ref rec);");
    else
      outData.print("      TJGeneric.ActionOnly<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN);");
    outData.println("    }");
  }
  private static void generateBinUsageBulkAction(Table table, Proc proc, String dataStruct, PrintWriter outData)
  {
    outData.println("    public static void " + proc.upperFirst() + "(ref " + dataStruct + "[] recs)");
    outData.println("    {");
    outData.println("      TJGeneric.BulkAction<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, ref recs);");
    outData.println("    }");
  }
  private static void generateBinUsageSingle(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    if (hasInput == true)
      outData.println("    public static bool " + proc.upperFirst() + "(ref " + dataStruct + " rec)");
    else
      outData.println("    public static bool " + proc.upperFirst() + "(ref " + dataStruct + " rec)");
    outData.println("    {");
    if (hasInput == true)
      outData.println("      return TJGeneric.Single<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, ref rec);");
    else
      outData.println("      return TJGeneric.SingleOnly<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, ref rec);");
    outData.println("    }");
  }
  private static void generateBinUsageMultiple(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    outData.println("    public static void " + proc.upperFirst() + "(" + (hasInput ? "ref " + dataStruct + " rec, " : "") + "ref "+ dataStruct + "[] recs)");
    outData.println("    {");
    if (hasInput == true)
      outData.println("    TJGeneric.Multiple<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, ref rec, ref recs);");
    else
      outData.println("    TJGeneric.MultipleOnly<" + dataStruct + ">(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, ref recs);");
    outData.println("    }");
  }
  private static PlaceHolder placeHolder;
  private static int recLength;
  private static HashMap<String, Integer> makeHashMap(Proc proc)
  {
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    recLength = 0;
    if (proc.isStd == true)
    {
      int offset = 0;
      for (int i = 0; i < proc.table.fields.size(); i++)
      {
        Field field = (Field)proc.table.fields.elementAt(i);
        int fieldLen = cppLength(field);
        map.put(field.name, new Integer(offset));
        offset += fieldLen;
        if (isNull(field) == true) offset += 8;
        recLength = offset;
      }
    }
    else
    {
      int offset = 0;
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        int fieldLen = cppLength(field);
        map.put(field.name, new Integer(offset));
        offset += fieldLen;
        if (isNull(field) == true) offset += 8;
        recLength = offset;
      }
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if (map.containsKey(field.name) == true)
          continue;
        int fieldLen = cppLength(field);
        map.put(field.name, new Integer(offset));
        offset += fieldLen;
        if (isNull(field) == true) offset += 8;
        recLength = offset;
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        String s = (String)proc.dynamics.elementAt(i);
        map.put(s, new Integer(offset));
        Integer n = (Integer)proc.dynamicSizes.elementAt(i);
        offset += upit(n.intValue());
        recLength = offset;
      }
    }
    return map;
  }
  private static String fieldIs(Field field)
  {
    String result = "[";
    String tween = "";
    if (field.isPrimaryKey) { result += tween + "PK"; tween = " "; }
    if (field.isSequence) { result += tween + "SEQ"; tween = " "; }
    if (field.isNull) { result += tween + "NULL"; tween = " "; }
    if (field.isIn) { result += tween + "IN"; tween = " "; }
    if (field.isOut) { result += tween + "OUT"; }
    return result + "]";
  }
  private static int noRows(Proc proc, int recLength)
  {
    if (proc.outputs.size() == 0)
      return 0;
    if (proc.noRows != 0)
      return proc.noRows;
    if (proc.isSingle == true)
      return 1;
    if (recLength < 262144 && recLength > 0)
      return 262144 / recLength;
    return 1;
  }
  private static void generateBinCode(Table table, Proc proc, Vector<Field> fields, PrintWriter outData)
  {
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    String dataStruct;
    if (proc.isStd || proc.isStdExtended() || proc.hasNoData())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    HashMap<?, ?> map = makeHashMap(proc);
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    outData.println("    const string " + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN =");
    outData.print("@\"");
    Database database = table.database;
    outData.print("conn " + database.name);
    if (database.server.length() > 0
    && database.schema.length() > 0)
      outData.print(" " + database.server + " " + database.schema);
    if (database.userid.length() > 0
    && database.password.length() > 0)
      outData.print(" " + database.userid + " " + database.password);
    outData.println();
    outData.print("  proc " + table.name + " " + proc.name);
    Vector<?> lines = placeHolder.getLines();
    outData.print("(" + noRows(proc, recLength) + " ");
    outData.print(proc.lines.size() + " ");
    outData.print(placeHolder.pairs.size() + " ");
    outData.print(proc.outputs.size() + " ");
    outData.print(proc.inputs.size() + " ");
    outData.print(proc.dynamics.size() + " ");
    outData.print(recLength + " ");
    String tween = "";
    outData.print("[");
    if (proc.isProc) { outData.print(tween + "PRC"); tween = " "; }
    if (proc.isSProc) { outData.print(tween + "SPR"); tween = " "; }
    if (proc.isData) { outData.print(tween + "DAT"); tween = " "; }
    if (proc.isIdlCode) { outData.print(tween + "IDL"); tween = " "; }
    if (proc.isSql) { outData.print(tween + "SQL"); tween = " "; }
    if (proc.isSingle) { outData.print(tween + "SNG"); tween = " "; }
    if (proc.isAction) { outData.print(tween + "ACT"); tween = " "; }
    if (proc.isStd) { outData.print(tween + "STD"); tween = " "; }
    if (proc.useStd) { outData.print(tween + "USE"); tween = " "; }
    if (proc.extendsStd) { outData.print(tween + "EXT"); tween = " "; }
    if (proc.useKey) { outData.print(tween + "KEY"); tween = " "; }
    if (proc.hasImage) { outData.print(tween + "IMG"); tween = " "; }
    if (proc.isMultipleInput) { outData.print(tween + "MUL"); tween = " "; }
    if (proc.isInsert) { outData.print(tween + "INS"); tween = " "; }
    if (proc.hasReturning) { outData.print(tween + "RET"); }
    outData.println("])");
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field)proc.outputs.elementAt(j);
      Integer offset = (Integer)map.get(field.name);
      int fieldLen = cppLength(field);
      outData.println("  out " + field.name + "(" + field.type + " " + field.length
        + " " + field.precision + " " + field.scale + " " + offset.intValue() + " "
        + fieldLen + " " + fieldIs(field) + ")");
    }
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      Integer offset = (Integer)map.get(field.name);
      int fieldLen = cppLength(field);
      outData.println("  inp " + field.name + "(" + field.type + " " + field.length
        + " " + field.precision + " " + field.scale + " " + offset.intValue() + " "
        + fieldLen + " " + fieldIs(field) + ")");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      Integer n = (Integer)proc.dynamicSizes.elementAt(j);
      int len = n.intValue();
      Integer offset = (Integer)map.get(s);
      outData.println("  dyn " + s + "(" + len + " " + offset.intValue() + ")");
    }
    if (placeHolder.pairs.size() > 0)
    {
      outData.print("  binds");
      tween = "(";
      for (int j = 0; j < placeHolder.pairs.size(); j++)
      {
        PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(j);
        Field field = pair.field;
        for (int k = 0; k < proc.inputs.size(); k++)
        {
          Field input = (Field)proc.inputs.elementAt(k);
          if (input.useName().compareTo(field.useName()) == 0)
          {
            outData.print(tween + k);
            tween = " ";
            break;
          }
        }
      }
      outData.println(")");
    }
    for (int j = 0; j < lines.size(); j++)
    {
      String line = (String)lines.elementAt(j);
      if (line.charAt(0) == ' ')
        outData.println("  `&" + (line.substring(1)) + "`");
      else if (line.charAt(0) != '"')
        outData.println("  `&" + line + "`");
      else
        outData.println("  `" + (line.substring(1, line.length() - 1)) + "`");
    }
    outData.println("  \";");
    if (proc.isMultipleInput)
      generateBinUsageBulkAction(table, proc, dataStruct, outData);
    else if (proc.isInsert && proc.hasReturning)
      generateBinUsageSingle(table, proc, dataStruct, hasInput, outData);
    else if (proc.outputs.size() > 0)
      if (proc.isSingle)
        generateBinUsageSingle(table, proc, dataStruct, hasInput, outData);
      else
        generateBinUsageMultiple(table, proc, dataStruct, hasInput, outData);
    else
      generateBinUsageAction(table, proc, dataStruct, hasInput, outData);
  }
  private static int upit(int no)
  {
    int n = no % 8;
    if (n > 0)
      return no + (8 - n);
    return no;
  }
  private static int cppLength(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
        return 2 + 6;
      case Field.INT:
      case Field.SEQUENCE:
      case Field.IDENTITY:
        return 4 + 4;
      case Field.LONG:
      case Field.BIGSEQUENCE:
      case Field.BIGIDENTITY:
        return 8;
      case Field.CHAR:
      case Field.ANSICHAR:
      case Field.TLOB:
      case Field.XML:
        return upit(field.length + 1);
      case Field.BLOB:
        return field.length;
      case Field.USERSTAMP:
        return 9 + 7;
      case Field.DATE:
        return 9 + 7;
      case Field.TIME:
        return 7 + 1;
      case Field.DATETIME:
      case Field.TIMESTAMP:
        return 15 + 1;
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return upit(field.precision + 3);
        return 8;
      case Field.MONEY:
        return 21 + 3;
    }
    return 0;
  }
}
