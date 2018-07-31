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

import bbd.jportal2.*;


import bbd.jportal2.Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.*;
import java.util.Vector;

public class IdlCode extends BaseGenerator implements IBuiltInSIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(IdlCode.class);

    public IdlCode() {
        super(IdlCode.class);
    }

    public String description() {
        return "Generate IDL Code for 3 Tier Access";
    }

    public String documentation() {
        return "Generate IDL Code for 3 Tier Access"
                + "\r\n- \"use package\" use package name for namespace"
                + "\r\n- \"use schema\" use schema name for namespace"
                ;
    }

    boolean usePackage;
    boolean useSchema;

    void setFlags(Database database) {
        usePackage = false;
        useSchema = false;
        for (int i = 0; i < database.flags.size(); i++) {
            String flag = (String) database.flags.elementAt(i);
            if (flag.equalsIgnoreCase("use package"))
                usePackage = true;
            else if (flag.equalsIgnoreCase("use package"))
                useSchema = true;
        }
        if (usePackage == true)
            logger.info(" (use package)");
        if (useSchema == true)
            logger.info(" (use schema)");
    }

    String padder(String s, int length) {
        for (int i = s.length(); i < length - 1; i++)
            s = s + " ";
        return s + " ";
    }

    /**
     * Generates the procedure classes for each table present.
     */
    public void generate(Database database, String output) throws Exception {
        setFlags(database);
        for (int i = 0; i < database.tables.size(); i++) {
            Table table = (Table) database.tables.elementAt(i);
            generate(table, output);
            generateStructs(table, output);

        }
    }

    Vector<String> enumsGenerated;

    private boolean hasEnum(String name) {
        for (int i = 0; i < enumsGenerated.size(); i++)
            if (name.compareTo((String) enumsGenerated.elementAt(i)) == 0)
                return true;
        return false;
    }

    private void generatePragmaEnums(Field field, PrintWriter outData) {
        if (hasEnum(field.name) == false) {
            enumsGenerated.addElement(field.name);
            StringBuffer buffer = new StringBuffer();
            buffer.append("pragma si enum " + field.useName());
            for (int j = 0; j < field.enums.size(); j++) {
                Enum num = field.enums.elementAt(j);
                buffer.append(" " + num.name + " " + num.value);
            }
            outData.println(buffer.toString());
        }
    }

    private void generatePragmaValueList(Field field, PrintWriter outData) {
        if (hasEnum(field.name) == false) {
            enumsGenerated.addElement(field.name);
            StringBuffer buffer = new StringBuffer();
            buffer.append("pragma si enum " + field.useName());
            for (int j = 0; j < field.valueList.size(); j++) {
                String name = field.valueList.elementAt(j);
                buffer.append(" " + name + " " + j);
            }
            outData.println(buffer.toString());
        }
    }

    private void generatePragma(Table table, PrintWriter outData) {
        outData.println("pragma si table "
                + table.name + " "
                + table.fields.size()
        );
        for (int i = 0; i < table.keys.size(); i++) {
            Key key = table.keys.elementAt(i);
            if (key.isPrimary == true) {
                outData.print("pragma si prime");
                for (int j = 0; j < key.fields.size(); j++) {
                    String name = key.fields.elementAt(j);
                    outData.print(" " + name);
                }
                outData.println();
            }
        }
        for (int i = 0; i < table.fields.size(); i++) {
            Field field = table.fields.elementAt(i);
            String name = field.useName();
            outData.println("pragma si field "
                    + name
                    + " " + field.type
                    + " " + field.length
                    + " " + field.precision
                    + " " + field.scale
                    + " " + (field.isNull ? "opt" : "mand")
            );
            if (field.enums.size() > 0)
                generatePragmaEnums(field, outData);
            if (field.valueList.size() > 0)
                generatePragmaValueList(field, outData);
        }
    }

    private void generatePragma(Table table, Proc proc, PrintWriter outData) {
        outData.println("pragma si proc "
                + table.name + " " + proc.name
                + " " + (proc.inputs.size() + proc.outputs.size() + proc.dynamics.size())
        );
        for (int i = 0; i < proc.inputs.size(); i++) {
            Field field = proc.inputs.elementAt(i);
            String name = field.useName();
            outData.println("pragma si input "
                    + name
                    + " " + field.type
                    + " " + field.length
                    + " " + field.precision
                    + " " + field.scale
                    + " " + (field.isNull ? "opt" : "mand")
            );
            if (field.enums.size() > 0)
                generatePragmaEnums(field, outData);
            if (field.valueList.size() > 0)
                generatePragmaValueList(field, outData);
        }
        for (int i = 0; i < proc.outputs.size(); i++) {
            Field field = proc.outputs.elementAt(i);
            String name = field.useName();
            outData.println("pragma si output "
                    + name
                    + " " + field.type
                    + " " + field.length
                    + " " + field.precision
                    + " " + field.scale
                    + " " + (field.isNull ? "opt" : "mand")
            );
            if (field.enums.size() > 0)
                generatePragmaEnums(field, outData);
            if (field.valueList.size() > 0)
                generatePragmaValueList(field, outData);
        }
        for (int i = 0; i < proc.dynamics.size(); i++) {
            String name = proc.dynamics.elementAt(i);
            Integer size = proc.dynamicSizes.elementAt(i);
            outData.println("pragma si dynamic "
                    + name
                    + " " + size.intValue()
            );
        }
    }

    /**
     * Build of standard and user defined procedures
     */
    private void generate(Table table, String output) throws Exception {
        try (PrintWriter outData = this.openOutputFileForGeneration("ii", output + table.useName().toLowerCase() + ".ii")) {
            String packageName = "bbd.idl2.jportal";
            if (usePackage == true)
                packageName = table.database.packageName;
            outData.println("// This code was generated, do not modify it, modify it at source and regenerate it.");
            outData.println("pragma \"ii(" + table.useName().toLowerCase() + ":" + packageName + ")\"");
            outData.println();
            if (table.hasStdProcs) {
                outData.println("struct D" + table.useName() + " \"" + table.useName().toLowerCase() + ".sh\"");
                outData.println("struct O" + table.useName() + " \"" + table.useName().toLowerCase() + ".sh\"");
            }
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = table.procs.elementAt(i);
                if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
                    continue;
                outData.println("struct D" + table.useName() + proc.upperFirst() + " \"" + table.useName().toLowerCase() + ".sh\"");
                if (proc.outputs.size() > 0)
                    outData.println("struct O" + table.useName() + proc.upperFirst() + " \"" + table.useName().toLowerCase() + ".sh\"");
            }
            outData.println();
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = table.procs.elementAt(i);
                if (proc.isData)
                    continue;
                if (proc.options.size() > 0) {
                    for (int j = 0; j < proc.options.size(); j++) {
                        String option = proc.options.elementAt(j);
                        outData.println("pragma \"" + table.name + proc.name + ":" + option + "\"");
                    }
                }
                if (proc.isMultipleInput)
                    generateBulkAction(table, proc, outData);
                else if (proc.isInsert && proc.hasReturning)
                    generateAction(table, proc, outData);
                else if (proc.outputs.size() > 0)
                    if (proc.isSingle)
                        generateSingle(table, proc, outData);
                    else
                        generateMultiple(table, proc, outData);
                else
                    generateAction(table, proc, outData);
            }
            enumsGenerated = new Vector<String>();
            if (table.hasStdProcs)
                generatePragma(table, outData);
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = table.procs.elementAt(i);
                if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
                    continue;
                generatePragma(table, proc, outData);
            }
        }
    }

    private void generateSingle(Table table, Proc proc, PrintWriter outData) {
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
        outData.println("int32 " + table.useName() + proc.upperFirst()
                + "(D" + dataStruct + " *rec)");
        outData.println("{");
        outData.println("message: #");
        if (hasInput) {
            outData.println("input");
            outData.println("  rec;");
        }
        outData.println("output");
        outData.println("  rec;");
        outData.println("code");
        outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
        if (hasInput)
            outData.println("  q.Exec(*rec);");
        else
            outData.println("  q.Exec();");
        outData.println("  if (q.Fetch())");
        outData.println("  {");
        outData.println("    *rec = *q.DRec();");
        outData.println("    return 1;");
        outData.println("  }");
        outData.println("  return 0;");
        outData.println("endcode");
        outData.println("}");
        outData.println();
    }

    private void generateMultiple(Table table, Proc proc, PrintWriter outData) {
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
        outData.print("void " + table.useName() + proc.upperFirst() + "(");
        if (hasInput)
            outData.print("D" + dataStruct + "* inRec, ");
        outData.println("int32* noOf, O" + dataStruct + "*& outRecs)");
        outData.println("{");
        outData.println("message: #");
        if (hasInput) {
            outData.println("input");
            outData.println("  inRec;");
        }
        outData.println("output");
        outData.println("  noOf;");
        outData.println("  outRecs size(noOf);");
        outData.println("code");
        outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
        if (hasInput)
            outData.println("  q.Exec(*inRec);");
        else
            outData.println("  q.Exec();");
        outData.println("  while (q.Fetch())");
        outData.println("    AddList(outRecs, *noOf, *q.ORec(), (int32)q.NOROWS);");
        outData.println("endcode");
        outData.println("}");
        outData.println();
    }

    private boolean businessLogic(Proc proc) {
        for (int i = 0; i < proc.options.size(); i++) {
            String option = proc.options.elementAt(i);
            if (option.compareTo("BL") == 0)
                return true;
        }
        return false;
    }

    private void generateAction(Table table, Proc proc, PrintWriter outData) {
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
        outData.print("public void " + table.useName() + proc.upperFirst() + "(");
        if (hasInput || proc.hasModifieds())
            outData.print("D" + dataStruct + " *rec");
        outData.println(")");
        outData.println("{");
        outData.println("message: #");
        if (hasInput || proc.hasModifieds())
            outData.println("input rec;");
        if (proc.hasModifieds() || proc.hasReturning)
            outData.println("output rec;");
        outData.println("code");
        outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
        if (hasInput || proc.hasModifieds())
            outData.println("  q.Exec(*rec);");
        else
            outData.println("  q.Exec();");
        if (proc.hasReturning && proc.outputs.size() > 0) {
            outData.println("  if (q.Fetch())");
            outData.println("    *rec = *q.DRec();");
        } else if (proc.hasModifieds())
            outData.println("  *rec = *q.DRec();");
        outData.println("endcode");
        outData.println("}");
        outData.println();
        if (businessLogic(proc) == true) {
            outData.print("void BL" + table.useName() + proc.upperFirst() + "(");
            if (hasInput || proc.hasModifieds())
                outData.print("D" + dataStruct + " *rec");
            outData.println(")");
            outData.println("{");
            outData.println("message: #");
            if (hasInput || proc.hasModifieds())
                outData.println("input rec;");
            if (proc.hasModifieds() || proc.hasReturning)
                outData.println("output rec;");
            outData.println("code");
            outData.println("  try");
            outData.println("  {");
            outData.print("    " + table.useName() + proc.upperFirst() + "(");
            if (hasInput || proc.hasModifieds() || proc.hasReturning)
                outData.print("rec");
            outData.println(");");
            outData.println("    Commit();");
            outData.println("  }");
            outData.println("  catch(xCept ex)");
            outData.println("  {");
            outData.println("    logFile->Log(ex);");
            outData.println("    Rollback();");
            outData.println("    throw;");
            outData.println("  }");
            outData.println("endcode");
            outData.println("}");
            outData.println();
        }
    }

    private void generateBulkAction(Table table, Proc proc, PrintWriter outData) {
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        outData.print("public void " + table.useName() + proc.upperFirst() + "(");
        outData.print("int noOf, D" + dataStruct + " *recs");
        outData.println(")");
        outData.println("{");
        outData.println("message: #");
        outData.println("input noOf; recs size(noOf);");
        outData.println("code");
        outData.println("  T" + table.useName() + proc.upperFirst() + " q(*connect, JP_MARK);");
        outData.println("  q.Exec(noOf, recs);");
        outData.println("endcode");
        outData.println("}");
        outData.println();
        if (businessLogic(proc) == true) {
            outData.print("void BL" + table.useName() + proc.upperFirst() + "(");
            outData.print("int noOf, D" + dataStruct + " *recs");
            outData.println(")");
            outData.println("{");
            outData.println("message: #");
            outData.println("input noOf; recs size(noOf);");
            outData.println("code");
            outData.println("  try");
            outData.println("  {");
            outData.print("    " + table.useName() + proc.upperFirst() + "(");
            outData.print("noOf, recs");
            outData.println(");");
            outData.println("    Commit();");
            outData.println("  }");
            outData.println("  catch(xCept ex)");
            outData.println("  {");
            outData.println("    logFile->Log(ex);");
            outData.println("    Rollback();");
            outData.println("    throw;");
            outData.println("  }");
            outData.println("endcode");
            outData.println("}");
            outData.println();
        }
    }

    private void generateStructs(Table table, String output) throws Exception {
        try (PrintWriter outData = this.openOutputFileForGeneration("cs", output + table.useName() + ".cs")) {
            outData.println("using System;");
            outData.println("using bbd.idl2;");
            String packageName = "bbd.idl2.jportal";
            if (usePackage == true)
                packageName = table.database.packageName;
            if (useSchema == true && table.database.schema.length() > 0)
                outData.println("namespace " + packageName + "" + table.database.schema);
            else
                outData.println("namespace " + packageName);
            outData.println("{");
            generateStructs(table, outData);
            outData.println("}");
        }
    }

    private void generateStructs(Table table, PrintWriter outData) {
        if (table.fields.size() > 0) {
            if (table.comments.size() > 0) {
                outData.println("  /// <summary>");
                for (int i = 0; i < table.comments.size(); i++) {
                    String s = table.comments.elementAt(i);
                    outData.println("  /// " + s);
                }
                outData.println("  /// </summary>");
            }
            generateTableStructs(table.fields, table.useName(), outData);
            generateEnumOrdinals(table, outData);
        }
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = table.procs.elementAt(i);
            if (proc.isData || proc.isStd || proc.isStdExtended() || proc.hasNoData())
                continue;
            if (proc.comments.size() > 0) {
                outData.println("  /// <summary>");
                for (int j = 0; j < proc.comments.size(); j++) {
                    String s = proc.comments.elementAt(j);
                    outData.println("  /// " + s);
                }
                outData.println("  /// </summary>");
            }
            generateStructPairs(proc, table.useName() + proc.upperFirst(), outData);
        }
    }

    private void generateTableStructs(Vector<Field> fields, String mainName, PrintWriter outData) {
        boolean usePartials = true;
        outData.println("  [Serializable()]");
        outData.println("  public " + (usePartials ? "partial " : "") + "class O" + mainName);
        outData.println("  {");
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            outData.println("    " + fieldDef(field));
            if (field.isNull && isNull(field))
                outData.println("    public short " + field.useLowerName() + "IsNull;" + fillerNull());
        }
        outData.println("  }");
        outData.println("  [Serializable()]");
        outData.println("  public " + (usePartials ? "partial " : "") + "class D" + mainName + " : O" + mainName + "{}");
    }

    private void generateStructPairs(Proc proc, String mainName, PrintWriter outData) {
        boolean usePartials = true;
        boolean didO = false;
        boolean didD = false;
        String typeChar = "D";
        Vector<Field> fields = proc.outputs;
        if (fields.size() > 0) {
            if (proc.hasDiscreteInput() || (proc.inputs.size() + proc.dynamics.size()) == 0) {
                typeChar = "O";
                didO = true;
            } else
                didD = true;
            outData.println("  [Serializable()]");
            outData.println("  public " + (usePartials ? "partial " : "") + "class " + typeChar + mainName);
            outData.println("  {");
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.elementAt(i);
                outData.println("    " + fieldDef(field));
                if (field.isNull && isNull(field))
                    outData.println("    public short " + field.useLowerName() + "IsNull;" + fillerNull());
            }
            outData.println("  }");
        }
        if (proc.hasDiscreteInput()) {
            outData.println("  [Serializable()]");
            if (proc.outputs.size() > 0)
                outData.println("  public " + (usePartials ? "partial " : "") + "class D" + mainName + " : O" + mainName);
            else
                outData.println("  public " + (usePartials ? "partial " : "") + "class D" + mainName);
            didD = true;
            outData.println("  {");
            Vector<Field> inputs = proc.inputs;
            for (int j = 0; j < inputs.size(); j++) {
                Field field = inputs.elementAt(j);
                if (proc.hasOutput(field.name))
                    continue;
                outData.println("    " + fieldDef(field));
                if (field.isNull && isNull(field))
                    outData.println("    public short " + field.useLowerName() + "IsNull;" + fillerNull());
            }
            for (int j = 0; j < proc.dynamics.size(); j++) {
                String s = proc.dynamics.elementAt(j);
                Integer n = proc.dynamicSizes.elementAt(j);
                Field field = new Field();
                field.name = s;
                field.type = Field.CHAR;
                field.length = n.intValue();
                outData.println("    " + fieldDef(field));
            }
            outData.println("  }");
        }
        if (didD == false && didO == true) {
            outData.println("  [Serializable()]");
            outData.println("  public " + (usePartials ? "partial " : "") + "class D" + mainName + " : O" + mainName + " {}");
        }
        if (didD == true && didO == false) {
            outData.println("  [Serializable()]");
            outData.println("  public " + (usePartials ? "partial " : "") + "class O" + mainName + " : D" + mainName + " {}");
        }
    }

    private void generateEnumOrdinals(Table table, PrintWriter outData) {
        for (int i = 0; i < table.fields.size(); i++) {
            Field field = table.fields.elementAt(i);
            if (field.enums.size() > 0) {
                outData.println("  public class " + table.useName() + field.useUpperName() + "Ord");
                outData.println("  {");
                String datatype = "Int32";
                if (field.type == Field.ANSICHAR && field.length == 1)
                    datatype = "string";
                for (int j = 0; j < field.enums.size(); j++) {
                    Enum en = field.enums.elementAt(j);
                    String evalue = "" + en.value;
                    if (field.type == Field.ANSICHAR && field.length == 1)
                        evalue = "\"" + (char) en.value + "\"";
                    outData.println("    public const " + datatype + " " + en.name + " = " + evalue + ";");
                }
                outData.println("    public static string ToString(" + datatype + " ordinal)");
                outData.println("    {");
                outData.println("      switch (ordinal)");
                outData.println("      {");
                for (int j = 0; j < field.enums.size(); j++) {
                    Enum en = field.enums.elementAt(j);
                    String evalue = "" + en.value;
                    if (field.type == Field.ANSICHAR && field.length == 1)
                        evalue = "\"" + (char) en.value + "\"";
                    outData.println("      case " + evalue + ": return \"" + en.name + "\";");
                }
                outData.println("      }");
                outData.println("      return \"unknown ordinal: \"+ordinal;");
                outData.println("    }");
                outData.println("  }");
            } else if (field.valueList.size() > 0) {
                outData.println("  public class " + table.useName() + field.useUpperName() + "Ord");
                outData.println("  {");
                for (int j = 0; j < field.valueList.size(); j++) {
                    String en = field.valueList.elementAt(j);
                    outData.println("    public const int " + en + " = " + j + ";");
                }
                outData.println("    public static string ToString(int ordinal)");
                outData.println("    {");
                outData.println("      switch (ordinal)");
                outData.println("      {");
                for (int j = 0; j < field.valueList.size(); j++) {
                    String en = field.valueList.elementAt(j);
                    outData.println("      case " + j + ": return \"" + en + "\";");
                }
                outData.println("      }");
                outData.println("      return \"unknown ordinal: \"+ordinal;");
                outData.println("    }");
                outData.println("  }");
            }
        }
    }

    private String fieldDef(Field field) {
        String result;
        String front = "";
        String newer = "";
        switch (field.type) {
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
                front = "public int _" + field.useLowerName() + "_dataLen; ";
                result = "byte[]";
                newer = " = new byte[" + (field.length - 4) + "]";
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
        return front
                + rpcAttributes(field)
                + "public " + result + " " + field.useLowerName() + newer + ";"
                + filler(field)
                + attributes(field)
                + " public " + result + " " + field.useUpperName()
                + " { get { return this." + field.useLowerName() + ";}"
                + " set { this." + field.useLowerName() + " = value; } }";
    }

    private int fillerno = 0;

    private String filler(Field field) {
        int l = field.length;
        if (field.type == Field.DOUBLE && field.precision > 15)
            l = field.precision + 3;
        else if (field.type == Field.MONEY)
            l = 21;
        else if (isStringOrDate(field))
            l++;
        else if (field.type == Field.BYTE || field.type == Field.STATUS)
            l = 2;
        int n = (8 - (l % 8)) % 8;
        if (n != 0) {
            fillerno++;
            return "[Field(Size=" + n + ")] protected byte[] _f_" + fillerno + " = new byte[" + n + "]; ";
        }
        return "";
    }

    private String fillerNull() {
        int n = 6;
        fillerno++;
        return "[Field(Size=" + n + ")] protected byte[] _f_" + fillerno + " = new byte[" + n + "]; ";
    }

    private boolean isStringOrDate(Field field) {
        switch (field.type) {
            case Field.ANSICHAR:
            case Field.CHAR:
            case Field.MONEY:
            case Field.TLOB:
            case Field.XML:
            case Field.USERSTAMP:
            case Field.DATE:
            case Field.DATETIME:
            case Field.TIME:
            case Field.TIMESTAMP:
                return true;
            case Field.DOUBLE:
                return field.precision > 15;
            default:
                break;
        }
        return false;
    }

    private boolean isNull(Field field) {
        switch (field.type) {
            case Field.ANSICHAR:
            case Field.CHAR:
            case Field.XML:
            case Field.TLOB:
            case Field.USERSTAMP:
                return false;
            default:
                break;
        }
        return true;
    }

    private String rpcAttributes(Field field) {
        int l = 0;
        if (field.type == Field.MONEY)
            l = 21;
        else if (field.type == Field.DOUBLE && field.precision > 15)
            l = field.precision + 3;
        else if (isStringOrDate(field))
            l = field.length + 1;
        if (l != 0)
            return "[Field(Size=" + (l) + ")] ";
        if (field.type == Field.BLOB)
            return "[Field(Size=" + (field.length - 4) + ")] ";
        else
            return "";
    }

    private String attributes(Field field) {
        String result = "";
        boolean continued = false;
        for (int i = 0; i < field.comments.size(); i++) {
            String comment = field.comments.elementAt(i);
            int last = comment.length() - 1;
            if (continued == true || comment.charAt(0) == '[') {
                result += " " + comment;
                continued = comment.charAt(last) == ',';
            }
        }
        return result;
    }
}
