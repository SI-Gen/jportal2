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

public class AdoCCode extends Generator
{
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i < args.length; i++)
      {
        outLog.println(args[i] + ": Generate Ado C++ Code");
        in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
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
    return "Generates ADO (OleDB) C++ Code";
  }
  public static String documentation()
  {
    return "Generates ADO (OleDB) C++ Code \r\n" +
        "allows for FLAGS :-\r\n" +
        "  sqlprocs\r\n" +
        "  mssql\r\n";
  }
  static String padder(String s, int length)
  {
    for (int i = s.length(); i < length - 1; i++)
      s = s + " ";
    return s + " ";
  }
  protected static Vector<Flag> flagsVector;
  static boolean sqlprocs;
  static boolean mssql;
  private static void flagDefaults()
  {
    sqlprocs = false;
    mssql = false;
  }
  public static Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<Flag>();
      flagDefaults();
      flagsVector.addElement(new Flag("sqlprocs", new Boolean(sqlprocs), "Generate Sql Procs"));
      flagsVector.addElement(new Flag("mssql", new Boolean(mssql), "Generate MSSql"));
    }
    return flagsVector;
  }
  static void setFlags(Database database, PrintWriter outLog)
  {
    if (flagsVector != null)
    {
      sqlprocs = toBoolean(((Flag) flagsVector.elementAt(0)).value);
      mssql = toBoolean(((Flag) flagsVector.elementAt(1)).value);
    }
    else
      flagDefaults();
    for (int i = 0; i < database.flags.size(); i++)
    {
      String flag = (String) database.flags.elementAt(i);
      if (flag.equalsIgnoreCase("sqlprocs"))
        sqlprocs = true;
      else if (flag.equalsIgnoreCase("mssql"))
        mssql = true;
    }
    if (sqlprocs)
      outLog.println(" (sqlprocs) -- should be used with 'mssql'");
    if (mssql)
      outLog.println(" (mssql)");
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    setFlags(database, outLog);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      generate(table, output, outLog);
      generateVB6(table, output, outLog);
    }
  }
  static void generate(Table table, String output, PrintWriter outLog)
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
          outData.println("#ifndef _" + table.useName().toUpperCase() + "_H_");
          outData.println("#define _" + table.useName().toUpperCase() + "_H_");
          outData.println();
          outData.println("#include \"tbuffer.h\" // used for XML production");
          outData.println("#include \"adoconnector.h\"");
          outData.println();
          generateEnums(table, outData);
          if (table.hasStdProcs)
            generateStdOutputRec(table, outData);
          generateUserOutputRecs(table, outData);
          generateInterface(table, outData);
          outData.println("#endif");
          outData.flush();
        }
        finally
        {
          outData.flush();
        }
        outLog.println("Code: " + output + table.useName() + ".cpp");
        outFile = new FileOutputStream(output + table.useName() + ".cpp");
        outData = new PrintWriter(outFile);
        try
        {
          outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
          outData.println();
          outData.println("#include \"" + table.useName() + ".h" + "\"");
          outData.println();
          generateImplementation(table, outData);
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
  static void generateStdOutputRec(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      outData.println("//" + s);
    }
    Padder pad = new Padder();
    outData.println("struct D" + table.useName());
    outData.println("{");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      pad.padC(paddingSize(field), relativeSize(field), outData);
      if (field.comments.size() > 0)
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  //" + s);
        }
      outData.println("  " + cppVar(field) + ";");
      if (field.type == Field.BLOB || field.type == Field.TLOB)
      {
        pad.padC(4, 4, outData);
        outData.println("  long   " + field.useName() + "LOBLen;");
      }
      if (field.isNull)
      {
        pad.padC(4, 2, outData);
        outData.println("  short  " + field.useName() + "IsNull;");
      }
    }
    pad.padC(8, 0, outData);
    outData.println("  void Trim()");
    outData.println("  {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (isChar(field) == true)
        outData.println("    TConnector::Trim(" + field.useName() + ", sizeof(" + field.useName() + "));");
    }
    outData.println("  }");
    outData.println("  void Pad()");
    outData.println("  {");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      if (isChar(field) == true)
        outData.println("    TConnector::Pad(" + field.useName() + ", sizeof(" + field.useName() + "));");
    }
    outData.println("  }");
    outData.println("  void XML(TBAmp &XRec, char* Outer=\"D" + table.useName().toUpperCase() + "\", char* Attr=0)");
    outData.println("  {");
    outData.println("    XRec.clear();");
    outData.println("    XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\");");
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      outData.println("    " + xmlOf(field));
    }
    outData.println("    XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\");");
    outData.println("  }");
    outData.println("};");
    outData.println();
    outData.println("typedef D" + table.useName() + " O" + table.useName() + ";");
    outData.println();
  }
  static void generateUserOutputRecs(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      String work = "";
      String superName = "";
      if (proc.outputs.size() > 0)
      {
        Padder pad = new Padder();
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println("//" + comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput())
          typeChar = "O";
        work = " : public " + typeChar + table.useName() + proc.upperFirst();
        superName = typeChar + table.useName() + proc.upperFirst();
        outData.println("struct " + typeChar + table.useName() + proc.upperFirst());
        outData.println("{");
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          pad.padC(paddingSize(field), relativeSize(field), outData);
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  //" + s);
          }
          outData.println("  " + cppVar(field) + ";");
          if (field.type == Field.BLOB || field.type == Field.TLOB)
          {
            pad.padC(4, 4, outData);
            outData.println("  long   " + field.useName() + "LOBLen;");
          }
          if (field.isNull)
          {
            pad.padC(4, 2, outData);
            outData.println("  short  " + field.useName() + "IsNull;");
          }
        }
        pad.padC(8, 0, outData);
        outData.println("  void Trim()");
        outData.println("  {");
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          if (isChar(field) == true)
            outData.println("    TConnector::Trim(" + field.useName() + ", sizeof(" + field.useName() + "));");
        }
        outData.println("  }");
        outData.println("  void Pad()");
        outData.println("  {");
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          if (isChar(field) == true)
            outData.println("    TConnector::Pad(" + field.useName() + ", sizeof(" + field.useName() + "));");
        }
        outData.println("  }");
        outData.println("  void XML(TBAmp &XRec, char* Outer=\"" + typeChar + table.useName().toUpperCase() + proc.name.toUpperCase() + "\", char *Attr = 0)");
        outData.println("  {");
        outData.println("    XRec.clear();");
        outData.println("    XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\");");
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          outData.println("    " + xmlOf(field));
        }
        outData.println("    XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\");");
        outData.println("  }");
        outData.println("};");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        Padder pad = new Padder();
        outData.println("struct D" + table.useName() + proc.upperFirst() + work);
        outData.println("{");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          pad.padC(paddingSize(field), relativeSize(field), outData);
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  //" + s);
          }
          outData.println("  " + cppVar(field) + ";");
          if (field.type == Field.BLOB || field.type == Field.TLOB)
          {
            pad.padC(4, 4, outData);
            outData.println("  long   " + field.useName() + "LOBLen;");
          }
          if (field.isNull)
          {
            pad.padC(4, 2, outData);
            outData.println("  short  " + field.useName() + "IsNull;");
          }
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          Integer n = (Integer) proc.dynamicSizes.elementAt(j);
          outData.println("  char   " + s + "[" + n.intValue() + "];");
          pad.incOffset(n.intValue());
        }
        pad.padC(8, 0, outData);
        outData.println("  void Trim()");
        outData.println("  {");
        if (superName.length() > 0)
          outData.println("    " + superName + "::Trim();");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          if (isChar(field) == true)
            outData.println("    TConnector::Trim(" + field.useName() + ", sizeof(" + field.useName() + "));");
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData.println("    TConnector::Trim(" + s + ", sizeof(" + s + "));");
        }
        outData.println("  }");
        outData.println("  void Pad()");
        outData.println("  {");
        if (superName.length() > 0)
          outData.println("    " + superName + "::Pad();");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          if (isChar(field) == true)
            outData.println("    TConnector::Pad(" + field.useName() + ", sizeof(" + field.useName() + "));");
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          outData.println("    TConnector::Pad(" + s + ", sizeof(" + s + "));");
        }
        outData.println("  }");
        outData.println("  void XML(TBAmp &XRec, char* Outer=\"D" + table.useName().toUpperCase() + proc.name.toUpperCase() + "\", char *Attr = 0)");
        outData.println("  {");
        outData.println("    XRec.clear();");
        outData.println("    XRec.append(\"<\");XRec.append(Outer);if (Attr) XRec.append(Attr);XRec.append(\">\");");
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          if (proc.hasOutput(field.name))
            continue;
          outData.println("    " + xmlOf(field));
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          outData.println("    " + xmlOf(field));
        }
        outData.println("    XRec.append(\"</\");XRec.append(Outer);XRec.append(\">\");");
        outData.println("  }");
        outData.println("};");
        outData.println();
      }
      else if (proc.outputs.size() > 0)
      {
        outData.println("typedef D" + table.useName() + proc.upperFirst() + " O" + table.useName() + proc.upperFirst() + ";");
        outData.println();
      }
    }
  }
  static void generateEnums(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      generateEnums(table.useName() + field.useName(), field, outData);
    }
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (!proc.isStd)
      {
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          generateEnums(table.useName() + proc.name + field.useName(), field, outData);
        }
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          generateEnums(table.useName() + proc.name + field.useName(), field, outData);
        }
      }
    }
  }
  static void generateEnums(String baseName, Field field, PrintWriter outData)
  {
    if (field.enums.size() > 0)
    {
      outData.println("enum e" + baseName);
      String startStr = "{ ";

      for (int j = 0; j < field.enums.size(); j++)
      {
        Enum element = (Enum) field.enums.elementAt(j);
        outData.println(startStr + baseName + element.name + " = " + element.value);
        startStr = ", ";
      }
      outData.println("};");

      outData.println("inline char *" + baseName + "Lookup(int no)");
      outData.println("{");
      outData.println("  switch(no)");
      outData.println("  {");
      for (int j = 0; j < field.enums.size(); j++)
      {
        Enum element = (Enum) field.enums.elementAt(j);
        outData.println("  case " + baseName + element.name + ": return \"" + element.name + "\";");
      }
      outData.println("  default: return \"<n/a>\";");
      outData.println("  }");
      outData.println("}");
      outData.println();
    }
  }
  static void generateInterface(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateInterface(table, proc, outData);
    }
  }
  static void generateInterface(Table table, Proc proc, PrintWriter outData)
  {
    String dataStruct;
    if (proc.comments.size() > 0)
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = (String) proc.comments.elementAt(i);
        outData.println("  //" + comment);
      }
    if (proc.hasNoData())
    {
      outData.println("struct T" + table.useName() + proc.upperFirst());
      outData.println("{");
      outData.println("  TConnector &conn;");
      outData.println("  TQuery qry;");
      outData.println("  void Exec();");
      outData.println("  T" + table.useName() + proc.upperFirst() + "(TConnector &aConn)");
      outData.println("  : conn(aConn)");
      outData.println("  {}");
      outData.println("};");
      outData.println();
    }
    else
    {
      if (proc.isStd)
        dataStruct = "D" + table.useName();
      else
        dataStruct = "D" + table.useName() + proc.upperFirst();
      outData.println("struct T" + table.useName() + proc.upperFirst() + " : public " + dataStruct);
      outData.println("{");
      generateInterface(table, proc, dataStruct, outData);
      outData.println("};");
      outData.println();
    }
  }
  static void generateInterface(Table table, Proc proc, String dataStruct, PrintWriter outData)
  {
    outData.println("  TConnector &conn;");
    outData.println("  TQuery qry;");
    outData.println("  void Clear() {memset(this, 0, sizeof(" + dataStruct + "));}");
    outData.println("  void Close() {qry.Close();}");
    outData.println("  void Exec();");
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("  void Exec(" + dataStruct + "& Rec) {*DRec() = Rec;Exec();}");
      outData.println("  void Exec(");
      generateWithParms(proc, outData, "  ");
      outData.println("  );");
    }
    if (proc.outputs.size() > 0)
      outData.println("  bool Fetch();");
    outData.println("  T" + table.useName() + proc.upperFirst() + "(TConnector &aConn)");
    outData.println("  : conn(aConn)");
    outData.println("  {Clear();}");
    outData.println("  " + dataStruct + "* DRec() {return this;}");
    if (proc.outputs.size() > 0)
      outData.println("  O" + dataStruct.substring(1) + "* ORec() {return this;}");
  }
  static void generateImplementation(Table table, PrintWriter outData)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateImplementation(table, proc, outData);
    }
  }
  static int questionsSeen;
private static ObjectInputStream in;
  static String question(Proc proc, String line)
  {
    String result = "";
    int p;
    while ((p = line.indexOf("?")) > -1)
    {
      if (p > 0)
      {
        result = result + line.substring(0, p);
        line = line.substring(p);
      }
      Field field = (Field) proc.inputs.elementAt(questionsSeen++);
      if (field.type == Field.IDENTITY && proc.isInsert)
        field = (Field) proc.inputs.elementAt(questionsSeen++);
      result = result + ":" + field.name;
      line = line.substring(1);
    }
    result = result + line;
    return result;
  }
  static int checkPlaceHolders(Proc proc, PrintWriter outData, String l, int phIndex)
  {
    if (phIndex >= proc.placeHolders.size())
    {
      outData.print(l);
      return phIndex;
    }
    int n = 0;
    while (phIndex < proc.placeHolders.size())
    {
      String placeHolder = ":" + (String) proc.placeHolders.elementAt(phIndex);
      n = l.indexOf(placeHolder);
      if (n == -1)
        break;
      phIndex++;
      String work = "";
      if (n > 0)
        work = work + l.substring(0, n);
      work = work + "?";
      n += placeHolder.length();
      if (n < l.length());
      work = work + l.substring(n);
      l = work;
    }
    outData.print(l);
    return phIndex;
  }
  static void generateCommand(Proc proc, PrintWriter outData)
  {
    int size = 1;
    questionsSeen = 0;
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
        size += proc.getDynamicSize(l.line);
      else
        size += question(proc, l.line).length();
    }
    outData.println("  char command[" + size + "];");
    outData.println("  memset(command, 0, sizeof(command));");
    if (proc.lines.size() > 0)
    {
      String strcat = "  strcat(command, ";
      String tail = "";
      int phIndex = 0;
      for (int i = 0; i < proc.lines.size(); i++)
      {
        Line l = (Line) proc.lines.elementAt(i);
        if (l.isVar)
        {
          tail = ");";
          if (i != 0)
            outData.println(tail);
          strcat = "  strcat(command, ";
          outData.print(strcat + l.line);
        }
        else
        {
          if (i != 0)
            outData.println(tail);
          tail = "";
          phIndex = checkPlaceHolders(proc, outData, strcat + "\"" + l.line + "\"", phIndex);
          strcat = "                  ";
        }
      }
      outData.println(");");
    }
  }
  static void generateImplementation(Table table, Proc proc, PrintWriter outData)
  {
    String fullName = table.useName() + proc.name;
    outData.println("void T" + fullName + "::Exec()");
    outData.println("{");
    generateCommand(proc, outData);
    outData.println("  qry.Open(conn, command, \"" + fullName + "\");");
    if (proc.placeHolders.size() > 0)
      for (int j = 0; j < proc.placeHolders.size(); j++)
      {
        String s = (String) proc.placeHolders.elementAt(j);
        int n = proc.indexOf(s);
        if (n < 0)
        {
          outData.println("  // " + s + " is not an input");
          continue;
        }
        Field field = (Field) proc.inputs.elementAt(n);
        if (field.type == Field.SEQUENCE && proc.isInsert)
          outData.println("  " + field.useName() + " = qry.conn->GetSequence(\"" + table.name + "\");");
        else if (field.type == Field.TIMESTAMP)
          outData.println("  " + field.useName() + " = qry.conn->GetTimeStamp();");
        else if (field.type == Field.USERSTAMP)
          outData.println("  " + field.useName() + " = qry.conn->GetUserStamp(" + field.useName() + ");");
        String sizeParm;
        if (field.type == Field.BLOB || field.type == Field.TLOB)
          sizeParm = field.useName() + "LOBLen";
        else
          sizeParm = "sizeof(" + field.useName() + ")";
        outData.print("  qry.Put(\"" + field.useName() + "_" + j + "\", " + generateCppPut(field) + ", " + sizeParm + ", qry." + generateCppType(field));
        if (field.isNull)
          outData.print(", " + field.useName() + "IsNull");
        outData.println(");");
      }
    else
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        if (field.type == Field.SEQUENCE && proc.isInsert)
          outData.println("  " + field.useName() + " = qry.conn->GetSequence(\"" + table.name + "\");");
        else if (field.type == Field.TIMESTAMP)
          outData.println("  " + field.useName() + " = qry.conn->GetTimeStamp();");
        else if (field.type == Field.USERSTAMP)
          outData.println("  " + field.useName() + " = qry.conn->GetUserStamp(" + field.useName() + ");");

        String sizeParm;
        if (field.type == Field.BLOB || field.type == Field.TLOB)
          sizeParm = field.useName() + "LOBLen";
        else
          sizeParm = "sizeof(" + field.useName() + ")";

        outData.print("  qry.Put(\"" + field.useName() + "\", " + generateCppPut(field) + ", " + sizeParm + ", qry." + generateCppType(field));
        if (field.isNull)
          outData.print(", " + field.useName() + "IsNull");
        outData.println(");");
      }
    outData.println("  qry.Exec();");
    outData.println("}");
    outData.println();
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      outData.println("void T" + fullName + "::Exec(");
      generateWithParms(proc, outData, "");
      outData.println(")");
      outData.println("{");
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = (Field) proc.inputs.elementAt(j);
        if ((field.type == Field.SEQUENCE && proc.isInsert) || (field.type == Field.IDENTITY && proc.isInsert) || field.type == Field.TIMESTAMP || field.type == Field.USERSTAMP)
          continue;
        outData.println("  " + cppCopy(field));
        if (field.type == Field.BLOB || field.type == Field.TLOB)
          outData.println("  " + field.useName() + "LOBLen = a" + field.useName() + "LOBLen;");
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = (String) proc.dynamics.elementAt(j);
        outData.println("  strncpy(" + s + ", a" + s + ", sizeof(" + s + "));");
      }
      outData.println("  Exec();");
      outData.println("}");
      outData.println();
    }
    if (proc.outputs.size() > 0)
    {
      outData.println("bool T" + fullName + "::Fetch()");
      outData.println("{");
      outData.println("  if (qry.EndOfFile() == true)");
      outData.println("    return false;");
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = (Field) proc.outputs.elementAt(j);

        if (field.type == Field.BLOB || field.type == Field.TLOB)
        {
          outData.println("  qry.GetLob(" + field.useName() + ", &" + field.useName() + "LOBLen, sizeof(" + field.useName() + "), " + j + ");");
          continue;
        }

        if (field.isNull)
        {
          outData.println("  qry.GetNull(" + field.useName() + "IsNull, " + j + ");");
          outData.println("  if(" + field.useName() + "IsNull == 1)");
          outData.print("  ");
        }
        outData.println("  qry.Get(" + generateCppPut(field) + ", sizeof(" + field.useName() + "), " + j + ", qry." + generateCppType(field) + ");");
      }
      outData.println("  qry.Next();");
      outData.println("  return true;");
      outData.println("}");
      outData.println();
    }
  }
  static void generateWithParms(Proc proc, PrintWriter outData, String pad)
  {
    String comma = "  ";
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = (Field) proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE && proc.isInsert) || (field.type == Field.IDENTITY && proc.isInsert) || field.type == Field.TIMESTAMP || field.type == Field.USERSTAMP)
        continue;
      outData.println(pad + comma + "const " + cppParm(field));
      comma = ", ";
      if (field.type == Field.BLOB || field.type == Field.TLOB)
        outData.println(pad + comma + "const long   a" + field.useName() + "LOBLen");
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = (String) proc.dynamics.elementAt(j);
      outData.println(pad + comma + "const char*   a" + s);
      comma = ", ";
    }
  }
  static String cppLength(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "sizeof(short)";
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "sizeof(int)";
    case Field.LONG:
      return "sizeof(long)";
    case Field.CHAR:
    case Field.ANSICHAR:
      return "" + (field.length + 1);
    case Field.XML:
      return "" + (field.length + 1);
    case Field.USERSTAMP:
      return "9";
    case Field.BLOB:
    case Field.TLOB:
      return "" + (field.length + 1);
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "8";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "sizeof(double)";
    case Field.UID:
      return "16";
    }
    return "0";
  }
  static String cppVar(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "short  " + field.useName();
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int    " + field.useName();
    case Field.LONG:
      return "long   " + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.XML:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.USERSTAMP:
      return "char   " + field.useName() + "[9]";
    case Field.BLOB:
    case Field.TLOB:
      return "char   " + field.useName() + "[" + (field.length + 1) + "]";
    case Field.DATE:
      return "char   " + field.useName() + "[9]";
    case Field.TIME:
      return "char   " + field.useName() + "[7]";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char   " + field.useName() + "[15]";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double " + field.useName();
    case Field.UID:
      return "unsigned char " + field.useName() + "[16]";
    }
    return field.useName() + " <unsupported>";
  }
  static boolean blobCheck(Vector<Field> fields)
  {
    for (int i = 0; i < fields.size(); i++)
    {
      Field f = (Field) fields.elementAt(i);
      if (f.type == Field.BLOB || f.type == Field.TLOB)
        return true;
    }
    return false;
  }
  static String cppParm(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return "short  a" + field.useName();
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return "int    a" + field.useName();
    case Field.LONG:
      return "long   a" + field.useName();
    case Field.CHAR:
    case Field.ANSICHAR:
      return "char*  a" + field.useName();
    case Field.XML:
      return "char*  a" + field.useName();
    case Field.USERSTAMP:
      return "char*  a" + field.useName();
    case Field.BLOB:
    case Field.TLOB:
      return "char*  a" + field.useName();
    case Field.DATE:
      return "char*  a" + field.useName();
    case Field.TIME:
      return "char*  a" + field.useName();
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "char*  a" + field.useName();
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return "double a" + field.useName();
    case Field.UID:
      return "unsigned char* a" + field.useName();
    }
    return field.useName() + " <unsupported>";
  }
  static String xmlOf(Field field) ////
  {
    String result = "";
    if (field.isNull)
      result = "if (" + field.useName() + "IsNull == 0)\r\n      {";
    result = result + "XRec.append(\"<" + field.useName().toUpperCase() + ">\");";
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.SEQUENCE:
    case Field.IDENTITY:
    case Field.LONG:
      result = result + "{char Work[32]; sprintf(Work, \"%d\", " + field.useName() + ");";
      result = result + "XRec.append(Work);}";
      break;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.BLOB:
    case Field.TLOB:
      result = result + "XRec.ampappend(" + field.useName() + ");";
      break;
    case Field.USERSTAMP:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      result = result + "XRec.append(" + field.useName() + ");";
      break;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      result = result + "{char Work[32]; sprintf(Work, \"%f\", " + field.useName() + ");";
      result = result + "XRec.append(Work);}";
      break;
    case Field.UID:
      result = result + "{char Work[37]; for (inti=0;i<16;i++) sprintf(&Work[i*2+(i>9?4:(i>7?3:(i>5?2:(i>3?1:0))))], \"%02X\", " + field.useName() + "[i]);";
      result = result + "Work[8]=Work[13]=Work[18]=Work[23]='-';Work[36]=0;XRec.append(Work);}";
    }
    result = result + "XRec.append(\"</" + field.useName().toUpperCase() + ">\");";
    if (field.isNull)
      result = result + "}\r\n    else\r\n      XRec.append(\"<" + field.useName().toUpperCase() + "/>\");";
    return result;
  }
  static String cppCopy(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
    case Field.INT:
    case Field.LONG:
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
    case Field.SEQUENCE:
      return field.useName() + " = a" + field.useName() + ";";
    case Field.CHAR:
    case Field.XML:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
      return "strncpy(" + field.useName() + ", a" + field.useName() + ", sizeof(" + field.useName() + "));";
    case Field.ANSICHAR:
    case Field.UID:
      return "memcpy(" + field.useName() + ", a" + field.useName() + ", sizeof(" + field.useName() + "));";
    case Field.BLOB:
    case Field.TLOB:
      return "memcpy(" + field.useName() + ", a" + field.useName() + ", a" + field.useName() + "LOBLen);";
    case Field.USERSTAMP:
    case Field.IDENTITY:
    case Field.TIMESTAMP:
      return "// " + field.useName() + " -- generated";
    }
    return field.useName() + " <unsupported>";
  }
  static String generateCppType(Field field)
  {
    switch (field.type)
    {
    case Field.BLOB:
      return "BLOB";
    case Field.BOOLEAN:
      return "BOOLEAN";
    case Field.BYTE:
      return "BYTE";
    case Field.CHAR:
      return "CHAR";
    case Field.XML:
      return "XML";
    case Field.DATE:
      return "DATE";
    case Field.DATETIME:
      return "DATETIME";
    case Field.DOUBLE:
      return "DOUBLE";
    case Field.DYNAMIC:
      return "DYNAMIC";
    case Field.FLOAT:
      return "FLOAT";
    case Field.IDENTITY:
      return "IDENTITY";
    case Field.INT:
      return "INT";
    case Field.LONG:
      return "LONG";
    case Field.MONEY:
      return "MONEY";
    case Field.SEQUENCE:
      return "SEQUENCE";
    case Field.SHORT:
      return "SHORT";
    case Field.STATUS:
      return "STATUS";
    case Field.TIME:
      return "TIME";
    case Field.TIMESTAMP:
      return "TIMESTAMP";
    case Field.TLOB:
      return "TLOB";
    case Field.USERSTAMP:
      return "USERSTAMP";
    case Field.ANSICHAR:
      return "ANSICHAR";
    case Field.UID:
      return "UNIQUEIDENTIFIER";
    }
    return "UNKNOWN";
  }
  static String generateCppPut(Field field)
  {
    switch (field.type)
    {
    case Field.BLOB:
      return field.useName();
    case Field.BOOLEAN:
      return "&" + field.useName();
    case Field.BYTE:
      return "&" + field.useName();
    case Field.CHAR:
      return field.useName();
    case Field.XML:
      return field.useName();
    case Field.DATE:
      return field.useName();
    case Field.DATETIME:
      return field.useName();
    case Field.DOUBLE:
      return "&" + field.useName();
    case Field.FLOAT:
      return "&" + field.useName();
    case Field.IDENTITY:
      return "&" + field.useName();
    case Field.INT:
      return "&" + field.useName();
    case Field.LONG:
      return "&" + field.useName();
    case Field.MONEY:
      return "&" + field.useName();
    case Field.SEQUENCE:
      return "&" + field.useName();
    case Field.SHORT:
      return "&" + field.useName();
    case Field.TIME:
      return field.useName();
    case Field.TIMESTAMP:
      return field.useName();
    case Field.TLOB:
      return field.useName();
    case Field.USERSTAMP:
      return field.useName();
    case Field.ANSICHAR:
      return field.useName();
    case Field.UID:
      return field.useName();
    }
    return "UNKNOWN";
  }
  static void generateVB6(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName() + ".bas");
      OutputStream outFile = new FileOutputStream(output + table.useName() + ".bas");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          generateVB6Structs(table, outData);
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
  public static void generateVB6Structs(Table table, PrintWriter outData)
  {
    outData.println("ATTRIBUTE VB_NAME=\"" + table.name + "\"");
    outData.println("Option explicit");
    outData.println("' This code was generated, do not modify it, modify it at source and regenerate it.");
    for (int i = 0; i < table.comments.size(); i++)
    {
      String s = (String) table.comments.elementAt(i);
      outData.println("'" + s);
    }
    outData.println();
    outData.println("Public Type D" + table.name);
    Padder pad = new Padder();
    for (int i = 0; i < table.fields.size(); i++)
    {
      Field field = (Field) table.fields.elementAt(i);
      pad.padVB(paddingSize(field), relativeSize(field), outData);
      outData.println("  " + varType(field));
      if (field.comments.size() > 0)
        for (int c = 0; c < field.comments.size(); c++)
        {
          String s = (String) field.comments.elementAt(c);
          outData.println("  '" + s);
        }
      if (field.type == Field.BLOB || field.type == Field.TLOB)
      {
        pad.padVB(4, 4, outData);
        outData.println("  " + field.useName() + "LOBLen As Long");
      }
      if (field.isNull)
      {
        pad.padVB(4, 2, outData);
        outData.println("  " + field.useName() + "IsNull As Integer");
      }
    }
    pad.padVB(8, 0, outData);
    outData.println("End Type");
    outData.println();
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = (Proc) table.procs.elementAt(i);
      if (proc.isData || proc.isStd || proc.hasNoData())
        continue;
      if (proc.outputs.size() > 0)
      {
        Padder p2 = new Padder();
        for (int j = 0; j < proc.comments.size(); j++)
        {
          String comment = (String) proc.comments.elementAt(j);
          outData.println("'" + comment);
        }
        String typeChar = "D";
        if (proc.hasDiscreteInput() || proc.inputs.size() == 0)
          typeChar = "O";
        outData.println("Public Type " + typeChar + table.useName() + proc.upperFirst());
        for (int j = 0; j < proc.outputs.size(); j++)
        {
          Field field = (Field) proc.outputs.elementAt(j);
          p2.padVB(paddingSize(field), relativeSize(field), outData);
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  '" + s);
          }
          outData.println("  " + varType(field));
          if (field.type == Field.BLOB || field.type == Field.TLOB)
          {
            pad.padVB(4, 4, outData);
            outData.println("  " + field.useName() + "LOBLen As Long");
          }
          if (field.isNull)
          {
            p2.padVB(4, 2, outData);
            outData.println("  " + field.useName() + "IsNull As Integer");
          }
        }
        p2.padVB(8, 0, outData);
        outData.println("End Type");
        outData.println();
      }
      if (proc.hasDiscreteInput())
      {
        Padder p2 = new Padder();
        outData.println("Public Type I" + table.useName() + proc.upperFirst());
        for (int j = 0; j < proc.inputs.size(); j++)
        {
          Field field = (Field) proc.inputs.elementAt(j);
          p2.padVB(paddingSize(field), relativeSize(field), outData);
          for (int c = 0; c < field.comments.size(); c++)
          {
            String s = (String) field.comments.elementAt(c);
            outData.println("  '" + s);
          }
          outData.println("  " + varType(field));
          if (field.type == Field.BLOB || field.type == Field.TLOB)
          {
            pad.padVB(4, 4, outData);
            outData.println("  " + field.useName() + "LOBLen As Long");
          }
          if (field.isNull)
          {
            p2.padVB(4, 2, outData);
            outData.println("  " + field.useName() + "IsNull As Integer");
          }
        }
        for (int j = 0; j < proc.dynamics.size(); j++)
        {
          String s = (String) proc.dynamics.elementAt(j);
          Integer ds = (Integer) proc.dynamicSizes.elementAt(j);
          outData.println("  " + s + " As String * " + ds);
          p2.incOffset(ds.intValue());
        }
        p2.padVB(8, 0, outData);
        outData.println("End Type");
        outData.println();
      }
    }
  }
  static String varType(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return field.useName() + " As Integer";
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return field.useName() + " As Long";
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.XML:
      return field.useName() + " As String * " + (field.length + 1);
    case Field.USERSTAMP:
      return field.useName() + " As String * 9";
    case Field.TLOB:
    case Field.BLOB:
      return field.useName() + " As String * " + (field.length + 1);
    case Field.DATE:
      return field.useName() + " As String * 9";
    case Field.TIME:
      return field.useName() + " As String * 7";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return field.useName() + " As String * 15";
    case Field.UID:
      return field.useName() + " As String * 17";
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
      return field.useName() + " As Double";
    }
    return "As unsupported";
  }
  static int relativeSize(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.XML:
      return (field.length + 1) % 8;
    case Field.USERSTAMP:
      return 1;
    case Field.TLOB:
    case Field.BLOB:
      return (field.length + 1) % 8;
    case Field.DATE:
      return 1;
    case Field.TIME:
      return 7;
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return 7;
    case Field.FLOAT:
    case Field.DOUBLE:
    case Field.MONEY:
    case Field.UID:
      return 8;
    }
    return 1;
  }
  static int paddingSize(Field field)
  {
    switch (field.type)
    {
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
    case Field.XML:
      return 1;
    case Field.BOOLEAN:
    case Field.BYTE:
    case Field.SHORT:
      return 2;
    case Field.INT:
    case Field.LONG:
    case Field.SEQUENCE:
    case Field.IDENTITY:
      return 4;
    default:
      return 8;
    }
  }
  static boolean isChar(Field field)
  {
    switch (field.type)
    {
    case Field.XML:
    case Field.CHAR:
    case Field.ANSICHAR:
    case Field.USERSTAMP:
    case Field.TLOB:
    case Field.BLOB:
    case Field.DATE:
    case Field.TIME:
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return true;
    }
    return false;
  }
}
