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
import java.io.File;

public class BinJavaCode extends Generator
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
        outLog.println(args[i] + ": Generate Code for Java Bin Access");
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
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  private static String[] seperate(String value, String delim)
  {
    int length = value.length();
    if (delim.length() != 1)
      return new String[0];
    int n = 0, p = 0;
    char ch = delim.charAt(0);
    for (; ; )
    {
      n++;
      int q = value.indexOf(delim, p);
      if (q < 0)
        break;
      p = q + 1;
      while (p < length && value.charAt(p) == ch)
        p++;
    }
    String[] result = new String[n];
    p = 0;
    for (int i = 0; i < n; i++)
    {
      int q = value.indexOf(delim, p);
      if (q < 0)
        result[i] = value.substring(p);
      else
        result[i] = value.substring(p, q);
      p = q + 1;
      while (p < length && value.charAt(p) == ch)
        p++;
    }
    return result;
  }
  private static String outputDir(Database database, String output)
  {
    StringBuffer result = new StringBuffer();
    String delim = "/";
    if (output.indexOf("\\") != -1)
      delim = "\\";
    String[] mp = seperate(database.packageName, "");
    String[] op = seperate(output, delim);
    int n = op.length - 1;
    int b = 1;
    if (op[0].length() > 1 && op[0].charAt(1) == ':')
      b++;
    for (int oi = op.length - 1; oi >= b; oi--)
    {
      if (mp[0].compareTo(op[oi]) == 0)
      {
        n = oi;
        break;
      }
    }
    for (int oi = 0; oi < n; oi++)
    {
      result.append(op[oi]);
      result.append(delim);
    }
    for (int mi = 0; mi < mp.length; mi++)
    {
      result.append(mp[mi]);
      result.append(delim);
    }
    File f = new File(result.toString());
    f.mkdirs();
    return result.toString();
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    String expanded = outputDir(database, output);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table)database.tables.elementAt(i);
      generateStructs(table, expanded, outLog);
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName() + ".java");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".java");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// ###############################################################");
          outData.println("// # PLEASE BEWARE OF THE GENERATED CODE MODIFIER ATTACK MONSTER #");
          outData.println("// #                        'Vengeance is mine' sayeth The Beast #");
          outData.println("// ###############################################################");
          outData.println("package " + table.database.packageName + ";");
          outData.println("");
          outData.println("import bbd.clibinu.*;");
          outData.println("");
          outData.println("public class " + table.useName());
          outData.println("{");
          generateStructs(table, outData);
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
      outData.println("//  }");
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      generateStructSetup(proc, table.useName() + proc.upperFirst(), outData);
      outData.println("//  }");
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
    outData.println("  public static class " + mainName + "Rec");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("    " + fieldDef(field));
    }
    generateReader(fields, outData);
    generateWriter(fields, outData);
  }
  private static void generateWriter(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("    public void write(TJWriter writer)");
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
    outData.println("    public void read(TJReader reader)");
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
    for (int i = 0; i < proc.outputs.size(); i++)
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
      String fieldName = field.useUpperName();
      if (field.enums.size() > 0)
      {
        outData.println("  public static enum " + fieldName);
        outData.println("  {");
        String datatype = "int";
        if (field.type == Field.ANSICHAR && field.length == 1)
          datatype = "char";
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)en.value + "'";
          if (j > 0) 
            outData.println(",");
          outData.print("    " + en.name.toUpperCase() + "(" + evalue + " ,\"" + en.name + "\")");
        }
        outData.println(";");
        outData.println("    public " + datatype + " key;");
        outData.println("    public String value;");
        outData.println("    " + fieldName + "(" + datatype + " key, String value)");
        outData.println("    {");
        outData.println("      this.key = key;");
        outData.println("      this.value = value;");
        outData.println("    }");
        outData.println("    public static " + fieldName + " get(int key)");
        outData.println("    {");
        outData.println("      for (" + fieldName + " op : values())");
        outData.println("        if (op.key == key) return op;");
        outData.println("      return null;");
        outData.println("    }");
        outData.println("    public String toString()");
        outData.println("    {");
        outData.println("      return value;");
        outData.println("    }");
        outData.println("  }");
      }
      else if (field.valueList.size() > 0)
      {
        outData.println("  public static enum " + fieldName);
        outData.println("  {");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String en = (String)field.valueList.elementAt(j);
          String evalue = "" + j;
          if (j > 0)
            outData.println(",");
          outData.print("    " + en.toUpperCase() + "(" + evalue + " ,\"" + en + "\")");
        }
        outData.println(";");
        outData.println("    public int key;");
        outData.println("    public String value;");
        outData.println("    " + fieldName + "(int key, String value)");
        outData.println("    {");
        outData.println("      this.key = key;");
        outData.println("      this.value = value;");
        outData.println("    }");
        outData.println("    public static " + fieldName + " get(int key)");
        outData.println("    {");
        outData.println("      for (" + fieldName + " op : values())");
        outData.println("        if (op.key == key) return op;");
        outData.println("      return null;");
        outData.println("    }");
        outData.println("    public String toString()");
        outData.println("    {");
        outData.println("      return value;");
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
        outData.print("      writer.putString(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("      writer.putDateTime(" + var + ", " + (field.length + 1) + ");");
        filler(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("      writer.putDecimal(" + var + ", 21);");
        filler(21, outData);
        break;
      case Field.BLOB:
        outData.print("      writer.putJPBlob(" + var + ", " + field.length + ");");
        filler(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("      writer.putInt16(" + var + ");writer.filler(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("      writer.putInt16(" + var + ");writer.filler(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("      writer.putDecimal(" + var + ", " + (field.precision + 3) + ");");
          filler(field.precision + 3, outData);
        }
        else
          outData.println("      writer.putDouble(" + var + ");");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      writer.putInt32(" + var + ");writer.filler(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      writer.putInt64(" + var + ");");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
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
        outData.print("      " + var + " = reader.getString(" + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        outData.print("      " + var + " = reader.getDateTime(" + (field.length + 1) + ");");
        skip(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("      " + var + " = reader.getDecimal(" + (21) + ");");
        skip(21, outData);
        break;
      case Field.BLOB:
        outData.print("      " + var + " = reader.getJPBlob(" + field.length + ");");
        skip(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("      " + var + " = reader.getInt16();reader.skip(6);");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("      " + var + " = reader.getInt16();reader.skip(6);");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("      " + var + " = reader.getDecimal(" + (field.precision + 3) + ");");
          skip(field.precision + 3, outData);
        }
        else
          outData.println("      " + var + " = reader.getDouble();");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("      " + var + " = reader.getInt32();reader.skip(4);");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("      " + var + " = reader.getInt64();");
        break;
      default:
        outData.println("    // " + var + " unsupported");
        break;
    }
  }
  private static String fieldDef(Field field)
  {
    String result = "";
    String setter = "";
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.XML:
        result = "TJString";
        setter = "new TJString(" + field.length + (field.isNull ? ", true" : "") + ")";
        break;
      case Field.MONEY:
        result = "TJMoney";
        setter = "new TJMoney(18, 2" + (field.isNull ? ", true" : "") + ")";
        break;
      case Field.BLOB:
        result = "TJBlob";
        setter = "new TJBlob(" + field.length + ")";
        break;
      case Field.DATE:
        result = "TJDate";
        setter = "new TJDate(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.DATETIME:
        result = "TJDateTime";
        setter = "new TJDateTime(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.TIME:
        result = "TJTime";
        setter = "new TJTime(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.TIMESTAMP:
        result = "TJTimeStamp";
        setter = "new TJTimeStamp(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.BOOLEAN:
        result = "TJBoolean";
        setter = "new TJBoolean(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        result = "TJShort";
        setter = "new TJShort(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          result = "TJMoney";
          setter = "new TJMoney(" + field.precision + ", " + field.scale + (field.isNull ? ", true" : "") + ")";
        }
        else
        {
          result = "TJDouble";
          setter = "new TJDouble(" + (field.isNull ? "true" : "") + ")";
        }
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        result = "TJInt";
        setter = "new TJInt(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        result = "TJLong";
        setter = "new TJLong(" + (field.isNull ? "true" : "") + ")";
        break;
      case Field.DYNAMIC:
        return "public String " + field.useName() + " = \"\";";
      default:
        result = "whoknows";
        break;
    }
    return
        padder("public " + result + " " + field.useLowerName() + " = " + setter + ";", maxVarNameLen + 22);
  }
  private static boolean isNull(Field field)
  {
    if (field.isNull == false)
      return false;
    switch (field.type)
    {
      case Field.FLOAT:
      case Field.DOUBLE:
        if (field.precision > 15)
          return false;
        return true;
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.SHORT:
      case Field.INT:
      case Field.LONG:
      case Field.IDENTITY:
      case Field.SEQUENCE:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return true;
    }
    return false;
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
  private static void generateBinUsageAction(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    outData.print("    //public static void " + proc.lowerFirst() + "(");
    if (hasInput)
      outData.print(dataStruct + " rec");
    outData.println(")");
    outData.println("    //{");
    outData.print("      //action(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN");
    if (hasInput)
      outData.print(", rec");
    outData.println(");");
    outData.println("    //}");
  }
  private static void generateBinUsageBulkAction(Table table, Proc proc, String dataStruct, PrintWriter outData)
  {
    outData.println("    //public static void " + proc.lowerFirst() + "("+ dataStruct + "[] recs)");
    outData.println("    //{");
    outData.println("      //bulkAction(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, recs);");
    outData.println("    //}");
  }
  private static void generateBinUsageSingle(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    if (hasInput == true)
      outData.println("    //public static bool " + proc.lowerFirst() + "(" + dataStruct + " rec)");
    else
      outData.println("    //public static bool " + proc.lowerFirst() + "(" + dataStruct + " rec)");
    outData.println("    //{");
    if (hasInput == true)
      outData.println("      //return readOne(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, rec);");
    else
      outData.println("      //return readOneOnly(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, rec);");
    outData.println("    //}");
  }
  private static void generateBinUsageMultiple(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    outData.println("    //public static void " + proc.lowerFirst() + "(" + (hasInput ? dataStruct + " rec, " : "") + dataStruct + "[] recs)");
    outData.println("    //{");
    if (hasInput == true)
      outData.println("    //multiple(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, rec, recs);");
    else
      outData.println("    //multiple(" + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN, recs);");
    outData.println("    //}");
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
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    HashMap<?, ?> map = makeHashMap(proc);
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    outData.println("    public static final String " + table.name.toUpperCase() + "_" + proc.name.toUpperCase() + "_BIN =");
    Database database = table.database;
    outData.print("    \"conn " + database.name);
    if (database.server.length() > 0
    && database.schema.length() > 0)
      outData.print(" " + database.server + " " + database.schema);
    if (database.userid.length() > 0
    && database.password.length() > 0)
      outData.print(" " + database.userid + " " + database.password);
    outData.println(" \"+");
    outData.print("    \"proc " + table.name + " " + proc.name);
    String tween = "";
    Vector<?> lines = placeHolder.getLines();
    outData.print("(" + noRows(proc, recLength) + " ");
    outData.print(proc.lines.size() + " ");
    outData.print(placeHolder.pairs.size() + " ");
    outData.print(proc.outputs.size() + " ");
    outData.print(proc.inputs.size() + " ");
    outData.print(proc.dynamics.size() + " ");
    outData.print(recLength + " ");
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
    outData.println("]) \"+");
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field)proc.outputs.elementAt(j);
      Integer offset = (Integer)map.get(field.name);
      int fieldLen = cppLength(field);
      outData.println("    \"out " + field.name + "(" + field.type + " " + field.length
        + " " + field.precision + " " + field.scale + " " + offset.intValue() + " " + fieldLen + " " + fieldIs(field) + ") \"+");
    }
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      Integer offset = (Integer)map.get(field.name);
      int fieldLen = cppLength(field);
      outData.println("    \"inp " + field.name + "(" + field.type + " " + field.length
        + " " + field.precision + " " + field.scale + " " + offset.intValue() + " " + fieldLen + " " + fieldIs(field) + ") \"+");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      Integer offset = (Integer)map.get(s);
      Integer n = (Integer)proc.dynamicSizes.elementAt(j);
      int len = n.intValue();
      outData.println("    \"dyn " + s + "(" + len + " " + offset.intValue() + ") \"+");
    }
    if (placeHolder.pairs.size() > 0)
    {
      outData.print("    \"binds");
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
      outData.println(") \"+");
    }
    for (int j = 0; j < lines.size(); j++)
    {
      String line = (String)lines.elementAt(j);
      if (line.charAt(0) == ' ')
        outData.println("    \"`&" + (line.substring(1)) + "` \"+");
      else if (line.charAt(0) != '"')
        outData.println("    \"`&" + line + "` \"+");
      else
        outData.println("    \"`" + (line.substring(1, line.length() - 1)) + "` \"+");
    }
    outData.println("    \"\";");
    if (proc.isMultipleInput)
      generateBinUsageBulkAction(table, proc, dataStruct, outData);
    else if (proc.isInsert && proc.hasReturning)
      generateBinUsageAction(table, proc, dataStruct, hasInput, outData);
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
