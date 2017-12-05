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
import java.io.DataOutputStream;
import java.lang.*;
import java.util.Vector;

public class IdlBinUCode extends Generator
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
      generateBin(table, output, outLog);
    }
  }
  private static boolean businessLogic(Proc proc)
  {
    for (int i = 0; i < proc.options.size(); i++)
    {
      String option = (String)proc.options.elementAt(i);
      if (option.compareTo("BL") == 0)
        return true;
    }
    return false;
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output + "U" + table.useName() + ".cs");
      OutputStream outFile = new FileOutputStream(output + "U" + table.useName() + ".cs");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("using System;");
          outData.println("using System.IO;");
          outData.println("using bbd.idl2;");
          outData.println("namespace Vlab.idl2.jportal");
          outData.println("{");
          generateStructs(table, outData);
          generateBinUsage(table, outData);
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
      if (table.comments.size() > 0)
      {
        outData.println("  /// <summary>");
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println("  /// " + s);
        }
        outData.println("  /// </summary>");
      }
      generateTableStructs(table.fields, table.useName(), outData);
      generateEnumOrdinals(table, outData);
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      if (proc.comments.size() > 0)
      {
        outData.println("  /// <summary>");
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String s = (String)proc.comments.elementAt(j);
          outData.println("  /// " + s);
        }
        outData.println("  /// </summary>");
      }
      generateStructPairs(proc, table.useName() + proc.upperFirst(), outData);
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
  private static void generateTableStructs(Vector<Field> fields, String mainName, PrintWriter outData)
  {
    setMaxVarNameLen(fields, 4);
    outData.println("  public partial class U" + mainName);
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
    outData.println("  }");
  }
  private static void generateWriter(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("    public void Writer(BinUWriter writer)");
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
    outData.println("    public void Reader(BinUReader reader)");
    outData.println("    {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      readCall(field, outData);
    }
    outData.println("    }");
  }
  private static void generateStructPairs(Proc proc, String mainName, PrintWriter outData)
  {
    setMaxVarNameLen(proc.outputs, 4);
    setMaxVarNameLen(proc.inputs, maxVarNameLen);
    outData.println("  public partial class U" + mainName);
    outData.println("  {");
    Vector<Field> fields = new Vector<Field>();
    for (int i=0; i<proc.outputs.size(); i++)
      fields.addElement(proc.outputs.elementAt(i));
    if (fields.size() > 0)
    {
      for (int i = 0; i < fields.size(); i++)
      {
        Field field = (Field)fields.elementAt(i);
        outData.println("    " + fieldDef(field));
        if (isNull(field))
          outData.println("    public bool " + field.useLowerName() + "IsNull;");
      }
    }
    if (proc.hasDiscreteInput())
    {
      Vector<Field> inputs = proc.inputs;
      for (int j = 0; j < inputs.size(); j++)
      {
        Field field = (Field)inputs.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        fields.addElement(field);
        outData.println("    " + fieldDef(field));
        if (isNull(field))
          outData.println("    public bool " + field.useLowerName() + "IsNull;");
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
        outData.println("    " + fieldDef(field));
      }
    }
    generateReader(fields, outData);
    generateWriter(fields, outData);
    outData.println("  }");
  }
  private static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("  public class U" + table.useName() + field.useUpperName() + "Ord");
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
        outData.println("  public class U" + table.useName() + field.useUpperName() + "Ord");
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
        //if (field.length == 1)
        //{
        //  outData.println("      writer.WriteByte(" + var + ");writer.Filler(7);");
        //  break;
        //}
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.print("      writer.WriteString(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("      writer.WriteDateTime(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("      writer.WriteDecimal(" + var + ", 21);");
        filler(21, outData);
        break;
      case Field.BLOB:
        outData.print("      writer.WriteJPBlob(" + var + ");");
        filler(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("      writer.WriteInt16((UInt16)" + var + ");writer.Filler(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("      writer.WriteInt16((UInt16)" + var + ");writer.Filler(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("      writer.WriteDecimal(" + var + ", " + (field.precision + 3) + ");");
          filler(field.precision + 3, outData);
        }
        else
          outData.println("      writer.WriteDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      writer.WriteInt32((UInt32)" + var + ");writer.Filler(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      writer.WriteInt64((UInt64)" + var + ");");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
    if (isNull(field))
      outData.println("      writer.WriteBool(" + var + "IsNull);writer.Filler(6);");
  }
  private static void readCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    switch (field.type)
    {
      case Field.ANSICHAR:
        //if (field.length == 1)
        //{
        //  outData.println("      " + padder(var, maxVarNameLen) + "= reader.ReadByte();reader.Skip(7);");
        //  break;
        //}
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        outData.print("      " + padder(var, maxVarNameLen) + "= reader.ReadString(" + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("      " + padder(var, maxVarNameLen) + "= reader.ReadDateTime(" + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("      " + padder(var, maxVarNameLen) + "= reader.ReadDecimal(" + (21) + ");");
        skip(21, outData);
        break;
      case Field.BLOB:
        outData.print("      " + padder(var, maxVarNameLen) + "= reader.ReadJPBlob();");
        skip(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("      " + padder(var, maxVarNameLen) + "= reader.ReadInt16();reader.Skip(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("      " + padder(var, maxVarNameLen) + "= reader.ReadInt16();reader.Skip(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("      " + padder(var, maxVarNameLen) + "= reader.ReadDecimal(" + (field.precision + 3) + ");");
          skip(field.precision + 3, outData);
        }
        else
          outData.println("      " + padder(var, maxVarNameLen) + "= reader.ReadDouble();");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      " + padder(var, maxVarNameLen) + "= reader.ReadInt32();reader.Skip(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      " + padder(var, maxVarNameLen) + "= reader.ReadInt64();");
        break;
      default:
        outData.println("    // " + var + " unsupported");
        break;
    }
    if (isNull(field))
      outData.println("      " + padder(field.useLowerName() + "IsNull", maxVarNameLen) + "= reader.ReadBool();reader.Skip(6);");
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
  private static void generateBinUsage(Table table, PrintWriter outData)
  {
    outData.println("  public class " + table.useName() + "BinUsage : BinUsage");
    outData.println("  {");
    outData.println("    const string TABLE = \"" + table.useName() + "\";");
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isMultipleInput)
        generateBinUsageBulkAction(table, proc, outData);
      else if (proc.isInsert && proc.hasReturning)
        generateBinUsageAction(table, proc, outData);
      else if (proc.outputs.size() > 0)
        if (proc.isSingle)
          generateBinUsageSingle(table, proc, outData);
        else
          generateBinUsageMultiple(table, proc, outData);
      else
        generateBinUsageAction(table, proc, outData);
    }
    outData.println("  }");
  }
  private static void generateBinUsageAction(Table table, Proc proc, PrintWriter outData)
  {
    if (businessLogic(proc) == false)
      return;
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.print("    public static void " + proc.upperFirst() + "(");
    if (hasInput)
      outData.print("U" + dataStruct + " rec");
    outData.println(")");
    outData.println("    {");
    outData.print("      Action(TABLE, \"" + proc.upperFirst() + "\"");
    if (hasInput)
      outData.print(", ref rec");
    outData.println(");");
    outData.println("    }");
  }
  private static void generateBinUsageBulkAction(Table table, Proc proc, PrintWriter outData)
  {
    if (businessLogic(proc) == false)
      return;
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    outData.println("    public static void " + proc.upperFirst() + "(U" + dataStruct + "[] recs)");
    outData.println("    {");
    outData.println("      BulkAction(TABLE, \"" + proc.upperFirst() + "\", ref recs);");
    outData.println("    }");
  }
  private static void generateBinUsageSingle(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.println("    public static bool " + proc.upperFirst() + "(U" + dataStruct + " rec)");
    outData.println("    {");
    if (hasInput == true)
      outData.println("      return ReadOneInput(TABLE, \"" + proc.upperFirst() + "\", ref rec);");
    else
      outData.println("      return ReadOne(TABLE, \"" + proc.upperFirst() + "\", ref rec);");
    outData.println("    }");
  }
  private static void generateBinUsageMultiple(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    outData.println("    public static void " + proc.upperFirst() + "(" + (hasInput ? "U" + dataStruct + " rec, " : "") + "U" + dataStruct + "[] recs)");
    outData.println("    {");
    if (hasInput == true)
      outData.println("      Multiple(TABLE, \"" + proc.upperFirst() + "\", ref rec, ref recs);");
    else
      outData.println("      Multiple(TABLE, \"" + proc.upperFirst() + "\", ref recs);");
    outData.println("    }");
  }
  private static void writeString(String value, DataOutputStream outData) throws IOException
  {
    outData.writeBytes(value);
    outData.writeByte(0);
  }
  private static PlaceHolder placeHolder;
  public static class OutPos
  {
    String name;
    int offset;
    OutPos(String name, int offset)
    {
      this.name = name;
      this.offset = offset;
    }
  }
  private static void generateBin(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + "U" + table.useName() + ".bin");
      OutputStream outFile = new FileOutputStream(output + "U" + table.useName() + ".bin");
      try
      {
        DataOutputStream outData = new DataOutputStream(outFile);
        try
        {
          int noProcs = 0;
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData == false)
              noProcs++;
          }
          String signature = "TAB";
          writeString(signature, outData);
          writeString(table.name, outData);
          writeString("p=" + noProcs, outData);
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData == true)
              continue;
            placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
            signature = "QRY";
            writeString(signature, outData);
            writeString(table.name, outData);
            writeString(proc.name, outData);
            writeString("r=" + proc.noRows, outData);
            outData.write(proc.isProc ? 'Y' : 'N');
            outData.write(proc.isSProc ? 'Y' : 'N');
            outData.write(proc.isData ? 'Y' : 'N');
            outData.write(proc.isIdlCode ? 'Y' : 'N');
            outData.write(proc.isSql ? 'Y' : 'N');
            outData.write(proc.isSingle ? 'Y' : 'N');
            outData.write(proc.isAction ? 'Y' : 'N');
            outData.write(proc.isStd ? 'Y' : 'N');
            outData.write(proc.useStd ? 'Y' : 'N');
            outData.write(proc.extendsStd ? 'Y' : 'N');
            outData.write(proc.useKey ? 'Y' : 'N');
            outData.write(proc.hasImage ? 'Y' : 'N');
            outData.write(proc.isMultipleInput ? 'Y' : 'N');
            outData.write(proc.isInsert ? 'Y' : 'N');
            outData.write(proc.hasReturning ? 'Y' : 'N');
            outData.writeByte(0);
            writeString("p=" + placeHolder.pairs.size(), outData);
            writeString("i=" + proc.inputs.size(), outData);
            writeString("o=" + proc.outputs.size(), outData);
            writeString("d=" + proc.dynamics.size(), outData);
            Vector<String> lines = placeHolder.getLines();
            writeString("l=" + lines.size(), outData);
            for (int j = 0; j < lines.size(); j++)
            {
              String line = (String)lines.elementAt(j);
              if (line.charAt(0) != '"')
              {
                outData.writeByte('&');
                writeString(line.trim(), outData);
              }
              else
                writeString(line.substring(1, line.length() - 1), outData);
            }
            for (int j = 0; j < placeHolder.pairs.size(); j++)
            {
              PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(j);
              Field field = pair.field;
              for (int k = 0; k < proc.inputs.size(); k++)
              {
                Field input = (Field)proc.inputs.elementAt(k);
                if (input.useName().compareTo(field.useName()) == 0)
                {
                  writeString("" + k, outData);
                  break;
                }
              }
            }
            int fieldPos = 0;
            Vector<OutPos> outFields = new Vector<OutPos>();
            for (int j = 0; j < proc.outputs.size(); j++)
            {
              Field field = (Field)proc.outputs.elementAt(j);
              int fieldLen = cppLength(field);
              signature = "OUT";
              writeString(signature, outData);
              writeString(field.name, outData);
              writeString(field.alias, outData);
              writeString("t=" + field.type, outData);
              writeString("l=" + field.length, outData);
              writeString("p=" + field.precision, outData);
              writeString("s=" + field.scale, outData);
              writeString("o=" + fieldPos, outData);
              writeString("u=" + fieldLen, outData);
              outData.write(field.isPrimaryKey ? 'Y' : 'N');
              outData.write(field.isSequence ? 'Y' : 'N');
              outData.write(field.isNull ? 'Y' : 'N');
              outData.write(field.isIn ? 'Y' : 'N');
              outData.write(field.isOut ? 'Y' : 'N');
              outData.writeByte(0);
              outFields.addElement(new OutPos(field.name, fieldPos));
              fieldPos += fieldLen;
              if (useNull(field) == true) fieldPos += 8;
            }
            for (int j = 0; j < proc.inputs.size(); j++)
            {
              Field field = (Field)proc.inputs.elementAt(j);
              int fieldLen = cppLength(field);
              boolean usedOF = false;
              int bindAt = 0;
              for (int k = 0; k < outFields.size(); k++)
              {
                OutPos outField = (OutPos)outFields.elementAt(k);
                if (outField.name.compareTo(field.name) == 0)
                {
                  usedOF = true;
                  bindAt = outField.offset;
                  break;
                }
              }
              if (usedOF == false)
              {
                bindAt = fieldPos;
                fieldPos += fieldLen;
                if (useNull(field) == true) fieldPos += 8;
              }
              signature = "INP";
              writeString(signature, outData);
              writeString(field.name, outData);
              writeString(field.alias, outData);
              writeString("t=" + field.type, outData);
              writeString("l=" + fieldLen, outData);
              writeString("p=" + field.precision, outData);
              writeString("s=" + field.scale, outData);
              writeString("o=" + bindAt, outData);
              outData.write(field.isPrimaryKey ? 'Y' : 'N');
              outData.write(field.isSequence ? 'Y' : 'N');
              outData.write(field.isNull ? 'Y' : 'N');
              outData.write(field.isIn ? 'Y' : 'N');
              outData.write(field.isOut ? 'Y' : 'N');
              outData.writeByte(0);
            }
            for (int j = 0; j < proc.dynamics.size(); j++)
            {
              signature = "DYN";
              writeString(signature, outData);
              String s = (String)proc.dynamics.elementAt(j);
              Integer n = (Integer)proc.dynamicSizes.elementAt(j);
              writeString(s, outData);
              int len = n.intValue();
              writeString("l=" + len, outData);
              writeString("o=" + fieldPos, outData);
              fieldPos += upit(len);
            }
          }
          signature = "END";
          writeString(signature, outData);
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
  private static int upit(int no)
  {
    int n = no % 8;
    if (n > 0)
      return no + (8 - n);
    return no;
  }
  private static boolean useNull(Field field)
  {
    switch (field.type)
    {
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.BLOB:
      case Field.DATE:
      case Field.TIME:
      case Field.DATETIME:
      case Field.TIMESTAMP:
      case Field.FLOAT:
      case Field.DOUBLE:
      case Field.MONEY:
        return true;
    }
    return false;
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
