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
import java.util.Vector;

/**
 *
 */
public class PythonCliCode extends BaseGenerator implements IBuiltInSIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PythonCliCode.class);
    public PythonCliCode() {
        super(PythonCliCode.class);
    }


    public String description() {
        return "Generates CLI Python Code";
    }

    public String documentation() {
        return "Generates CLI Python Code";
    }

    protected Vector<Flag> flagsVector;
    static String pymodName;
    static String pymodFront;
    static boolean dontQualify;
    static boolean useUTF8;
    static boolean useLatin1;

    private static void flagDefaults() {
        pymodName = "";
        pymodFront = "";
        dontQualify = false;
        useUTF8 = false;
        useLatin1 = false;
    }

    public Vector<Flag> getFlags() {
        if (flagsVector == null) {
            flagDefaults();
            flagsVector = new Vector<Flag>();
            flagsVector.addElement(new Flag("use pymod", new String(pymodName), "Use pymod"));
            flagsVector.addElement(new Flag("dont qualify", Boolean.valueOf(dontQualify), "Dont Qualify"));
            flagsVector.addElement(new Flag("utf-8", Boolean.valueOf(useUTF8), "use utf-8"));
            flagsVector.addElement(new Flag("iso-8859-1", Boolean.valueOf(useLatin1), "use iso-8859-1"));
        }
        return flagsVector;
    }

    /**
     * Sets generation getFlags.
     */
    void setFlags(Database database) {
        if (flagsVector != null) {
            pymodName = (String) ((Flag) flagsVector.elementAt(0)).value;
            dontQualify = toBoolean(((Flag) flagsVector.elementAt(1)).value);
            useUTF8 = toBoolean(((Flag) flagsVector.elementAt(2)).value);
            useLatin1 = toBoolean(((Flag) flagsVector.elementAt(3)).value);
        } else
            flagDefaults();
        for (int i = 0; i < database.flags.size(); i++) {
            String flag = (String) database.flags.elementAt(i);
            if (flag.length() > 6 && flag.substring(0, 6).equalsIgnoreCase("pymod="))
                pymodName = flag.substring(6);
            else if (flag.equalsIgnoreCase("dont qualify"))
                dontQualify = true;
            else if (flag.equalsIgnoreCase("utf-8") || flag.equalsIgnoreCase("utf-8")) {
                useUTF8 = true;
                useLatin1 = false;
            } else if (flag.equalsIgnoreCase("iso-8859-1") || flag.equalsIgnoreCase("latin1")) {
                useLatin1 = true;
                useUTF8 = false;
            }
        }
        if (pymodName.length() > 0) {
            logger.info(" (pymod=" + pymodName + ")");
            pymodFront = pymodName + "";
            if (dontQualify == false)
                pymodFront += pymodName.substring(0, 1).toUpperCase() + pymodName.substring(1) + "_";
        }
        if (dontQualify == true)
            logger.info(" (dont qualify)");
        if (useUTF8 == true)
            logger.info(" (utf-8)");
        if (useLatin1 == true)
            logger.info(" (iso-8859-1)");
    }

    /**
     * Padder function
     */
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
            generateTable(table, output);
        }
    }

    /**
     * Build of standard and user defined procedures
     */
    void generateTable(Table table, String output) throws Exception {
        try (PrintWriter outData = this.openOutputFileForGeneration("py", output + "DB_" + table.useName().toUpperCase() + ".py")) {
            if (useUTF8 == true)
                outData.println("# -*- coding: utf-8 -*-");
            else if (useLatin1 == true)
                outData.println("# -*- coding: iso-8859-1 -*-");
            outData.println("# This code was generated, do not modify it, modify it at source and regenerate it.");
            outData.println("# " + table.useName().toUpperCase() + ".py");
            outData.println();
            if (pymodName.length() > 0)
                outData.println("import " + pymodName);
            outData.println();
            outData.println("from bbdcommon.idl.idl_misc import _str, _validate");
            // outData.println("_BLOB = 1;_BOOLEAN = 2;_BYTE = 3;_CHAR = 4;_DATE = 5;_DATETIME = 6");
            // outData.println("_DOUBLE = 7;_DYNAMIC = 8;_FLOAT = 9;_IDENTITY = 10;_INT = 11;_LONG = 12");
            // outData.println("_MONEY = 13;_SEQUENCE = 14;_SHORT = 15;_STATUS = 16;_TIME = 17");
            // outData.println("_TIMESTAMP = 18;_TLOB = 19;_USERSTAMP = 20;_ANSICHAR = 21;_UID = 22;_XML = 23");
            // outData.println("_BIGSEQUENCE = 24;_BIGIDENTITY = 25");
            // outData.println();
            // outData.println("# =i=n=d=e=n=t=a=t=i=o=n===b=y===f=o=u=r======");
            // outData.println("# s    : value as a string");
            // outData.println("# attr : (type, length, scale, precision)");
            // outData.println("# name : name of field for reporting");
            // outData.println("# =i=s===a===p=a=i=n==========================");
            // outData.println();
            // outData.println("def _validate(s, attr, name):");
            // outData.println("    if attr[0] in (_CHAR, _ANSICHAR, _DATE, _DATETIME, _TIME, _TIMESTAMP, _USERSTAMP, _XML):");
            // outData.println("        if len(s) > attr[1]: raise AssertionError('%s:Length exceeds %d' % (name, attr[1]))");
            // outData.println("    elif attr[0] in (_DOUBLE, _FLOAT) and attr[2] > 15:");
            // outData.println("        if len(s) > attr[2]+2: raise AssertionError('%s:Length exceeds %d' % (name, attr[2]+2))");
            // outData.println("    elif attr[0] == _MONEY:");
            // outData.println("        if len(s) > 20: raise AssertionError('%s:Length exceeds %d' % (name, 20))");
            // outData.println("    return s");
            // outData.println();
            // outData.println("def _str(s, attr, name):");
            // outData.println("    if s == None:");
            // outData.println("        return None");
            // if (useUTF8 == true) {
            //     outData.println("    elif isinstance(s, unicode):");
            //     outData.println("        fix = s.encode('utf-8')");
            //     outData.println("        return _validate(str(fix), attr, name)");
            // } else if (useLatin1 == true) {
            //     outData.println("    elif isinstance(s, unicode):");
            //     outData.println("        fix = s.encode('iso-8859-1')");
            //     outData.println("        return _validate(str(fix), attr, name)");
            // } else {
            //     outData.println("    elif isinstance(s, unicode):");
            //     outData.println("        fix = ''");
            //     outData.println("        for c in s: fix += chr(ord(c)%256)");
            //     outData.println("        return _validate(str(fix), attr, name)");
            // }

            // outData.println("    elif isinstance(s, float):");
            // outData.println("        return '%0.15g' % (s)");
            // outData.println("    return _validate(str(s), attr, name)");
            // outData.println();
            generateEnums(table, outData);
            if (table.hasStdProcs)
                generateStdOutputRec(table, outData);
            generateUserOutputRecs(table, outData);
            outData.flush();
        }
    }

    void generateDataFields(Vector<Field> fields, String superName, String className,
                            String tableName, PrintWriter outData) {
        outData.print("    __slots__ = [");
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            if (i != 0) {
                outData.println(",");
                outData.print("        ");
            }
            outData.print("'" + field.useName() + "'");
        }
        outData.println("]");
        outData.print("    __attrs__ = [");
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            if (i != 0) {
                outData.println(",");
                outData.print("        ");
            }
            outData.print("(" + field.type + ", " + field.length + ", " + field.precision + ", " + field.scale + ")");
        }
        outData.println("]");
        outData.println("    def __init__(self):");
        if (superName.length() > 0)
            outData.println("        " + superName + ".__init__(self) ## \\field see:" + superName);
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            if (isNull(field) == true)
                outData.println("        self." + field.useName() + " = None ## \\field " + field.useName() + ":" + varName(field) + " nullable");
            else
                outData.println("        self." + field.useName() + " = '' ## \\field " + field.useName() + ":" + varName(field));
        }
        outData.println("    def _fromList(self, result):");
        String no = "";
        if (superName.length() > 0) {
            outData.println("        no = " + superName + "._fromList(self, result)");
            no = "no+";
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            if (field.type == Field.LONG || field.type == Field.BIGSEQUENCE || field.type == Field.BIGIDENTITY) {
                if (isNull(field) == true)
                    outData.println("        self." + field.useName() + " = None if result[" + no + i + "] == None else int(result[" + no + i + "])");
                else
                    outData.println("        self." + field.useName() + " = int(result[" + no + i + "])");
            } else
                outData.println("        self." + field.useName() + " = result[" + no + i + "]");
        }
        outData.println("        return " + no + fields.size());
        outData.println("    def _toList(self):");
        outData.println("        names = " + className + ".__slots__");
        outData.println("        attrs = " + className + ".__attrs__");
        if (superName.length() > 0)
            outData.print("        result = " + superName + "._toList(self) + [");
        else
            outData.print("        result = [");
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            if (i != 0) {
                outData.println(",");
                outData.print("            ");
            }
            outData.print("_str(self." + field.useName() + ", attrs[" + i + "], names[" + i + "])");
        }
        outData.println("]");
        outData.println("        return result");
        outData.println("    def _display(self):");
        outData.print("        names = ");
        if (superName.length() > 0)
            outData.print(superName + ".__slots__ + ");
        outData.println(className + ".__slots__");
        outData.println("        data = self._toList()");
        outData.println("        for i in range(len(data)):");
        outData.println("            print('%s: %s' % (names[i], data[i]))");
    }

    /**
     * Build of output data rec for standard procedures
     */
    void generateStdOutputRec(Table table, PrintWriter outData) {
        for (int i = 0; i < table.comments.size(); i++) {
            String s = (String) table.comments.elementAt(i);
            outData.println("## " + s);
        }
        outData.println("## \\class D" + table.useName());
        outData.println("class D" + table.useName() + "(object):");
        outData.println("    def _make(self): return D" + table.useName() + "()");
        outData.println("    def _name(self): return ('D" + table.useName() + "','O" + table.useName() + "')");
        generateDataFields(table.fields, "", "D" + table.useName(), table.useName(), outData);
        outData.println();
        outData.println("## \\class O" + table.useName());
        outData.println("## \\field see:D" + table.useName());
        outData.println("O" + table.useName() + " = D" + table.useName());
        outData.println();
        if (pymodName.length() > 0) {
            outData.println("class " + table.useName() + "(D" + table.useName() + "):");
            outData.println("    def __init__(self): D" + table.useName() + ".__init__(self)");
            coverFunctions = new Vector<String>();
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = (Proc) table.procs.elementAt(i);
                if (proc.isData == true || (proc.isStd == false && proc.isStdExtended() == false))
                    continue;
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
            outData.println();
            for (int i = 0; i < coverFunctions.size(); i++)
                outData.println((String) coverFunctions.elementAt(i));
        }
    }

    private Vector<String> coverFunctions;

    private void generateInput(String tableName, String dataStruct, String procName, String parms, PrintWriter outData) {
        String parameters = "";
        if (parms.length() > 2)
            parameters = parms.substring(2);
        outData.println("    def _" + procName + "_dict(self, parms):");
        outData.println("        'low level call with dictionary input'");
        outData.println("        for parm in parms: setattr(self, parm, parms[parm])");
        outData.println("        return self." + procName + "()");
        outData.println("    def " + procName + "_with(self" + parms + "):");
        outData.println("        'with method - it is suggested for maintenance named parameters are used'");
        outData.println("        return self._" + procName + "_dict(vars())");
        coverFunctions.addElement("def " + tableName + "_" + procName + "(" + parameters + "):");
        coverFunctions.addElement("    'It is suggested for maintenance named parameters are used'");
        coverFunctions.addElement("    return " + dataStruct + "()._" + procName + "_dict(vars())");
        coverFunctions.addElement("");
    }

    private void generateCover(String tableName, String dataStruct, String procName, PrintWriter outData) {
        coverFunctions.addElement("def " + tableName + "_" + procName + "():");
        coverFunctions.addElement("    return " + dataStruct + "()." + procName + "()");
        coverFunctions.addElement("");
    }

    private void generateMultiple(Table table, Proc proc, PrintWriter outData) {
        String parameters = "";
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
        String firstParm = "";
        if (hasInput == true)
            firstParm = "self, ";
        outData.println("    def " + proc.name + "(self):");
        outData.println("        ''' Multiple returns count and recs");
        if (hasInput == true) {
            outData.println("        Input:");
            for (int f = 0; f < proc.inputs.size(); f++) {
                Field field = (Field) proc.inputs.elementAt(f);
                outData.println("            " + field.useName());
                parameters += ", " + field.useName() + "=" + defValue(field);
            }
            for (int f = 0; f < proc.dynamics.size(); f++) {
                String field = (String) proc.dynamics.elementAt(f);
                outData.println("            " + field);
                parameters += ", " + field + "=''";
            }
        }
        outData.println("        Output:");
        for (int f = 0; f < proc.outputs.size(); f++) {
            Field field = (Field) proc.outputs.elementAt(f);
            outData.println("            " + field.useName());
        }
        outData.println("        '''");
        outData.println("        return " + pymodFront + table.useName() + proc.upperFirst() + "(" + firstParm + "0, O" + dataStruct + "())");
        if (hasInput == true)
            generateInput(table.useName(), dataStruct, proc.name, parameters, outData);
        else
            generateCover(table.useName(), dataStruct, proc.name, outData);
    }

    private void generateSingle(Table table, Proc proc, PrintWriter outData) {
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        String parameters = "";
        boolean hasInput = (proc.inputs.size() > 0 || proc.dynamics.size() > 0);
        outData.println("    def " + proc.name + "(self):");
        outData.println("        ''' Single returns boolean and record");
        if (hasInput == true) {
            outData.println("        Input:");
            for (int f = 0; f < proc.inputs.size(); f++) {
                Field field = (Field) proc.inputs.elementAt(f);
                outData.println("            " + field.useName());
                parameters += ", " + field.useName() + "=" + defValue(field);
            }
            for (int f = 0; f < proc.dynamics.size(); f++) {
                String field = (String) proc.dynamics.elementAt(f);
                outData.println("            " + field);
                parameters += ", " + field + "=''";
            }
        }
        outData.println("        Output:");
        for (int f = 0; f < proc.outputs.size(); f++) {
            Field field = (Field) proc.outputs.elementAt(f);
            outData.println("            " + field.useName());
        }
        outData.println("        '''");
        outData.println("        return " + pymodFront + table.useName() + proc.upperFirst() + "(self)");
        if (hasInput == true)
            generateInput(table.useName(), dataStruct, proc.name, parameters, outData);
        else
            generateCover(table.useName(), dataStruct, proc.name, outData);
    }

    private void generateAction(Table table, Proc proc, PrintWriter outData) {
        String dataStruct;
        if (proc.isStd || proc.isStdExtended())
            dataStruct = table.useName();
        else
            dataStruct = table.useName() + proc.upperFirst();
        String parameters = "";
        outData.println("    def " + proc.name + "(self):");
        outData.println("        ''' Action");
        outData.println("        Input:");
        for (int f = 0; f < proc.inputs.size(); f++) {
            Field field = (Field) proc.inputs.elementAt(f);
            outData.println("            " + field.useName());
            parameters += ", " + field.useName() + "=" + defValue(field);
        }
        for (int f = 0; f < proc.dynamics.size(); f++) {
            String field = (String) proc.dynamics.elementAt(f);
            outData.println("            " + field);
            parameters += ", " + field + "=''";
        }
        outData.println("        '''");
        outData.println("        return " + pymodFront + table.useName() + proc.upperFirst() + "(self)");
        generateInput(table.useName(), dataStruct, proc.name, parameters, outData);
    }

    private void generateBulkAction(Table table, Proc proc,
                                    PrintWriter outData) {
        outData.println("    def " + proc.name + "(self, recs):");
        outData.println("        ''' Bulk Action");
        outData.println("        Input:");
        for (int f = 0; f < proc.inputs.size(); f++) {
            Field field = (Field) proc.inputs.elementAt(f);
            outData.println("            " + field.useName());
        }
        for (int f = 0; f < proc.dynamics.size(); f++) {
            String field = (String) proc.dynamics.elementAt(f);
            outData.println("            " + field);
        }
        outData.println("        '''");
        outData.println("        " + pymodFront + table.useName() + proc.upperFirst() + "(len(recs), recs)");
    }

    /**
     * Build of output data rec for user procedures
     */
    void generateUserOutputRecs(Table table, PrintWriter outData) {
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData || proc.isStd || proc.hasNoData())
                continue;
            if (proc.isStdExtended())
                continue;
            String work = "(object)";
            String superName = "";
            if (proc.outputs.size() > 0) {
                for (int j = 0; j < proc.comments.size(); j++)
                    outData.println("##" + (String) proc.comments.elementAt(j));
                String typeChar = "D";
                if (proc.hasDiscreteInput())
                    typeChar = "O";
                work = "(" + typeChar + table.useName() + proc.upperFirst() + ")";
                superName = typeChar + table.useName() + proc.upperFirst();
                outData.println("## \\class " + typeChar + table.useName() + proc.upperFirst());
                outData.println("class " + typeChar + table.useName() + proc.upperFirst() + "(object):");
                outData.println("    def _make(self): return " + typeChar + table.useName() + proc.upperFirst() + "()");
                if (proc.hasDiscreteInput())
                    outData.println("    def _name(self): return ('" + typeChar + table.useName() + proc.upperFirst() + "')");
                else
                    outData.println("    def _name(self): return ('D" + table.useName() + proc.upperFirst() + "', 'O" + table.useName() + proc.upperFirst() + "')");
                generateDataFields(proc.outputs, "", typeChar + table.useName() + proc.upperFirst(), table.useName(), outData);
                outData.println();
            }
            if (proc.hasDiscreteInput()) {
                outData.println("## \\class D" + table.useName() + proc.upperFirst());
                outData.println("class D" + table.useName() + proc.upperFirst() + work + ":");
                outData.println("    def _make(self): return D" + table.useName() + proc.upperFirst() + "()");
                outData.println("    def _name(self): return ('D" + table.useName() + proc.upperFirst() + "')");
                Vector<Field> discreteInputs = new Vector<Field>();
                for (int j = 0; j < proc.inputs.size(); j++) {
                    Field field = (Field) proc.inputs.elementAt(j);
                    if (!proc.hasOutput(field.name))
                        discreteInputs.addElement(field);
                }

                for (int j = 0; j < proc.dynamics.size(); j++) {
                    Field f = new Field();
                    Integer length = (Integer) proc.dynamicSizes.elementAt(j);
                    f.name = (String) proc.dynamics.elementAt(j);
                    f.type = Field.CHAR;
                    f.length = length.intValue();
                    discreteInputs.addElement(f);
                }
                generateDataFields(discreteInputs, superName, "D" + table.useName() + proc.upperFirst(), table.useName(), outData);
                outData.println();
            } else if (proc.outputs.size() > 0) {
                outData.println("## \\class O" + table.useName() + proc.upperFirst());
                outData.println("## \\field see:O" + table.useName() + proc.upperFirst());
                outData.println("O" + table.useName() + proc.upperFirst() + " = D" + table.useName() + proc.upperFirst());
                outData.println();
            }
            if (pymodName.length() > 0) {
                coverFunctions = new Vector<String>();
                outData.println("class " + table.useName() + proc.upperFirst() + "(D" + table.useName() + proc.upperFirst() + "):");
                outData.println("    def __init__(self): D" + table.useName() + proc.upperFirst() + ".__init__(self)");
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
                outData.println();
                for (int j = 0; j < coverFunctions.size(); j++)
                    outData.println((String) coverFunctions.elementAt(j));
            }
        }
    }

    /**
     * Build of enums
     */
    void generateEnums(Table table, PrintWriter outData) {
        for (int i = 0; i < table.fields.size(); i++) {
            Field field = (Field) table.fields.elementAt(i);
            generateEnums(table.useName() + field.useName(), field, outData);
        }
    }

    void generateEnums(String baseName, Field field, PrintWriter outData) {
        if (field.enums.size() > 0) {
            outData.println("class " + field.useName() + "Enum(object):");
            for (int j = 0; j < field.enums.size(); j++)
            {
                Enum entry = (Enum) field.enums.elementAt(j);
                String entryName = entry.name;
                if(entryName.equals("None")) {
                    entryName = "None_";
                }
                if(varName(field) == "int") {
                    outData.println("    " + entryName + " = " + entry.value);
                }
                else {
                    outData.println("    " + entryName + " = '" + entry.value + "'");
                }
            }
            outData.println();

            outData.println(baseName + " = {}");
            for (int j = 0; j < field.enums.size(); j++) {
                Enum entry = (Enum) field.enums.elementAt(j);
                outData.println(baseName + "['" + entry.name + "'] = " + entry.value);
            }
            for (int j = 0; j < field.enums.size(); j++) {
                Enum entry = (Enum) field.enums.elementAt(j);
                outData.println(baseName + "[" + entry.value + "] = '" + entry.name + "'");
            }
            outData.println();
        } else if (field.valueList.size() > 0) {
            outData.println("class " + baseName + "():");
            for (int j = 0; j < field.valueList.size(); j++) {
                String entry = (String) field.valueList.elementAt(j);
                outData.println("    " + entry + " = " + j);
            }
            outData.print("    lookup = (");
            for (int j = 0; j < field.valueList.size(); j++) {
                String entry = (String) field.valueList.elementAt(j);
                outData.print((j == 0 ? "" : ", ") + "'" + entry + "'");
            }
            outData.println(")");
            outData.println();
        }
    }

    String varName(Field field) {
        switch (field.type) {
            case Field.BOOLEAN:
            case Field.BYTE:
            case Field.SHORT:
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "int";
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
                return "long";
            case Field.CHAR:
            case Field.ANSICHAR:
            case Field.TLOB:
            case Field.XML:
            case Field.USERSTAMP:
                return "char";
            case Field.BLOB:
                return "BLOB";
            case Field.DATE:
            case Field.TIME:
            case Field.DATETIME:
            case Field.TIMESTAMP:
                return "char";
            case Field.FLOAT:
            case Field.DOUBLE:
            case Field.MONEY:
                return "double";
        }
        return "<unsupported>";
    }

    String defValue(Field field) {
        if (isNull(field) == true)
            return "None";
        switch (field.type) {
            case Field.BOOLEAN:
            case Field.BYTE:
            case Field.SHORT:
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "'0'";
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
                return "'0'";
            case Field.CHAR:
            case Field.ANSICHAR:
            case Field.TLOB:
            case Field.XML:
            case Field.USERSTAMP:
                return "''";
            case Field.BLOB:
                return "'BLOB'";
            case Field.DATE:
            case Field.TIME:
            case Field.DATETIME:
            case Field.TIMESTAMP:
                return "''";
            case Field.FLOAT:
            case Field.DOUBLE:
            case Field.MONEY:
                return "'0.0'";
        }
        return "<unsupported>";
    }

    boolean isNull(Field field) {
        if (field.isNull == false)
            return false;
        switch (field.type) {
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
