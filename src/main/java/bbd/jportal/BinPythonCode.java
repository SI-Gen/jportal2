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

public class BinPythonCode extends Generator
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
      outLog.println("Code: "+output + "binu_" + table.useName().toLowerCase() + ".py");
      OutputStream outFile = new FileOutputStream(output + "binu_" + table.useName().toLowerCase() + ".py");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("###############################################################");
          outData.println("# PLEASE BEWARE OF THE GENERATED CODE MODIFIER ATTACK MONSTER #");
          outData.println("#                        'Vengeance is mine' sayeth The Beast #");
          outData.println("###############################################################");
          outData.println();
          outData.println("import struct");
          outData.println("import binu");
          outData.println();
          generateStructs(table, outData);
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
      outData.println();
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc = (Proc)table.procs.elementAt(i);
        if (proc.isStd || proc.isStdExtended() || proc.hasNoData())
          generateMethods(table, proc, outData);
      }
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      generateStructSetup(proc, table.useName() + proc.upperFirst(), outData);
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
    outData.println("class " + mainName + "(object):");
    outData.println("    __slots__ = [");
    String comma = ",";
    for (int i = 0; i < fields.size(); i++)
    {
      if (i == fields.size() - 1)
        comma = "]";
      Field field = (Field)fields.elementAt(i);
      outData.println("        '" + field.useLowerName() + "'" + comma);
    }
    comma = ",";
    outData.println("    def __init__(self, ");
    for (int i = 0; i < fields.size(); i++)
    {
      if (i == fields.size() - 1)
        comma = "):";
      Field field = (Field)fields.elementAt(i);
      outData.println("            " + fieldDef(field) + comma);
    }
    outData.println("        self._set(vars())");
    outData.println("    def _set(self, parms):");
    outData.println("        for parm in parms:");
    outData.println("            if parm in self.__slots__:");
    outData.println("                setattr(self, parm, parms[parm])");
    generateReader(fields, outData);
    generateWriter(fields, outData);
    outData.println("    def _display(self):");
    outData.println("        for slot in self.__slots__:");
    outData.println("            print '%s = %s' % (slot, repr(getattr(self, slot)))");
  }
  private static void generateWriter(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("    def _write(self):");
    outData.println("        buffer = ''");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      writeCall(field, outData);
    }
    outData.println("        return buffer");
  }
  private static void generateReader(Vector<Field> fields, PrintWriter outData)
  {
    outData.println("    def _read(self, buffer):");
    outData.println("        _ofs = 0");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      readCall(field, outData);
    }
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
    outData.println();
    generateMethods(proc.table, proc, outData);
  }
  private static void generateEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        outData.println("class " + table.useName() + field.useUpperName() + "Ord:");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)en.value + "'";
          outData.println("    " + en.name + " = " + evalue);
        }
        outData.println("    values = {}");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum en = (Enum)field.enums.elementAt(j);
          String evalue = "" + en.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)en.value + "'";
          outData.println("    values[" + evalue + "] = '" + en.name + "'");
        }
        outData.println();
      }
      else if (field.valueList.size() > 0)
      {
        outData.println("class " + table.useName() + field.useUpperName() + "Ord:");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String en = (String)field.valueList.elementAt(j);
          outData.println("    " + en + " = " + "'" + en + "'");
        }
        outData.println();
      }
    }
  }
  private static int skip(int used)
  {
    int size = used % 8;
    if (size == 0)
      return 0;
    else
      return 8 - size;
  }
  private static String filler(int used)
  {
    int size = used % 8;
    if (size == 0)
      return "";
    else
      return "" + (8 - size) + "x";
  }
  private static void writeCall(Field field, PrintWriter outData)
  {
    String var = field.useLowerName();
    String fill;
    outData.print("        _val = self." + var + ";");
    String set = "_val";
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        fill = filler(field.length + 1);
        if (isNull(field))
          set = "_val if _val != None else ''";
        outData.println("buffer += struct.pack('" + (field.length + 1) + "s" + fill + "', str(" + set + "))");
        break;
      case Field.DATE:
        fill = filler(field.length + 1);
        if (isNull(field))
          set = "_val if _val != None else '19010101'";
        outData.println("buffer += struct.pack('" + (field.length + 1) + "s" + fill + "', str(" + set + "))");
        break;
      case Field.DATETIME:
        fill = filler(field.length + 1);
        if (isNull(field))
          set = "_val if _val != None else '19010101000000'";
        outData.println("buffer += struct.pack('" + (field.length + 1) + "s" + fill + "', str(" + set + "))");
        break;
      case Field.TIME:
        fill = filler(field.length + 1);
        if (isNull(field))
          set = "_val if _val != None else '000000'";
        outData.println("buffer += struct.pack('" + (field.length + 1) + "s" + fill + "', str(" + set + "))");
        break;
      case Field.TIMESTAMP:
        fill = filler(field.length + 1);
        set = "_val if _val != None and len(_val) == 14 else '19010101000000'";
        outData.println("buffer += struct.pack('" + (field.length + 1) + "s" + fill + "', str(" + set + "))");
        break;
      case Field.MONEY:
        fill = filler(21);
        if (isNull(field))
          set = "_val if _val != None else ''";
        outData.println("buffer += struct.pack('21s" + fill + "', str(" + set + "))");
        break;
      case Field.BLOB:
        fill = filler(field.length);
        if (isNull(field))
          set = "_val if _val != None else (0,'')";
        outData.println("buffer += struct.pack('!l" + (field.length - 4) + "s" + fill + "', " + set + "[0], " + set + "[1])");
        break;
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        if (isNull(field))
          set = "_val if _val != None else 0";
        outData.println("buffer += struct.pack('!h6x', int(" + set + "))");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          if (isNull(field))
            set = "_val if _val != None else ''";
          fill = filler(field.precision + 3);
          outData.println("buffer += struct.pack('" + (field.precision + 3) + "s" + fill + "', str(" + set + "))");
        }
        else
        {
          if (isNull(field))
            set = "_val if _val != None else 0.0";
          outData.println("buffer += struct.pack('!d', float(" + set + "))");
        }
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        if (isNull(field))
          set = "_val if _val != None else 0";
        outData.println("buffer += struct.pack('!l4x', int(" + set + "))");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        if (isNull(field))
          set = "_val if _val != None else 0L";
        outData.println("buffer += struct.pack('!q', long(" + set + "))");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
    if (isNull(field))
    {
      outData.print("        _nul = -1 if _val == None else 0; ");
      outData.println("buffer += struct.pack('!h6x', _nul)");
    }
  }
  private static void readCall(Field field, PrintWriter outData)
  {
    String var = "self." + field.useLowerName();
    int sk = 0;
    outData.print("        " + var + " = ");
    int len;
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DYNAMIC:
        len = field.length + 1;
        sk = skip(len);
        outData.println("struct.unpack_from('" + len + "s" + "', buffer, _ofs)[0].rstrip('\\x00');_ofs += " + (len + sk));
        break;
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        len = field.length + 1;
        sk = skip(len);
        outData.println("struct.unpack_from('" + len + "s" + "', buffer, _ofs)[0].rstrip('\\x00');_ofs += " + (len + sk));
        break;
      case Field.MONEY:
        len = 21;
        sk = skip(len);
        outData.println("struct.unpack_from('" + len + "s" + "', buffer, _ofs)[0].rstrip('\\x00');_ofs += " + (len + sk));
        break;
      case Field.BLOB:
        len = field.length;
        sk = skip(len);
        outData.println("struct.unpack_from('!l" + (len-4) + "s" + "', buffer, _ofs)[0:2];_ofs += " + (len + sk));
        break;
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        len = 2;
        sk = skip(len);
        outData.println("struct.unpack_from('!h', buffer, _ofs)[0];_ofs += " + (len + sk));
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          len = field.precision + 3;
          sk = skip(len);
          outData.println("struct.unpack_from('" + len + "s" + "', buffer, _ofs)[0].rstrip('\\x00');_ofs += " + (len + sk));
        }
        else
        {
          len = 8;
          outData.println("struct.unpack_from('!d', buffer, _ofs)[0];_ofs += " + len);
        }
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        len = 4;
        sk = skip(len);
        outData.println("struct.unpack_from('!l', buffer, _ofs)[0];_ofs += " + (len + sk));
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        len = 8;
        outData.println("struct.unpack_from('!q', buffer, _ofs)[0];_ofs += " + len);
        break;
      default:
        outData.println("// " + var + " unsupported");
        break;
    }
    if (isNull(field))
    {
      len = 2;
      sk = skip(len);
      outData.println("        _nul = struct.unpack_from('!h', buffer, _ofs)[0];_ofs += " + (len + sk));
      outData.println("        if _nul != 0: " + var + " = None");
    }
  }
  private static String fieldDef(Field field)
  {
    if (isNull(field) == true)
      return field.useLowerName() + "=None";
    switch (field.type)
    {
      case Field.ANSICHAR:
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.XML:
        return field.useLowerName() + "=''";
      case Field.MONEY:
        return field.useLowerName() + "='0.0'";
      case Field.BLOB:
        return field.useLowerName() + "=(0,'')";
      case Field.DATE:
        return field.useLowerName() + "='19010101'";
      case Field.DATETIME:
        return field.useLowerName() + "='19010101000000'";
      case Field.TIME:
        return field.useLowerName() + "='000000'";
      case Field.TIMESTAMP:
        return field.useLowerName() + "='19010101000000'";
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return field.useLowerName() + "='0.0'";
        else
          return field.useLowerName() + "=0.0";
      case Field.BOOLEAN:
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        return field.useLowerName() + "=0";
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        return field.useLowerName() + "=0L";
      case Field.DYNAMIC:
        return field.useLowerName() + "''";
      default:
        return "eh";
    }
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
  private static void generateMethods(Table table, Proc proc, PrintWriter outData)
  {
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    String dataStruct;
    if (proc.isStd || proc.isStdExtended() || proc.hasNoData())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    outData.print("def " + table.useName() + "_" + proc.upperFirst() + "(");
    if (proc.isMultipleInput)
      outData.print("recs");
    else if (hasInput)
    {
      String comma = ",";
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        outData.println();
        if (i == proc.inputs.size() - 1 && proc.dynamics.size() == 0)
          comma = "";
        Field field = (Field)proc.inputs.elementAt(i);
        outData.print("        " + fieldDef(field) + comma);
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        outData.println();
        if (i == proc.dynamics.size() - 1)
          comma = "";
        String s = (String)proc.dynamics.elementAt(i);
        outData.print("        " + s + "=''" + comma);
      }
    }
    outData.println("):");
    outData.println("    rec = " + dataStruct + "()");
    if (proc.isMultipleInput)
      outData.println("    rec." + proc.upperFirst() + "(recs)");
    else if (proc.isInsert && proc.hasReturning)
    {
      outData.println("    rec._set(vars())");
      outData.println("    rc, new_rec = rec." + proc.upperFirst() + "()");
      outData.println("    return rc, new_rec");
    }
    else if (proc.outputs.size() > 0)
    {
      if (proc.isSingle)
      {
        if (hasInput == true)
          outData.println("    rec._set(vars())");
        outData.println("    rc, new_rec = rec." + proc.upperFirst() + "()");
        outData.println("    return rc, new_rec");
      }
      else
      {
        if (hasInput == true)
          outData.println("    rec._set(vars())");
        outData.println("    no_recs, recs = rec." + proc.upperFirst() + "()");
        outData.println("    return no_recs, recs");
      }
    }
    else
    {
      if (hasInput == true)
        outData.println("    rec._set(vars())");
      outData.println("    rec." + proc.upperFirst() + "()");
    }
    outData.println();
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
      int offset = recLength = 0;
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
  private static void generateBinCode(Table table, Proc proc, Vector<Field> fields, PrintWriter outData)
  {
    boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
    String dataStruct;
    if (proc.isStd || proc.isStdExtended())
      dataStruct = table.useName();
    else
      dataStruct = table.useName() + proc.upperFirst();
    outData.print("    def " + proc.upperFirst() + "(self");
    if (proc.isMultipleInput)
      outData.print(", recs");
    outData.println("):");
    placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
    HashMap<?, ?> map = makeHashMap(proc);
    outData.println("        query = '''\\");
    Database database = table.database;
    outData.print("conn " + database.name);
    if (database.server.length() > 0
    && database.schema.length() > 0)
      outData.print(" " + database.server + " " + database.schema);
    if (database.userid.length() > 0
    && database.password.length() > 0)
      outData.print(" " + database.userid + " " + database.password);
    outData.println();
    outData.print("proc " + table.name + " " + proc.name);
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
    outData.println("])");
    for (int j = 0; j < proc.outputs.size(); j++)
    {
      Field field = (Field)proc.outputs.elementAt(j);
      Integer offset = (Integer)map.get(field.name);
      outData.println("out " + field.name + "(" + field.type + " " + field.length
        + " " + field.precision + " " + field.scale + " " + offset.intValue() + " "
        + cppLength(field) + " " + fieldIs(field) + ")");
    }
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field)proc.inputs.elementAt(j);
      Integer offset = (Integer)map.get(field.name);
      outData.println("inp " + field.name + "(" + field.type + " " + field.length
        + " " + field.precision + " " + field.scale + " " + offset.intValue() + " "
        + cppLength(field) + " " + fieldIs(field) + ")");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String)proc.dynamics.elementAt(j);
      Integer n = (Integer)proc.dynamicSizes.elementAt(j);
      Integer offset = (Integer)map.get(s);
      int len = n.intValue();
      outData.println("dyn " + s + "(" + len + " " + offset.intValue() + ")");
    }
    if (placeHolder.pairs.size() > 0)
    {
      outData.print("binds");
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
        outData.println("`&" + (line.substring(1)) + "`");
      else if (line.charAt(0) != '"')
        outData.println("`&" + line + "`");
      else
        outData.println("`" + (line.substring(1, line.length() - 1)) + "`");
    }
    outData.println("'''");
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
  private static void generateBinUsageAction(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    if (hasInput)
    {
      outData.println("        buffer = self._write()");
      outData.println("        binu.Action(len(query), query, len(buffer), buffer)");
    }
    else
      outData.println("        binu.ActionOnly(len(query), query)");
  }
  private static void generateBinUsageBulkAction(Table table, Proc proc, String dataStruct, PrintWriter outData)
  {
    outData.println("        buffer = ''");
    outData.println("        for rec in recs:");
    outData.println("            block = rec._write()");
    outData.println("            buffer += block");
    outData.println("        binu.BulkAction(len(query), query, len(recs), len(buffer), buffer);");
  }
  private static void generateBinUsageSingle(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    if (hasInput == true)
    {
      outData.println("        buffer = self._write()");
      outData.println("        rc, size, outbuffer = binu.Single(len(query), query, len(buffer), buffer, 0, '')");
    }
    else
      outData.println("        rc, size, outbuffer = binu.SingleOnly(len(query), query, 0, '')");
    outData.println("        if rc != 0:");
    outData.println("          self._read(outbuffer)");
    outData.println("        return rc, self");
  }
  private static void generateBinUsageMultiple(Table table, Proc proc, String dataStruct, boolean hasInput, PrintWriter outData)
  {
    if (hasInput == true)
    {
      outData.println("        buffer = self._write()");
      outData.println("        no_recs, out_buffer_size, out_buffer = binu.Multiple(len(query), query, len(buffer), buffer, 0, 0, '')");
    }
    else
      outData.println("        no_recs, out_buffer_size, out_buffer = binu.MultipleOnly(len(query), query, 0, 0, '')");
    outData.println("        recs = []");
    outData.println("        if no_recs > 0 and out_buffer_size % no_recs == 0:");
    outData.println("            rec_size = out_buffer_size / no_recs");
    outData.println("            k = 0");
    outData.println("            while k < out_buffer_size:");
    outData.println("                j = k");
    outData.println("                k += rec_size");
    outData.println("                rec_buffer = out_buffer[j:k]");
    outData.println("                rec = " + dataStruct + "()");
    outData.println("                rec._read(rec_buffer)");
    outData.println("                recs.append(rec)");
    outData.println("        return no_recs, recs");
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
