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
import bbd.jportal2.Database;
import bbd.jportal2.Enum;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSNetCode extends BaseGenerator implements IBuiltInSIProcessor {

    public CSNetCode() {
        super(CSNetCode.class);
    }

    private static final Logger logger = LoggerFactory.getLogger(CSNetCode.class);

    public String description() {
        return "Generate C# Code for ADO.NET via IDbConnection";
    }

    public String documentation() {
        return "Generate C# Code for ADO.NET via IDbConnection"
                + "\r\nDATABASE name FLAGS flag"
                + "\r\n- \"mssql storedprocs\" generate stored procedures for MSSql"
                + "\r\n- \"use generics\" generate lists as generics"
                + "\r\n- \"use partials\" generate classes as partials"
                + "\r\n- \"use yields\" generate code for yields"
                + "\r\n- \"no datatables\" do not generate code for datatables"
                + "\r\n- \"use C# 2.0\" generate classes with above uses"
                + "\r\n- \"use C# 1.0\" generate classes compatable with C# 1.0"
                + "\r\n- \"use separate\" generate classes in separate files"
                + "\r\n- \"use notify\" generate classes with INotifyPropertyChanged implemented"
                ;
    }

    protected Vector<Flag> flagsVector;
    boolean mSSqlStoredProcs;
    boolean hasStoredProcs;
    boolean useGenerics;
    boolean usePartials;
    boolean useSeparate;
    boolean noDatatables;
    boolean useYields;
    boolean useCSharp2;
    boolean useCSharp1;
    boolean useNotify;
    boolean useFunc;
    String version = "4.0.";
    String runTimeVersion = "4.0.30319";

    private void flagDefaults() {
        hasStoredProcs = false;
        mSSqlStoredProcs = false;
        useGenerics = false;
        usePartials = false;
        useYields = false;
        useSeparate = false;
        noDatatables = false;
        useCSharp1 = false;
        useCSharp2 = false;
        useNotify = false;
        useFunc = false;
    }

    public Vector<Flag> getFlags() {
        if (flagsVector == null) {
            flagsVector = new Vector<Flag>();
            flagDefaults();
            flagsVector.addElement(new Flag("mssql storedprocs", new Boolean(mSSqlStoredProcs), "Generate MSSql Stored Procedures"));
            flagsVector.addElement(new Flag("use generics", new Boolean(useGenerics), "Generate C# 2.0 Generics"));
            flagsVector.addElement(new Flag("use partials", new Boolean(usePartials), "Generate C# 2.0 Partials"));
            flagsVector.addElement(new Flag("use yields", new Boolean(useYields), "Generate C# 2.0 Yields"));
            flagsVector.addElement(new Flag("use separate", new Boolean(useSeparate), "Generate Separate Files"));
            flagsVector.addElement(new Flag("no datatables", new Boolean(noDatatables), "Do not Generate Datatables"));
            flagsVector.addElement(new Flag("use C#2.0", new Boolean(useCSharp2), "Generate for C#2.0"));
            flagsVector.addElement(new Flag("use C#1.0", new Boolean(useCSharp1), "Generate for C#1.0"));
            flagsVector.addElement(new Flag("use notify", new Boolean(useNotify), "Generate for INotifyPropertyChanged"));
            flagsVector.addElement(new Flag("use func", new Boolean(useFunc), "Generate Functions"));
        }
        return flagsVector;
    }

    /**
     * Sets generation getFlags.
     */
    void setFlags(Database database) {
        if (flagsVector != null) {
            mSSqlStoredProcs = toBoolean(((Flag) flagsVector.elementAt(0)).value);
            useGenerics = toBoolean(((Flag) flagsVector.elementAt(1)).value);
            usePartials = toBoolean(((Flag) flagsVector.elementAt(2)).value);
            useYields = toBoolean(((Flag) flagsVector.elementAt(3)).value);
            useSeparate = toBoolean(((Flag) flagsVector.elementAt(4)).value);
            noDatatables = toBoolean(((Flag) flagsVector.elementAt(5)).value);
            useCSharp2 = toBoolean(((Flag) flagsVector.elementAt(6)).value);
            useCSharp1 = toBoolean(((Flag) flagsVector.elementAt(7)).value);
            useNotify = toBoolean(((Flag) flagsVector.elementAt(8)).value);
            useFunc = toBoolean(((Flag) flagsVector.elementAt(9)).value);
        } else
            flagDefaults();
        for (int i = 0; i < database.flags.size(); i++) {
            String flag = (String) database.flags.elementAt(i);
            if (flag.equalsIgnoreCase("mssql storedprocs"))
                mSSqlStoredProcs = true;
            else if (flag.equalsIgnoreCase("use generics"))
                useGenerics = true;
            else if (flag.equalsIgnoreCase("use partials"))
                usePartials = true;
            else if (flag.equalsIgnoreCase("use yields"))
                useYields = true;
            else if (flag.equalsIgnoreCase("use separate"))
                useSeparate = true;
            else if (flag.equalsIgnoreCase("no datatables"))
                noDatatables = true;
            else if (flag.equalsIgnoreCase("use C#2.0"))
                useGenerics = usePartials = useYields = useCSharp2 = true;
            else if (flag.equalsIgnoreCase("use C#1.0")) {
                useGenerics = usePartials = useYields = useCSharp2 = false;
                useCSharp1 = true;
                runTimeVersion = "1.1";
            } else if (flag.equalsIgnoreCase("use notify"))
                useNotify = true;
            else if (flag.equalsIgnoreCase("use func"))
                useFunc = true;
        }
        if (mSSqlStoredProcs)
            logger.info(" (mssql storedprocs)");
        if (useGenerics)
            logger.info(" (use generics)");
        if (usePartials)
            logger.info(" (use partials)");
        if (useYields)
            logger.info(" (use yields)");
        if (useSeparate)
            logger.info(" (use separate)");
        if (noDatatables)
            logger.info(" (no datatables)");
        if (useNotify)
            logger.info(" (use notify)");
        if (useFunc)
            logger.info(" (use func)");
    }

    public void generate(Database database, String output) throws Exception {
        setFlags(database);
        for (int i = 0; i < database.tables.size(); i++) {
            Table table = (Table) database.tables.elementAt(i);
            generate(table, output);
        }
    }

    OutputStream procFile;
    PrintWriter procData;

    void generate(Table table, String output) throws Exception {
        String added = "";

        if (useSeparate == true)
            added = "Structs";
        PrintWriter outFile = this.openOutputFileForGeneration("cs", openOutputStream(table, output, added));
        hasStoredProcs = false;
        if (mSSqlStoredProcs == false && isStoredProcs(table)) {
            hasStoredProcs = true;
        }

        if (mSSqlStoredProcs == true || hasStoredProcs == true) {
            try (PrintWriter procData = this.openOutputFileForGeneration("sproc.sql", output + table.name + ".sproc.sql")) {
                procData.println("USE " + table.database.name);
                procData.println();
            }
        }
        openWriterPuttingTop(table, outFile);
        generateStructs(table, outFile);
        if (!noDatatables) {
            if (useSeparate == true) {
                outFile.println("}");
                outFile.flush();
                outFile.close();
                outFile = openOutputFileForGeneration("Tables.cs", openOutputStream(table, output, "Tables"));
                openWriterPuttingTop(table, outFile);
            }
            generateDataTables(table, outFile);
        }
        if (useSeparate == true) {
            outFile.println("}");
            outFile.flush();
            outFile.close();
            outFile = openOutputFileForGeneration("cs", openOutputStream(table, output, ""));
            openWriterPuttingTop(table, outFile);
        }
        generateCode(table, outFile);
        outFile.println("}");
        outFile.flush();
        if (mSSqlStoredProcs == true || hasStoredProcs == true)
            procData.flush();
    }

    private void openWriterPuttingTop(Table table, PrintWriter outData) {
        String packageName = table.database.packageName;
        if (packageName.length() == 0)
            packageName = "bbd.jportal2";
        outData.println("//------------------------------------------------------------------------------");
        outData.println("// <auto-generated>");
        outData.println("//     This code was generated by a tool.");
        outData.println("//     Runtime Version: " + runTimeVersion);
        outData.println("//");
        outData.println("//     Changes to this file may cause incorrect behavior and will be lost if");
        outData.println("//     the code is regenerated.");
        outData.println("// </auto-generated>");
        outData.println("//------------------------------------------------------------------------------");
        outData.println("");
        outData.println("// ");
        outData.println("// This source code was auto-generated by jportal2.jar, Version=" + version);
        outData.println("// ");
        outData.println("using System;");
        if (useGenerics)
            outData.println("using System.Collections.Generic;");
        else {
            outData.println("using System.Collections;");
            outData.println("using System.Collections.Specialized;");
        }
        if (useNotify) {
            outData.println("using System.ComponentModel;");
            outData.println("using System.Runtime.CompilerServices;");
        }
        outData.println("using System.Data;");
        outData.println("using bbd.jportal2;");
        outData.println("");
        outData.println("namespace " + packageName);
        outData.println("{");
    }

    private String openOutputStream(Table table, String output, String added) {
        return output + table.name + added + ".cs";
    }

    private void generateSelector(Field field, PrintWriter outData) {
        outData.println(indent(2) + "public class Nfpp" + field.useUpperName() + " // NO _______ PARAMETER PROPERTIES ");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "DataTable parent;");
        outData.println(indent(3) + "public " + fieldCastNo(field) + " this[int row]");
        outData.println(indent(3) + "{");
        outData.println(indent(4) + "get");
        outData.println(indent(4) + "{");
        if (field.isNull == true) {
            outData.println(indent(5) + "if (parent.Rows[row][c" + field.useUpperName() + "] == DBNull.Value)");
            outData.println(indent(5) + "{");
            if (useCSharp1) {
                outData.println(indent(6) + "return " + validNull(field) + ";");
            } else {
                outData.println(indent(6) + "return default(" + fieldCastNo(field) + ");");
            }
            outData.println(indent(5) + "}");
            outData.println(indent(5) + "else");
            outData.println(indent(5) + "{");
            outData.println(indent(6) + "return (" + fieldCastNo(field) + ")parent.Rows[row][c" + field.useUpperName() + "];");
            outData.println(indent(5) + "}");
        } else
            outData.println(indent(5) + "return (" + fieldCastNo(field) + ")parent.Rows[row][c" + field.useUpperName() + "];");
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "set");
        outData.println(indent(4) + "{");
        outData.println(indent(5) + "parent.Rows[row][c" + field.useUpperName() + "] = value;");
        outData.println(indent(4) + "}");
        outData.println(indent(3) + "}");
        if (field.isNull == true) {
            outData.println(indent(3) + "public bool IsNull(int row){ return parent.Rows[row].IsNull(c" + field.useUpperName() + "); }");
            outData.println(indent(3) + "public void SetNull(int row){ parent.Rows[row][c" + field.useUpperName() + "] = DBNull.Value; }");
        }
        outData.println(indent(3) + "public Nfpp" + field.useUpperName() + "(DataTable parent) { this.parent = parent; }");
        outData.println(indent(2) + "}");
        outData.println(indent(2) + "private Nfpp" + field.useUpperName() + " m" + field.useUpperName() + ";");
        outData.println(indent(2) + "public Nfpp" + field.useUpperName() + " " + field.useUpperName());
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "get { return m" + field.useUpperName() + "; }");
        outData.println(indent(3) + "set { m" + field.useUpperName() + " = value; }");
        outData.println(indent(2) + "}");
    }

    public boolean isStoredProcs(Table table) {
        boolean hasStoredProcs = false;
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isSProc == true)
                return true;
            if (proc.outputs.size() == 0 || proc.isSingle)
                continue;
        }
        return hasStoredProcs;
    }

    public void generateDataTables(Table table, PrintWriter outData) {
        boolean hasDataTables = false;
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.outputs.size() == 0 || proc.isSingle)
                continue;
            hasDataTables = true;
            outData.println(indent(1) + "[Serializable()]");
            outData.println(indent(1) + "public " + (usePartials ? "partial " : "") + "class " + table.useName() + proc.upperFirst() + "DataTable : DataTable");
            outData.println(indent(1) + "{");
            int noInDataSet = 0;
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                outData.println(indent(2) + "public const int c" + field.useUpperName() + " = " + noInDataSet + ";");
                noInDataSet++;
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                outData.println(indent(2) + "public const int c" + field.useUpperName() + " = " + noInDataSet + ";");
                noInDataSet++;
            }
            outData.println(indent(2) + "public static string ToString(int ordinal)");
            outData.println(indent(2) + "{");
            outData.println(indent(3) + "switch (ordinal)");
            outData.println(indent(3) + "{");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                outData.println(indent(4) + "case c" + field.useUpperName() + ": return \"" + field.useUpperName() + "\";");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                outData.println(indent(4) + "case c" + field.useUpperName() + ": return \"" + field.useUpperName() + "\";");
            }
            outData.println(indent(3) + "}");
            outData.println(indent(3) + "return \"<??\"+ordinal+\"??>\";");
            outData.println(indent(2) + "}");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                generateSelector(field, outData);
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                generateSelector(field, outData);
            }
            String mainName = table.useName();
            if (proc.isStd == false)
                mainName = mainName + proc.upperFirst();
            outData.println(indent(2) + "public class RowBag");
            outData.println(indent(2) + "{");
            outData.println(indent(3) + "public " + mainName + "Rec mRec;");
            outData.println(indent(3) + "public object tag = null;");
            outData.println(indent(3) + "public RowBag(" + mainName + "Rec aRec)");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "mRec = aRec;");
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
            if (useGenerics)
                outData.println(indent(2) + "public Dictionary<DataRow, RowBag> dictionary;");
            else
                outData.println(indent(2) + "public HybridDictionary dictionary;");
            if (useGenerics)
                outData.println(indent(2) + "public " + table.useName() + proc.upperFirst() + "DataTable(List<" + mainName + "Rec> aList)");
            else
                outData.println(indent(2) + "public " + table.useName() + proc.upperFirst() + "DataTable(ArrayList aList)");
            outData.println(indent(3) + ": base(\"" + table.useName() + proc.upperFirst() + "\")");
            outData.println(indent(2) + "{");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                outData.println(indent(3) + "Columns.Add(new DataColumn(\"" + field.useUpperName() + "\", typeof(" + dataTableType(field) + ")));");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                outData.println(indent(3) + "Columns.Add(new DataColumn(\"" + field.useUpperName() + "\", typeof(" + dataTableType(field) + ")));");
            }
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                outData.println(indent(3) + "m" + field.useUpperName() + " = new Nfpp" + field.useUpperName() + "(this);");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                outData.println(indent(3) + "m" + field.useUpperName() + " = new Nfpp" + field.useUpperName() + "(this);");
            }
            if (useGenerics)
                outData.println(indent(3) + "dictionary = new Dictionary<DataRow, RowBag>();");
            else
                outData.println(indent(3) + "dictionary = new HybridDictionary();");
            outData.println(indent(3) + "foreach (" + mainName + "Rec wRec in aList)");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "DataRow wRow = NewRow();");
            outData.println(indent(4) + "dictionary.Add(wRow, new RowBag(wRec));");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                if (field.isNull == true) {
                    outData.println(indent(4) + "if (wRec." + field.useUpperName() + "IsNull == true)");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = DBNull.Value;");
                    outData.println(indent(4) + "}");
                    outData.println(indent(4) + "else");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = wRec." + field.useUpperName() + ";");
                    outData.println(indent(4) + "}");
                } else
                    outData.println(indent(4) + "wRow[c" + field.useUpperName() + "] = wRec." + field.useUpperName() + ";");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                if (field.isNull == true) {
                    outData.println(indent(4) + "if (wRec." + field.useUpperName() + "IsNull == true)");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = DBNull.Value;");
                    outData.println(indent(4) + "}");
                    outData.println(indent(4) + "else");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = wRec." + field.useUpperName() + ";");
                    outData.println(indent(4) + "}");
                } else
                    outData.println(indent(4) + "wRow[c" + field.useUpperName() + "] = wRec." + field.useUpperName() + ";");
            }
            outData.println(indent(4) + "Rows.Add(wRow);");
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
            outData.println(indent(2) + "public RowBag GetRowBag(int row)");
            outData.println(indent(2) + "{");
            outData.println(indent(3) + "DataRow wRow = Rows[row];");
            if (useGenerics)
                outData.println(indent(3) + "return dictionary[wRow];");
            else
                outData.println(indent(3) + "return (RowBag)dictionary[wRow];");
            outData.println(indent(2) + "}");
            outData.println(indent(2) + "public " + mainName + "Rec this[int row]");
            outData.println(indent(2) + "{");
            outData.println(indent(3) + "get");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "DataRow wRow = Rows[row];");
            outData.println(indent(4) + mainName + "Rec wRec = new " + mainName + "Rec();");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                if (field.isNull == true) {
                    outData.println(indent(4) + "if (wRow.IsNull(c" + field.useUpperName() + "))");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRec." + field.useUpperName() + "IsNull = true;");
                    outData.println(indent(4) + "}");
                    outData.println(indent(4) + "else");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRec." + field.useUpperName() + " = " + fieldCast(field) + "wRow[c" + field.useUpperName() + "];");
                    outData.println(indent(4) + "}");
                } else
                    outData.println(indent(5) + "wRec." + field.useUpperName() + " = " + fieldCast(field) + "wRow[c" + field.useUpperName() + "];");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                if (field.isNull == true) {
                    outData.println(indent(4) + "if (wRow.IsNull(c" + field.useUpperName() + "))");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRec." + field.useUpperName() + "IsNull = true;");
                    outData.println(indent(4) + "}");
                    outData.println(indent(4) + "else");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRec." + field.useUpperName() + " = " + fieldCast(field) + "wRow[c" + field.useUpperName() + "];");
                    outData.println(indent(4) + "}");
                } else
                    outData.println(indent(4) + "wRec." + field.useUpperName() + " = " + fieldCast(field) + "wRow[c" + field.useUpperName() + "];");
            }
            outData.println(indent(4) + "return wRec;");
            outData.println(indent(3) + "}");
            outData.println(indent(3) + "set");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "DataRow wRow = Rows[row];");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = (Field) proc.inputs.elementAt(j);
                if (field.isNull == true) {
                    outData.println(indent(4) + "if (value." + field.useUpperName() + "IsNull == true)");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = DBNull.Value;");
                    outData.println(indent(4) + "}");
                    outData.println(indent(4) + "else");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = value." + field.useUpperName() + ";");
                    outData.println(indent(4) + "}");
                } else
                    outData.println(indent(4) + "wRow[c" + field.useUpperName() + "] = value." + field.useUpperName() + ";");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = (Field) proc.outputs.elementAt(j);
                if (proc.hasInput(field.name))
                    continue;
                if (field.isNull == true) {
                    outData.println(indent(4) + "if (value." + field.useUpperName() + "IsNull == true)");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = DBNull.Value;");
                    outData.println(indent(4) + "}");
                    outData.println(indent(4) + "else");
                    outData.println(indent(4) + "{");
                    outData.println(indent(5) + "wRow[c" + field.useUpperName() + "] = value." + field.useUpperName() + ";");
                    outData.println(indent(4) + "}");
                } else
                    outData.println(indent(4) + "wRow[c" + field.useUpperName() + "] = value." + field.useUpperName() + ";");
            }
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
            outData.println(indent(1) + "}");
        }
        if (hasDataTables == true && usePartials == true && useSeparate == true) {
            String mainName = table.useName();
            //outData.println("  [Serializable()]");
            outData.println(indent(1) + "public partial class " + mainName);
            outData.println(indent(1) + "{");
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = (Proc) table.procs.elementAt(i);
                if (proc.isData == true || proc.isStd == false)
                    continue;
                if (!noDatatables) {
                    if (proc.outputs.size() > 0 && !proc.isSingle)
                        generateFetchProcDataTables(proc, mainName, outData);
                }
            }
            outData.println(indent(1) + "}");
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = (Proc) table.procs.elementAt(i);
                if (proc.isData == true || proc.isStd == true)
                    continue;
                if (proc.outputs.size() > 0 && !proc.isSingle) {
                    mainName = table.useName() + proc.upperFirst();
                    //outData.println("  [Serializable()]");
                    outData.println(indent(1) + "public partial class " + mainName);
                    outData.println(indent(1) + "{");
                    if (!noDatatables) {
                        generateFetchProcDataTables(proc, mainName, outData);
                    }
                    outData.println(indent(1) + "}");
                }
            }
        }
    }

    public void generateStructPairs(Proc proc, Vector<Field> fields, Vector<?> dynamics, String mainName, PrintWriter outData, String tableName, boolean hasReturning) {
        outData.println(indent(1) + "[Serializable()]");
        String inherit = "";
        if (proc != null && proc.extendsStd == true) {
            inherit = " : " + tableName + "Rec" + (useNotify ? ", INotifyPropertyChanged" : "");
        } else {
            inherit = useNotify ? " : INotifyPropertyChanged" : "";
        }
        outData.println(indent(1) + "public " + (usePartials ? "partial " : "") + "class " + mainName + "Rec" + inherit);
        outData.println(indent(1) + "{");
        if (useNotify && fields.size() > 0) {
            outData.println(indent(2) + "#region INotifyPropertyChanged Members ");
            outData.println("");
            outData.println(indent(2) + "public event PropertyChangedEventHandler PropertyChanged;");
            outData.println("");
            outData.println(indent(2) + "public void NotifyPropertyChanged([CallerMemberName] string propertyName = null)");
            outData.println(indent(2) + "{");
            outData.println(indent(3) + "if (this.PropertyChanged != null)");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "this.PropertyChanged(this, new PropertyChangedEventArgs(propertyName));");
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
            outData.println("");
            outData.println(indent(2) + "#endregion");
            outData.println("");
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            String temp = "[Column(MaxLength=" + field.length;
            if (field.precision > 0) {
                temp = "[Column(MaxLength=" + field.precision + field.scale + 1;
            }

            if (field.isNull) {
                temp = temp + ", CanBeNull=true";
            }
            if (field.isPrimaryKey) {
                temp = temp + ", IsPrimaryKey=true";
            }
            if (field.isSequence) {
                temp = temp + ", isSequence=true";
            }
            if (field.precision > 0) {
                temp = temp + ", Precision=" + field.precision + ", Scale=" + field.scale;
            }

            outData.println(fieldDef(field, temp));
            if (field.isNull) {
                if (getDataType(field, new StringBuffer(), new StringBuffer()) == "string") {
                    outData.println(indent(2) + "private bool " + field.useLowerName() + "IsNull;");
                    outData.println(indent(2) + "public bool " + field.useUpperName() + "IsNull");
                    outData.println(indent(2) + "{");
                    outData.println(indent(3) + "get { return this." + field.useLowerName() + " == null ? true : " + field.useLowerName() + "IsNull; }");
                    outData.println(indent(3) + "set { this." + field.useLowerName() + "IsNull = value; }");
                    outData.println(indent(2) + "}");
                } else {
                    outData.println(indent(2) + "private bool " + field.useLowerName() + "IsNull;");
                    outData.println(indent(2) + "public bool " + field.useUpperName() + "IsNull");
                    outData.println(indent(2) + "{ ");
                    outData.println(indent(3) + "get { return this." + field.useLowerName() + "IsNull; }");
                    outData.println(indent(3) + "set { this." + field.useLowerName() + "IsNull = value; }");
                    outData.println(indent(2) + "}");
                }
            }
        }
        outData.println(indent(1) + "}");
    }

    public void generateEnumOrdinals(Table table, PrintWriter outData) {
        for (int i = 0; i < table.fields.size(); i++) {
            Field field = (Field) table.fields.elementAt(i);
            if (field.enums.size() > 0) {
                outData.println(indent(1) + "public class " + table.useName() + field.useUpperName() + "Ord");
                outData.println(indent(1) + "{");
                String datatype = "int";
                if (field.type == Field.ANSICHAR && field.length == 1)
                    datatype = "string";
                for (int j = 0; j < field.enums.size(); j++) {
                    Enum en = (Enum) field.enums.elementAt(j);
                    String evalue = "" + en.value;
                    if (field.type == Field.ANSICHAR && field.length == 1)
                        evalue = "\"" + (char) en.value + "\"";
                    outData.println(indent(2) + "public const " + datatype + " " + en.name + " = " + evalue + ";");
                }
                outData.println(indent(2) + "public static string ToString(" + datatype + " ordinal)");
                outData.println(indent(2) + "{");
                outData.println(indent(3) + "switch (ordinal)");
                outData.println(indent(3) + "{");
                for (int j = 0; j < field.enums.size(); j++) {
                    bbd.jportal2.Enum en = (bbd.jportal2.Enum) field.enums.elementAt(j);
                    String evalue = "" + en.value;
                    if (field.type == Field.ANSICHAR && field.length == 1)
                        evalue = "\"" + (char) en.value + "\"";
                    outData.println(indent(4) + "case " + evalue + ": return \"" + en.name + "\";");
                }
                outData.println(indent(3) + "}");
                outData.println(indent(3) + "return \"unknown ordinal: \"+ordinal;");
                outData.println(indent(2) + "}");
                outData.println(indent(1) + "}");
            }
        }
    }

    public void generateStructs(Table table, PrintWriter outData) {
        if (table.fields.size() > 0) {
            if (table.comments.size() > 0) {
                outData.println(indent(1) + "/// <summary>");
                for (int i = 0; i < table.comments.size(); i++) {
                    String s = (String) table.comments.elementAt(i);
                    outData.println(indent(1) + "/// " + s);
                }
                outData.println(indent(1) + "/// </summary>");
            }
            boolean hasReturning = false;
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = (Proc) table.procs.elementAt(i);
                if (proc.hasReturning && proc.isStd) {
                    hasReturning = true;
                    break;
                }
            }
            generateStructPairs(null, table.fields, null, table.useName(), outData, null, hasReturning);
            generateEnumOrdinals(table, outData);
            for (int i = 0; i < table.procs.size(); i++) {
                Proc proc = (Proc) table.procs.elementAt(i);
                if (proc.isData || proc.isStd || proc.hasNoData())
                    continue;
                if (proc.comments.size() > 0) {
                    outData.println(indent(1) + "/// <summary>");
                    for (int j = 0; j < proc.comments.size(); j++) {
                        String s = (String) proc.comments.elementAt(j);
                        outData.println(indent(1) + "/// " + s);
                    }
                    outData.println(indent(1) + "/// </summary>");
                }
                Vector<Field> fields = new Vector<Field>();
                if (!proc.extendsStd)
                    for (int j = 0; j < proc.outputs.size(); j++)
                        fields.addElement(proc.outputs.elementAt(j));
                for (int j = 0; j < proc.inputs.size(); j++) {
                    Field field = (Field) proc.inputs.elementAt(j);
                    if (proc.hasOutput(field.name) == false || field.isExtStd)
                        fields.addElement(field);
                }
                for (int j = 0; j < proc.dynamics.size(); j++) {
                    String s = (String) proc.dynamics.elementAt(j);
                    Integer n = (Integer) proc.dynamicSizes.elementAt(j);
                    Field field = new Field();
                    field.name = s;
                    field.type = Field.DYNAMIC;
                    field.length = n.intValue();
                    fields.addElement(field);
                }
                generateStructPairs(proc, fields, proc.dynamics, table.useName() + proc.upperFirst(), outData, table.useName(), false);
            }
        }
    }

    public void generateCode(Table table, PrintWriter outData) {
        boolean firsttime = true;
        boolean isLoaded = true;
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData == true || proc.isStd == false)
                continue;
            generateStdCode(table, proc, outData, firsttime, isLoaded);
            if (proc.outputs.size() > 0 && !proc.isSingle) {
                isLoaded = false;
            }
            firsttime = false;
        }
        if (firsttime == false)
            outData.println(indent(1) + "}");
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData == true || proc.isStd == true)
                continue;
            generateCode(table, proc, outData);
        }
    }

    PlaceHolder placeHolder;

    void generateStoredProc(Proc proc, String storedProcName, Vector<?> lines) {
        //procData.println("IF EXISTS (SELECT * FROM SYSOBJECTS WHERE ID = OBJECT_ID('dbo." + storedProcName + "') AND SYSSTAT & 0xf = 4)");
        procData.println("IF OBJECT_ID('dbo." + storedProcName + "','P') IS NOT NULL");
        procData.println(indent(1) + "DROP PROCEDURE dbo." + storedProcName);
        procData.println("GO");
        procData.println("");
        procData.println("CREATE PROCEDURE dbo." + storedProcName);
        String comma = "(";
        for (int i = 0; i < placeHolder.pairs.size(); i++) {
            PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
            Field field = pair.field;
            procData.println(comma + " @P" + i + " " + varType(field) + " -- " + field.name);
            comma = ",";
        }
        if (placeHolder.pairs.size() > 0)
            procData.println(")");
        procData.println("AS");
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            procData.println(line.substring(1, line.length() - 1));
        }
        if (proc.isInsert && proc.hasReturning && proc.table.hasIdentity) {
            procData.println("; SELECT CAST(SCOPE_IDENTITY() AS INT)");
        }
        procData.println("GO");
        procData.println("");
    }

    void generateStoredProcCommand(Proc proc, PrintWriter outData) {
        placeHolder = new PlaceHolder(proc, PlaceHolder.AT, "");
        String storedProcName = proc.table.useName() + proc.upperFirst();
        Vector<?> lines = placeHolder.getLines();
        generateStoredProc(proc, storedProcName, lines);
        outData.println(indent(2) + "public string Command" + proc.upperFirst() + "()");
        outData.println(indent(2) + "{");
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            outData.println(indent(3) + "// " + line.substring(1, line.length() - 1));
        }
        outData.println(indent(3) + "return \"" + storedProcName + "\";");
        outData.println(indent(2) + "}");
    }

    void generateCommand(Proc proc, PrintWriter outData) {
        if (proc.hasReturning) {
            placeHolder = new PlaceHolder(proc, PlaceHolder.CURLY, "");
            outData.println(indent(2) + "public string Command" + proc.upperFirst() + "(JConnect aConnect, string aTable, string aField)");
        } else {
            placeHolder = new PlaceHolder(proc, PlaceHolder.CURLY, "Rec.");
            outData.println(indent(2) + "public string Command" + proc.upperFirst() + "()");
        }
        Vector<?> lines = placeHolder.getLines();
        outData.println(indent(2) + "{");
        if (proc.hasReturning)
            outData.println(indent(3) + "Returning _ret = new Returning(aConnect.TypeOfVendor, aTable, aField);");
        outData.println(indent(3) + "return ");
        String plus = indent(4);
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.elementAt(i);
            outData.println(plus + line);
            plus = indent(4) + "+ ";
        }
        outData.println(indent(4) + ";");
        outData.println(indent(2) + "}");
    }

    void generateNonQueryProc(Proc proc, String mainName, PrintWriter outData) {
        Field identity = null;
        for (int i = 0; i < proc.table.fields.size(); i++) {
            Field field = (Field) proc.table.fields.elementAt(i);
            if (field.isPrimaryKey) {
                identity = field;
                break;
            }
        }
        if (proc.hasReturning)
            outData.println(indent(2) + "public bool " + proc.upperFirst() + "(JConnect aConnect)");
        else
            outData.println(indent(2) + "public void " + proc.upperFirst() + "(JConnect aConnect)");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "using (JCursor wCursor = new JCursor(aConnect))");
        outData.println(indent(3) + "{");
        if (doMSSqlStoredProcs(proc))
            outData.println(indent(4) + "wCursor.Procedure(Command" + proc.upperFirst() + "());");
        else {
            if (placeHolder.pairs.size() > 0)
                outData.println(indent(4) + "// format command to change {n} to ?, @Pn or :Pn placeholders depending on Vendor");
            outData.println(indent(4) + "wCursor.Format(Command" + proc.upperFirst() + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
        }
        for (int i = 0; i < placeHolder.pairs.size(); i++) {
            PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
            Field field = pair.field;
            String member = "";
            if (field.type == Field.BLOB)
                member = ".getBlob()";
            String tail = "";
            if (field.isNull)
                tail = ", mRec." + field.useUpperName() + "IsNull";
            if (proc.isInsert && field.isSequence) {
                outData.println(indent(4) + "var " + field.useLowerName() + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetSequence(\"" + proc.table.name + "\",\"" + field.name + "\", ref " + field.useLowerName() + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + ";");
            } else if (field.type == Field.TIMESTAMP) {
                outData.println(indent(4) + "var " + field.useLowerName() + i + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetTimeStamp(ref " + field.useLowerName() + i + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + i + ";");
            } else if (field.type == Field.USERSTAMP) {
                outData.println(indent(4) + "var " + field.useLowerName() + i + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetUserStamp(ref " + field.useLowerName() + i + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + i + ";");
            } else
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", mRec." + field.useUpperName() + member + tail + ");");
        }
        if (proc.hasReturning) {
            outData.println(indent(4) + "wCursor.Run();");
            outData.println(indent(4) + "bool wResult = (wCursor.HasReader() && wCursor.Read());");
            outData.println(indent(4) + "if (wResult == true)");
            outData.println(indent(4) + "{");
            if (identity.isNull && getDataType(identity, new StringBuffer(), new StringBuffer()) == "string") {
                outData.println(indent(5) + "var " + identity.useLowerName() + "IsNull = mRec." + identity.useUpperName() + "IsNull;");
                outData.println(indent(5) + "mRec." + identity.useUpperName() + " = " + castOf(identity) + "wCursor.GetString(0, out " + identity.useLowerName() + "IsNull);");
                outData.println(indent(5) + "mRec." + identity.useUpperName() + "IsNull = " + identity.useLowerName() + "IsNull;");
            } else if (identity.isNull) {
                outData.println(indent(5) + "var " + identity.useLowerName() + "IsNull = mRec." + identity.useUpperName() + "IsNull;");
                outData.println(indent(5) + "mRec." + identity.useUpperName() + " = " + castOf(identity) + "wCursor." + cursorGet(identity, 0) + ";");
                outData.println(indent(5) + "mRec." + identity.useUpperName() + "IsNull = " + identity.useLowerName() + "IsNull;");
            } else {
                outData.println(indent(5) + "mRec." + identity.useUpperName() + " = " + castOf(identity) + "wCursor." + cursorGet(identity, 0) + ";");
            }
            outData.println(indent(4) + "}");
            outData.println(indent(4) + "if (wCursor.HasReader())");
            outData.println(indent(4) + "{");
            outData.println(indent(5) + "wCursor.Close();");
            outData.println(indent(4) + "}");
            outData.println(indent(4) + "return wResult;");
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
        } else {
            outData.println(indent(4) + "wCursor.Exec();");
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
        }
    }

    void generateFunc(Proc proc, String mainName, PrintWriter outData) {
        String line, input = "", postfix = "";
        boolean isLoaded = false;
        int ind = 0;
        String placeHolder = "";
        if (proc.outputs.size() > 0 && !proc.isSingle && !proc.isInsert) {
            isLoaded = true;
        }

        for (int i = 0; i < proc.inputs.size(); i++) {

            Field field = (Field) proc.inputs.elementAt(i);
            if (field.isPrimaryKey || proc.updateFields.contains(field.name)) {
                StringBuffer maker = new StringBuffer();
                StringBuffer temp2 = new StringBuffer();
                String result = returnNullableDataType(field, maker, temp2);
                line = ", ";
                input = input + line + result + " " + field.useLowerName();
            }
        }

        for (int i = 0; i < proc.inputs.size(); i++) {
            Field field = (Field) proc.inputs.elementAt(i);
            if (field.isExtStdOut)
                continue;
            if (field.type == Field.TIMESTAMP || field.type == Field.USERSTAMP) {
                continue;
            }
            if (!(field.isPrimaryKey || proc.updateFields.contains(field.name))) {
                StringBuffer maker = new StringBuffer();
                StringBuffer temp2 = new StringBuffer();
                String result = returnNullableDataType(field, maker, temp2);
                line = ", ";
                input = input + line + result + " " + field.useLowerName();
            }
        }
        for (int j = 0; j < proc.dynamics.size(); j++) {
            String s = (String) proc.dynamics.elementAt(j);
            Integer n = (Integer) proc.dynamicSizes.elementAt(j);
            Field field = new Field();
            field.name = s;
            field.type = Field.DYNAMIC;
            field.length = n.intValue();
            line = ", ";
            input = input + line + "string " + field.useLowerName();
        }
        input = input + ")";
        if (input.equals(")")) {
            postfix = "Struct";
        }

        if (proc.isSingle)
            outData.println(indent(2) + "public " + mainName + "Rec " + proc.upperFirst() + postfix + "(JConnect aConnect" + input);
        else if (!isLoaded) {
            if (proc.isMultipleInput) {
                outData.println(indent(2) + "public void " + proc.upperFirst() + postfix + "(JConnect aConnect, List<" + mainName + "Rec> aList)");
            } else {
                outData.println(indent(2) + "public void " + proc.upperFirst() + postfix + "(JConnect aConnect" + input);
            }
        } else if (proc.hasReturning) {
            outData.println(indent(2) + "public bool " + proc.upperFirst() + postfix + "(JConnect aConnect" + input);
        } else
            outData.println(indent(2) + "public List<" + mainName + "Rec> " + proc.upperFirst() + postfix + "(JConnect aConnect" + input);
        outData.println(indent(2) + "{");
        if (proc.isMultipleInput) {
            outData.println(indent(3) + "foreach (var item in aList)");
            outData.println(indent(3) + "{");
            placeHolder = "item.";
            ind = 1;
        }
        for (int i = 0; i < proc.inputs.size(); i++) {
            Field field = (Field) proc.inputs.elementAt(i);
            if (field.isExtStdOut)
                continue;
            if (field.type == Field.TIMESTAMP || field.type == Field.USERSTAMP) {
                continue;
            }
            placeHolder = field.useLowerName();
            if (proc.isMultipleInput) {
                placeHolder = "item." + field.useUpperName();
            }
            if (field.isNull && getNullableType(field)) {

                outData.println(indent(3 + ind) + "if (" + placeHolder + ".HasValue)");
                outData.println(indent(3 + ind) + "{");
                outData.println(indent(4 + ind) + "mRec." + field.useUpperName() + " = " + placeHolder + ".Value;");
                outData.println(indent(3 + ind) + "}");
                outData.println(indent(3 + ind) + "mRec." + field.useUpperName() + "IsNull = !" + placeHolder + ".HasValue;");

            } else {
                outData.println(indent(3 + ind) + "mRec." + field.useUpperName() + " = " + placeHolder + ";");
            }
        }
        for (int j = 0; j < proc.dynamics.size(); j++) {
            String s = (String) proc.dynamics.elementAt(j);
            Integer n = (Integer) proc.dynamicSizes.elementAt(j);
            Field field = new Field();
            field.name = s;
            field.type = Field.DYNAMIC;
            field.length = n.intValue();
            line = ", ";
            if (proc.isMultipleInput)
                outData.println(indent(3 + ind) + "mRec." + field.useUpperName() + " = item." + field.useUpperName() + ";");
            else
                outData.println(indent(3 + ind) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + ";");
        }
        if (proc.isSingle) {
            outData.println(indent(3) + "if (" + proc.upperFirst() + "(aConnect))");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "return mRec;");
            outData.println(indent(3) + "}");
            outData.println(indent(3) + "else");
            outData.println(indent(3) + "{");
            outData.println(indent(4) + "return null;");
            outData.println(indent(3) + "}");
            outData.println(indent(2) + "}");
        } else if (!isLoaded) {
            outData.println(indent(3 + ind) + proc.upperFirst() + "(aConnect);");
            if (proc.isMultipleInput) {
                outData.println(indent(3) + "}");
            }
            outData.println(indent(2) + "}");
        } else if (proc.hasReturning) {
            outData.println(indent(3) + "return " + proc.upperFirst() + "(aConnect);");
            outData.println(indent(2) + "}");
        } else {
            outData.println(indent(3) + proc.upperFirst() + "Load(aConnect);");
            outData.println(indent(3) + "return Loaded;");
            outData.println(indent(2) + "}");
        }

    }

    void generateReturningProc(Proc proc, String mainName, PrintWriter outData) {
        Field identity = null;
        for (int i = 0; i < proc.table.fields.size(); i++) {
            Field field = (Field) proc.table.fields.elementAt(i);
            //if (field.isPrimaryKey) -- not all primary keys are sequences or identities
            if (field.isSequence) {
                identity = field;
                break;
            }
        }
        if (identity == null) {
            generateNonQueryProc(proc, mainName, outData);
            return;
        }
        outData.println(indent(2) + "public bool " + proc.upperFirst() + "(JConnect aConnect)");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "using (JCursor wCursor = new JCursor(aConnect))");
        outData.println(indent(3) + "{");
        if (doMSSqlStoredProcs(proc))
            outData.println(indent(4) + "wCursor.Procedure(Command" + proc.upperFirst() + "());");
        else {
            if (placeHolder.pairs.size() > 0)
                outData.println(indent(4) + "// format command to change {n} to ?, @Pn or :Pn placeholders depending on Vendor");
            outData.println(indent(4) + "wCursor.Format(Command" + proc.upperFirst() + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
        }
        for (int i = 0; i < placeHolder.pairs.size(); i++) {
            PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
            Field field = pair.field;
            String member = "";
            if (field.type == Field.BLOB)
                member = ".getBlob()";
            String tail = "";
            if (field.isNull)
                tail = ", mRec." + field.useUpperName() + "IsNull";
            if (field.type == Field.TIMESTAMP) {
                outData.println(indent(4) + "var " + field.useLowerName() + i + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetTimeStamp(ref " + field.useLowerName() + i + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + i + ";");
            } else if (field.type == Field.USERSTAMP) {
                outData.println(indent(4) + "var " + field.useLowerName() + i + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetUserStamp(ref " + field.useLowerName() + i + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + i + ";");
            } else
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", mRec." + field.useUpperName() + member + tail + ");");
        }
        outData.println(indent(4) + "wCursor.Run();");
        outData.println(indent(4) + "bool wResult = (wCursor.HasReader() && wCursor.Read());");
        outData.println(indent(4) + "if (wResult == true)");
        outData.println(indent(4) + "{");
        if (identity.isNull && getDataType(identity, new StringBuffer(), new StringBuffer()) == "string") {
            outData.println(indent(5) + "var " + identity.useLowerName() + "IsNull = mRec." + identity.useUpperName() + "IsNull;");
            outData.println(indent(5) + "mRec." + identity.useUpperName() + " = " + castOf(identity) + "wCursor.GetString(0, out " + identity.useLowerName() + "IsNull);");
            outData.println(indent(5) + "mRec." + identity.useUpperName() + "IsNull = " + identity.useLowerName() + "IsNull;");
        } else if (identity.isNull) {
            outData.println(indent(4) + "var " + identity.useLowerName() + "IsNull = mRec." + identity.useUpperName() + "IsNull;");
            outData.println(indent(4) + "mRec." + identity.useUpperName() + " = " + castOf(identity) + "wCursor." + cursorGet(identity, 0) + ";");
            outData.println(indent(4) + "mRec." + identity.useUpperName() + "IsNull = " + identity.useLowerName() + "IsNull;");
        } else {
            outData.println(indent(5) + "mRec." + identity.useUpperName() + " = " + castOf(identity) + "wCursor." + cursorGet(identity, 0) + ";");
        }
//    outData.println("        wCursor.Run();");
//    outData.println("        bool wResult = (wCursor.HasReader() && wCursor.Read());");
//    outData.println("        if (wResult == true)");
//    outData.println("          mRec." + identity.useLowerName() + " = " + castOf(identity) + "wCursor." + cursorGet(identity, 0) + ";");
//    outData.println("        if (wCursor.HasReader())");
//    outData.println("          wCursor.Close();");
//    outData.println("        return wResult;");
//    outData.println("      }");
//    outData.println("    }");
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "if (wCursor.HasReader())");
        outData.println(indent(4) + "{");
        outData.println(indent(5) + "wCursor.Close();");
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "return wResult;");
        outData.println(indent(3) + "}");
        outData.println(indent(2) + "}");
    }

    void generateReadOneProc(Proc proc, String mainName, PrintWriter outData) {
        outData.println(indent(2) + "public bool " + proc.upperFirst() + "(JConnect aConnect)");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "using (JCursor wCursor = new JCursor(aConnect))");
        outData.println(indent(3) + "{");
        if (doMSSqlStoredProcs(proc))
            outData.println(indent(4) + "wCursor.Procedure(Command" + proc.upperFirst() + "());");
        else {
            if (placeHolder.pairs.size() > 0)
                outData.println(indent(4) + "// format command to change {n} to ?, @Pn or :Pn placeholders depending on Vendor");
            outData.println(indent(4) + "wCursor.Format(Command" + proc.upperFirst() + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
        }
        for (int i = 0; i < placeHolder.pairs.size(); i++) {
            PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
            Field field = pair.field;
            String member = "";
            if (field.type == Field.BLOB)
                member = ".getBlob()";
            String tail = "";
            if (field.isNull)
                tail = ", mRec." + field.useUpperName() + "IsNull";
            if (field.type == Field.TIMESTAMP) {
                outData.println(indent(4) + "var " + field.useLowerName() + i + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetTimeStamp(ref " + field.useLowerName() + i + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + i + ";");
            } else if (field.type == Field.USERSTAMP) {
                outData.println(indent(4) + "var " + field.useLowerName() + i + " = mRec." + field.useUpperName() + ";");
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", wCursor.GetUserStamp(ref " + field.useLowerName() + i + "));");
                outData.println(indent(4) + "mRec." + field.useUpperName() + " = " + field.useLowerName() + i + ";");
            } else
                outData.println(indent(4) + "wCursor.Parameter(" + i + ", mRec." + field.useUpperName() + member + tail + ");");
        }
        outData.println(indent(4) + "wCursor.Run();");
        outData.println(indent(4) + "bool wResult = (wCursor.HasReader() && wCursor.Read());");
        outData.println(indent(4) + "if (wResult == true)");
        outData.println(indent(4) + "{");
        for (int i = 0; i < proc.outputs.size(); i++) {
            Field field = (Field) proc.outputs.elementAt(i);
            String member = "";
            if (field.type == Field.BLOB)
                member = ".Buffer";
            if (field.isNull && getDataType(field, new StringBuffer(), new StringBuffer()) == "string") {
                outData.println(indent(5) + "var " + field.useLowerName() + "IsNull = mRec." + field.useUpperName() + "IsNull;");
                outData.println(indent(5) + "mRec." + field.useUpperName() + member + " = " + castOf(field) + "wCursor.GetString(" + i + ", out " + field.useLowerName() + "IsNull);");
                outData.println(indent(5) + "mRec." + field.useUpperName() + "IsNull = " + field.useLowerName() + "IsNull;");
            } else if (field.isNull) {
                outData.println(indent(5) + "var " + field.useLowerName() + "IsNull = mRec." + field.useUpperName() + "IsNull;");
                outData.println(indent(5) + "mRec." + field.useUpperName() + member + " = " + castOf(field) + "wCursor." + cursorGet(field, i) + ";");
                outData.println(indent(5) + "mRec." + field.useUpperName() + "IsNull = " + field.useLowerName() + "IsNull;");
            } else {
                outData.println(indent(5) + "mRec." + field.useUpperName() + member + " = " + castOf(field) + "wCursor." + cursorGet(field, i) + ";");
            }
        }
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "if (wCursor.HasReader())");
        outData.println(indent(4) + "{");
        outData.println(indent(5) + "wCursor.Close();");
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "return wResult;");
        outData.println(indent(3) + "}");
        outData.println(indent(2) + "}");
    }

    String returning(Proc proc) {
        if (proc.hasReturning == false)
            return "";
        String tableName = proc.table.useName();
        String fieldName = "";
        for (int i = 0; i < proc.table.fields.size(); i++) {
            Field field = (Field) proc.table.fields.elementAt(i);
            if (field.isSequence == true) {
                fieldName = field.useName();
                break;
            }
        }
        return "aConnect, \"" + tableName + "\", \"" + fieldName + "\"";
    }

    /*String returningField(Proc proc)
    {
      if (proc.hasReturning == false)
        return "";
      String fieldName = "";
      for (int i = 0; i < proc.table.fields.size(); i++)
      {
        Field field = (Field)proc.table.fields.elementAt(i);
        if (field.isSequence == true)
        {
          fieldName = field.useName();
          break;
        }
      }
      return fieldName;
    }*/
    void generateFetchProc(Proc proc, String mainName, PrintWriter outData, boolean isLoaded) {
        outData.println(indent(2) + "private void " + proc.upperFirst() + "(JConnect aConnect)");
        outData.println(indent(2) + "{");
        if (doMSSqlStoredProcs(proc))
            outData.println(indent(3) + "mCursor.Procedure(Command" + proc.upperFirst() + "());");
        else {
            if (placeHolder.pairs.size() > 0)
                outData.println(indent(3) + "// format command to change {n} to ?, @Pn or :Pn placeholders depending on Vendor");
            outData.println(indent(3) + "mCursor.Format(Command" + proc.upperFirst() + "(" + returning(proc) + "), " + placeHolder.pairs.size() + ");");
        }
        for (int i = 0; i < placeHolder.pairs.size(); i++) {
            PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
            Field field = pair.field;
            String member = "";
            if (field.type == Field.BLOB)
                member = ".getBlob()";
            String tail = "";
            if (field.isNull)
                tail = ", mRec." + field.useUpperName() + "IsNull";
            outData.println(indent(3) + "mCursor.Parameter(" + i + ", mRec." + field.useUpperName() + member + tail + ");");
        }
        outData.println(indent(3) + "mCursor.Run();");
        outData.println(indent(2) + "}");
        outData.println(indent(2) + "private bool " + proc.upperFirst() + "Fetch()");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "bool wResult = (mCursor.HasReader() && mCursor.Read());");
        outData.println(indent(3) + "if (wResult == true)");
        outData.println(indent(3) + "{");
        for (int i = 0; i < proc.outputs.size(); i++) {
            Field field = (Field) proc.outputs.elementAt(i);
            String member = "";
            if (field.type == Field.BLOB)
                member = ".Buffer";
            if (field.isNull && getDataType(field, new StringBuffer(), new StringBuffer()) == "string") {
                outData.println(indent(4) + "var " + field.useLowerName() + "IsNull = mRec." + field.useUpperName() + "IsNull;");
                outData.println(indent(4) + "mRec." + field.useUpperName() + member + " = " + castOf(field) + "mCursor.GetString(" + i + ", out " + field.useLowerName() + "IsNull);");
                outData.println(indent(4) + "mRec." + field.useUpperName() + "IsNull = " + field.useLowerName() + "IsNull;");
            } else if (field.isNull) {
                outData.println(indent(4) + "var " + field.useLowerName() + "IsNull = mRec." + field.useUpperName() + "IsNull;");
                outData.println(indent(4) + "mRec." + field.useUpperName() + member + " = " + castOf(field) + "mCursor." + cursorGet(field, i) + ";");
                outData.println(indent(4) + "mRec." + field.useUpperName() + "IsNull = " + field.useLowerName() + "IsNull;");
            } else {
                outData.println(indent(4) + "mRec." + field.useUpperName() + member + " = " + castOf(field) + "mCursor." + cursorGet(field, i) + ";");
            }
        }
        outData.println(indent(3) + "}");
        outData.println(indent(3) + "else if (mCursor.HasReader())");
        outData.println(indent(3) + "{");
        outData.println(indent(4) + "mCursor.Close();");
        outData.println(indent(3) + "}");
        outData.println(indent(3) + "return wResult;");
        outData.println(indent(2) + "}");
        outData.println(indent(2) + "public void " + proc.upperFirst() + "Load(JConnect aConnect)");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "using (mCursor = new JCursor(aConnect))");
        outData.println(indent(3) + "{");
        outData.println(indent(4) + proc.upperFirst() + "(aConnect);");
        outData.println(indent(4) + "while (" + proc.upperFirst() + "Fetch())");
        outData.println(indent(4) + "{");
        outData.println(indent(5) + "mList.Add(mRec);");
        outData.println(indent(5) + "mRec = new " + mainName + "Rec();");
        outData.println(indent(4) + "}");
        outData.println(indent(3) + "}");
        outData.println(indent(2) + "}");
        outData.println(indent(2) + "public bool " + proc.upperFirst() + "First(JConnect aConnect)");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "using (mCursor = new JCursor(aConnect))");
        outData.println(indent(3) + "{");
        outData.println(indent(4) + proc.upperFirst() + "(aConnect);");
        outData.println(indent(4) + "if (" + proc.upperFirst() + "Fetch())");
        outData.println(indent(4) + "{");
        outData.println(indent(5) + "mCursor.Close();");
        outData.println(indent(5) + "return true;");
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "return false;");
        outData.println(indent(3) + "}");
        outData.println(indent(2) + "}");
        if (isLoaded) {
            if (useGenerics)
                outData.println(indent(2) + "public List<" + mainName + "Rec> Loaded { get { return mList; } }");
            else
                outData.println(indent(2) + "public ArrayList Loaded { get { return mList; } }");
        }
        outData.println(indent(2) + "public class " + proc.upperFirst() + "Ord");
        outData.println(indent(2) + "{");
        int noInDataSet = 0;
        for (int i = 0; i < proc.inputs.size(); i++) {
            Field field = (Field) proc.inputs.elementAt(i);
            outData.println(indent(3) + "public const int " + field.useUpperName() + " = " + noInDataSet + ";");
            noInDataSet++;
        }
        for (int i = 0; i < proc.outputs.size(); i++) {
            Field field = (Field) proc.outputs.elementAt(i);
            if (proc.hasInput(field.name))
                continue;
            outData.println(indent(3) + "public const int " + field.useUpperName() + " = " + noInDataSet + ";");
            noInDataSet++;
        }
        outData.println(indent(3) + "public static string ToString(int ordinal)");
        outData.println(indent(3) + "{");
        outData.println(indent(4) + "switch (ordinal)");
        outData.println(indent(4) + "{");
        noInDataSet = 0;
        for (int i = 0; i < proc.inputs.size(); i++) {
            Field field = (Field) proc.inputs.elementAt(i);
            outData.println(indent(5) + "case " + noInDataSet + ": return \"" + field.useUpperName() + "\";");
            noInDataSet++;
        }
        for (int i = 0; i < proc.outputs.size(); i++) {
            Field field = (Field) proc.outputs.elementAt(i);
            if (proc.hasInput(field.name))
                continue;
            outData.println(indent(5) + "case " + noInDataSet + ": return \"" + field.useUpperName() + "\";");
            noInDataSet++;
        }
        outData.println(indent(4) + "}");
        outData.println(indent(4) + "return \"<??\"+ordinal+\"??>\";");
        outData.println(indent(3) + "}");
        outData.println(indent(2) + "}");
        if (!noDatatables) {
            if (useSeparate == false && usePartials == false)
                generateFetchProcDataTables(proc, mainName, outData);
        }
    }

    void generateFetchProcDataTables(Proc proc, String mainName, PrintWriter outData) {
        outData.println(indent(2) + "public " + proc.table.useName() + proc.upperFirst() + "DataTable " + proc.upperFirst() + "DataTable()");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + proc.table.useName() + proc.upperFirst() + "DataTable wResult = new " + proc.table.useName() + proc.upperFirst() + "DataTable(mList);");
        outData.println(indent(3) + "return wResult;");
        outData.println(indent(2) + "}");
        outData.println(indent(2) + "public " + proc.table.useName() + proc.upperFirst() + "DataTable " + proc.upperFirst() + "DataTable(Connect aConnect)");
        outData.println(indent(2) + "{");
        outData.println(indent(3) + "" + proc.upperFirst() + "Load(aConnect);");
        outData.println(indent(3) + "return " + proc.upperFirst() + "DataTable();");
        outData.println(indent(2) + "}");
    }

    void generateProcFunctions(Proc proc, String name, PrintWriter outData, boolean isLoaded) {
        if (proc.outputs.size() > 0 && !proc.isSingle)
            generateFetchProc(proc, name, outData, isLoaded);
        else if (proc.outputs.size() > 0)
            generateReadOneProc(proc, name, outData);
        else if (proc.isInsert && proc.hasReturning)
            generateReturningProc(proc, name, outData);
        else
            generateNonQueryProc(proc, name, outData);
        if (useFunc)
            generateFunc(proc, name, outData);
    }

    void generateCClassTop(Proc proc, String mainName, PrintWriter outData, boolean doCursor) {
        outData.println(indent(1) + "[Serializable()]");
        outData.println(indent(1) + "public " + (usePartials ? "partial " : "") + "class " + mainName);
        outData.println(indent(1) + "{");
        if (doCursor == true || proc.hasNoData() == false) {
            outData.println(indent(2) + "private " + mainName + "Rec mRec;");
            outData.println(indent(2) + "public " + mainName + "Rec Rec { get { return mRec; } set { mRec = value; } }");
            if (doCursor == true || (proc.outputs.size() > 0 && !proc.isSingle)) {
                if (useGenerics)
                    outData.println(indent(2) + "private List<" + mainName + "Rec> mList;");
                else
                    outData.println(indent(2) + "private ArrayList mList;");
                outData.println(indent(2) + "public int Count { get { return mList.Count; } }");
                outData.println(indent(2) + "public JCursor mCursor;");
                outData.println(indent(2) + "public " + mainName + "Rec this[int i]");
                outData.println(indent(2) + "{");
                outData.println(indent(3) + "get");
                outData.println(indent(3) + "{");
                outData.println(indent(4) + "if (i >= 0 && i < mList.Count)");
                outData.println(indent(4) + "{");
                if (useGenerics)
                    outData.println(indent(5) + "return mList[i];");
                else
                    outData.println(indent(5) + "return (" + mainName + "Rec)mList[i];");
                outData.println(indent(4) + "}");
                outData.println(indent(4) + "throw new JPortalException(\"" + mainName + " index out of range\");");
                outData.println(indent(3) + "}");
                outData.println(indent(3) + "set");
                outData.println(indent(3) + "{");
                outData.println(indent(4) + "if (i < mList.Count)");
                outData.println(indent(4) + "{");
                outData.println(indent(5) + "mList.RemoveAt(i);");
                outData.println(indent(4) + "}");
                outData.println(indent(4) + "mList.Insert(i, value);");
                outData.println(indent(3) + "}");
                outData.println(indent(2) + "}");
                if (useYields) {
                    if (useGenerics)
                        outData.println(indent(2) + "public IEnumerable<" + mainName + "Rec> Yielded()");
                    else
                        outData.println(indent(2) + "public IEnumerable Yielded()");
                    outData.println(indent(2) + "{");
                    outData.println(indent(3) + "for (int i=0; i<Count; i++)");
                    outData.println(indent(4) + "yield return this[i];");
                    outData.println(indent(2) + "}");
                }
            }
            outData.println(indent(2) + "public void Clear()");
            outData.println(indent(2) + "{");
            if (doCursor == true || (proc.outputs.size() > 0 && !proc.isSingle))
                if (useGenerics)
                    outData.println(indent(3) + "mList = new List<" + mainName + "Rec>();");
                else
                    outData.println(indent(3) + "mList = new ArrayList();");
            outData.println(indent(3) + "mRec = new " + mainName + "Rec();");
            outData.println(indent(2) + "}");
            outData.println(indent(2) + "public " + mainName + "()");
            outData.println(indent(2) + "{");
            outData.println(indent(3) + "Clear();");
            outData.println(indent(2) + "}");
        }
    }

    boolean doMSSqlStoredProcs(Proc proc) {
        return (mSSqlStoredProcs == true && proc.dynamics.size() == 0) || (proc.isSProc == true && proc.dynamics.size() == 0);
    }

    void generateCode(Table table, Proc proc, PrintWriter outData) {
        if (proc.comments.size() > 0) {
            outData.println(indent(1) + "/// <summary>");
            for (int i = 0; i < proc.comments.size(); i++) {
                String comment = (String) proc.comments.elementAt(i);
                outData.println(indent(1) + "/// " + comment);
            }
            outData.println(indent(1) + "/// </summary>");
        }

        generateCClassTop(proc, table.useName() + proc.upperFirst(), outData, false);
        if (doMSSqlStoredProcs(proc) == true)
            generateStoredProcCommand(proc, outData);
        else
            generateCommand(proc, outData);
        generateProcFunctions(proc, table.useName() + proc.upperFirst(), outData, true);
        outData.println(indent(1) + "}");
    }

    void generateStdCode(Table table, Proc proc, PrintWriter outData, boolean firsttime, boolean isLoaded) {
        if (firsttime == true)
            generateCClassTop(proc, table.useName(), outData, table.hasCursorStdProc());
        if (proc.comments.size() > 0) {
            outData.println(indent(2) + "/// <summary>");
            for (int i = 0; i < proc.comments.size(); i++) {
                String comment = (String) proc.comments.elementAt(i);
                outData.println(indent(2) + "/// " + comment);
            }
            outData.println(indent(2) + "/// </summary>");
        }
        if (doMSSqlStoredProcs(proc) == true)
            generateStoredProcCommand(proc, outData);
        else
            generateCommand(proc, outData);
        generateProcFunctions(proc, table.useName(), outData, isLoaded);
    }

    String castOf(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return "(byte)";
            case Field.SHORT:
                return "(short)";
        }
        return "";
    }

    String validNull(Field field) {
        switch (field.type) {
            case Field.DATE:
            case Field.DATETIME:
            case Field.TIMESTAMP:
            case Field.TIME:
                return "DateTime.MinValue";
            case Field.BOOLEAN:
                return "false";
            case Field.BYTE:
            case Field.DOUBLE:
            case Field.FLOAT:
            case Field.IDENTITY:
            case Field.INT:
            case Field.LONG:
            case Field.SEQUENCE:
            case Field.BIGIDENTITY:
            case Field.BIGSEQUENCE:
            case Field.SHORT:
            case Field.MONEY:
            case Field.STATUS:
                return "0";
            case Field.UID:
                return "Guid.Empty";
        }
        return "null";
    }

    String cursorGet(Field field, int occurence) {
        String tail = ")";
        if (field.isNull)
            tail = ", out " + field.useLowerName() + "IsNull)";
        switch (field.type) {
            case Field.ANSICHAR:
                return "GetString(" + occurence + tail;
            case Field.BLOB:
                return "GetBlob(" + occurence + ", " + field.length + tail;
            case Field.BOOLEAN:
                return "GetBoolean(" + occurence + tail;
            case Field.BYTE:
                return "GetByte(" + occurence + tail;
            case Field.CHAR:
                return "GetString(" + occurence + tail;
            case Field.DATE:
                return "GetDateTime(" + occurence + tail;
            case Field.DATETIME:
                return "GetDateTime(" + occurence + tail;
            case Field.DYNAMIC:
                return "GetString(" + occurence + tail;
            case Field.DOUBLE:
            case Field.FLOAT:
                if (field.precision > 15)
                    return "GetDecimal(" + occurence + tail;
                return "GetDouble(" + occurence + tail;
            case Field.IDENTITY:
                return "GetInt(" + occurence + tail;
            case Field.INT:
                return "GetInt(" + occurence + tail;
            case Field.LONG:
                return "GetLong(" + occurence + tail;
            case Field.BIGSEQUENCE:
                return "GetLong(" + occurence + tail;
            case Field.BIGIDENTITY:
                return "GetLong(" + occurence + tail;
            case Field.MONEY:
                return "GetDecimal(" + occurence + tail;
            case Field.SEQUENCE:
                return "GetInt(" + occurence + tail;
            case Field.SHORT:
                return "GetShort(" + occurence + tail;
            case Field.TIME:
                return "GetDateTime(" + occurence + tail;
            case Field.TIMESTAMP:
                return "GetDateTime(" + occurence + tail;
            case Field.TLOB:
                return "GetString(" + occurence + tail;
            case Field.XML:
            case Field.BIGXML:
                return "GetString(" + occurence + tail;
            case Field.JSON:
            case Field.BIGJSON:
                return "GetString(" + occurence + tail;
            case Field.UID:
                return "GetGuid(" + occurence + tail;
            case Field.USERSTAMP:
                return "GetString(" + occurence + tail;
            default:
                break;
        }
        return "Get(" + occurence + tail;
    }

    String dataTableType(Field field) {
        switch (field.type) {
            case Field.ANSICHAR:
                return "String";
            case Field.BLOB:
                return "Byte[]";
            case Field.BOOLEAN:
                return "Boolean";
            case Field.BYTE:
                return "Byte";
            case Field.CHAR:
                return "String";
            case Field.DATE:
                return "DateTime";
            case Field.DATETIME:
                return "DateTime";
            case Field.DYNAMIC:
                return "String";
            case Field.DOUBLE:
            case Field.FLOAT:
                if (field.precision > 15)
                    return "String";
                return "Double";
            case Field.IDENTITY:
                return "Int32";
            case Field.BIGIDENTITY:
                return "Int64";
            case Field.INT:
                return "Int32";
            case Field.LONG:
                return "Int64";
            case Field.MONEY:
                return "String";
            case Field.SEQUENCE:
                return "Int32";
            case Field.BIGSEQUENCE:
                return "Int64";
            case Field.SHORT:
                return "Int16";
            case Field.TIME:
                return "DateTime";
            case Field.TIMESTAMP:
                return "DateTime";
            case Field.TLOB:
                return "String";
            case Field.XML:
            case Field.BIGXML:
                return "String";
            case Field.JSON:
            case Field.BIGJSON:
                return "String";

            case Field.UID:
                return "Guid";
            case Field.USERSTAMP:
                return "String";
            default:
                break;
        }
        return "dataTableType";
    }

    String fieldDef(Field field, String temp) {
        StringBuffer maker = new StringBuffer();
        StringBuffer temp2 = new StringBuffer(temp);
        String result = getDataType(field, maker, temp2);
        String set = indent(3) + "set { this." + field.useLowerName() + " = value; } \r\n";

        if (useNotify) {
            set = indent(3) + "set\r\n"
                    + indent(4) + "{\r\n"
                    + indent(5) + "this." + field.useLowerName() + " = value; \r\n"
                    + indent(5) + "NotifyPropertyChanged();\r\n"
                    + indent(4) + "}\r\n";

        }

        if (field.isNull && result == "string") {
            set = indent(3) + "set\r\n"
                    + indent(3) + "{\r\n"
                    + indent(4) + "this." + field.useLowerName() + " = value; \r\n"
                    + indent(4) + "this." + field.useUpperName() + "IsNull = value == null ? true : false;\r\n"
                    + (useNotify ? indent(4) + "NotifyPropertyChanged();\r\n" : "")
                    + indent(3) + "}\r\n";
        }
        String ret = indent(2) + "private " + result + " " + field.useLowerName() + maker.toString() + ";\r\n"
                + indent(2) + temp2.toString() + ")]\r\n"
                + indent(2) + "public " + result + " " + field.useUpperName() + "\r\n"
                + indent(2) + "{ \r\n"
                + indent(3) + "get { return this." + field.useLowerName() + ";}\r\n"
                + set
                + indent(2) + "}";
        return ret;
    }

    String getDataType(Field field, StringBuffer maker, StringBuffer temp) {
        String result;
        switch (field.type) {
            case Field.ANSICHAR:
            case Field.CHAR:
            case Field.USERSTAMP:
                result = "string";
                break;
            case Field.MONEY:
                result = "decimal";
                break;
            case Field.BLOB:
                result = "JPBlob";
                maker.append(" = new JPBlob()");
                break;
            case Field.TLOB:
                result = "string";
                break;
            case Field.XML:
            case Field.BIGXML:
                result = "string";
                break;
            case Field.JSON:
            case Field.BIGJSON:
                result = "string";
                break;
            case Field.UID:
                result = "Guid";
                break;
            case Field.DATE:
            case Field.DATETIME:
            case Field.TIME:
            case Field.TIMESTAMP:
                result = "DateTime";
                temp.append(", IsVersion=true");
                break;
            case Field.BOOLEAN:
                result = "bool";
                break;
            case Field.BYTE:
                result = "byte";
                break;
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
            case Field.SEQUENCE:
            case Field.IDENTITY:
                result = "int";
                break;
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
                result = "long";
                break;
            case Field.SHORT:
                result = "short";
                break;
            case Field.DYNAMIC:
                result = "string";
                break;
            default:
                result = "whoknows";
                break;
        }
        return result;
    }

    String returnNullableDataType(Field field, StringBuffer maker, StringBuffer temp) {
        String datatype = getDataType(field, maker, temp);
        if (getNullableType(field) && field.isNull)
            datatype = datatype + "?";
        return datatype;
    }

    boolean getNullableType(Field field) {
        boolean nullableType;
        switch (field.type) {

            case Field.MONEY:
            case Field.DATE:
            case Field.DATETIME:
            case Field.TIME:
            case Field.TIMESTAMP:
            case Field.BOOLEAN:
            case Field.BYTE:
            case Field.STATUS:
            case Field.DOUBLE:
            case Field.FLOAT:
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
            case Field.SHORT:
                nullableType = true;
                break;
            default:
                nullableType = false;
                break;
        }
        return nullableType;
    }

    String fieldCastNo(Field field) {
        String result;
        switch (field.type) {
            case Field.ANSICHAR:
            case Field.CHAR:
            case Field.USERSTAMP:
            case Field.DYNAMIC:
                result = "string";
                break;
            case Field.MONEY:
                result = "decimal";
                break;
            case Field.BLOB:
                result = "JPBlob";
                break;
            case Field.TLOB:
                result = "string";
                break;
            case Field.XML:
            case Field.BIGXML:
                result = "string";
                break;
            case Field.JSON:
            case Field.BIGJSON:
                result = "string";
                break;
            case Field.UID:
                result = "Guid";
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
                result = "byte";
                break;
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
            case Field.SEQUENCE:
            case Field.IDENTITY:
                result = "int";
                break;
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
                result = "long";
                break;
            case Field.SHORT:
                result = "short";
                break;
            default:
                result = "whoknows";
                break;
        }
        return result;
    }

    String fieldCast(Field field) {
        return "(" + fieldCastNo(field) + ")";
    }

    /**
     * Translates field type to SQLServer SQL column types
     */
    String varType(Field field) {
        switch (field.type) {
            case Field.BOOLEAN:
                return "bit";
            case Field.BYTE:
                return "tinyint";
            case Field.SHORT:
                return "smallint";
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "integer";
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
                return "longint";
            case Field.CHAR:
                if (field.length > 8000) {
                    return field.name + " varchar(MAX)";
                }
                return field.name + " varchar(" + String.valueOf(field.length) + ")";
            case Field.ANSICHAR:
                return "char(" + String.valueOf(field.length) + ")";
            case Field.DATE:
                return "datetime";
            case Field.DATETIME:
                return "datetime";
            case Field.TIME:
                return "datetime";
            case Field.TIMESTAMP:
                return "datetime";
            case Field.FLOAT:
            case Field.DOUBLE:
                if (field.precision > 15)
                    return "decimal";
                return "float";
            case Field.BLOB:
                return "image";
            case Field.TLOB:
                return "text";
            case Field.XML:
            case Field.BIGXML:
                return "xml";
            case Field.JSON:
            case Field.BIGJSON:
                return "json";
            case Field.UID:
                return "uniqueidentifier";
            case Field.MONEY:
                return "money";
            case Field.USERSTAMP:
                return "varchar(50)";
            default:
                break;
        }
        return "unknown";
    }

    String indent(int lvl) {
        if (lvl == 0) {
            return "";
        }
        String ind = "";
        for (int i = 0; i < lvl; i++) {
            ind = ind + "    ";
        }
        return ind;
    }
}
