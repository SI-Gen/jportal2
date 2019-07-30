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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * <p>Generates Java code for JDBC and Crackle consumption </p>
 * <br> Supported Flags
 * <ul>
 * <li><code>utilizeEnums</code> - Not only generates Enum types but also uses them on generated fields and
 * method parameters and return values.
 * </li>
 * <li><code>generateLombok</code> - Generates Lombok annotations for <code>equals</code>, <code>hashCode</code>,
 * accessor, mutator and <code>toString</code> method implementations, in addition to a builder implementation.
 * </li>
 * </ul>
 *
 * When specifying the <code>generateLombok</code> flag, ensure that Lombok is on the classpath in client
 * applications.
 */
public class JavaJCCode extends BaseGenerator implements IBuiltInSIProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JavaJCCode.class);
    public static final String GENERATE_PROCS_IO_ERROR = "Generate Procs IO Error";
    public static final String FLAG_UTILIZE_ENUMS = "utilizeenums";
    public static final String FLAG_GEN_LOMBOK = "generatelombok";
    public static final String ENTITY_CLASS_SUFFIX = "Struct";

    private Set<String> flags = new HashSet<>();

    public JavaJCCode() {
        super(JavaJCCode.class);
    }

    /**
     * Generates the procedure classes for each table present.
     */
    public String description() {
        return "generate Java code for jdbc and crackle consumption - separates structs and others from main";
    }

    public String documentation() {
        return "generate Java code for jdbc and crackle consumption - separates structs and others from main";
    }

    public void generate(Database database, String output) {

        flags = database.getFlags().stream().map(String::toLowerCase).collect(Collectors.toSet());

        for (int i = 0; i < database.tables.size(); i++) {
            Table table = database.tables.elementAt(i);
            generateStructs(table, output);
            generate(table, output);
        }
    }

    /**
     * Build of standard and user defined procedures
     */
    private void generate(Table table, String output) {
        generateStdProcs(table, output);
        generateOtherProcs(table, output);
    }

    private void generateStructs(Table table, String output) {
        generateStdProcStruct(table, output);
        generateOtherProcStructs(table, output);
    }

    private void generateStdProcStruct(Table table, String output) {

        String fileName = output + table.useName() + ENTITY_CLASS_SUFFIX + ".java";
        logger.info("Code: {}", fileName);

        try (PrintWriter outData = openOutputFileForGeneration("Java", fileName)) {
            if (table.database.packageName.length() > 0) {
                outData.println("package " + table.database.packageName + ";");
                outData.println();
            }
            outData.println("import java.io.Serializable;");
            outData.println("import java.sql.*;");
            outData.println("import java.math.*;");

            generateLombokImport(outData);

            outData.println();
            outData.println("/**");
            for (int i = 0; i < table.comments.size(); i++) {
                String s = table.comments.elementAt(i);
                outData.println(" *" + s);
            }
            outData.println(" * This code was generated, do not modify it, modify it at source and regenerate it.");
            outData.println(" * Does not use inner public classes and separates structs out.");
            outData.println(" */");

            generateLombokAnnotations(outData);

            outData.println("public class " + table.useName() + ENTITY_CLASS_SUFFIX + " implements Serializable");
            outData.println("{");
            outData.println("  public static final long serialVersionUID = 1L;");
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
                outData.println("  protected " + javaVar(field) + ";");
                outData.println("  public " + getterSetter(field));
            }
            outData.println("  public " + table.useName() + ENTITY_CLASS_SUFFIX + "()");
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
                outData.println("\"  " + field.useLowerName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
            }
            outData.println("    ;");
            outData.println("  }");
            outData.println("}");
            outData.flush();

        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR, e1);
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

        String fileName = output + table.useName() + proc.upperFirst() + ENTITY_CLASS_SUFFIX + ".java";

        logger.info("Code: {}", fileName);

        try (PrintWriter outData = openOutputFileForGeneration("Java", fileName)) {
            if (table.database.packageName.length() > 0) {
                outData.println("package " + table.database.packageName + ";");
                outData.println();
            }
            outData.println("import java.io.Serializable;");
            outData.println("import java.sql.*;");
            outData.println("import java.math.*;");

            generateEnumImports(table, outData);
            generateLombokImport(outData);

            outData.println();
            outData.println("/**");
            for (int j = 0; j < proc.comments.size(); j++) {
                String comment = proc.comments.elementAt(j);
                outData.println(" *" + comment);
            }
            outData.println(" */");

            generateLombokAnnotations(outData);

            outData.println("public class " + table.useName() + proc.upperFirst() + ENTITY_CLASS_SUFFIX + " implements Serializable");
            outData.println("{");
            outData.println("    private static final long serialVersionUID = 1L;");
            int maxSize = 0;
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = proc.inputs.elementAt(j);
                if (field.useName().length() > maxSize)
                    maxSize = field.useName().length();
                outData.println("  /**");
                for (int c = 0; c < field.comments.size(); c++) {
                    String s = field.comments.elementAt(c);
                    outData.println("   *" + s);
                }
                if (!proc.hasOutput(field.name))
                    outData.println("   * (input)");
                else
                    outData.println("   * (input/output)");
                outData.println("   */");
                outData.println("  protected " + javaVar(field) + ";");
                outData.println("  public " + getterSetter(field));
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = proc.outputs.elementAt(j);
                if (field.useName().length() > maxSize)
                    maxSize = field.useName().length();
                if (!proc.hasInput(field.name)) {
                    outData.println("  /**");
                    for (int c = 0; c < field.comments.size(); c++) {
                        String s = field.comments.elementAt(c);
                        outData.println("   *" + s);
                    }
                    outData.println("   * (output)");
                    outData.println("   */");
                    outData.println("  protected " + javaVar(field) + ";");
                    outData.println("  public " + getterSetter(field));
                }
            }
            for (int j = 0; j < proc.dynamics.size(); j++) {
                String s = proc.dynamics.elementAt(j);
                if (s.length() > maxSize)
                    maxSize = s.length();
                outData.println("  /**");
                outData.println("   * (dynamic)");
                outData.println("   */");
                outData.println("  public String " + s + ";");
            }
            outData.println("  public " + table.useName() + proc.upperFirst() + ENTITY_CLASS_SUFFIX + "()");
            outData.println("  {");
            for (int j = 0; j < proc.inputs.size(); j++) {
                Field field = proc.inputs.elementAt(j);
                outData.println("    " + initJavaVar(field));
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = proc.outputs.elementAt(j);
                if (!proc.hasInput(field.name))
                    outData.println("    " + initJavaVar(field));
            }
            for (int j = 0; j < proc.dynamics.size(); j++) {
                String s = proc.dynamics.elementAt(j);
                outData.println("    " + s + " = \"\";");
            }
            outData.println("  }");
            outData.println("  public String toString()");
            outData.println("  {");
            outData.println("    String CRLF = System.lineSeparator();");
            String ret = "    return ";
            for (int j = 0; j < proc.inputs.size(); j++) {
                outData.print(ret);
                ret = "         + ";
                Field field = proc.inputs.elementAt(j);
                int no = maxSize - field.useName().length();
                outData.println("\"  " + field.useLowerName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
            }
            for (int j = 0; j < proc.outputs.size(); j++) {
                Field field = proc.outputs.elementAt(j);
                if (!proc.hasInput(field.name)) {
                    outData.print(ret);
                    ret = "         + ";
                    int no = maxSize - field.useName().length();
                    outData.println("\"  " + field.useLowerName() + padded(no + 1) + ": \" + " + field.useLowerName() + " + CRLF");
                }
            }
            for (int j = 0; j < proc.dynamics.size(); j++) {
                String s = proc.dynamics.elementAt(j);
                outData.print(ret);
                ret = "         + ";
                int no = maxSize - s.length();
                outData.println("\"  " + s + padded(no + 1) + ": \" + " + s + " + CRLF");
            }
            outData.println("    ;");
            outData.println("  }");
            outData.println("}");
            outData.flush();

        } catch (
                IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR, e1);
        }

    }

    /**
     * Build of all required standard procedures
     */
    private String extendsName;

    private void generateStdProcs(Table table, String output) {

        String fileName = output + table.useName() + ".java";
        logger.info("Code: {}", fileName);
        try (PrintWriter outData = openOutputFileForGeneration("Java", fileName)) {
            if (table.database.packageName.length() > 0) {
                outData.println("package " + table.database.packageName + ";");
                outData.println("");
            }
            outData.println("import bbd.jportal2.util.*;");
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
            extendsName = table.useName() + ENTITY_CLASS_SUFFIX;
            outData.println("public class " + table.useName() + " extends " + extendsName);
            outData.println("{");

            outData.println("  private static final long serialVersionUID = 1L;");

            outData.println("  Connector connector;");
            outData.println("  Connection connection;");
            outData.println("  public " + table.useName() + "()");
            outData.println("  {");
            outData.println("    super();");
            outData.println("  }");

            outData.println("  /**");
            outData.println("   * @param conn for specific database");
            outData.println("   */");
            outData.println("  public void setConnector(Connector conn)");
            outData.println("  {");
            outData.println("    this.connector = conn;");
            outData.println("    connection = connector.connection;");
            outData.println("  }");

            outData.println("  /**");
            outData.println("   * @param connector for specific database");
            outData.println("   */");
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

        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR, e1);
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

        String fileName = output + table.useName() + proc.upperFirst() + ".java";

        logger.info("Code: {}", fileName);

        try (PrintWriter outData = openOutputFileForGeneration("Java", fileName)) {
            if (table.database.packageName.length() > 0) {
                outData.println("package " + table.database.packageName + ";");
                outData.println("");
            }
            outData.println("import bbd.jportal2.util.*;");
            outData.println("import java.sql.*;");
            outData.println("import java.util.*;");
            outData.println("import java.math.*;");

            generateEnumImports(table, outData);

            outData.println("");
            outData.println("/**");
            for (int j = 0; j < proc.comments.size(); j++) {
                String comment = proc.comments.elementAt(j);
                outData.println(" *" + comment);
            }
            outData.println(" */");
            extendsName = table.useName() + proc.upperFirst() + ENTITY_CLASS_SUFFIX;
            outData.println("public class " + table.useName() + proc.upperFirst() + " extends " + extendsName);
            outData.println("{");
            outData.println("  private static final long serialVersionUID = 1L;");

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
        } catch (IOException e1) {
            logger.error(GENERATE_PROCS_IO_ERROR, e1);
        }

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
        if (proc.comments.size() > 0) {
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
                    outData.println("    " + field.useLowerName() + " = connector.getBigSequence(\"" + proc.table.name + "\");");
                else if (field.type == Field.SEQUENCE)
                    outData.println("    " + field.useLowerName() + " = connector.getSequence(\"" + proc.table.name + "\");");
            }
            if (field.type == Field.TIMESTAMP)
                outData.println("    " + field.useLowerName() + " = connector.getTimestamp();");
            if (field.type == Field.USERSTAMP)
                outData.println("    " + field.useLowerName() + " = connector.getUserstamp();");
        }
        Vector<PlaceHolderPairs> pairs = placeHolders.getPairs();
        for (int i = 0; i < pairs.size(); i++) {
            PlaceHolderPairs pair = pairs.elementAt(i);
            Field field = pair.field;

            if (field.isNull) {
                outData.println("    if(" + field.useLowerName() + " == null) {");
                outData.print("        prep.setNull(");
                outData.print(i + 1);
                outData.println(", java.sql.Types.NULL);");
                outData.println("    } else {");
                outData.print("    ");
            }

            String enumToInt = "%s";

            if (shouldUtilizeEnums() && hasEnums(field)) {
                enumToInt = "%s.key";
            }

            String prepSet = "    prep.set%s(%d, %s);";
            outData.println(String.format(prepSet, setType(field), i + 1,
                    String.format(enumToInt, field.useLowerName())));

            if (field.isNull) {
                outData.println("    };");
            }

        }
        if (!proc.outputs.isEmpty()) {
            outData.println("    ResultSet result = prep.executeQuery();");
            if (!proc.isSingle) {
                outData.println("    Query query = new Query(prep, result);");
                outData.println("    return query;");
                outData.println("  }");
                outData.println("  /**");
                outData.println("   * Returns the next record in a result set.");
                outData.println("   * @param query The result set for the query.");
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

                String enumWrappedResultGet = "%s;";

                if (shouldUtilizeEnums() && hasEnums(field)) {
                    enumWrappedResultGet = field.useUpperName() + ".get(%s);";
                }

                String resultGet = "result.get%s(%d)";
                enumWrappedResultGet = String.format(enumWrappedResultGet,
                        String.format(resultGet, setType(field), i + 1));

                if (!field.isNull) {
                    outData.print("    " + field.useLowerName() + " =  ");
                } else {
                    outData.print("    " + field.useLowerName() + " =  result.getObject(");
                    outData.print(i + 1);
                    outData.print(") == null?null:");
                }
                outData.println(enumWrappedResultGet);
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
            outData.println("    Vector<" + extendsName + "> recs = new Vector<>();");
            outData.println("    Query query = " + procName + "();");
            outData.println("    while (" + procName + "(query) == true)");
            outData.println("    {");
            outData.println("      " + extendsName + " rec = new " + extendsName + "();");
            for (int i = 0; i < proc.outputs.size(); i++) {
                Field field = proc.outputs.elementAt(i);
                outData.println("      rec." + field.useLowerName() + " = " + field.useLowerName() + ";");
            }
            outData.println("      recs.addElement(rec);");
            outData.println("    }");
            outData.println("    " + extendsName + "[] result = new " + extendsName + "[recs.size()];");
            outData.println("    for (int i=0; i<recs.size();i++)");
            outData.println("      result[i] = recs.elementAt(i); ");
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
                outData.println("   * @param " + field.useLowerName() + " key input.");
            }
            for (int i = 0; i < proc.inputs.size(); i++) {
                Field field = proc.inputs.elementAt(i);
                if ((field.isSequence && proc.isInsert)
                        || (field.type == Field.TIMESTAMP)
                        || (field.type == Field.USERSTAMP))
                    continue;
                if (field.isPrimaryKey)
                    continue;
                outData.println("   * @param " + field.useLowerName() + " input.");
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
                String usename = field.useLowerName();
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
                return "Byte " + field.useLowerName();
            case Field.SHORT:
                return "Short " + field.useLowerName();
            case Field.BIGSEQUENCE:
                return "Long " + field.useLowerName();
            case Field.INT:
                if (shouldUtilizeEnums() && hasEnums(field)) {
                    return getEnumTypeName(field) + " " + field.useLowerName();
                }
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "Integer " + field.useLowerName();
            case Field.LONG:
                return "Long " + field.useLowerName();
            case Field.CHAR:
            case Field.ANSICHAR:
                return "String " + field.useLowerName();
            case Field.DATE:
                return "java.sql.Date " + field.useLowerName();
            case Field.DATETIME:
                return "Timestamp " + field.useLowerName();
            case Field.TIME:
                return "Time " + field.useLowerName();
            case Field.TIMESTAMP:
                return "Timestamp " + field.useLowerName();
            case Field.FLOAT:
            case Field.DOUBLE:
                return "BigDecimal " + field.useLowerName();
            case Field.BLOB:
                return "byte[] " + field.useLowerName();
            case Field.TLOB:
                return "String " + field.useLowerName();
            case Field.MONEY:
                return "BigDecimal " + field.useLowerName();
            case Field.USERSTAMP:
                return "String " + field.useLowerName();
            case Field.BOOLEAN:
                return "Boolean " + field.useLowerName();
        }
        return "unknown";
    }

    /**
     * Translates field type to java data member type
     */
    private String getterSetter(Field field) {
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
                if (shouldUtilizeEnums() && hasEnums(field)) {
                    type = getEnumTypeName(field) + " ";
                    break;
                }
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
                type = "BigDecimal ";
                break;
            case Field.BLOB:
                type = "byte[] ";
                break;
            case Field.TLOB:
                type = "String ";
                break;
            case Field.MONEY:
                type = "BigDecimal ";
                break;
            case Field.USERSTAMP:
                type = "String ";
                break;
            case Field.BOOLEAN:
                type = "Boolean ";
                break;
        }
        if (type == null) {
            return "unknown";

        } else {
            return type + "get" + field.useName() + "(){ return " + field.useLowerName() + "; } \n  public void set" + field.useName() + "(" + type + " " + field.useLowerName() + "){ this." + field.useLowerName() + " = " + field.useLowerName() + "; }\n";

        }
    }

    /**
     * returns the data member initialisation code (not always neccessary in java but still we do it)
     */
    private String initJavaVar(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return field.useLowerName() + " = null;";
            case Field.CHAR:
            case Field.ANSICHAR:
                return field.useLowerName() + " = null;";
            case Field.DATE:
                return field.useLowerName() + " = new Date(0);";
            case Field.DATETIME:
                return field.useLowerName() + " = new Timestamp(0);";
            case Field.FLOAT:
            case Field.DOUBLE:
                return field.useLowerName() + " = null;";
            case Field.BLOB:
            case Field.TLOB:
                return field.useLowerName() + " = null;";
            case Field.BIGSEQUENCE:
                return field.useLowerName() + " = null;";
            case Field.INT:
                if (shouldUtilizeEnums() && hasEnums(field)) {
                    return field.useLowerName() + " = null;";
                }
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return field.useLowerName() + " = null;";
            case Field.LONG:
                return field.useLowerName() + " = null;";
            case Field.MONEY:
                return field.useLowerName() + " = null;";
            case Field.SHORT:
                return field.useLowerName() + " = null;";
            case Field.TIME:
                return field.useLowerName() + " = new Time(0);";
            case Field.TIMESTAMP:
                return field.useLowerName() + " = new Timestamp(0);";
            case Field.USERSTAMP:
                return field.useLowerName() + " = null;";
            case Field.BOOLEAN:
                return field.useLowerName() + " = null;";
        }
        return "unknown";
    }

    /**
     * JDBC get and set type for field data transfers
     */
    private String setType(Field field) {
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
                return "BigDecimal";
            case Field.BLOB:
                return "Bytes";
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
            case Field.BOOLEAN:
                return "Boolean";
        }

        return "unknown";
    }

    private String padded(int size) {
        String padString = "                                                         ";
        if (size == 0)
            return "";
        if (size > padString.length())
            size = padString.length();
        return padString.substring(0, size);
    }

    private String underScoreWords(String input) {
        char[] bits = input.toCharArray();
        StringBuilder builder = new StringBuilder();
        builder.append(bits[0]);
        for (int i = 1; i < bits.length; i++) {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(bits[i]) >= 0
                    && bits[i - 1] != ' ') {
                builder.append('_');
                builder.append(bits[i]);
            } else
                builder.append(bits[i]);
        }
        return builder.toString();
    }

    private String splitWords(String input) {
        char[] bits = underScoreWords(input).toCharArray();
        StringBuilder builder = new StringBuilder();
        builder.append(bits[0]);
        for (int i = 1; i < bits.length; i++) {
            if (bits[i] == '_')
                builder.append(' ');
            else
                builder.append(bits[i]);
        }
        return builder.toString();
    }

    private void generateEnum(Table table, PrintWriter outData) {
        for (int i = 0; i < table.fields.size(); i++) {
            Field field = table.fields.elementAt(i);
            if (!field.enums.isEmpty()) {
                outData.println("  public enum " + getEnumTypeName(field));
                outData.println("  {");
                for (int j = 0; j < field.enums.size(); j++) {
                    Enum element = field.enums.elementAt(j);
                    String evalue = "" + element.value;
                    if (field.type == Field.ANSICHAR && field.length == 1)
                        evalue = "'" + (char) element.value + "'";
                    String keyName = underScoreWords(element.name).toUpperCase();
                    outData.println("    " + keyName + "(" + evalue + ", \"" + splitWords(element.name) + "\")" + (((j + 1) < field.enums.size()) ? "," : ";"));
                }
                outData.println("    public int key;");
                outData.println("    public String value;");
                outData.println("    " + field.useUpperName() + "(int key, String value)");
                outData.println("    {");
                outData.println("      this.key = key;");
                outData.println("      this.value = value;");
                outData.println("    }");
                outData.println("    public static " + field.useUpperName() + " get(int key)");
                outData.println("    {");
                outData.println("      for (" + field.useUpperName() + " op : values())");
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

    private boolean hasEnums(Field field) {
        return field.getEnums() != null
                && !field.getEnums().isEmpty();
    }


    private boolean hasEnums(Table table) {
        return table.getFields().stream().anyMatch(this::hasEnums);
    }

    public List<Field> getAllEnumFields(Table table) {
        return table.getFields().stream()
                .filter(this::hasEnums)
                .collect(Collectors.toList());
    }

    private void generateEnumImports(Table table, PrintWriter outData) {
        String enumImport = "import %s.%s%s.%s;";

        if (shouldUtilizeEnums() && hasEnums(table)) {
            for (Field field : getAllEnumFields(table)) {
                outData.println(String.format(enumImport,
                        table.getDatabase().getPackageName(),
                        table.getName(), ENTITY_CLASS_SUFFIX, getEnumTypeName(field))
                );
            }
        }
    }

    private void generateLombokImport(PrintWriter outData) {
        if (shouldGenerateLombok()) {
            outData.println("import lombok.*;");
        }
    }

    private void generateLombokAnnotations(PrintWriter outData) {
        if (shouldGenerateLombok()) {
            outData.println("@Data");
            outData.println("@AllArgsConstructor");
            outData.println("@NoArgsConstructor");
        }
    }

    private String getEnumTypeName(Field field) {
        if (field.getEnumType() != null && !field.getEnumType().isEmpty()) {
            return field.getEnumType();
        } else {
            return field.useUpperName();
        }
    }

    private boolean shouldUtilizeEnums() {
        return flags.contains(FLAG_UTILIZE_ENUMS);
    }

    private boolean shouldGenerateLombok() {
        return flags.contains(FLAG_GEN_LOMBOK);
    }

}
