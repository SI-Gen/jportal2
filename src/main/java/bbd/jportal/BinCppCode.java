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

public class BinCppCode extends Generator
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
      generateBinCppCode(table, output, outLog);
    }
  }
  private static PlaceHolder placeHolder;
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
  private static void generateBinCppCode(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName().toLowerCase() + "bin.cpp");
      outLog.println("Code: " + output + table.useName().toLowerCase() + "bin.h");
      OutputStream outCppFile = new FileOutputStream(output + table.useName().toLowerCase() + "bin.cpp");
      OutputStream outHeadFile = new FileOutputStream(output + table.useName().toLowerCase() + "bin.h");
      try
      {
        PrintWriter outCppData = new PrintWriter(outCppFile);
        PrintWriter outHeadData = new PrintWriter(outHeadFile);
        outCppData.println("// ###############################################################");
        outCppData.println("// # PLEASE BEWARE OF THE GENERATED CODE MODIFIER ATTACK MONSTER #");
        outCppData.println("// #                        'Vengeance is mine' sayeth The Beast #");
        outCppData.println("// ###############################################################");
        outCppData.println("#include \"" + table.useName().toLowerCase() + "bin.h\"");
        outCppData.println();
        outHeadData.println("// ###############################################################");
        outHeadData.println("// # PLEASE BEWARE OF THE GENERATED CODE MODIFIER ATTACK MONSTER #");
        outHeadData.println("// #                        'Vengeance is mine' sayeth The Beast #");
        outHeadData.println("// ###############################################################");
        outHeadData.println("#ifndef _" + table.useName().toLowerCase() + "binH");
        outHeadData.println("#define _" + table.useName().toLowerCase() + "binH");
        outHeadData.println();
        try
        {
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData == true)
              continue;
            HashMap<?, ?> map = makeHashMap(proc);
            placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
            outHeadData.println("extern const char *" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN;");
            outCppData.println("const char *" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN =");
            Database database = table.database;
            outCppData.print("  \"conn " + database.name);
            if (database.server.length() > 0
            && database.schema.length() > 0)
              outCppData.print(" " + database.server + " " + database.schema);
            if (database.userid.length() > 0
            && database.password.length() > 0)
              outCppData.print(" " + database.userid + " " + database.password);
            outCppData.println(" \"");
            outCppData.print("  \"proc " + table.name + " " + proc.name);
            String tween = "";
            Vector<?> lines = placeHolder.getLines();
            outCppData.print("(" + noRows(proc, recLength) + " ");
            outCppData.print(proc.lines.size() + " ");
            outCppData.print(placeHolder.pairs.size() + " ");
            outCppData.print(proc.outputs.size() + " ");
            outCppData.print(proc.inputs.size() + " ");
            outCppData.print(proc.dynamics.size() + " ");
            outCppData.print(recLength + " ");
            outCppData.print("[");
            if (proc.isProc) { outCppData.print(tween + "PRC"); tween = " "; }
            if (proc.isSProc) { outCppData.print(tween + "SPR"); tween = " "; }
            if (proc.isData) { outCppData.print(tween + "DAT"); tween = " "; }
            if (proc.isIdlCode) { outCppData.print(tween + "IDL"); tween = " "; }
            if (proc.isSql) { outCppData.print(tween + "SQL"); tween = " "; }
            if (proc.isSingle) { outCppData.print(tween + "SNG"); tween = " "; }
            if (proc.isAction) { outCppData.print(tween + "ACT"); tween = " "; }
            if (proc.isStd) { outCppData.print(tween + "STD"); tween = " "; }
            if (proc.useStd) { outCppData.print(tween + "USE"); tween = " "; }
            if (proc.extendsStd) { outCppData.print(tween + "EXT"); tween = " "; }
            if (proc.useKey) { outCppData.print(tween + "KEY"); tween = " "; }
            if (proc.hasImage) { outCppData.print(tween + "IMG"); tween = " "; }
            if (proc.isMultipleInput) { outCppData.print(tween + "MUL"); tween = " "; }
            if (proc.isInsert) { outCppData.print(tween + "INS"); tween = " "; }
            if (proc.hasReturning) { outCppData.print(tween + "RET"); }
            outCppData.println("]) \"");
            for (int j = 0; j < proc.outputs.size(); j++)
            {
              Field field = (Field)proc.outputs.elementAt(j);
              Integer offset = (Integer) map.get(field.name);
              int fieldLen = cppLength(field);
              outCppData.println("  \"out " + field.name + "(" + field.type + " " + field.length
                + " " + field.precision + " " + field.scale + " " + offset.intValue() + " " + fieldLen + " " + fieldIs(field) + ") \"");
            }
            for (int j = 0; j < proc.inputs.size(); j++)
            {
              Field field = (Field)proc.inputs.elementAt(j);
              Integer offset = (Integer)map.get(field.name);
              int fieldLen = cppLength(field);
              outCppData.println("  \"inp " + field.name + "(" + field.type + " " + field.length
                + " " + field.precision + " " + field.scale + " " + offset.intValue()
                + " " + fieldLen + " " + fieldIs(field) + ") \"");
            }
            for (int j = 0; j < proc.dynamics.size(); j++)
            {
              String s = (String)proc.dynamics.elementAt(j);
              Integer offset = (Integer)map.get(s);
              Integer n = (Integer)proc.dynamicSizes.elementAt(j);
              int len = n.intValue();
              outCppData.println("  \"dyn " + s + "(" + len + " " + offset.intValue() + ") \"");
            }
            if (placeHolder.pairs.size() > 0)
            {
              outCppData.print("  \"binds");
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
                    outCppData.print(tween + k);
                    tween = " ";
                    break;
                  }
                }
              }
              outCppData.println(") \"");
            }
            for (int j = 0; j < lines.size(); j++)
            {
              String line = (String)lines.elementAt(j);
              if (line.charAt(0) == ' ')
                outCppData.println("  \"`&" + (line.substring(1)) + "` \"");
              else if (line.charAt(0) != '"')
                outCppData.println("  \"`&" + line + "` \"");
              else
                outCppData.println("  \"`" + (line.substring(1, line.length() - 1)) + "` \"");
            }
            outCppData.println("  ;");
            outCppData.println();
          }
          outHeadData.println();
          outHeadData.println("#endif");
        }
        finally
        {
          outCppData.flush();
          outHeadData.flush();
          outCppData.close();
          outHeadData.close();
        }
      }
      finally
      {
        outCppFile.close();
        outHeadFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate Procs IO Error");
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: "+output + table.useName().toLowerCase() + ".h");
      OutputStream outFile = new FileOutputStream(output +  table.useName().toLowerCase() + ".h");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// ###############################################################");
          outData.println("// # PLEASE BEWARE OF THE GENERATED CODE MODIFIER ATTACK MONSTER #");
          outData.println("// #                        'Vengeance is mine' sayeth The Beast #");
          outData.println("// ###############################################################");
          outData.println("#ifndef _" + table.useName().toLowerCase() + "H");
          outData.println("#define _" + table.useName().toLowerCase() + "H");
          outData.println("#include \"clibinu.h\"");
          outData.println("#include \"padgen.h\"");
          outData.println("");
          generateStructs(table, outData);
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
  private static void generateStructs(Table table, PrintWriter outData)
  {
    if (table.fields.size() > 0)
    {
      if (table.comments.size() > 0)
      {
        outData.println("/**");
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println(" * " + s);
        }
        outData.println(" */");
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
        outData.println("/**");
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String s = (String)proc.comments.elementAt(j);
          outData.println(" * " + s);
        }
        outData.println(" */");
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
    outData.println("struct " + mainName);
    outData.println("{");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("  " + fieldDef(field, i));
      if (isNull(field))
        outData.println("  int16  " + field.useLowerName() + "IsNull;    IDL2_INT16_PAD(" + (i+fields.size()) + ");");
    }
    outData.println("  " + mainName + "()");
    outData.println("  {");
    outData.println("    _clear();");
    outData.println("  }");
    outData.println("  void _clear()");
    outData.println("  {");
    outData.println("    memset(this, 0, sizeof(*this));");
    outData.println("  }");
    generateReader(fields, outData);
    generateWriter(fields, outData);
    outData.println("};");
  }
  private static void generateWriter(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("  void _write(TJWriter &writer)");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      writeCall(field, outData);
    }
    outData.println("  }");
  }
  private static void generateReader(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("  void _read(TJReader &reader)");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      readCall(field, outData);
    }
    outData.println("  }");
  }
  private static void generateStructPairs(Proc proc, String mainName, PrintWriter outData)
  {
    setMaxVarNameLen(proc.outputs, 4);
    setMaxVarNameLen(proc.inputs, maxVarNameLen);
    outData.println("struct " + mainName);
    outData.println("{");
    Vector<Field> fields = new Vector<Field>();
    for (int i=0; i<proc.outputs.size(); i++)
      fields.addElement(proc.outputs.elementAt(i));
    if (fields.size() > 0)
    {
      for (int i = 0; i < fields.size(); i++)
      {
        Field field = (Field)fields.elementAt(i);
        outData.println("  " + fieldDef(field, i));
        if (isNull(field))
          outData.println("  int16  " + field.useLowerName() + "IsNull;    IDL2_INT16_PAD(" + (i + fields.size()) + ");");
      }
    }
    if (proc.hasDiscreteInput())
    {
      Vector<?> inputs = proc.inputs;
      for (int j = 0; j < inputs.size(); j++)
      {
        Field field = (Field)inputs.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        fields.addElement(field);
        outData.println("  " + fieldDef(field, j+proc.outputs.size()*2));
        if (isNull(field))
          outData.println("  int16  " + field.useLowerName() + "IsNull;    IDL2_INT16_PAD(" + (j + fields.size()) + ");");
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
        outData.println("  " + fieldDef(field, j + (proc.inputs.size() + proc.outputs.size()) * 2));
      }
    }
    outData.println("  " + mainName + "()");
    outData.println("  {");
    outData.println("    _clear();");
    outData.println("  }");
    outData.println("  void _clear()");
    outData.println("  {");
    outData.println("    memset(this, 0, sizeof(*this));");
    outData.println("  }");
    generateReader(fields, outData);
    generateWriter(fields, outData);
    outData.println("};");
  }
  private static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("struct " + table.useName() + field.useUpperName() + "Ord");
        outData.println("{");
        String datatype = "int32";
        if (field.type == Field.ANSICHAR && field.length == 1)
          datatype = "char";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)en.value + "'";
          outData.println("  static const " + datatype + " " + en.name + " = " + evalue + ";");
        }
        outData.println("  static const char* toString(" + datatype + " ordinal)");
        outData.println("  {");
        outData.println("    switch (ordinal)");
        outData.println("    {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "\"" + (char)en.value + "\"";
          outData.println("    case " + evalue + ": return \"" + en.name + "\";");
        }
        outData.println("    }");
        outData.println("    return \"unknown ordinal\";");
        outData.println("  }");
        outData.println("};");
      }
      else if (field.valueList.size() > 0)
      {
        outData.println("struct " + table.useName() + field.useUpperName() + "Ord");
        outData.println("{");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String en = (String)field.valueList.elementAt(j);
          outData.println("  static const int " + en + " = " + j + ";");
        }
        outData.println("  static const char* toString(int ordinal)");
        outData.println("  {");
        outData.println("    switch (ordinal)");
        outData.println("    {");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String en = (String)field.valueList.elementAt(j);
          outData.println("    case " + j + ": return \"" + en + "\";");
        }
        outData.println("    }");
        outData.println("    return \"unknown ordinal\";");
        outData.println("  }");
        outData.println("};");
      }
    }
  }
  private static void skip(int used, PrintWriter outData)
  {
    int size = used % 8;
    if (size == 0)
      outData.println();
    else
      outData.println("reader.skip(" + (8 - size) + ");");
  }
  private static void filler(int used, PrintWriter outData)
  {
    int size = used % 8;
    if (size == 0)
      outData.println();
    else
      outData.println("writer.filler(" + (8 - size) + ");");
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
        outData.print("    writer.putChar(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("    writer.putChar(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("    writer.putChar(" + var + ", 21);");
        filler(21, outData);
        break;
      case Field.BLOB:
        outData.print("    writer.putBlob(&" + var + ", " + field.length + ");");
        filler(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("    writer.putInt16(" + var + ");writer.filler(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    writer.putInt16(" + var + ");writer.filler(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("    writer.putChar(" + var + ", " + (field.precision + 3) + ");");
          filler(field.precision + 3, outData);
        }
        else
          outData.println("    writer.putDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("    writer.putInt32((int32)" + var + ");writer.filler(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("    writer.putInt64((int64)" + var + ");");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
    if (isNull(field))
      outData.println("    writer.putInt16(" + var + "IsNull);writer.filler(6);");
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
        outData.print("    reader.getChar(" + var + ", " + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("    reader.getChar(" + var + ", " + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("    reader.getChar(" + var + ", 21);");
        skip(21, outData);
        break;
      case Field.BLOB:
        outData.print("    reader.getBlob(&" + var + ", " + field.length + ");");
        skip(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("    reader.getInt16(" + var + ");reader.skip(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    reader.getInt16(" + var + ");reader.skip(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("    reader.getChar(" + var + ", " + (field.precision + 3) + ");");
          skip(field.precision + 3, outData);
        }
        else
          outData.println("    reader.getDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("    reader.getInt32(" + var + ");reader.skip(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("    reader.getInt64(" + var + ");");
        break;
      default:
        outData.println("  // " + var + " unsupported");
        break;
    }
    if (isNull(field))
      outData.println("    reader.getInt16(" + var + "IsNull);reader.skip(6);");
  }
  private static String charFill(int used, int no)
  {
    int size = used % 8;
    if (size == 0)
      return "";
    else
      return "   IDL2_CHAR_PAD(" + no + ", " + (8 - size) + ");";
  }
  private static String fieldDef(Field field, int no)
  {
    String result;
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.XML:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        result = "char   " + field.useLowerName() + "[" + (field.length + 1) + "];" + charFill(field.length + 1, no);
        break;
      case Field.MONEY:
        result = "char   " + field.useLowerName() + "[21];" + charFill(21, no);
        break;
      case Field.BLOB:
        result = "TJBlob<" + (field.length) + "> " + field.useLowerName() + ";" + charFill(field.length, no);
        break;
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        result = "int16  " + field.useLowerName() + ";    IDL2_INT16_PAD(" + no + ");";
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          result = "char   " + field.useLowerName() + "[" + (field.precision + 3) + "];" + charFill(field.precision + 3, no);
        else
          result = "double " + field.useLowerName() + ";";
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        result = "int32  " + field.useLowerName() + ";    IDL2_INT32_PAD(" + no + ");";
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        result = "int64  " + field.useLowerName() + ";";
        break;
      case Field.DYNAMIC:
        result = "char   " + field.useName() + "[" + (field.length + 1) + "];" + charFill(field.length + 1, no);
      default:
        result = "whoknows";
        break;
    }
    return result;
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
}
