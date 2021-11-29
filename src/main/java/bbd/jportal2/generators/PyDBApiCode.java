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

package bbd.jportal2.generators;

import bbd.jportal2.Enum;
import bbd.jportal2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Vector;

import static bbd.jportal2.generators.Writer.*;

public class PyDBApiCode extends BaseGenerator implements IBuiltInSIProcessor
{
  private static final Logger logger = LoggerFactory.getLogger(PyDBApiCode.class);

  public PyDBApiCode()
  {
    super(PyDBApiCode.class);
  }

  static public String description()
  {
    return "Generate DBApi Code for Python";
  }

  static public String documentation()
  {
    return "Generate DBApi Code for Python";
  }

  /**
   * Padding function
   */
  static private String padder(String s, int length)
  {
    for (int i = s.length(); i < length-1; i++)
      s = s + " ";
    return s + " ";
  }

  /**
   * DBApi param styles
   * ============ ==============================================================
   * paramstyle   Meaning
   * ============ ==============================================================
   * 'qmark'    Question mark style, e.g. '...WHERE name=?'
   * 'numeric'  Numeric, positional style, e.g. '...WHERE name=:1'
   * 'named'    Named style, e.g. '...WHERE name=:name'
   * 'format'   ANSI C printf format codes, e.g. '...WHERE name=%s'
   * 'pyformat' Python extended format codes, e.g.  '...WHERE name=%(name)s'
   * ============ ==============================================================
   */

  static private Properties properties;
  static private byte paramStyle = PlaceHolder.QUESTION;
  static private boolean useEnum = false;

  /*
  * Generates the procedure classes for each table present.
  */
  static private void setParamStyle(String flag)
  {
    if (flag.equalsIgnoreCase("qmark"))
      paramStyle = PlaceHolder.QUESTION;
    else if (flag.equalsIgnoreCase("numeric"))
      paramStyle = PlaceHolder.COLON_NO;
    else if (flag.equalsIgnoreCase("named"))
      paramStyle = PlaceHolder.COLON;
    else if (flag.equalsIgnoreCase("format"))
      paramStyle = PlaceHolder.FORMAT;
    else if (flag.equalsIgnoreCase("pyformat"))
      paramStyle = PlaceHolder.PYFORMAT;
  }

  public void generate(Database database, String output) throws Exception
  {
    try
    {
      String propertiesName = format("%s%s.properties", output, database.name);
      InputStream input = new FileInputStream(propertiesName);
      properties = new Properties();
      properties.load(input);
    }
    catch (Exception ex)
    {
      properties = null;
    }
    for (int i=database.flags.size()-1; i>=0; i--)
    {
       String flag = database.flags.elementAt(i);
       flag = flag.toLowerCase();
       boolean dropParameter = false;
       if (flag.startsWith("%"))
       {
         flag = flag.substring(1);
         dropParameter = true;
       }
       if (flag.startsWith("param="))
         setParamStyle(flag.substring(6));
       if (dropParameter)
         database.flags.remove(i);
       if (flag.equalsIgnoreCase("useenum")|| flag.equalsIgnoreCase("use enum"))
         useEnum = true;
    }
    String value = getProperty("param", null);
    if (value != null)
      setParamStyle(value.toLowerCase());
    useEnum = getProperty("useenum", useEnum);
    for (int i=0; i<database.tables.size(); i++)
    {
      Table table = database.tables.elementAt(i);
      generateStructs(database, table, output);
    }
  }

  static private String getProperty(String propName, String propDefault)
  {
    if (properties == null) 
      return propDefault;
    String propValue = properties.getProperty(propName);
    if (propValue == null) 
      return propDefault;
    return propName + "=" + propValue;
  }

  static private boolean getProperty(String propName, boolean propDefault)
  {
    if (properties == null) 
      return propDefault;
    String propValue = properties.getProperty(propName);
    if (propValue == null) 
      return propDefault;
    return propValue.equalsIgnoreCase("true");
  }

  /**
   * Build of standard and user defined procedures
   */
  static private void generateStructs(Database database, Table table, String output) throws Exception
  {
    try (OutputStream outFile = new FileOutputStream(output + table.useName() + "DBApi.py"))
    {
      logger.info(format("Code: %s%sDBApi.py", output, table.useName()));
      writer = new PrintWriter(outFile);
      indent_size = 4;
      writeln("# This code was generated, do not modify it, modify it at source and regenerate it.");
      writeln("# see " + table.useName() + " source file");
      writeln();
      writeln("import dbapi_util");
      writeln("from dbapi_annotate import *");
      writeln();
      generateEnums(database);
      generateEnums(table);
      if (table.hasStdProcs)
        generateStdOutputRec(table);
      generateUserOutputRecs(table);
      generateCode(table);
      writer.flush();
    }
  }

  static private String _commanull(Field field)
  {
    if (field.isNull) return(", null=True");    
    return "";
  }

  static private String _null(Field field)
  {
    if (field.isNull) return("null=True");    
    return "";
  }

  static private void generateAnnotates(Vector allFields)
  {
    for (int i = 0; i < allFields.size(); i++)
    {
      Field field = (Field) allFields.elementAt(i);
      write(1, format("%s: ",field.useName()));
      switch (field.type)
      {
      case Field.ANSICHAR:
        writeln(format("Char(%d%s)", field.length+1, _commanull(field)));
        break;
      case Field.AUTOTIMESTAMP:
        writeln(format("TimeStamp(%s)", _null(field)));
        break;
      case Field.BIGIDENTITY:
      case Field.BIGSEQUENCE:
      case Field.LONG:
        writeln(format("LongInt(%d%s)", field.length, _commanull(field)));
        break;
      case Field.BIGXML:
        writeln(format("XMLTYPE(%d%s)", field.length+1, _commanull(field)));
        break;
      case Field.BLOB:
        writeln(format("Blob(%d%s)", field.length+1, _commanull(field)));
        break;
      case Field.BOOLEAN:
        writeln(format("Boolean(%d%s)", field.length, _commanull(field)));
        break;
      case Field.BYTE:
        writeln(format("TinyInt(%d%s)", field.length, _commanull(field)));
        break;
      case Field.CHAR:
        writeln(format("Char(%d%s)", field.length+1, _commanull(field)));
        break;
      case Field.DATE:
        writeln(format("Date(%s)", _null(field)));
        break;
      case Field.DATETIME:
        writeln(format("DateTime(%s)", _null(field)));
        break;
      case Field.DOUBLE:
      case Field.FLOAT:
        writeln(format("Float(%d, %d%s)", field.precision, field.scale, _commanull(field)));
        break;
      case Field.DYNAMIC:
        writeln(format("Unhandled('Dynamic', %d%s)", field.length, _commanull(field)));
        break;
      case Field.IDENTITY:
      case Field.INT:
      case Field.SEQUENCE:
        writeln(format("Int(%s%s)", field.length, _commanull(field)));
        break;
      case Field.IMAGE:
        writeln(format("Image(%s%s)", field.length+1, _commanull(field)));
        break;
      case Field.MONEY:
        writeln(format("Char(%s%s)", field.length+1, _commanull(field)));
        break;
      case Field.SHORT:
        writeln(format("SmallInt(%s%s)", field.length, _commanull(field)));
        break;
      case Field.STATUS:
        writeln(format("Status(%s%s)", field.length, _commanull(field)));
        break;
      case Field.TIME:
        writeln(format("Time(%s)", _null(field)));
        break;
      case Field.TIMESTAMP:
        writeln(format("TimeStamp(%s)", _null(field)));
        break;
      case Field.TLOB:
        writeln(format("Clob(%s%s)", field.length+1, _commanull(field)));
        break;
      case Field.UID:
        writeln(format("Unhandled('UID', %s%s)", field.length, _commanull(field)));
        break;
      case Field.USERSTAMP:
        writeln(format("UserStamp(%s%s)", field.length+1, _commanull(field)));
        break;
      case Field.UTF8:
        writeln(format("Unhandled('UTF8', %s%s)", field.length+1, _commanull(field)));
        break;
      case Field.WANSICHAR:
        writeln(format("Unhandled('WANSICHAR', %s%s)", field.length+1, _commanull(field)));
        break;
      case Field.WCHAR:
        writeln(format("Unhandled('WCHAR', %s%s)", field.length+1, _commanull(field)));
        break;
      case Field.XML:
        writeln(format("XMLTYPE(%s%s)", field.length+1, _commanull(field)));
        break;
      }
    }
  }

  static private void generateDataFields(Vector allFields, String superName, String tableName)
  {
    String recName = superName.length() > 0 ? superName : tableName;
    write(1, "__slots__ = [");
    for (int i = 0; i < allFields.size(); i++)
    {
      Field field = (Field)allFields.elementAt(i);
      if (i != 0)
      {
        writeln(",");
        write(2, "");
      }
      write("'" + field.useName() + "'");
    }
    writeln("]");
    writeln(1, "def __init__(self):");
    for (int i = 0; i < allFields.size(); i++)
    {
      Field field = (Field)allFields.elementAt(i);
      if (isNull(field) == true)
        writeln(2, "self." + field.useName() + " = None");
      else
        writeln(2, "self." + field.useName() + " = ''");
    }
    writeln(1, "def _fields(self):");
    writeln(2, "return D" + recName + ".__slots__");
  }

  static private void generateStdOutputRec(Table table)
  {
    writeln("class D" + table.useName() + "():");
    generateAnnotates(table.fields);
    writeln(1, "def _make(self): return D" + table.useName() + "()");
    generateDataFields(table.fields, "", table.useName());
    writeln();
  }

  static private void generateUserOutputRecs(Table table)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      if (proc.isStdExtended())
        continue;
      String superName = table.useName() + proc.upperFirst();
      Vector<Field> procFields = new Vector<Field>();
      writeln("class D" + superName + "():");
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = proc.inputs.elementAt(j);
        procFields.addElement(field);
      }
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = proc.outputs.elementAt(j);
        if (proc.hasInput(field.name) == true)
          continue;
        procFields.addElement(field);
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        Field f = new Field();
        f.name = proc.dynamics.elementAt(j);
        f.type = Field.CHAR;
        procFields.addElement(f);
      }
      generateAnnotates(procFields);
      writeln(1, "def _make(self): return D" + superName + "()");
      generateDataFields(procFields, superName, table.useName());
      writeln();
    }
  }

  static private void generateEnums(Table table)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = table.fields.elementAt(i);
      generateEnums(table.useName() + field.useName(), field);
    }
  }

  static private void generateEnums(Database database)
  {
    for (int i = 0; i < database.declares.size(); i++)
    {
      Field field = database.declares.elementAt(i);
      generateEnums(database.name + field.useName(), field);
    }
  }

  static private void generateEnums(String baseName, Field field)
  {
    if (field.enums.size() > 0)
    {
      if (useEnum == true)
        generateEnumsAsEnum(baseName, field);
      else
        generateEnumsAsDict(baseName, field);
    }
    else if (field.valueList.size() > 0)
    {
      writeln("class " + baseName + ":");
      for (int j = 0; j < field.valueList.size(); j++)
      {
        String entry = field.valueList.elementAt(j);
        writeln(1, entry + " = '" + entry + "'");
      }
      writeln();
    }
  }

  static private boolean enumImport = false;

  static private void generateEnumsAsEnum(String baseName, Field field)
  {
    if (enumImport == false)
    {
      writeln("from enum import Enum");
      writeln();
      enumImport = true;
    }
    writeln(format("class %s(Enum):", baseName));
    for (int i = 0; i < field.enums.size(); i++)
    {
      bbd.jportal.Enum entry = field.enums.elementAt(i);
      writeln(1, format("%s = %s", entry.name, entry.value));
    }
    writeln(1, "def __str__(self):");
    writeln(2, "return str(self.value)");
    writeln();
  }

  static private void generateEnumsAsDict(String baseName, Field field)
  {
    if (field.enums.size() > 0)
    {
      writeln(baseName + " = {}");
      for (int j = 0; j < field.enums.size(); j++)
      {
        bbd.jportal.Enum entry = field.enums.elementAt(j);
        writeln(baseName + "['" + entry.name + "'] = " + entry.value);
      }
      for (int j = 0; j < field.enums.size(); j++)
      {
        Enum entry = field.enums.elementAt(j);
        writeln(baseName + "[" + entry.value + "] = '" + entry.name + "'");
      }
      writeln();
    }
  }

  static private void generateCode(Table table)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = table.procs.elementAt(i);
      if (proc.isData)
        continue;
      PlaceHolder holder = new PlaceHolder(proc, paramStyle, "");
      Vector pairs = holder.getPairs();
      String parent = "";
      String current="";
      if (proc.hasNoData() == true)
      {
        parent = "object";
        current = table.useName() + proc.upperFirst();
      }
      else if (proc.isStd == true || proc.isStdExtended() == true)
      {
        parent = "D" + table.useName();
        current = table.useName() + proc.upperFirst();
      }
      else
      {
        parent = "D" + table.useName() + proc.upperFirst();
        current = table.useName() + proc.upperFirst();
      }
      writeln("class " + current + "(" + parent + "):");
      if (proc.hasNoData() == false)
      {
        String timestampName = "";
        String sequenceName = "";
        if (proc.hasReturning && (proc.isInsert || proc.isUpdate))
        {
          String returningName = "";
          for (int p=0; p<table.fields.size(); p++)
          {
            Field field = table.fields.elementAt(p);
            if (proc.isInsert && (field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE))
              returningName = field.useName();
            else if (field.type == Field.TIMESTAMP)
              timestampName = field.useName();  
          }
          writeln(1, "def __init__(self, ret):");
          writeln(2, parent + ".__init__(self)");
          writeln(2, "self._ret = ret('" + table.name + "', '" + returningName + "')");
          if (timestampName.length() > 0)
            writeln(2, format("self.%s = dbapi_util.get_timestamp()", timestampName));
        }
        else if (proc.isInsert || proc.isUpdate)
        {
          for (int p=0; p<proc.inputs.size(); p++)
          {
            Field field = proc.inputs.elementAt(p);
            if (proc.isInsert && (field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE))
              sequenceName = field.useName();
            else if (field.type == Field.TIMESTAMP)
              timestampName = field.useName();
          }
          if (sequenceName.length() > 0)
            writeln(1, "def __init__(self, connect):");
          else
            writeln(1, "def __init__(self):");
          writeln(2, parent + ".__init__(self)");
          if (sequenceName.length() > 0)
            writeln(2, format("self.%s = dbapi_util.get_sequence(connect, '%s')", sequenceName, table.name));
          if (timestampName.length() > 0)
            writeln(2, format("self.%s = dbapi_util.get_timestamp()", timestampName));
        }
      }
      int inouts = 0;
      boolean hasInputs = false;
      if (proc.outputs.size() > 0)
      {
        writeln(1, "def _get_output(self, _result):");
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = proc.outputs.elementAt(j);
          writeln(2, "self." + field.useName() + " = _result[" + j + "]");
          if (proc.hasInput(field.name) == true)
            inouts++;
        }
        writeln(2, "return " + proc.outputs.size());
      }
      if (proc.outputs.size() > 0 && proc.inputs.size() + proc.dynamics.size() - inouts > 0)
      {
        hasInputs = true;
        writeln(1, "def _copy_input(self, record):");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name) == true)
          {
            writeln(2, "# " + field.name + " is an output");
            continue;
          }
          writeln(2, "record." + field.name + " = self."+ field.name);  
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String name = proc.dynamics.elementAt(j);
          writeln(2, "record." + name + " = self."+ name);  
        }
      }
      writeln(1, "def execute(self, connect): # "+ proc.lowerFirst());
      Vector lines = holder.getLines();
      String command = "_command";
      generateString(proc, command, lines);
      writeln(2, "cursor = connect.cursor()");
      if (pairs.size() > 0)
      {
        writeln(2, "cursor.execute(" + command + ", [");
        for (int j = 0; j < pairs.size(); j++)
        {
          if (j > 0)
            writeln(",");
          PlaceHolderPairs pair = (PlaceHolderPairs)pairs.elementAt(j);
          Field field = pair.field;
          write(3, "self." + field.useName());
        }
        writeln("])");
      }
      else
      {
        writeln(2, "cursor.execute(" + command + ")");
      }
      if (proc.outputs.size() > 0)
        if (proc.isSingle)
          checkPythonSingle(table, proc, current, hasInputs);
        else
          generatePythonMultiple(table, proc, current, hasInputs);
      else
        generatePythonAction(table, proc, hasInputs);
      writeln();
    }
  }

  static private void checkPythonSingle(Table table, Proc proc, String current, boolean hasInputs)
  {
    if (proc.hasReturning && proc.isInsert == true)
    {
      for (int p=0; p<table.fields.size(); p++)
      {
        Field field = table.fields.elementAt(p);
        if (proc.isInsert && (field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE))
        {
          writeln(2, "if self._ret.usesPlSql == true:");
          writeln(3, "self." + field.name + " = cursor.var('" + field.name + "')");
          writeln(3, "return self");
          break;
        }
      }
    }
    generatePythonSingle(table, proc, current, hasInputs);
  }

  static private void generatePythonSingle(Table table, Proc proc, String current, boolean hasInputs)
  {
    writeln(2, "record = " + current + "()");
    if (hasInputs)
      writeln(2, "self._copy_input(record)");
    writeln(2, "result = cursor.fetchone()");
    writeln(2, "if result == None:");
    writeln(3, "return None");
    writeln(2, "record._get_output(result)");
    writeln(2, "return record");
  }

  static private void generatePythonMultiple(Table table, Proc proc, String current, boolean hasInputs)
  {
    writeln(2, "records = []");
    writeln(2, "for row in cursor:");
    writeln(3, "record = " + current + "()");
    if (hasInputs)
      writeln(3, "self._copy_input(record)");
    writeln(3, "record._get_output(row)");
    writeln(3, "records.append(record)");
    writeln(2, "return records");
  }

  static private void generatePythonAction(Table table, Proc proc, boolean hasInputs)
  {
  }

  static private void generateString(Proc proc, String name, Vector strings)
  {
    String added = "";
    writeln(2, name+" = '''\\");
    for (int i = 0; i < strings.size(); i++)
    {
      String string = (String)strings.elementAt(i);
      if (string.charAt(0) == '"')
        writeln(string.substring(1, string.length() - 1));
      else
      {
        if (added.length() == 0)
          added = added + " % (";
        else
          added = added + ", ";
        String l = string.trim();
        added = added + "self." + l;
        String quotes = "";
        if (proc.isStrung(l) == true)
          quotes = "'";
        writeln(quotes + "%s" + quotes);
      }
    }
    if (added.length() > 0)
      added = added + ")";
    writeln("'''"+added);
  }

  static private boolean isNull(Field field)
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
