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
import java.io.PrintWriter;
import java.util.Vector;

import static bbd.jportal2.generators.TJCStructs.*;
import static bbd.jportal2.generators.Writer.*;

public class OciCCode extends BaseGenerator implements IBuiltInSIProcessor
{
  private static final Logger logger = LoggerFactory.getLogger(OciCCode.class);

  public OciCCode()
  {
    super(OciCCode.class);
  }

  public String description()
  {
    return "Generate OCI C++ Code";
  }

  public String documentation()
  {
    return "Generate OCI C++ Code";
  }

  static private PlaceHolder placeHolder;
  static private byte paramStyle = PlaceHolder.COLON;

  /**
   * Generates the procedure classes for each table present.
   */
  public void generate(Database database, String output) throws Exception
  {
    setFlags(database);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = database.tables.elementAt(i);
      generate(table, output);
      generateSnips(table, output, true);
    }
  }

  static protected Vector<Flag> flagsVector;
  static boolean aix;
  static boolean lowercase;
  static boolean xmlValue;

  static private void flagDefaults()
  {
    aix = false;
    lowercase = false;
    xmlValue = false;
  }

  static public Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<>();
      flagDefaults();
      flagsVector.addElement(new Flag("aix", aix,
              "Generate for AIX"));
      flagsVector.addElement(new Flag("lowercase", lowercase,
              "Generate lowercase"));
      flagsVector.addElement(new Flag("xmlValue", xmlValue,
              "Generate lowercase"));
    }
    return flagsVector;
  }

  static private void setFlags(Database database)
  {
    if (flagsVector != null)
    {
      aix = toBoolean((flagsVector.elementAt(0)));
      lowercase = toBoolean(( flagsVector.elementAt(1)));
    } else
      flagDefaults();
    for (int i = 0; i < database.flags.size(); i++)
    {
      String flag = database.flags.elementAt(i);
      if (flag.equalsIgnoreCase("lowercase"))
        lowercase = true;
      else if (flag.equalsIgnoreCase("aix"))
        aix = true;
      else if (flag.equalsIgnoreCase("xmlValue"))
        xmlValue = true;
    }
    if (xmlValue)
      logger.info(" (xmlValue)");
    if (lowercase)
      logger.info(" (lowercase)");
    if (aix)
      logger.info(" (aix)");
  }

  static private String fileName(String output, String node, String ext)
  {
    int p = output.indexOf('\\');
    if (lowercase == true)
      node = node.toLowerCase();
    return output + node + ext;
  }

  static private void generate(Table table, String output) throws Exception
  {
    try (PrintWriter outData = new PrintWriter(new FileOutputStream(fileName(output, table.useName().toLowerCase(), ".sh"))))
    {
      writer = outData;
      indent_size = 4;
      writeln("// This code was generated, do not modify it, modify it at source and regenerate it.");
      writeln("#ifndef _" + table.useName().toLowerCase() + "SH");
      writeln("#define _" + table.useName().toLowerCase() + "SH");
      writeln();
      writeln("#include <stddef.h>");
      writeln("#include \"padgen.h\"");
      writeln("#include \"ociapi.h\"");
      writeln();
      if (table.hasStdProcs)
        generateStdOutputRec(table);
      generateUserOutputRecs(table);
      generateInterface(table);
      writeln("#endif");
      writer.flush();
    }
    try (PrintWriter outData2 = new PrintWriter(new FileOutputStream(fileName(output, table.useName().toLowerCase(), ".cpp"))))
    {
      writer = new PrintWriter(outData2);
      indent_size = 4;
      writeln("// This code was generated, do not modify it, modify it at source and regenerate it.");
      writeln();
      writeln("#include \"" + fileName("", table.useName().toLowerCase(), ".sh") + "\"");
      writeln();
      generateImplementation(table);
      writer.flush();
    }
  }

  /**
   * Build of output data rec for standard procedures
   */
  static private void generateInterface(Table table)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = table.procs.elementAt(i);
      if (proc.isData)
        continue;
      generateInterface(table, proc);
    }
  }

  /**
   * Emits class method for processing the database activity
   */
  static private void generateInterface(Table table, Proc proc)
  {
    String dataStruct;
    if (proc.comments.size() > 0)
    {
      for (int i = 0; i < proc.comments.size(); i++)
      {
        String comment = proc.comments.elementAt(i);
        writeln(1, "//" + comment);
      }
    }
    if (proc.hasNoData())
    {
      writeln("struct T" + table.useName() + proc.upperFirst());
      writeln("{");
      writeln(1, "TJQuery q_;");
      writeln(1, "void Exec();");
      writeln(1, "T" + table.useName() + proc.upperFirst()
              + "(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
      writeln(1, ": q_(conn)");
      writeln(1, "{q_.FileAndLine(aFile,aLine);}");
      writeln("};");
      writeln();
    } else
    {
      if (proc.isStd || proc.isStdExtended())
        dataStruct = "D" + table.useName();
      else
        dataStruct = "D" + table.useName() + proc.upperFirst();
      writeln("struct T" + table.useName() + proc.upperFirst()
              + " : public " + dataStruct);
      writeln("{");
      if (proc.isMultipleInput)
        generateArrayInterface(table, proc, dataStruct);
      else
        generateInterface(table, proc, dataStruct);
      writeln("};");
      writeln();
    }
  }

  static private void generateArrayInterface(Table table, Proc proc, String dataStruct)
  {
    writeln(1, "enum");
    Field field = proc.inputs.elementAt(0);
    String thisOne = field.useName().toUpperCase() + "_OFFSET";
    String lastOne = thisOne;
    String lastSize = cppLength(field);
    writeln(1, "{ " + padder(thisOne, 24) + "= 0");
    for (int j = 1; j < proc.inputs.size(); j++)
    {
      field = proc.inputs.elementAt(j);
      thisOne = field.useName().toUpperCase() + "_OFFSET";
      writeln(1, ", " + padder(thisOne, 24) + "= (" + lastOne + "+"
              + lastSize + ")");
      lastOne = thisOne;
      lastSize = cppLength(field);
    }
    writeln(1, ", " + padder("ROWSIZE", 24) + "= (" + lastOne + "+"
            + lastSize + ")");
    if (proc.noRows > 0)
      writeln(1, ", " + padder("NOROWS", 24) + "= " + proc.noRows);
    else
      writeln(1, ", " + padder("NOROWS", 24) + "= (24576 / ROWSIZE) + 1");
    writeln(1, ", " + padder("NOBINDS", 24) + "= " + proc.inputs.size());
    field = proc.inputs.elementAt(0);
    thisOne = field.useName().toUpperCase();
    writeln(1, ", " + padder(thisOne + "_POS", 24) + "= 0");
    for (int j = 1; j < proc.inputs.size(); j++)
    {
      field = proc.inputs.elementAt(j);
      thisOne = field.useName().toUpperCase();
      writeln(1, ", " + padder(thisOne + "_POS", 24) + "= "
              + padder(thisOne + "_OFFSET", 24) + "* NOROWS");
    }
    writeln(1, "};");
    writeln(1, "TJQuery q_;");
    writeln(1, "void Init(int Commit=1); // Commit after each block inserted");
    writeln(1, "void Fill();");
    if ((proc.inputs.size() > 0) || proc.dynamics.size() > 0)
    {
      writeln(1, "void Fill(" + dataStruct
              + "& Rec) {*DRec() = Rec;Fill();}");
      writeln(1, "void Fill(");
      generateWithArrayParms(proc, "  ");
      writeln(1, ");");
    }
    writeln(1, "void Done();");
    writeln(1, "T" + table.useName() + proc.upperFirst()
            + "(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
    writeln(1, ": q_(conn)");
    writeln(1, "{Clear();q_.FileAndLine(aFile,aLine);}");
    writeln(1, "" + dataStruct + "* DRec() {return this;}");
  }

  static private void generateInterface(Table table, Proc proc, String dataStruct)
  {
    if (proc.outputs.size() > 0)
      writeln(1, "enum");
    String front = "  { ";
    if (proc.outputs.size() > 0)
    {
      Field field = proc.outputs.elementAt(0);
      String thisOne = field.useName().toUpperCase() + "_OFFSET";
      String lastOne = thisOne;
      String lastSize = cppLength(field);
      writeln(front + padder(thisOne, 24) + "= 0");
      front = "  , ";
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase() + "_OFFSET";
        writeln(1, ", " + padder(thisOne, 24) + "= (" + lastOne + "+"
                + lastSize + ")");
        lastOne = thisOne;
        lastSize = cppLength(field);
      }
      writeln(1, ", " + padder("ROWSIZE", 24) + "= (" + lastOne + "+"
              + lastSize + ")");
      if (proc.isSingle)
        writeln(1, ", " + padder("NOROWS", 24) + "= 1");
      else if (proc.noRows > 0)
        writeln(1, ", " + padder("NOROWS", 24) + "= " + proc.noRows);
      else
        writeln(1, ", " + padder("NOROWS", 24)
                + "= (24576 / ROWSIZE) + 1");
      writeln(1, ", " + padder("NOBINDS", 24) + "= "
              + proc.inputs.size());
      writeln(1, ", " + padder("NODEFINES", 24) + "= "
              + proc.outputs.size());
      field = proc.outputs.elementAt(0);
      thisOne = field.useName().toUpperCase();
      writeln(1, ", " + padder(thisOne + "_POS", 24) + "= 0");
      for (int j = 1; j < proc.outputs.size(); j++)
      {
        field = proc.outputs.elementAt(j);
        thisOne = field.useName().toUpperCase();
        writeln(1, ", " + padder(thisOne + "_POS", 24) + "= "
                + padder(thisOne + "_OFFSET", 24) + "* NOROWS");
      }
    }
    if (proc.outputs.size() > 0)
      writeln(1, "};");
    writeln(1, "TJQuery q_;");
    writeln(1, "void Exec();");
    boolean hasBlob = false;
    for (int i = 0; i < proc.inputs.size(); i++)
    {
      Field field = proc.inputs.elementAt(i);
      if (field.type == Field.BLOB)
      {
        hasBlob = true;
        break;
      }
    }
      writeln(1, "void Exec(" + dataStruct
              + "& Rec) {*DRec() = Rec;Exec();}");
    if (hasBlob == false && (proc.inputs.size() > 0 || proc.dynamics.size() > 0))
    {
      writeln(1, "void Exec(");
      generateWithParms(proc, "  ");
      writeln(1, ");");
    }
    if (proc.outputs.size() > 0)
      writeln(1, "bool Fetch();");
    writeln(1, "T" + table.useName() + proc.upperFirst()
            + "(TJConnector &conn, char *aFile=__FILE__, long aLine=__LINE__)");
    writeln(1, ": q_(conn)");
    writeln(1, "{Clear();q_.FileAndLine(aFile,aLine);}");
    writeln(1, "" + dataStruct + "* DRec() {return this;}");
    if (proc.outputs.size() > 0)
      writeln(1, "O" + dataStruct.substring(1)
              + "* ORec() {return this;}");
  }

  /**
   *
   */
  static private void generateImplementation(Table table)
  {
    for (int i = 0; i < table.procs.size(); i++)
    {
      Proc proc = table.procs.elementAt(i);
      if (proc.isData)
        continue;
      if (proc.isMultipleInput)
        generateArrayImplementation(table, proc);
      else
        generateImplementation(table, proc);
    }
  }

  static private void generateCommand(Proc proc)
  {
    boolean isReturning = false;
    boolean isBulkSequence = false;
    String fieldName = "";
    String tableName = proc.table.useName();
    String serverName = proc.table.database.server;
    Vector<String> lines = placeHolder.getLines();
    if (proc.isInsert == true && proc.hasReturning == true && proc.outputs.size() == 1)
    {
      Field field = proc.outputs.elementAt(0);
      if (field.isSequence == true)
      {
        fieldName = field.useName();
        isReturning = true;
      }
    }
    if (proc.isMultipleInput == true && proc.isInsert == true)
      isBulkSequence = true;
    int size = 1;
    for (String line: lines)
      size += line.length();
    if (placeHolder.limit != null)
      size += placeHolder.limit.fetchRowsSize();
    writeln(1, format("size_t size = %d;", size));
    if (isReturning == true || isBulkSequence == true)
    {
      writeln(1, "TJCppRet _ret;");
      writeln(1, format("size += _ret.setup(\"%s\", \"%s\", \"%s\", \"%s\", %s);", serverName, tableName, proc.name, fieldName, isReturning ? "true" : "false"));
    }
    writeln(1, "if (q_.command != 0) delete [] q_.command;");
    writeln(1, "q_.command = new char [size];");
    writeln(1, "memset(q_.command, 0, size);");
    if (lines.size() > 0)
    {
      String terminate = "";
      String strcat = "strcat(q_.command, ";
      boolean begin = true;
      for (String line: lines)
      {
        if (line.charAt(0) != '"')
        {
          terminate = ");";
          strcat = "strcat(q_.command, ";
          if (begin == false)
            writeln(terminate);
        }
        else if (begin == false)
          writeln(terminate);
        begin = false;
        if (line.charAt(0) != '"')
          write(1, strcat + line);
        else
          write(1, strcat + line);
        if (line.charAt(0) == '"')
        {
          terminate = "\"\\n\"";
          strcat = indent(2);
        }
      }
      writeln(");");
      if (placeHolder.limit != null)
      {
        String[] code = placeHolder.limit.fetchRowsLines();
        for (String line: code)
          writeln(line);
      }
    }
  }

  /**
   * Emits class method for processing the database activity
   */
  static private void generateArrayImplementation(Table table, Proc proc)
  {
    String fullName = table.useName() + proc.name;
    writeln("void T" + fullName + "::Init(int Commit)");
    writeln("{");
    generateCommand(proc);
    writeln(1, "q_.OpenArray(q_.command, NOBINDS, NOROWS, ROWSIZE);");
    writeln(1, "q_.SetCommit(Commit);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      writeln(1, "q_.BindArray("
              + padder("\":" + field.name + "\",", 24) + padder("" + j + ",", 4)
              + cppBindArray(field, table.name) + ");");
    }
    writeln("}");
    writeln();
    writeln("void T" + fullName + "::Fill()");
    writeln("{");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      if ((field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE) && proc.isInsert)
        writeln(1, "q_.Sequence(" + field.useName() + ", \""
                + table.name + "Seq\");");
      if (field.type == Field.TIMESTAMP)
        writeln(1, "q_.conn.TimeStamp(" + field.useName() + ");");
      if (field.type == Field.USERSTAMP)
        writeln(1, "q_.UserStamp(" + field.useName() + ");");
      writeln(1, "q_.Put(" + cppPut(field) + ");");
      if (isNull(field))
        writeln(1, "q_.PutNull(" + field.useName() + "IsNull, " + j
                + ");");
    }
    writeln(1, "q_.Deliver(0); // 0 indicates defer doing it if not full");
    writeln("}");
    writeln();
    writeln("void T" + fullName + "::Fill(");
    generateWithArrayParms(proc, "");
    writeln(")");
    writeln("{");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      if (((field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE) && proc.isInsert)
              || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
              || field.type == Field.USERSTAMP)
        continue;
      writeln(1, "" + cppCopy(field));
    }
    writeln(1, "Fill();");
    writeln("}");
    writeln();
    writeln("void T" + fullName + "::Done()");
    writeln("{");
    writeln(1, "q_.Deliver(1); // 1 indicates doit now");
    writeln("}");
    writeln();
  }

  /**
   * Emits class method for processing the database activity
   */
  static private void generateImplementation(Table table, Proc proc)
  {
    boolean doReturning = false;
    placeHolder = new PlaceHolder(proc, paramStyle, "");
    String fullName = table.useName() + proc.name;
    writeln("void T" + fullName + "::Exec()");
    writeln("{");
    generateCommand(proc);
    if (proc.isInsert == true && proc.hasReturning == true && proc.outputs.size() == 1)
    {
      writeln(1, format("q_.Open(q_.command, %d);", proc.inputs.size() + 1));
      doReturning = true;
    } else if (proc.outputs.size() > 0)
      writeln(1, "q_.Open(q_.command, NOBINDS, NODEFINES, NOROWS, ROWSIZE);");
    else if (proc.inputs.size() > 0)
      writeln(1, "q_.Open(q_.command, " + proc.inputs.size() + ");");
    else
      writeln(1, "q_.Open(q_.command);");
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      generateCppBind(field);
    }
    boolean hasBlob = false;
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      String nullField = "";
      if (isNull(field))
        nullField = format(", &%sIsNull", field.useName());
      String BLOB = "";
      if (field.type == Field.BLOB)
      {
        BLOB = "Blob";
        hasBlob = true;
      }
      writeln(1, format("q_.Bind%s(\":%s\", %d, %s%s);", BLOB, field.name, j, cppBind(field, table.name, proc.isInsert), nullField));
    }
    if (doReturning)
    {
      Field field = proc.outputs.elementAt(0);
      int pos = proc.inputs.size();
      writeln(1, format("q_.Bind(\":%s\", %d, %s);", field.name, pos, cppBind(field)));
    } else
    {
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = proc.outputs.elementAt(j);
        String BLOB = "";
        if (field.type == Field.BLOB)
          BLOB = "Blob";
        writeln(1, "q_.Define" + BLOB + "(" + padder("" + j + ",", 4)
                + cppDefine(field) + ");");
      }
    }
    writeln(1, "q_.Exec();");
    writeln("}");
    writeln();
    if (proc.inputs.size() > 0 || proc.dynamics.size() > 0)
    {
      if (hasBlob == false)
    {
      writeln("void T" + fullName + "::Exec(");
      generateWithParms(proc, "");
      writeln(")");
      writeln("{");
      for (int j = 0; j < proc.inputs.size(); j++)
      {
        Field field = proc.inputs.elementAt(j);
        if (((field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE) && proc.isInsert)
                || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
                || field.type == Field.USERSTAMP)
          continue;
        writeln(1, "" + cppCopy(field));
      }
      for (int j = 0; j < proc.dynamics.size(); j++)
      {
        String s = proc.dynamics.elementAt(j);
        writeln(1, "strncpy(" + s + ", a" + s + ", sizeof(" + s + ")-1);");
      }
      writeln(1, "Exec();");
      writeln("}");
      }
      writeln();
    }
    if (doReturning == false && proc.outputs.size() > 0)
    {
      writeln("bool T" + fullName + "::Fetch()");
      writeln("{");
      writeln(1, "if (q_.Fetch() == false)");
      writeln(2, "return false;");
      for (int j = 0; j < proc.outputs.size(); j++)
      {
        Field field = proc.outputs.elementAt(j);
        String BLOB = "";
        if (field.type == Field.BLOB)
          BLOB = "Blob";
        writeln(1, "q_.Get" + BLOB + "(" + cppGet(field) + ");");
        if (isNull(field))
          writeln(1, "q_.GetNull(" + field.useName() + "IsNull, " + j + ");");
      }
      writeln(1, "return true;");
      writeln("}");
      writeln();
    }
  }

  static private void generateWithArrayParms(Proc proc, String pad)
  {
    String comma = "  ";
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      if (((field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE) && proc.isInsert)
              || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
              || field.type == Field.USERSTAMP)
        continue;
      writeln(pad + comma + cppParm(field));
      comma = ", ";
    }
  }

  static private void generateWithParms(Proc proc, String pad)
  {
    String comma = "  ";
    for (int j = 0; j < proc.inputs.size(); j++)
    {
      Field field = proc.inputs.elementAt(j);
      if (((field.type == Field.SEQUENCE || field.type == Field.BIGSEQUENCE) && proc.isInsert)
              || field.type == Field.IDENTITY || field.type == Field.TIMESTAMP
              || field.type == Field.USERSTAMP)
        continue;
      writeln(pad + comma + cppParm(field));
      comma = ", ";
    }
    for (int j = 0; j < proc.dynamics.size(); j++)
    {
      String s = proc.dynamics.elementAt(j);
      writeln(pad + comma + "const char*   a" + s);
      comma = ", ";
    }
  }

  static private int questionsSeen;

  static private String question(Proc proc, String line)
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
      Field field = proc.inputs.elementAt(questionsSeen++);
      if (field.type == Field.IDENTITY && proc.isInsert)
        field = proc.inputs.elementAt(questionsSeen++);
      result = result + ":" + field.name;
      line = line.substring(1);
    }
    result = result + line;
    return result;
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppLength(Field field)
  {
    return switch (field.type)
    {
        // return "sizeof(bool)";
        // return "sizeof(signed char)";
      case Field.BOOLEAN, Field.BYTE, Field.SHORT -> "sizeof(int16)";
      case Field.INT, Field.SEQUENCE, Field.IDENTITY -> "sizeof(int32)";
      case Field.LONG, Field.BIGSEQUENCE, Field.BIGIDENTITY -> "sizeof(int64)";
      case Field.CHAR, Field.ANSICHAR -> "" + (field.length + 1);
      case Field.USERSTAMP -> "64";
      case Field.BLOB, Field.TLOB -> format("%d", field.length);
      case Field.DATE, Field.TIME, Field.DATETIME, Field.TIMESTAMP -> "8";
      case Field.FLOAT, Field.DOUBLE, Field.MONEY -> "sizeof(double)";
      default -> "0";
    };
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppParm(Field field)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT -> "int16  a" + field.useName();
      case Field.INT, Field.SEQUENCE, Field.IDENTITY -> "int32  a" + field.useName();
      case Field.LONG, Field.BIGSEQUENCE, Field.BIGIDENTITY -> "int64  a" + field.useName();
      case Field.CHAR, Field.ANSICHAR, Field.USERSTAMP, Field.DATE, Field.TIME, Field.DATETIME, Field.TIMESTAMP -> "const char*  a" + field.useName();
      case Field.BLOB, Field.TLOB -> format("TJBLob<%d>  a%s", field.length, field.useName());
      case Field.FLOAT, Field.DOUBLE, Field.MONEY -> "double a" + field.useName();
      default -> field.useName() + " <unsupported>";
    };
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppCopy(Field field)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT, Field.INT, Field.LONG, Field.FLOAT, Field.DOUBLE, Field.MONEY, Field.SEQUENCE, Field.BIGSEQUENCE, Field.BLOB, Field.TLOB ->
              //case Field.IMAGE:
              field.useName() + " = a" + field.useName() + ";";
      case Field.CHAR, Field.DATE, Field.TIME, Field.DATETIME -> "strncpy(" + field.useName() + ", a" + field.useName()
                + ", sizeof(" + field.useName() + ")-1);";
      case Field.ANSICHAR -> "memcpy(" + field.useName() + ", a" + field.useName()
                + ", sizeof(" + field.useName() + "));";
      case Field.USERSTAMP, Field.IDENTITY, Field.TIMESTAMP -> "// " + field.useName() + " -- generated";
      default -> field.useName() + " <unsupported>";
    };
  }

  /**
   * generate Holding variables
   */
  static private void generateCppBind(Field field)
  {
    switch (field.type)
    {
      case Field.DATE, Field.TIME, Field.DATETIME, Field.TIMESTAMP -> writeln("  TJOCIDate " + field.useName() + "_OCIDate;");
    }
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppBind(Field field)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT, Field.INT, Field.LONG, Field.FLOAT, Field.DOUBLE, Field.MONEY, Field.SEQUENCE, Field.BIGSEQUENCE -> field.useName();
      case Field.CHAR, Field.ANSICHAR -> field.useName() + ", " + (field.length + 1);
      case Field.BLOB, Field.TLOB -> "q_.LobLocator(q_.ociLobs[" + field.useName().toUpperCase()
                + "_LOB], " + field.useName() + "), " + field.useName().toUpperCase()
                + "_LOB_TYPE";
      case Field.USERSTAMP -> "q_.UserStamp(" + field.useName() + "), 51";
      case Field.DATE -> "q_.Date(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      case Field.TIME -> "q_.Time(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      case Field.DATETIME -> "q_.DateTime(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      case Field.TIMESTAMP -> "q_.TimeStamp(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      default -> field.useName() + ", <unsupported>";
    };
  }

  static private String cppBind(Field field, String tableName, boolean isInsert)
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
        return field.useName();
      case Field.SEQUENCE:
      case Field.BIGSEQUENCE:
        if (isInsert)
          return "q_.Sequence(" + field.useName() + ", \"" + tableName + "Seq\")";
        else
          return field.useName();
      case Field.CHAR:
      case Field.ANSICHAR:
        return field.useName() + ", " + (field.length + 1);
      case Field.BLOB:
      case Field.TLOB:
        return "(char*)&" + field.useName() + ", sizeof(" + field.useName() + ".data)";
      case Field.USERSTAMP:
        return "q_.UserStamp(" + field.useName() + "), 51";
      case Field.DATE:
        return "q_.Date(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      case Field.TIME:
        return "q_.Time(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      case Field.DATETIME:
        return "q_.DateTime(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
      case Field.TIMESTAMP:
        return "q_.TimeStamp(" + field.useName() + "_OCIDate, " + field.useName()
                + ")";
    }
    return field.useName() + ", <unsupported>";
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppBindArray(Field field, String tableName)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT -> "(int16*)  (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.INT, Field.SEQUENCE, Field.IDENTITY -> "(int32*)    (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.LONG, Field.BIGSEQUENCE, Field.BIGIDENTITY -> "(int64*)   (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.CHAR, Field.ANSICHAR -> "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
                + (field.length + 1);
      case Field.USERSTAMP -> "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), 51";
      case Field.BLOB, Field.TLOB -> "(char*)&" + field.useName() + ", sizeof(" + field.useName() + ".data)";
      case Field.DATE, Field.TIME, Field.DATETIME, Field.TIMESTAMP -> "(TJOCIDate*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.FLOAT, Field.DOUBLE, Field.MONEY -> "(double*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      default -> field.useName() + " <unsupported>";
    };
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppDefine(Field field)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT -> "(int16*)  (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.INT, Field.SEQUENCE, Field.IDENTITY -> "(int32*)    (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.LONG, Field.BIGSEQUENCE, Field.BIGIDENTITY -> "(int64*)   (q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.CHAR, Field.ANSICHAR -> "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), "
                + (field.length + 1);
      case Field.USERSTAMP -> "(char*)   (q_.data+" + field.useName().toUpperCase() + "_POS), 51";
      case Field.BLOB, Field.TLOB -> format("(char*) (q_.data+%s_POS), %d", field.useName().toUpperCase(), field.length);
      case Field.DATE, Field.TIME, Field.DATETIME, Field.TIMESTAMP -> "(TJOCIDate*)(q_.data+" + field.useName().toUpperCase() + "_POS)";
      case Field.FLOAT, Field.DOUBLE, Field.MONEY -> "(double*) (q_.data+" + field.useName().toUpperCase() + "_POS)";
      default -> field.useName() + " <unsupported>";
    };
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppGet(Field field)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT, Field.INT, Field.SEQUENCE, Field.IDENTITY, Field.LONG, Field.BIGSEQUENCE, Field.BIGIDENTITY, Field.FLOAT, Field.DOUBLE, Field.MONEY -> padder(field.useName() + ",", 32) + " q_.data+"
                + field.useName().toUpperCase() + "_POS";
      case Field.CHAR, Field.ANSICHAR -> padder(field.useName() + ",", 32) + " q_.data+"
                + field.useName().toUpperCase() + "_POS, " + (field.length + 1);
      case Field.USERSTAMP -> padder(field.useName() + ",", 32) + " q_.data+"
                + field.useName().toUpperCase() + "_POS, 51";
      case Field.BLOB, Field.TLOB -> format("%1$s.len, %1$s.data,  q_.data+%2$s_POS, sizeof(%1$s)", field.useName(), field.useName().toUpperCase());
      case Field.DATE -> padder("TJDate(" + field.useName() + "),", 32) + " q_.data+"
                + field.useName().toUpperCase() + "_POS";
      case Field.TIME -> padder("TJTime(" + field.useName() + "),", 32) + " q_.data+"
                + field.useName().toUpperCase() + "_POS";
      case Field.DATETIME, Field.TIMESTAMP -> padder("TJDateTime(" + field.useName() + "),", 32) + " q_.data+"
                + field.useName().toUpperCase() + "_POS";
      default -> field.useName() + " <unsupported>";
    };
  }

  /**
   * Translates field type to cpp data member type
   */
  static private String cppPut(Field field)
  {
    return switch (field.type)
    {
      case Field.BOOLEAN, Field.BYTE, Field.SHORT, Field.INT, Field.SEQUENCE, Field.IDENTITY, Field.LONG, Field.BIGSEQUENCE, Field.BIGIDENTITY, Field.FLOAT, Field.DOUBLE, Field.MONEY, Field.BLOB, Field.TLOB -> padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
                + field.useName();
      case Field.CHAR, Field.ANSICHAR -> padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
                + field.useName() + ", " + (field.length + 1);
      case Field.USERSTAMP -> padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
                + field.useName() + ", 51";
      case Field.DATE -> padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
                + "TJDate(" + field.useName() + ")";
      case Field.TIME -> padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
                + "TJTime(" + field.useName() + ")";
      case Field.DATETIME, Field.TIMESTAMP -> padder("q_.data+" + field.useName().toUpperCase() + "_POS,", 32)
                + "TJDateTime(" + field.useName() + ")";
      default -> field.useName() + " <unsupported>";
    };
  }

  static boolean isLob(Field field)
  {
    return field.type == Field.TLOB || field.type == Field.BLOB;
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
      case Field.IDENTITY:
      case Field.SEQUENCE:
      case Field.LONG:
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
