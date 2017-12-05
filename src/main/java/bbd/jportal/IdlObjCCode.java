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
import java.lang.*;
import java.util.Vector;

public class IdlObjCCode extends Generator
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
        outLog.println(args[i] + ": Generate objective C Code for 3 Tier Access");
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
    return "Generate IDL objective C Code for 3 Tier Access";
  }
  public static String documentation()
  {
    return "Generate IDL objective C Code for 3 Tier Access";
  }
  /**
  * Generates the procedure classes for each table present.
  */
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table)database.tables.elementAt(i);
      generateStructs(table, output, outLog);
    }
  }
  private static void generateStructs(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName() + ".h");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".h");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("// 1: Mutilation, Spindlization and Bending will result in goto 1");
          outData.println();
          outData.println("#import \"cracklerdc.h\"");
          outData.println();
          generateInterfaceStructs(table, outData);
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
      outLog.println("Code: " + output + table.useName() + ".m");
      outFile = new FileOutputStream(output + table.useName() + ".m");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println("// 1: Mutilation, Spindlization and Bending will result in goto 1");
          outData.println();
          outData.println("#import \"" + table.useName() + ".h\"");
          outData.println();
          generateImplementationStructs(table, outData);
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
  private static void generateInterfaceStructs(Table table, PrintWriter outData)
  {
    if (table.fields.size() > 0)
    {
      if (table.comments.size() > 0)
      {
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println("// " + s);
        }
      }
      generateInterfaceTableStructs(table.fields, table.useName(), outData);
      generateEnumOrdinals(table, outData);
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      if (proc.comments.size() > 0)
      {
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String s = (String)proc.comments.elementAt(j);
          outData.println("// " + s);
        }
      }
      generateInterfaceStructPairs(proc, table.useName() + proc.upperFirst(), outData);
    }
  }
  private static String useLowerName(Field field)
  {
    String name = field.useLowerName();
    if (name.indexOf("new") == 0)
      return "nu" + name.substring(3);
    return name;
  }
  private static void generateInterfaceTableStructs(Vector<?> fields, String mainName, PrintWriter outData)
  {
    outData.println("@interface D" + mainName + "Rec:NSObject");
    outData.println("{");
    outData.println(" @protected");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      outData.println("  " + fieldDef(field));
      if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
        outData.println("  BOOL " + useLowerName(field) + "IsNull;");
    }
    outData.println("}");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      if (field.type == Field.BLOB)
      {
        outData.println("@property (readwrite) int32 " + useLowerName(field) + "_len;");
        outData.println("@property (retain) NSMutableData* " + useLowerName(field) + "_data;");
      }
      else
        outData.println("@property (" + fieldProp(field) + ") " + fieldDef(field));
      if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
        outData.println("@property (readwrite) BOOL " + useLowerName(field) + "IsNull;");
    }
    outData.println("- (id)init;");
    outData.println("- (void)clear;");
    outData.println("- (void)write:(Writer*)writer;");
    outData.println("- (void)read:(Reader*)reader;");
    outData.println("- (void)dealloc;");
    outData.println("@end");
    outData.println("#define O" + mainName + "Rec D" + mainName + "Rec");
    outData.println();
  }
  private static void generateInterfaceStructPairs(Proc proc, String mainName, PrintWriter outData)
  {
    if (proc.outputs.size() > 0)
    {
      outData.println("@interface O" + mainName + "Rec:NSObject");
      outData.println("{");
      outData.println(" @protected");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        outData.println("  " + fieldDef(field));
        if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
          outData.println("  BOOL " + useLowerName(field) + "IsNull;");
      }
      outData.println("}");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        if (field.type == Field.BLOB)
        {
          outData.println("@property (readwrite) int32 " + useLowerName(field) + "_len;");
          outData.println("@property (retain) NSMutableData* " + useLowerName(field) + "_data;");
        }
        else
          outData.println("@property (" + fieldProp(field) + ") " + fieldDef(field));
        if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
          outData.println("@property (readwrite) BOOL " + useLowerName(field) + "IsNull;");
      }
      outData.println("- (id)init;");
      outData.println("- (void)clear;");
      outData.println("- (void)write:(Writer*)writer;");
      outData.println("- (void)read:(Reader*)reader;");
      outData.println("- (void)dealloc;");
      outData.println("@end");
      if (proc.inputs.size() + proc.dynamics.size() == 0)
        outData.println("#define D" + mainName + "Rec O" + mainName + "Rec");
      outData.println();
    }
    if (proc.inputs.size() + proc.dynamics.size() > 0)
    {
      if (proc.outputs.size() > 0)
        outData.println("@interface D" + mainName + "Rec:O" + mainName + "Rec");
      else
        outData.println("@interface D" + mainName + "Rec:NSObject");
      outData.println("{");
      outData.println(" @protected");
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field)proc.inputs.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        outData.println("  " + fieldDef(field));
        if (isNull(field) == true)
          outData.println("  BOOL " + useLowerName(field) + "IsNull;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        Integer n = (Integer)proc.dynamicSizes.elementAt(j);
        Field field = new Field();
        field.name = s;
        field.type = Field.CHAR;
        field.length = n.intValue();
        outData.println("  " + fieldDef(field));
      }
      outData.println("}");
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field)proc.inputs.elementAt(j);
        if (proc.hasOutput(field.name))
          continue;
        if (field.type == Field.BLOB)
        {
          outData.println("@property (readwrite) int32 " + useLowerName(field) + "_len;");
          outData.println("@property (retain) NSMutableData* " + useLowerName(field) + "_data;");
        }
        else
          outData.println("@property (" + fieldProp(field) + ") " + fieldDef(field));
        if (isNull(field) == true)
          outData.println("@property (readwrite) BOOL " + useLowerName(field) + "IsNull;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String)proc.dynamics.elementAt(j);
        Field field = new Field();
        field.name = s;
        outData.println("@property (retain) NSString* " + useLowerName(field) + ";");
      }
      outData.println("- (id)init;");
      outData.println("- (void)clear;");
      outData.println("- (void)write:(Writer*)writer;");
      outData.println("- (void)read:(Reader*)reader;");
      outData.println("- (void)dealloc;");
      outData.println("@end");
      if (proc.outputs.size() == 0)
        outData.println("#define O" + mainName + "Rec D" + mainName + "Rec");
      outData.println();
    }
  }
  private static void generateEnumOrdinals(Table table, PrintWriter outData)
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
        outData.println("inline NSString *" + table.useName() + field.useName() + "Lookup(int no)");
        outData.println("{");
        outData.println("  switch(no)");
        outData.println("  {");
        for (int j = 0; j < field.enums.size(); j++)
        {
          Enum element = (Enum)field.enums.elementAt(j);
          String evalue = "" + element.value;
          if (field.type == Field.ANSICHAR && field.length == 1)
            evalue = "'" + (char)element.value + "'";
          outData.println("  case " + evalue + ": return @\"" + element.name + "\";");
        }
        outData.println("  default: return @\"<unknown value>\";");
        outData.println("  }");
        outData.println("}");
        outData.println();
      }
      if (field.valueList.size() > 0)
      {
        outData.println("enum e" + table.useName() + field.useName());
        String start = "{";
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String evalue = (String)field.valueList.elementAt(j);
          outData.println(start + " " + table.useName() + field.useName() + evalue);
          start = ",";
        }
        outData.println("};");
        outData.println();
        outData.println("inline NSString *" + table.useName() + field.useName() + "Lookup(int no)");
        outData.println("{");
        outData.println("  switch(no)");
        outData.println("  {");
        for (int j = 0; j < field.valueList.size(); j++)
        {
          String evalue = (String)field.valueList.elementAt(j);
          outData.println("  case " + j + ": return @\"" + evalue + "\";");
        }
        outData.println("  default: return @\"<unknown value>\";");
        outData.println("  }");
        outData.println("}");
        outData.println();
      }
    }
  }
  private static void generateImplementationStructs(Table table, PrintWriter outData)
  {
    if (table.fields.size() > 0)
    {
      if (table.comments.size() > 0)
      {
        for (int i = 0; i < table.comments.size(); i++)
        {
          String s = (String)table.comments.elementAt(i);
          outData.println("// " + s);
        }
      }
      generateImplementationTableStructs(table.fields, table.useName(), outData);
      generateImplementationEnumOrdinals(table, outData);
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc)table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
        continue;
      if (proc.comments.size() > 0)
      {
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String s = (String)proc.comments.elementAt(j);
          outData.println("// " + s);
        }
      }
      generateImplementationStructPairs(proc, table.useName() + proc.upperFirst(), outData);
    }
  }
  private static void generateImplementationTableStructs(Vector<?> fields, String mainName, PrintWriter outData)
  {
    outData.println("@implementation D" + mainName + "Rec");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      if (field.type == Field.BLOB)
      {
        outData.println("@synthesize " + useLowerName(field) + "_len;");
        outData.println("@synthesize " + useLowerName(field) + "_data;");
      }
      else
        outData.println("@synthesize " + useLowerName(field) + ";");
      if (isNull(field) == true)
        outData.println("@synthesize " + useLowerName(field) + "IsNull;");
    }
    outData.println("- (id)init");
    outData.println("  {");
    outData.println("    self = [super init];");
    outData.println("    return self;");
    outData.println("  }");
    outData.println("- (void)clear");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      clearCall(field, outData);
      if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    self." + useLowerName(field) + "IsNull = NO;");
    }
    outData.println("  }");
    outData.println("- (void)write:(Writer*)writer");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      writeCall(field, outData);
      if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    [writer putBoolean:self." + useLowerName(field) + "IsNull];[writer filler:6];");
    }
    outData.println("  }");
    outData.println("- (void)read:(Reader*)reader");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      readCall(field, outData);
      if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
        outData.println("    self." + useLowerName(field) + "IsNull = [reader getBoolean];[reader skip:6];");
    }
    outData.println("  }");
    outData.println("- (void)dealloc");
    outData.println("  {");
    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field)fields.elementAt(i);
      freeCall(field, outData);
    }
    outData.println("    [super dealloc];");
    outData.println("  }");
    outData.println("@end");
    outData.println();
  }
  private static void generateImplementationStructPairs(Proc proc, String mainName, PrintWriter outData)
  {
    if (proc.outputs.size() > 0)
    {
      outData.println("@implementation O" + mainName + "Rec");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        if (field.type == Field.BLOB)
        {
          outData.println("@synthesize " + useLowerName(field) + "_len;");
          outData.println("@synthesize " + useLowerName(field) + "_data;");
        }
        else
          outData.println("@synthesize " + useLowerName(field) + ";");
        if (isNull(field) == true)
          outData.println("@synthesize " + useLowerName(field) + "IsNull;");
      }
      outData.println("- (id)init");
      outData.println("  {");
      outData.println("    self = [super init];");
      outData.println("    return self;");
      outData.println("  }");
      outData.println("- (void)clear");
      outData.println("  {");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        clearCall(field, outData);
        if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
          outData.println("    self." + useLowerName(field) + "IsNull = NO;");
      }
      outData.println("  }");
      outData.println("- (void)write:(Writer*)writer");
      outData.println("  {");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        writeCall(field, outData);
        if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
          outData.println("    [writer putBoolean:self." + useLowerName(field) + "IsNull];[writer filler:6];");
      }
      outData.println("  }");
      outData.println("- (void)read:(Reader*)reader");
      outData.println("  {");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        readCall(field, outData);
        if (isNull(field) == true)// && field.isEmptyOrAnsiAsNull() == false)
          outData.println("    self." + useLowerName(field) + "IsNull = [reader getBoolean];[reader skip:6];");
      }
      outData.println("  }");
      outData.println("- (void)dealloc");
      outData.println("  {");
      for (int i = 0; i < proc.outputs.size(); i++)
      {
        Field field = (Field)proc.outputs.elementAt(i);
        freeCall(field, outData);
      }
      outData.println("    [super dealloc];");
      outData.println("  }");
      outData.println("@end");
      outData.println();
    }
    if (proc.inputs.size() + proc.dynamics.size() > 0)
    {
      outData.println("@implementation D" + mainName + "Rec");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        if (proc.hasOutput(field.name))
          continue;
        if (field.type == Field.BLOB)
        {
          outData.println("@synthesize " + useLowerName(field) + "_len;");
          outData.println("@synthesize " + useLowerName(field) + "_data;");
        }
        else
          outData.println("@synthesize " + useLowerName(field) + ";");
        if (isNull(field) == true)
          outData.println("@synthesize " + useLowerName(field) + "IsNull;");
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        String name = (String)proc.dynamics.elementAt(i);
        Field field = new Field();
        field.name = name;
        outData.println("@synthesize " + useLowerName(field) + ";");
      }
      outData.println("- (id)init");
      outData.println("  {");
      outData.println("    self = [super init];");
      outData.println("    return self;");
      outData.println("  }");
      outData.println("- (void)clear");
      outData.println("  {");
      if (proc.outputs.size() > 0)
        outData.println("    [super clear];");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        clearCall(field, outData);
        if (isNull(field) == true)// && field.isEmptyOrAnsiAsNull() == false)
          outData.println("    self." + useLowerName(field) + "IsNull = NO;");
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        Field field = new Field();
        field.name = (String)proc.dynamics.elementAt(i);
        field.length = ((Integer)proc.dynamicSizes.elementAt(i)).intValue();
        field.type = Field.CHAR;
        clearCall(field, outData);
      }
      outData.println("  }");
      outData.println("- (void)write:(Writer*)writer");
      outData.println("  {");
      if (proc.outputs.size() > 0)
        outData.println("    [super write:writer];");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        writeCall(field, outData);
        if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
          outData.println("    [writer putBoolean:self." + useLowerName(field) + "IsNull];[writer filler:6];");
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        Field field = new Field();
        field.name = (String)proc.dynamics.elementAt(i);
        field.length = ((Integer)proc.dynamicSizes.elementAt(i)).intValue();
        field.type = Field.CHAR;
        writeCall(field, outData);
      }
      outData.println("  }");
      outData.println("- (void)read:(Reader*)reader");
      outData.println("  {");
      if (proc.outputs.size() > 0)
        outData.println("    [super read:reader];");
      for (int i = 0; i < proc.inputs.size(); i++)
      {
        Field field = (Field)proc.inputs.elementAt(i);
        readCall(field, outData);
        if (isNull(field) == true) // && field.isEmptyOrAnsiAsNull() == false)
          outData.println("    self." + useLowerName(field) + "IsNull = [reader getBoolean];[reader skip:6];");
      }
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        Field field = new Field();
        field.name = (String)proc.dynamics.elementAt(i);
        field.length = ((Integer)proc.dynamicSizes.elementAt(i)).intValue();
        field.type = Field.CHAR;
        readCall(field, outData);
      }
      outData.println("  }");
      outData.println("- (void)dealloc");
      outData.println("  {");
      for (int i = 0; i < proc.dynamics.size(); i++)
      {
        Field field = new Field();
        field.name = (String)proc.dynamics.elementAt(i);
        outData.println("    [" + useLowerName(field) + " release];");
      }
      outData.println("    [super dealloc];");
      outData.println("  }");
      outData.println("@end");
      outData.println();
    }
  }
  private static void generateImplementationEnumOrdinals(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field)table.fields.elementAt(i);
      if (field.enums.size() > 0)
      {
        // TBD
      }
    }
  }
  private static void skip(int used, PrintWriter outData)
  {
    int size = used % 8;
    if (size == 0) 
      outData.println();
    else
      outData.println("[reader skip:" + (8 - size) + "];");
  }
  private static void filler(int used, PrintWriter outData)
  {
    int size = used % 8;
    if (size == 0)
      outData.println();
    else
      outData.println("[writer filler:" + (8 - size) + "];");
  }
  private static void clearCall(Field field, PrintWriter outData)
  {
    String var = useLowerName(field);
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("    self." + var + " = 0;");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
      case Field.DYNAMIC:
        outData.println("    self." + var + " = @\"\";");
        break;
      case Field.MONEY:
        outData.println("    self." + var + " = @\"\";");
        break;
      case Field.BLOB:
        outData.println("    self." + var + "_len = 0;");
        outData.println("    self." + var + "_data = nil;");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    self." + var + " = 0;");
        break;
      case Field.BOOLEAN:
        outData.println("    self." + var + " = NO;");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          outData.println("    self." + var + " = @\"\";");
        else
          outData.println("    self." + var + " = 0.0;");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
  }
  private static void writeCall(Field field, PrintWriter outData)
  {
    String var = useLowerName(field);
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("    [writer putByte:self." + var + "];[writer filler:7];");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
      case Field.DYNAMIC:
        outData.print("    [writer putString:self." + var + " length:" + (field.length + 1) + "];");
        filler(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("    [writer putString:self." + var + " length:21];");
        filler(21, outData);
        break;
      case Field.BLOB:
        outData.print("    [writer putInt:self." + var + "_len];");
        outData.print("[writer putData:self." + var + "_data length:" + (field.length - 4) + "];");
        filler(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("    [writer putBoolean:self." + var + "];[writer filler:6];");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    [writer putShort:self." + var + "];[writer filler:6];");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("    [writer putString:self." + var + " length:" + (field.precision + 3) + "];");
          filler(field.precision + 3, outData);
        }
        else
          outData.println("    [writer putDouble:self." + var + "];");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("    [writer putInt:self." + var + "];[writer filler:4];");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("    [writer putLong:self." + var + "];");
        break;
      default:
        outData.println("    //" + var + " unsupported");
        break;
    }
  }
  private static void readCall(Field field, PrintWriter outData)
  {
    String var = useLowerName(field);
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          outData.println("    self." + var + " = [reader getByte];[reader skip:7];");
          break;
        }
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
      case Field.DYNAMIC:
        outData.print("    self." + var + " = [reader getString:" + (field.length + 1) + "];");
        skip(field.length + 1, outData);
        break;
      case Field.MONEY:
        outData.print("    self." + var + " = [reader getString:21];");
        skip(21, outData);
        break;
      case Field.BLOB:
        outData.print("    self." + var + "_len = [reader getInt];");
        outData.print("self." + var + "_data = [reader getData:" + (field.length - 4) + "];");
        skip(field.length, outData);
        break;
      case Field.BOOLEAN:
        outData.println("    self." + var + " = [reader getBoolean];[reader skip:6];");
        break;
      case Field.BYTE:
      case Field.STATUS:
      case Field.SHORT:
        outData.println("    self." + var + " = [reader getShort];[reader skip:6];");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
        {
          outData.print("    self." + var + " = [reader getString:" + (field.precision + 3) + "];");
          skip(field.precision + 3, outData);
        }
        else
          outData.println("    self." + var + " = [reader getDouble];");
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        outData.println("    self." + var + " = [reader getInt];[reader skip:4];");
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        outData.println("    self." + var + " = [reader getLong];");
        break;
      default:
        outData.println("    // " + var + " unsupported");
        break;
    }
  }
  private static void freeCall(Field field, PrintWriter outData)
  {
    String var = useLowerName(field);
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
          break;
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
      case Field.DYNAMIC:
        outData.println("    [" + var + " release];");
        break;
      case Field.MONEY:
        outData.println("    [" + var + " release];");
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          outData.println("    [" + var + " release];");
        break;
      case Field.BLOB:
        outData.println("    [" + var + "_data release];");
        break;
    }
  }
  private static String fieldProp(Field field)
  {
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
          break;
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        return "retain";
      case Field.MONEY:
        return "retain";
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return "retain";
        break;
      case Field.DYNAMIC:
        return "retain";
    }
    return "readwrite";
  }
  private static String fieldDef(Field field)
  {
    String result = "";
    String name = useLowerName(field);
    switch (field.type)
    {
      case Field.ANSICHAR:
        if (field.length == 1)
        {
          result = "uint8";
          break;
        } 
      case Field.CHAR:
      case Field.TLOB:
      case Field.USERSTAMP:
      case Field.DATE:
      case Field.DATETIME:
      case Field.TIME:
      case Field.TIMESTAMP:
        return "NSString *" + name + "; // [" + (field.length+1) + "];";
      case Field.MONEY:
        return "NSString *" + name + "; // [21];";
      case Field.BLOB:
        return "int32 " + name + "_len; NSMutableData* " + name + "_data; // [" + (field.length - 4) + "]";
      case Field.BOOLEAN:
        result = "BOOL";
        break;
      case Field.BYTE:
        result = "int16";
        break;
      case Field.STATUS:
        result = "int16";
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        if (field.precision > 15)
          return "NSString *" + name + "; // [" + (field.precision + 3) + "];";
        else
          result = "double";
        break;
      case Field.INT:
      case Field.IDENTITY:
      case Field.SEQUENCE:
        result = "int32";
        break;
      case Field.LONG:
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
        result = "int64";
        break;
      case Field.SHORT:
        result = "int16";
        break;
      case Field.DYNAMIC:
        return "NSString *" + name + "; // [" + (field.length + 1) + "];";
      default:
        result = "whoknows";
        break;
    }
    return result + " " + name + ";";
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
