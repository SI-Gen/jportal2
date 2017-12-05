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

public class JSONCode extends Generator
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
        outLog.println(args[i]+": Generate IDL Code for JSON Access");
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
    return "Generate IDL Code for JSON Access";
  }
  public static String documentation()
  {
    return "Generate IDL Code for JSON Access";
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
      generateJSONCode(table, output, outLog);
    }
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
  private static void generateJSONCode(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName().toLowerCase() + ".json");
      OutputStream outFile = new FileOutputStream(output + table.useName().toLowerCase() + ".json");
      String list0 = "[";
      String list1 = "[";
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          Database database = table.database;
          outData.printf("{ \"table\": \"%s\"\n", table.name);
          outData.printf(", \"database\": \"%s\"\n", database.name);
          outData.printf(", \"procs\":\n");
          for (Proc proc : table.procs)
          {
            if (proc.isData == true)
              continue;
            HashMap<String, Integer> map = makeHashMap(proc);
            placeHolder = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
            outData.printf("  %s\n    { \"name\" : \"%s_%s\"\n"
              , list0  
              , table.name.toUpperCase() 
              , proc.name.toUpperCase()
              );
            list0 = ",";
            outData.printf("    , \"conn\": \"%s\"\n" , database.name);
            outData.printf("    , \"server\": \"%s\" ,\"schema\": \"%s\"\n" 
              , database.server.length() > 0 ? database.server : "" 
              , database.schema.length() > 0 ? database.schema : ""
              );
            outData.printf("    , \"userid\": \"%s\" ,\"password\": \"%s\"\n" 
              , database.userid.length() > 0 ? database.userid : "" 
              , database.password
              );
            outData.printf("    , \"tableName\": \"%s\"\n    , \"procName\": \"%s\""
              , table.name 
              , proc.name
              );
            Vector<String> lines = placeHolder.getLines();
            noRows(proc, recLength);
            outData.printf("    , \"noLines\": \"%d\"\n", proc.lines.size());
            outData.printf("    , \"noPairs\": \"%d\"\n", placeHolder.pairs.size());
            outData.printf("    , \"noOutputs\": \"%d\"\n", proc.outputs.size());
            outData.printf("    , \"noInputs\": \"%d\"\n", proc.inputs.size());
            outData.printf("    , \"noDynamics\": \"%d\"\n", proc.dynamics.size());
            outData.printf("    , \"recLength\": \"%d\"\n", recLength);
            outData.printf("    , \"isProc\": \"%d\"\n", proc.isProc ? 1 : 0);
            outData.printf("    , \"isSProc\": \"%d\"\n", proc.isSProc ? 1 : 0);
            outData.printf("    , \"isData\": \"%d\"\n", proc.isData ? 1 : 0);
            outData.printf("    , \"isIdlCode\": \"%d\"\n", proc.isIdlCode ? 1 : 0);
            outData.printf("    , \"isSql\": \"%d\"\n", proc.isSql ? 1 : 0);
            outData.printf("    , \"isSingle\": \"%d\"\n", proc.isSingle ? 1 : 0);
            outData.printf("    , \"isAction\": \"%d\"\n", proc.isAction ? 1 : 0);
            outData.printf("    , \"isStd\": \"%d\"\n", proc.isStd ? 1 : 0);
            outData.printf("    , \"useStd\": \"%d\"\n", proc.useStd ? 1 : 0);
            outData.printf("    , \"extendsStd\": \"%d\"\n", proc.extendsStd ? 1 : 0);
            outData.printf("    , \"useKey\": \"%d\"\n", proc.useKey ? 1 : 0);
            outData.printf("    , \"hasImage\": \"%d\"\n", proc.hasImage ? 1 : 0);
            outData.printf("    , \"isMultipleInput\": \"%d\"\n", proc.isMultipleInput ? 1 : 0);
            outData.printf("    , \"isInsert\": \"%d\"\n", proc.isInsert ? 1 : 0);
            outData.printf("    , \"hasReturning\": \"%d\"\n", proc.hasReturning ? 1 : 0);
            if (proc.outputs.size() > 0)
            {
              outData.printf("    , \"outputs\":\n");
              list1 = "[";
              for (Field field : proc.outputs)
              {
                generateField(field, list1, map, outData);
                list1 = ",";
              }
              outData.printf("      ]\n");
            }
            if (proc.inputs.size() > 0)
            {
              outData.printf("    ,  \"inputs\":\n");
              list1 = "[";
              for (Field field : proc.inputs)
              {
                generateField(field, list1, map, outData);
                list1 = ",";
              }
              outData.printf("      ]\n");
            }
            if (proc.dynamics.size() > 0)
            {
              outData.printf("    , \"dynamics\":\n");
              list1 = "[";
              for (int i = 0; i < proc.dynamics.size(); i++)
              {
                String s = proc.dynamics.elementAt(i);
                Integer offset = map.get(s);
                Integer n = proc.dynamicSizes.elementAt(i);
                int len = n.intValue();
                outData.printf("      %s {\"name\": \"%s\", \"length\": \"%d\", \"offset\": \"%d\"}", list1, s, len, offset.intValue());
              }
              outData.printf("      ]\n");
            }
            if (placeHolder.pairs.size() > 0)
            {
              outData.printf("    , \"binds\": ");
              list1 = "[";
              for (int j = 0; j < placeHolder.pairs.size(); j++)
              {
                PlaceHolderPairs pair = (PlaceHolderPairs)placeHolder.pairs.elementAt(j);
                Field field = pair.field;
                for (int k = 0; k < proc.inputs.size(); k++)
                {
                  Field input = (Field)proc.inputs.elementAt(k);
                  if (input.useName().compareTo(field.useName()) == 0)
                  {
                    outData.printf("%s \"%d\"", list1, k);
                    list1 = ",";
                    break;
                  }
                }
              }
              outData.println("]");
            }
            if (lines.size() > 0)
            {
              outData.printf("    , \"lines\":\n");
              list1 = "[";
              for (String line : lines)
              {
                if (line.charAt(0) == ' ')
                  outData.printf("      %s \"%s\"\n", list1, (line.substring(1)));
                else if (line.charAt(0) != '"')
                  outData.printf("      %s \"%s\"\n", list1, line);
                else
                  outData.printf("      %s \"%s\"\n", list1, line.substring(1, line.length() - 1));
                list1 = ",";
              }
              outData.println("      ]");
            }
            outData.println("    }");
          }
          outData.println("  ]");
        }
        finally
        {
          outData.flush();
          outData.close();
        }
        outData.println("}");
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
  private static void generateField(Field field, String list1, HashMap<String, Integer> map, PrintWriter outData)
  {
    Integer offset = map.get(field.name);
    int fieldLen = cppLength(field);
    //outData.printf("      %s\n", list1);
    outData.printf("      %s { \"name\": \"%s\"\n", list1, field.name);
    outData.printf("        , \"type\": \"%d\"\n", field.type);
    outData.printf("        , \"length\": \"%d\"\n", field.length);
    outData.printf("        , \"precision\": \"%d\"\n", field.precision); 
    outData.printf("        , \"scale\": \"%d\"\n", field.scale); 
    outData.printf("        , \"offset\": \"%d\"\n", offset.intValue()); 
    outData.printf("        , \"fieldLen\": \"%d\"\n", fieldLen); 
    outData.printf("        , \"isSequence\": \"%d\"\n", field.isSequence ? 1 : 0);
    outData.printf("        , \"isNull\": \"%d\"\n", field.isNull ? 1 : 0);
    outData.printf("        , \"isIn\": \"%d\"\n", field.isIn ? 1 : 0);
    outData.printf("        , \"isOut\": \"%d\"\n", field.isOut ? 1 : 0);
    outData.printf("        }\n");
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
