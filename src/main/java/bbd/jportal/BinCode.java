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

public class BinCode extends Generator
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
      generateBinCode(table, output, outLog);
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
  private static void generateBinCode(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName().toLowerCase() + ".code");
      OutputStream outFile = new FileOutputStream(output + table.useName().toLowerCase() + ".code");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData == false) {
			}
          }
          for (int i = 0; i < table.procs.size(); i++)
          {
            Proc proc = (Proc)table.procs.elementAt(i);
            if (proc.isData == true)
              continue;
            HashMap<String, Integer> map = makeHashMap(proc);
            placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
            outData.println("code " + table.name.toUpperCase() + "_" + proc.name.toUpperCase());
            Database database = table.database;
            outData.print("  conn " + database.name);
            if (database.server.length() > 0
            && database.schema.length() > 0)
              outData.print(" " + database.server + " " + database.schema);
            if (database.userid.length() > 0
            && database.password.length() > 0)
              outData.print(" " + database.userid + " " + database.password);
            outData.println();
            outData.print("  proc " + table.name + " " + proc.name);
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
              Integer offset = map.get(field.name);
              int fieldLen = cppLength(field);
              outData.println("  out " + field.name + "(" + field.type + " " + field.length
                + " " + field.precision + " " + field.scale + " " + offset.intValue() + " " + fieldLen + " " + fieldIs(field) + ")");
            }
            for (int j = 0; j < proc.inputs.size(); j++)
            {
              Field field = (Field)proc.inputs.elementAt(j);
              Integer offset = map.get(field.name);
              int fieldLen = cppLength(field);
              outData.println("  inp " + field.name + "(" + field.type + " " + field.length
                + " " + field.precision + " " + field.scale + " " + offset.intValue() + " " + fieldLen + " " + fieldIs(field) + ")");
            }
            for (int j = 0; j < proc.dynamics.size(); j++)
            {
              String s = (String)proc.dynamics.elementAt(j);
              Integer offset = map.get(s);
              Integer n = (Integer)proc.dynamicSizes.elementAt(j);
              int len = n.intValue();
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
            outData.println("endcode");
            outData.println();
          }
        }
        finally
        {
          outData.flush();
          outData.close();
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
