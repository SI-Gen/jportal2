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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class JavaJCCode extends BaseGenerator implements IBuiltInSIProcessor {

    public static final String GENERATE_PROCS_IO_ERROR = "Generate Procs IO Error";

    public JavaJCCode() {
        super(JavaJCCode.class);
    }

    private static final Logger logger = LoggerFactory.getLogger(JavaJCCode.class);

    @Override
    public String description() {
        return "generate Java code for jdbc and crackle consumption - separates structs and others from main";
    }

    @Override
    public String documentation() {
        return "generate Java code for jdbc and crackle consumption - separates structs and others from main";
    }

    /**
     * Generates the procedure classes for each table present.
     *
     * @param database
     * @param output
     */
    @Override
    public void generate(Database database, String output) {

        for (int i = 0; i < database.tables.size(); i++) {
            Table table = database.tables.elementAt(i);
            generateStructs(table, output);
            generate(table, output);
        }
    }

    private void generateStructs(Table table, String output) {
        generateStdProcStruct(table, output);
        generateOtherProcStructs(table, output);
    }

    private void generateStdProcStruct(Table table, String output) {
        try {

            if (logger.isInfoEnabled()) {
                logger.info("Code: {}{}Struct.java", output, table.useName());
            }

            try (PrintWriter outData = openOutputFileForGeneration("Java",
                    output + table.useName() + "Struct.java")) {
                if (table.database.packageName.length() > 0) {
                    outData.println("package " + table.database.packageName + ";");
                    outData.println();
                }
                outData.println("import java.io.Serializable;");
                outData.println("import java.sql.*;");
                outData.println("import java.math.*;");
                outData.println();
                outData.println("/**");
                for (int i = 0; i < table.comments.size(); i++) {
                    String s = table.comments.elementAt(i);
                    outData.println(" *" + s);
                }
                outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
                outData.println(" * Does not use inner public classes and separates structs out.");
                outData.println(" */");
                outData.println("public class " + table.useName() + "Struct implements Serializable");
                outData.println("{");
                outData.println("  private static final long serialVersionUID = 1L;");
                generateEnum(table, outData);
                for (int i = 0; i < table.fields.size(); i++) {
                    Field field = table.fields.elementAt(i);
                    if (!field.comments.isEmpty()) {
                        outData.println("  /**");
                        for (int c = 0; c < field.comments.size(); c++) {
                            String s = field.comments.elementAt(c);
                            outData.println("   *" + s);
                        }
                        outData.println("   */");
                    }
                    outData.println("  public " + javaVar(field) + ";");
                    outData.println("  public " + getterSetter(field));
                }
                outData.println("  public " + table.useName() + "Struct()");
                outData.println("  {");
                int maxSize = 0;
                for (int i = 0; i < table.fields.size(); i++) {
                    Field field = table.fields.elementAt(i);
                    if (field.useName().length() > maxSize)
                        maxSize = field.useName().length();
                    outData.println("    " + initJavaVar(field));
                }
                outData.println("  }");

                outData.println("  public String toString()");
                outData.println("  {");
                outData.println("    String CRLF = System.lineSeparator();");
                for (int i = 0; i < table.fields.size(); i++) {
                    if (i == 0)
                        outData.print("    return ");
                    else
                        outData.print("         + ");
                    Field field = table.fields.elementAt(i);
                    int no = maxSize - field.useName().length();
                    outData.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useName() + " + CRLF");
                }
                outData.println("    ;");
                outData.println("  }");
                outData.println("}");
                outData.flush();
            }
        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR);
        }
    }

    private void generateOtherProcStructs(Table table, String output) {
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = table.procs.elementAt(i);
            if (proc.isData)
                continue;
            if (!proc.isStd && !proc.hasNoData())
                generateOtherProcStruct(table, proc, output);
        }
    }

    private void generateOtherProcStruct(Table table, Proc proc, String output) {
        try {
            if (logger.isInfoEnabled()){
                logger.info("Code: {}Struct.java", output + table.useName() + proc.upperFirst());
            }

            try (PrintWriter outData2 = openOutputFileForGeneration("java",
                    output + table.useName() + proc.upperFirst() + "Struct.java")) {
                if (table.database.packageName.length() > 0) {
                    outData2.println("package " + table.database.packageName + ";");
                    outData2.println();
                }
                outData2.println("import java.io.Serializable;");
                outData2.println("import java.sql.*;");
                outData2.println("import java.math.*;");
                outData2.println();
                outData2.println("/**");
                for (int j = 0; j < proc.comments.size(); j++) {
                    String comment = proc.comments.elementAt(j);
                    outData2.println(" *" + comment);
                }
                outData2.println(" */");
                outData2.println("public class " + table.useName() + proc.upperFirst() + "Struct implements Serializable");
                outData2.println("{");
                int maxSize = 0;
                for (int j = 0; j < proc.inputs.size(); j++) {
                    Field field = proc.inputs.elementAt(j);
                    if (field.useName().length() > maxSize)
                        maxSize = field.useName().length();
                    outData2.println("  /**");
                    for (int c = 0; c < field.comments.size(); c++) {
                        String s = field.comments.elementAt(c);
                        outData2.println("   *" + s);
                    }
                    if (!proc.hasOutput(field.name))
                        outData2.println("   * (input)");
                    else
                        outData2.println("   * (input/output)");
                    outData2.println("   */");
                    outData2.println("  public " + javaVar(field) + ";");
                    outData2.println("  public " + getterSetter(field));
                }
                for (int j = 0; j < proc.outputs.size(); j++) {
                    Field field = proc.outputs.elementAt(j);
                    if (field.useName().length() > maxSize)
                        maxSize = field.useName().length();
                    if (!proc.hasInput(field.name)) {
                        outData2.println("  /**");
                        for (int c = 0; c < field.comments.size(); c++) {
                            String s = field.comments.elementAt(c);
                            outData2.println("   *" + s);
                        }
                        outData2.println("   * (output)");
                        outData2.println("   */");
                        outData2.println("  public " + javaVar(field) + ";");
                        outData2.println("  public " + getterSetter(field));
                    }
                }
                for (int j = 0; j < proc.dynamics.size(); j++) {
                    String s = proc.dynamics.elementAt(j);
                    if (s.length() > maxSize)
                        maxSize = s.length();
                    outData2.println("  /**");
                    outData2.println("   * (dynamic)");
                    outData2.println("   */");
                    outData2.println("  public String " + s + ";");
                }
                outData2.println("  public " + table.useName() + proc.upperFirst() + "Struct()");
                outData2.println("  {");
                for (int j = 0; j < proc.inputs.size(); j++) {
                    Field field = proc.inputs.elementAt(j);
                    outData2.println("    " + initJavaVar(field));
                }
                for (int j = 0; j < proc.outputs.size(); j++) {
                    Field field = proc.outputs.elementAt(j);
                    if (!proc.hasInput(field.name))
                        outData2.println("    " + initJavaVar(field));
                }
                for (int j = 0; j < proc.dynamics.size(); j++) {
                    String s = proc.dynamics.elementAt(j);
                    outData2.println("    " + s + " = \"\";");
                }
                outData2.println("  }");
                outData2.println("  public String toString()");
                outData2.println("  {");
                outData2.println("    String CRLF = System.lineSeparator();");
                String ret = "    return ";
                for (int j = 0; j < proc.inputs.size(); j++) {
                    outData2.print(ret);
                    ret = "         + ";
                    Field field = proc.inputs.elementAt(j);
                    int no = maxSize - field.useName().length();
                    outData2.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useName() + " + CRLF");
                }
                for (int j = 0; j < proc.outputs.size(); j++) {
                    Field field = proc.outputs.elementAt(j);
                    if (!proc.hasInput(field.name)) {
                        outData2.print(ret);
                        ret = "         + ";
                        int no = maxSize - field.useName().length();
                        outData2.println("\"  " + field.useName() + padded(no + 1) + ": \" + " + field.useName() + " + CRLF");
                    }
                }
                for (int j = 0; j < proc.dynamics.size(); j++) {
                    String s = proc.dynamics.elementAt(j);
                    outData2.print(ret);
                    ret = "         + ";
                    int no = maxSize - s.length();
                    outData2.println("\"  " + s + padded(no + 1) + ": \" + " + s + " + CRLF");
                }
                outData2.println("    ;");
                outData2.println("  }");
                outData2.println("}");
                outData2.flush();
            }
        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR);
        }
    }

    /**
     * Build of standard and user defined procedures
     */
    private void generate(Table table, String output) {
        generateStdProcs(table, output);
        generateOtherProcs(table, output);
    }

    /**
     * Build of all required standard procedures
     */
    private String extendsName;

    private void generateStdProcs(Table table, String output) {
        try {

            if (logger.isInfoEnabled()) {
                logger.info("Code: {}{}.java", output, table.useName());
            }

            try (PrintWriter outData = openOutputFileForGeneration("java", output + table.useName() + ".java")) {

                if (table.database.packageName.length() > 0) {
                    outData.println("package " + table.database.packageName + ";");
                    outData.println("");
                }
                outData.println("import bbd.jportal.*;");
                outData.println("import java.sql.*;");
                outData.println("import java.math.*;");
                outData.println("import java.util.*;");
                outData.println("");
                outData.println("/**");
                for (int i = 0; i < table.comments.size(); i++) {
                    String s = table.comments.elementAt(i);
                    outData.println(" *" + s);
                }
                outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
                outData.println(" */");
                extendsName = table.useName() + "Struct";
                outData.println("public class " + table.useName() + " extends " + extendsName);
                outData.println("{");
                outData.println("  Connector connector;");
                outData.println("  Connection connection;");
                outData.println("  /**");
                outData.println("   * @param Connector for specific database");
                outData.println("   */");

                outData.println("  public " + table.useName() + "()");
                outData.println("  {");
                outData.println("    super();");
                outData.println("  }");

                outData.println("  public void setConnector(Connector conn)");
                outData.println("  {");
                outData.println("    this.connector = conn;");
                outData.println("    connection = connector.connection;");
                outData.println("  }");

                outData.println("  public " + table.useName() + "(Connector connector)");
                outData.println("  {");
                outData.println("    super();");
                outData.println("    this.connector = connector;");
                outData.println("    connection = connector.connection;");
                outData.println("  }");
                for (int i = 0; i < table.procs.size(); i++) {
                    Proc proc = table.procs.elementAt(i);
                    if (proc.isData)
                        continue;
                    if (proc.isStd)
                        emitProc(proc, outData);
                    else if (proc.hasNoData())
                        emitStaticProc(proc, outData);
                }
                outData.println("}");
                outData.flush();
            }
        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR);
        }
    }

    /**
     * Build of user defined procedures
     */
    private void generateOtherProcs(Table table, String output) {
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = table.procs.elementAt(i);
            if (proc.isData)
                continue;
            if (!proc.isStd && !proc.hasNoData())
                generateOtherProc(table, proc, output);
        }
    }

    private void generateOtherProc(Table table, Proc proc, String output) {
        try {

            if (logger.isInfoEnabled()) {
                logger.info("Code: {}.java", output + table.useName() + proc.upperFirst());
            }

            try (PrintWriter outData = openOutputFileForGeneration("java",
                    output + table.useName() + proc.upperFirst() + ".java")) {
                if (table.database.packageName.length() > 0) {
                    outData.println("package " + table.database.packageName + ";");
                    outData.println("");
                }
                outData.println("import bbd.jportal.*;");
                outData.println("import java.sql.*;");
                outData.println("import java.math.*;");
                outData.println("import java.util.*;");
                outData.println("");
                outData.println("/**");
                for (int j = 0; j < proc.comments.size(); j++) {
                    String comment = proc.comments.elementAt(j);
                    outData.println(" *" + comment);
                }
                outData.println(" */");
                extendsName = table.useName() + proc.upperFirst() + "Struct";
                outData.println("public class " + table.useName() + proc.upperFirst() + " extends " + extendsName);
                outData.println("{");
                outData.println("  Connector connector;");
                outData.println("  Connection connection;");

                outData.println("  public " + table.useName() + proc.upperFirst() + "()");
                outData.println("  {");
                outData.println("    super();");
                outData.println("  }");

                outData.println("  public void setConnector(Connector conn)");
                outData.println("  {");
                outData.println("    this.connector = conn;");
                outData.println("    connection = connector.connection;");
                outData.println("  }");

                outData.println("  public " + table.useName() + proc.upperFirst() + "(Connector connector)");
                outData.println("  {");
                outData.println("    super();");
                outData.println("    this.connector = connector;");
                outData.println("    connection = connector.connection;");
                outData.println("  }");
                emitProc(proc, outData);
                outData.println("}");
                outData.flush();
            }
        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR);
        }
    }

    private void generateEnum(Table table, PrintWriter outData) {
        for (int i = 0; i < table.fields.size(); i++) {
            Field field = (Field) table.fields.elementAt(i);
            if (field.enums.size() > 0) {
                outData.println("  public static enum " + getEnumTypeName(field));
                outData.println("  {");
                for (int j = 0; j < field.enums.size(); j++) {
                    bbd.jportal2.Enum element = field.enums.elementAt(j);
                    String evalue = "" + element.value;
                    if (field.type == Field.ANSICHAR && field.length == 1)
                        evalue = "'" + (char) element.value + "'";
                    String keyName = underScoreWords(element.name).toUpperCase();
                    outData.println("    " + keyName + "(" + evalue + ", \"" + splitWords(element.name) + "\")" + (((j + 1) < field.enums.size()) ? "," : ";"));
                }
                outData.println("    public int key;");
                outData.println("    public String value;");
                outData.println("    E" + field.useUpperName() + "(int key, String value)");
                outData.println("    {");
                outData.println("      this.key = key;");
                outData.println("      this.value = value;");
                outData.println("    }");
                outData.println("    public static E" + field.useUpperName() + " get(int key)");
                outData.println("    {");
                outData.println("      for (E" + field.useUpperName() + " op : values())");
                outData.println("        if (op.key == key) return op;");
                outData.println("      return null;");
                outData.println("    }");
                outData.println("    public String toString()");
                outData.println("    {");
                outData.println("      return value;");
                outData.println("    }");
                outData.println("  }");
            }
        }
    }

    private String underScoreWords(String input) {
        char[] bits = input.toCharArray();
        StringBuffer buffer = new StringBuffer();
        buffer.append(bits[0]);
        for (int i = 1; i < bits.length; i++) {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(bits[i]) >= 0
                    && bits[i - 1] != ' ') {
                buffer.append('_');
                buffer.append(bits[i]);
            } else
                buffer.append(bits[i]);
        }
        return buffer.toString();
    }

    private String splitWords(String input) {
        char[] bits = underScoreWords(input).toCharArray();
        StringBuffer buffer = new StringBuffer();
        buffer.append(bits[0]);
        for (int i = 1; i < bits.length; i++) {
            if (bits[i] == '_')
                buffer.append(' ');
            else
                buffer.append(bits[i]);
        }
        return buffer.toString();
    }

    private String getEnumTypeName(Field field){
        return "E" + field.useUpperName();
    }

    /**
     *
     */
    private PlaceHolder placeHolders;

    /**
     * Emits a static or class method
     */
    private void emitStaticProc(Proc proc, PrintWriter outData) {
        outData.println("  /**");
        outData.println("   * class method as it has no input or output.");
        outData.println("   * @exception SQLException is passed through");
        outData.println("   */");
        outData.println("  public static void " + proc.lowerFirst() + "(Connector connector) throws SQLException");
        outData.println("  {");
        placeHolders = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
        Vector<?> lines = placeHolders.getLines();
        outData.println("    String statement = ");
        String plus = "    ";
        for (int i = 0; i < lines.size(); i++) {
            outData.println(plus + (String) lines.elementAt(i));
            plus = "    +";
        }
        outData.println("    ;");
        outData.println("    PreparedStatement prep = connector.prepareStatement(statement);");
        outData.println("    prep.executeUpdate();");
        outData.println("    prep.close();");
        outData.println("  }");
    }

    /**
     * Emits class method for processing the database activity
     */
    private void emitProc(Proc proc, PrintWriter outData) {
        outData.println("  /**");
        if (!proc.comments.isEmpty()) {
            for (int i = 0; i < proc.comments.size(); i++) {
                String comment = proc.comments.elementAt(i);
                outData.println("    *" + comment);
            }
        }
        if (proc.outputs.isEmpty())
            outData.println("   * Returns no output.");
        else if (proc.isSingle) {
            outData.println("   * Returns at most one record.");
            outData.println("   * @return true if a record is found");
        } else {
            outData.println("   * Returns any number of records.");
            outData.println("   * @return result set of records found");
        }
        outData.println("   * @exception SQLException is passed through");
        outData.println("   */");
        String procName = proc.lowerFirst();
        if (proc.outputs.isEmpty())
            outData.println("  public void " + procName + "() throws SQLException");
        else if (proc.isSingle)
            outData.println("  public boolean " + procName + "() throws SQLException");
        else
            outData.println("  public Query " + procName + "() throws SQLException");
        outData.println("  {");
        placeHolders = new PlaceHolder(proc, PlaceHolder.QUESTION, "");
        Vector<?> lines = placeHolders.getLines();

        Field primaryKeyField = null;
        for (int i = 0; i < proc.table.fields.size() && primaryKeyField == null; i++) {
            Field fEval = proc.table.fields.get(i);
            if (fEval.isPrimaryKey)
                primaryKeyField = fEval;
        }

        if (proc.hasReturning)
            outData.println("Connector.Returning _ret = connector.getReturning(\"" + proc.table.name + "\",\"" + primaryKeyField.useName() + "\");");


        outData.println("    String statement = ");
        String plus = "      ";
        for (int i = 0; i < lines.size(); i++) {
            outData.println(plus + (String) lines.elementAt(i));
            plus = "    + ";
        }
        outData.println("    ;");
        outData.println("    PreparedStatement prep = connector.prepareStatement(statement);");
        for (int i = 0; i < proc.inputs.size(); i++) {
            Field field = proc.inputs.elementAt(i);
            if (proc.isInsert) {
                if (field.type == Field.BIGSEQUENCE)
                    outData.println("    " + field.useName() + " = connector.getBigSequence(\"" + proc.table.name + "\");");
                else if (field.type == Field.SEQUENCE)
                    outData.println("    " + field.useName() + " = connector.getSequence(\"" + proc.table.name + "\");");
            }
            if (field.type == Field.TIMESTAMP)
                outData.println("    " + field.useName() + " = connector.getTimestamp();");
            if (field.type == Field.USERSTAMP)
                outData.println("    " + field.useName() + " = connector.getUserstamp();");
        }
        Vector<PlaceHolderPairs> pairs = placeHolders.getPairs();
        for (int i = 0; i < pairs.size(); i++) {
            PlaceHolderPairs pair = pairs.elementAt(i);
            Field field = pair.field;
            outData.print("    prep.set");
            outData.print(setType(field));
            outData.print("(");
            outData.print(i + 1);
            outData.println(", " + field.useName() + ");");
        }
        if (!proc.outputs.isEmpty()) {
            outData.println("    ResultSet result = prep.executeQuery();");
            if (!proc.isSingle) {
                outData.println("    Query query = new Query(prep, result);");
                outData.println("    return query;");
                outData.println("  }");
                outData.println("  /**");
                outData.println("   * Returns the next record in a result set.");
                outData.println("   * @param result The result set for the query.");
                outData.println("   * @return true while records are found.");
                outData.println("   * @exception SQLException is passed through");
                outData.println("   */");
                outData.println("  public boolean " + procName + "(Query query) throws SQLException");
                outData.println("  {");
                outData.println("    if (!query.result.next())");
                outData.println("    {");
                outData.println("      query.close();");
                outData.println("      return false;");
                outData.println("    }");
                outData.println("    ResultSet result = query.result;");
            } else {
                outData.println("    if (!result.next())");
                outData.println("    {");
                outData.println("      result.close();");
                outData.println("      prep.close();");
                outData.println("      return false;");
                outData.println("    }");
            }
            for (int i = 0; i < proc.outputs.size(); i++) {
                Field field = proc.outputs.elementAt(i);
                outData.print("    " + field.useName() + " =  result.get");
                outData.print(setType(field));
                outData.print("(");
                outData.print(i + 1);
                outData.println(");");
            }
            if (proc.isSingle) {
                outData.println("    result.close();");
                outData.println("    prep.close();");
            }
            outData.println("    return true;");
        } else {
            outData.println("    prep.executeUpdate();");
            outData.println("    prep.close();");
        }
        outData.println("  }");

        if (!proc.outputs.isEmpty() && !proc.isSingle) {
            outData.println("  /**");
            outData.println("   * Returns all the records in a result set as array of " + extendsName + ".");
            outData.println("   * @return array of " + extendsName + ".");
            outData.println("   * @exception SQLException is passed through");
            outData.println("   */");
            outData.println("  public " + extendsName + "[] " + procName + "Load() throws SQLException");
            outData.println("  {");
            outData.println("    Vector recs = new Vector();");
            outData.println("    Query query = " + procName + "();");
            outData.println("    while (" + procName + "(query) == true)");
            outData.println("    {");
            outData.println("      " + extendsName + " rec = new " + extendsName + "();");
            for (int i = 0; i < proc.outputs.size(); i++) {
                Field field = proc.outputs.elementAt(i);
                outData.println("      rec." + field.useName() + " = " + field.useName() + ";");
            }
            outData.println("      recs.addElement(rec);");
            outData.println("    }");
            outData.println("    " + extendsName + "[] result = new " + extendsName + "[recs.size()];");
            outData.println("    for (int i=0; i<recs.size();i++)");
            outData.println("      result[i] = (" + extendsName + ")recs.elementAt(i); ");
            outData.println("    return result;");
            outData.println("  }");
        }
        if (!proc.inputs.isEmpty() || !proc.dynamics.isEmpty()) {
            outData.println("  /**");
            if (proc.outputs.isEmpty())
                outData.println("   * Returns no records.");
            else if (proc.isSingle) {
                outData.println("   * Returns at most one record.");
                outData.println("   * @return true if a record is returned.");
            } else {
                outData.println("   * Returns any number of records.");
                outData.println("   * @return result set of records found");
            }
            for (int i = 0; i < proc.inputs.size(); i++) {
                Field field = proc.inputs.elementAt(i);
                if ((field.isSequence && proc.isInsert)
                        || (field.type == Field.TIMESTAMP)
                        || (field.type == Field.USERSTAMP))
                    continue;
                if (!field.isPrimaryKey)
                    continue;
                outData.println("   * @param " + field.useName() + " key input.");
            }
            for (int i = 0; i < proc.inputs.size(); i++) {
                Field field = proc.inputs.elementAt(i);
                if ((field.isSequence && proc.isInsert)
                        || (field.type == Field.TIMESTAMP)
                        || (field.type == Field.USERSTAMP))
                    continue;
                if (field.isPrimaryKey)
                    continue;
                outData.println("   * @param " + field.useName() + " input.");
            }
            for (int i = 0; i < proc.dynamics.size(); i++)
                outData.println("   * @param " + proc.name + " dynamic input.");
            outData.println("   * @exception SQLException is passed through");
            outData.println("   */");
            if (proc.outputs.isEmpty())
                outData.println("  public void " + procName + "(");
            else if (proc.isSingle)
                outData.println("  public boolean " + procName + "(");
            else
                outData.println("  public Query " + procName + "(");
            String comma = "    ";
            for (int i = 0; i < proc.inputs.size(); i++) {
                Field field = proc.inputs.elementAt(i);
                if ((field.isSequence && proc.isInsert)
                        || (field.type == Field.TIMESTAMP)
                        || (field.type == Field.USERSTAMP))
                    continue;
                if (!field.isPrimaryKey)
                    continue;
                outData.println(comma + javaVar(field));
                comma = "  , ";
            }
            for (int i = 0; i < proc.inputs.size(); i++) {
                Field field = proc.inputs.elementAt(i);
                if ((field.isSequence && proc.isInsert)
                        || (field.type == Field.TIMESTAMP)
                        || (field.type == Field.USERSTAMP))
                    continue;
                if (field.isPrimaryKey)
                    continue;
                outData.println(comma + javaVar(field));
                comma = "  , ";
            }
            for (int i = 0; i < proc.dynamics.size(); i++) {
                String name = proc.dynamics.elementAt(i);
                outData.println(comma + "String " + name);
                comma = "  , ";
            }
            outData.println("  ) throws SQLException");
            outData.println("  {");
            for (int i = 0; i < proc.inputs.size(); i++) {
                Field field = proc.inputs.elementAt(i);
                if ((field.isSequence && proc.isInsert)
                        || (field.type == Field.TIMESTAMP)
                        || (field.type == Field.USERSTAMP))
                    continue;
                String usename = field.useName();
                outData.println("    this." + usename + " = " + usename + ";");
            }
            for (int i = 0; i < proc.dynamics.size(); i++) {
                String name = proc.dynamics.elementAt(i);
                outData.println("    this." + name + " = " + name + ";");
            }
            if (!proc.outputs.isEmpty())
                outData.println("    return " + procName + "();");
            else
                outData.println("    " + procName + "();");
            outData.println("  }");
        }
    }

    /**
     * Translates field type to java data member type
     */
    private String javaVar(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return "Byte " + field.useName();
            case Field.SHORT:
                return "Short " + field.useName();
            case Field.BIGSEQUENCE:
                return "Long " + field.useName();
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "Integer " + field.useName();
            case Field.LONG:
                return "Long " + field.useName();
            case Field.CHAR:
            case Field.ANSICHAR:
                return "String " + field.useName();
            case Field.DATE:
                return "java.sql.Date " + field.useName();
            case Field.DATETIME:
                return "Timestamp " + field.useName();
            case Field.TIME:
                return "Time " + field.useName();
            case Field.TIMESTAMP:
                return "Timestamp " + field.useName();
            case Field.FLOAT:
            case Field.DOUBLE:
                return "Double " + field.useName();
            case Field.BLOB:
            case Field.TLOB:
                return "String " + field.useName();
            case Field.MONEY:
                return "BigDecimal " + field.useName();
            case Field.USERSTAMP:
                return "String " + field.useName();
            default:
                return "unknown";
        }
    }

    /**
     * Translates field type to java data member type
     */
    private static String getterSetter(Field field) {
        String type = null;
        switch (field.type) {
            case Field.BYTE:
                type = "Byte ";
                break;
            case Field.SHORT:
                type = "Short ";
                break;
            case Field.BIGSEQUENCE:
                type = "Long ";
                break;
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                type = "Integer ";
                break;
            case Field.LONG:
                type = "Long ";
                break;
            case Field.CHAR:
            case Field.ANSICHAR:
                type = "String ";
                break;
            case Field.DATE:
                type = "java.sql.Date ";
                break;
            case Field.DATETIME:
                type = "Timestamp ";
                break;
            case Field.TIME:
                type = "Time ";
                break;
            case Field.TIMESTAMP:
                type = "Timestamp ";
                break;
            case Field.FLOAT:
            case Field.DOUBLE:
                type = "Double ";
                break;
            case Field.BLOB:
            case Field.TLOB:
                type = "String ";
                break;
            case Field.MONEY:
                type = "BigDecimal ";
                break;
            case Field.USERSTAMP:
                type = "String ";
                break;
        }
        if (type == null) {
            return "unknown";

        } else {
            return type + "get" + field.useName() + "(){ return " + field.useName() + "; } \n  public void set" + field.useName() + "(" + type + " " + field.useName() + "){ this." + field.useName() + " = " + field.useName() + "; }\n";

        }
    }

    /**
     * returns the data member initialisation code (not always necessary in java but still we do it)
     */
    private static String initJavaVar(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return field.useName() + " = 0;";
            case Field.CHAR:
            case Field.ANSICHAR:
                return field.useName() + " = \"\";";
            case Field.DATE:
                return field.useName() + " = new Date(0);";
            case Field.DATETIME:
                return field.useName() + " = new Timestamp(0);";
            case Field.FLOAT:
            case Field.DOUBLE:
                return field.useName() + " = 0.0;";
            case Field.BLOB:
            case Field.TLOB:
                return field.useName() + " = \"\";";
            case Field.BIGSEQUENCE:
                return field.useName() + " = 0L;";
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return field.useName() + " = 0;";
            case Field.LONG:
                return field.useName() + " = 0L;";
            case Field.MONEY:
                return field.useName() + " = BigDecimal.ZERO;";
            case Field.SHORT:
                return field.useName() + " = 0;";
            case Field.TIME:
                return field.useName() + " = new Time(0);";
            case Field.TIMESTAMP:
                return field.useName() + " = new Timestamp(0);";
            case Field.USERSTAMP:
                return field.useName() + " = \"\";";
        }
        return "unknown";
    }

    /**
     * JDBC get and set type for field data transfers
     */
    private static String setType(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return "Byte";
            case Field.CHAR:
            case Field.ANSICHAR:
                return "String";
            case Field.DATE:
                return "Date";
            case Field.DATETIME:
                return "Timestamp";
            case Field.FLOAT:
            case Field.DOUBLE:
                return "Double";
            case Field.BLOB:
            case Field.TLOB:
                return "String";
            case Field.BIGSEQUENCE:
                return "Long";
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "Int";
            case Field.LONG:
                return "Long";
            case Field.MONEY:
                return "BigDecimal";
            case Field.SHORT:
                return "Short";
            case Field.TIME:
                return "Time";
            case Field.TIMESTAMP:
                return "Timestamp";
            case Field.USERSTAMP:
                return "String";
        }
        return "unknown";
    }

    private static String padded(int size) {
        String padString = "                                                         ";
        if (size == 0)
            return "";
        if (size > padString.length())
            size = padString.length();
        return padString.substring(0, size);
    }
}
