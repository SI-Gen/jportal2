/// ------------------------------------------------------------------
/// Copyright (c) from 1996 Vincent Risi
///
/// All rights reserved.
/// This program and the accompanying materials are made available
/// under the terms of the Common Public License v1.0
/// which accompanies this distribution and is available at
/// http://www.eclipse.org/legal/cpl-v10.html
/// Contributors:
///    Vincent Risi, Hennie Hammann
/// ------------------------------------------------------------------

package bbd.jportal2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;


/**
 * Table identified by name holds fields, keys, links, grants, views and procedures associated with the table.
 */
public class Table implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Table.class);

    private static final long serialVersionUID = 1L;
    public Database database;
    public String literalName;
    public String name;
    public String alias;
    public String check;
    public Vector<Field> fields;
    public Vector<Key> keys;
    public Vector<Link> links;
    public Vector<Grant> grants;
    public Vector<View> views;
    public Vector<Proc> procs;
    public Vector<String> comments;
    public Vector<String> options;
    public Vector<String> allUsers;
    public Vector<Parameter> parameters;
    public Vector<Const> consts;
    public boolean hasPrimaryKey;
    public boolean hasSequence;
    public boolean hasTimeStamp;
    public boolean hasAutoTimeStamp;
    public boolean hasUserStamp;
    public boolean hasExecute;
    public boolean hasSelect;
    public boolean hasInsert;
    public boolean hasDelete;
    public boolean hasUpdate;
    public boolean hasStdProcs;
    public boolean hasIdentity;
    public boolean hasSequenceReturning;
    public boolean hasBigXML;
    public boolean hasBigJSON;
    public boolean isStoredProc;
    public boolean isLiteral;
    public boolean useBrackets;
    public boolean useReturningOutput;
    public int start;

    public Table() {
        name = "";
        alias = "";
        check = "";
        literalName = "";
        fields = new Vector<>();
        keys = new Vector<>();
        links = new Vector<>();
        grants = new Vector<>();
        views = new Vector<>();
        procs = new Vector<>();
        comments = new Vector<>();
        options = new Vector<>();
        allUsers = new Vector<>();
        parameters = new Vector<>();
        consts = new Vector<>();
        hasExecute = false;
        hasSelect = false;
        hasInsert = false;
        hasDelete = false;
        hasUpdate = false;
        hasPrimaryKey = false;
        hasSequence = false;
        hasTimeStamp = false;
        hasAutoTimeStamp = false;
        hasUserStamp = false;
        hasStdProcs = false;
        hasIdentity = false;
        hasSequenceReturning = false;
        hasBigXML = false;
        hasBigJSON = false;
        isStoredProc = false;
        isLiteral = false;
        useBrackets = false;
        useReturningOutput = false;
        start = 0;
    }

    static boolean isIdentity(Field field) {
        return field.type == Field.BIGIDENTITY || field.type == Field.IDENTITY;
    }

    static boolean isSequence(Field field) {
        return field.type == Field.BIGSEQUENCE || field.type == Field.SEQUENCE;
    }

    /**
     * Translates field type to DB2 SQL column types
     */
    static String varType(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return "SMALLINT";
            case Field.SHORT:
                return "SMALLINT";
            case Field.INT:
            case Field.SEQUENCE:
            case Field.IDENTITY:
                return "INT";
            case Field.LONG:
            case Field.BIGSEQUENCE:
            case Field.BIGIDENTITY:
                return "BIGINT";
            case Field.CHAR:
                if (field.length > 32762)
                    return "CLOB(" + field.length + ")";
                else
                    return "VARCHAR(" + field.length + ")";
            case Field.ANSICHAR:
                return "CHAR(" + field.length + ")";
            case Field.DATE:
                return "DATE";
            case Field.DATETIME:
                return "TIMESTAMP";
            case Field.TIME:
                return "TIME";
            case Field.TIMESTAMP:
            case Field.AUTOTIMESTAMP:
                return "TIMESTAMP";
            case Field.FLOAT:
            case Field.DOUBLE:
                if (field.scale != 0)
                    return "DECIMAL(" + field.precision + ", " + field.scale + ")";
                else if (field.precision != 0)
                    return "DECIMAL(" + String.valueOf(field.precision) + ", 0)";
                return "DOUBLE";
            case Field.BLOB:
                return "BLOB(" + String.valueOf(field.length) + ")";
            case Field.TLOB:
                return "CLOB(" + String.valueOf(field.length) + ")";
            case Field.MONEY:
                return "DECIMAL(18,2)";
            case Field.USERSTAMP:
                return "VARCHAR(50)";
            case Field.XML:
                return "XML";
            case Field.JSON:
            case Field.BIGJSON:
                return "JSON";
        }
        return "unknown";
    }

    public Database getDatabase() {
        return database;
    }

    public String getLiteralName() {
        return literalName;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getCheck() {
        return check;
    }

    public Vector<Field> getFields() {
        return fields;
    }

    public Link getLinkForField(Field field) {
        for (Link link : getLinks())
        {
            if (link.fields.size() < 1) {
                System.out.println("ERR");
                continue;
            }
            if (link.getFields().get(0).compareTo(field.name) == 0) {
                return link;
            }
        }
        return null;
    }

    public Field getFieldForLink(Link link) {
        for (Field field : getFields())
        {
            if (link.getFields().get(0).compareTo(field.name) == 0) {
                return field;
            }
        }
        return null;
    }

    public Vector<Key> getKeys() {
        return keys;
    }

    public Vector<Link> getLinks() {
        return links;
    }

    public Vector<Grant> getGrants() {
        return grants;
    }

    public Vector<View> getViews() {
        return views;
    }

    public Vector<Proc> getProcs() {
        return procs;
    }

    public Vector<String> getComments() {
        return comments;
    }

    public Vector<String> getOptions() {
        return options;
    }

    public Vector<String> getAllUsers() {
        return allUsers;
    }

    public Vector<Parameter> getParameters() {
        return parameters;
    }

    public Vector<Const> getConsts() {
        return consts;
    }

    public boolean isHasPrimaryKey() {
        return hasPrimaryKey;
    }

    public boolean isHasSequence() {
        return hasSequence;
    }

    public boolean isHasTimeStamp() {
        return hasTimeStamp;
    }

    public boolean isHasAutoTimeStamp() {
        return hasAutoTimeStamp;
    }

    public boolean isHasUserStamp() {
        return hasUserStamp;
    }

    public boolean isHasExecute() {
        return hasExecute;
    }

    public boolean isHasSelect() {
        return hasSelect;
    }

    public boolean isHasInsert() {
        return hasInsert;
    }

    public boolean isHasDelete() {
        return hasDelete;
    }

    public boolean isHasUpdate() {
        return hasUpdate;
    }

    public boolean isHasStdProcs() {
        return hasStdProcs;
    }

    public boolean isHasIdentity() {
        return hasIdentity;
    }

    public boolean isHasSequenceReturning() {
        return hasSequenceReturning;
    }

    public boolean hasBigXML() {
        return hasBigXML;
    }

    public boolean hasBigJSON() {
        return hasBigJSON;
    }

    public boolean isStoredProc() {
        return isStoredProc;
    }

    public boolean isLiteral() {
        return isLiteral;
    }

    public void reader(DataInputStream ids) throws IOException {
        reader(ids, null);
    }

    public void reader(DataInputStream ids, Vector<?> useProcs) throws IOException {
        int signature = ids.readInt();
        if (signature != 0xBABA00D)
            return;
        name = ids.readUTF();
        literalName = ids.readUTF();
        alias = ids.readUTF();
        check = ids.readUTF();
        int noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Field value = new Field();
            value.reader(ids);
            fields.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Key value = new Key();
            value.reader(ids);
            keys.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Link value = new Link();
            value.reader(ids);
            links.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Grant value = new Grant();
            value.reader(ids);
            grants.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            View value = new View();
            value.reader(ids);
            views.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Proc value = new Proc();
            value.reader(ids);
            if (useProcs == null)
                procs.addElement(value);
            else
                for (int p = 0; p < useProcs.size(); p++) {
                    String name = (String) useProcs.elementAt(p);
                    if (value.name.compareToIgnoreCase(name) == 0) {
                        procs.addElement(value);
                        break;
                    }
                }
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            String value = ids.readUTF();
            comments.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            String value = ids.readUTF();
            options.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Parameter value = new Parameter();
            value.table = this;
            value.reader(ids);
            parameters.addElement(value);
        }
        noOf = ids.readInt();
        for (int i = 0; i < noOf; i++) {
            Const value = new Const();
            value.reader(ids);
            consts.addElement(value);
        }
        hasExecute = ids.readBoolean();
        hasSelect = ids.readBoolean();
        hasInsert = ids.readBoolean();
        hasDelete = ids.readBoolean();
        hasUpdate = ids.readBoolean();
        hasPrimaryKey = ids.readBoolean();
        hasSequence = ids.readBoolean();
        hasTimeStamp = ids.readBoolean();
        hasAutoTimeStamp = ids.readBoolean();
        hasUserStamp = ids.readBoolean();
        hasStdProcs = ids.readBoolean();
        hasIdentity = ids.readBoolean();
        hasSequenceReturning = ids.readBoolean();
        isStoredProc = ids.readBoolean();
        isLiteral = ids.readBoolean();
        start = ids.readInt();
    }

    public void writer(DataOutputStream ods) throws IOException {
        ods.writeInt(0xBABA00D);
        ods.writeUTF(name);
        ods.writeUTF(literalName);
        ods.writeUTF(alias);
        ods.writeUTF(check);
        ods.writeInt(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field value = fields.elementAt(i);
            value.writer(ods);
        }
        ods.writeInt(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            Key value = keys.elementAt(i);
            value.writer(ods);
        }
        ods.writeInt(links.size());
        for (int i = 0; i < links.size(); i++) {
            Link value = links.elementAt(i);
            value.writer(ods);
        }
        ods.writeInt(grants.size());
        for (int i = 0; i < grants.size(); i++) {
            Grant value = grants.elementAt(i);
            value.writer(ods);
        }
        ods.writeInt(views.size());
        for (int i = 0; i < views.size(); i++) {
            View value = views.elementAt(i);
            value.writer(ods);
        }
        int noProcs = 0;
        for (int i = 0; i < procs.size(); i++) {
            Proc value = procs.elementAt(i);
            if (value.isData == true)
                continue;
            noProcs++;
        }
        ods.writeInt(noProcs);
        for (int i = 0; i < procs.size(); i++) {
            Proc value = procs.elementAt(i);
            if (value.isData == true)
                continue;
            value.writer(ods);
        }
        ods.writeInt(comments.size());
        for (int i = 0; i < comments.size(); i++) {
            String value = comments.elementAt(i);
            ods.writeUTF(value);
        }
        ods.writeInt(options.size());
        for (int i = 0; i < options.size(); i++) {
            String value = options.elementAt(i);
            ods.writeUTF(value);
        }
        ods.writeInt(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            Parameter value = parameters.elementAt(i);
            value.table = this;
            value.writer(ods);
        }
        ods.writeInt(consts.size());
        for (int i = 0; i < consts.size(); i++) {
            Const value = consts.elementAt(i);
            value.writer(ods);
        }
        ods.writeBoolean(hasExecute);
        ods.writeBoolean(hasSelect);
        ods.writeBoolean(hasInsert);
        ods.writeBoolean(hasDelete);
        ods.writeBoolean(hasUpdate);
        ods.writeBoolean(hasPrimaryKey);
        ods.writeBoolean(hasSequence);
        ods.writeBoolean(hasTimeStamp);
        ods.writeBoolean(hasAutoTimeStamp);
        ods.writeBoolean(hasUserStamp);
        ods.writeBoolean(hasStdProcs);
        ods.writeBoolean(hasIdentity);
        ods.writeBoolean(hasSequenceReturning);
        ods.writeBoolean(isStoredProc);
        ods.writeBoolean(isLiteral);
        ods.writeInt(start);
    }

    /**
     * If there is a literal uses that else returns name
     */
    public String useLiteral() {
        if (isLiteral)
            return literalName;
        return name;
    }

    /**
     * If there is an alias uses that else returns name
     */
    public String useName() {
        if (alias.length() > 0)
            return alias;
        return name;
    }

    /**
     * Checks for the existence of a field
     */
    public boolean hasField(String s) {
        int i;
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.name.equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    public Field getField(String s) {
        int i;
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.name.equalsIgnoreCase(s))
                return field;
        }
        return null;
    }

    public int getFieldIndex(String s) {
        int i;
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.name.equalsIgnoreCase(s))
                return i;
        }
        return -1;
    }

    /**
     * Checks if table field is declared as null
     */
    public boolean hasFieldAsNull(String s) {
        int i;
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.name.equalsIgnoreCase(s))
                return field.isNull;
        }
        return false;
    }

    /**
     * Checks for the existence of a proc
     */
    public boolean hasProc(Proc p) {
        for (int i = 0; i < procs.size(); i++) {
            Proc proc = procs.elementAt(i);
            if (proc.name.equalsIgnoreCase(p.name))
                return true;
        }
        return false;
    }

    /**
     * Returns proc or null
     */
    public Proc getProc(String name) {
        for (int i = 0; i < procs.size(); i++) {
            Proc proc = procs.elementAt(i);
            if (proc.name.equalsIgnoreCase(name))
                return proc;
        }
        return null;
    }

    /**
     * Checks for the existence of a proc
     */
    public boolean hasCursorStdProc() {
        for (int i = 0; i < procs.size(); i++) {
            Proc proc = procs.elementAt(i);
            if (proc.isStd == true && proc.isSingle == false && proc.outputs.size() > 0)
                return true;
        }
        return false;
    }

    /**
     * Sets a field to be primary key
     */
    public void setPrimary(String s) {
        int i;
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.name.equalsIgnoreCase(s)) {
                field.isPrimaryKey = true;
                return;
            }
        }
    }

    public String tableNameWithSchema() {
        if (database.schema.length() == 0)
            if (isLiteral)
                return literalName;
            else
                return name;
        if (isLiteral)
            return database.schema + "." + literalName;
        return database.schema + "." + name;
    }

    /**
     * Builds a merge proc generated as part of standard record class
     */
    public void buildMerge(Proc proc) {
        String name = tableNameWithSchema();
        int i;
        String comma = "  ";
        String front = "  ";
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions,new SQLProcStringToken("merge into "), new SQLProcTableNameToken(this)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("using table")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("      (")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("        values")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("        (")));
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("        "),
                                            new SQLProcStringToken( comma),
                                            new SQLProcStringToken( "cast("), new SQLProcFieldToken(field),new SQLProcStringToken(" as " + varType(field) + ")")));
            proc.inputs.addElement(field);
            comma = ", ";
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("        )")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("      )")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("      temp_" + proc.table.name)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("      (")));
        comma = "  ";
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("      "), new SQLProcStringToken(comma),  new SQLProcFieldToken(field)));
            comma = ", ";
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("      )")));
        front = "on  ";
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey == true) {
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(front + name + ""), new SQLProcFieldToken(field), new SQLProcStringToken( " = temp_"), new SQLProcTableNameToken(proc.table), new SQLProcStringToken("."), new SQLProcFieldToken(field)));
                front = "and ";
            }
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("when matched then")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  update set")));
        comma = "    ";
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey == false) {
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(comma + name + ""), new SQLProcFieldToken(field), new SQLProcStringToken(" = temp_"), new SQLProcTableNameToken(proc.table), new SQLProcStringToken("."), new SQLProcFieldToken(field)));
                comma = "  , ";
            }
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("when not matched then")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  insert")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  (")));
        comma = "    ";
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(comma + field.useLiteral())));
            comma = "  , ";
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  )")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  values")));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  (")));
        comma = "    ";
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(comma + "temp_"), new SQLProcTableNameToken(proc.table), new SQLProcStringToken( ""), new SQLProcFieldToken(field)));
            comma = "  , ";
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("  )")));
    }

    /**
     * Builds an insert proc generated as part of standard record class
     */
    public void buildInsert(Proc proc) {
        String name = tableNameWithSchema();
        int i;
        String line = "  ";
        proc.isStd = true;
        proc.isSql = true;
        proc.isInsert = true;
        String identityName = "";
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("insert into "), new SQLProcTableNameToken(this), new SQLProcStringToken(" (")));
        for (i = 0; i < fields.size(); i++) {
            String comma = i + 1 < fields.size() ? "," : "";
            Field field = fields.elementAt(i);

            if (field.isCalc)
                continue;

            if (isIdentity(field)) {
                hasIdentity = true;
                identityName = field.name;

                if (proc.hasReturning) {
                    proc.hasUpdates = true;
                    proc.isSingle = true;
                    proc.outputs.addElement(field);
                }
            } else if (isSequence(field)) {

                if (proc.hasReturning) {
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line) , new SQLProcFieldToken(field), new SQLProcStringToken(comma)));
                }

                if (proc.hasReturning) {
                    proc.hasUpdates = true;
                    proc.isSingle = true;
                    proc.outputs.addElement(field);
                }

            } else {
                proc.inputs.addElement(field);
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field),new SQLProcStringToken(comma)));
            }

        }

        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" ) ")));

        if (hasIdentity == true) {
            //The original use for IDENTITY was for SQL Server.
            //But since other databases (other than SQL Server) also support IDENTITY
            //now, we will stop generating the SQL Server specific version, and
            //handle using the normal _ret style.
            //SQL Server Generators can be updated to support _ret going forward
            //proc.lines.addElement(new Line(" OUTPUT INSERTED." + identityName));
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Output), true));
        } else if (proc.hasReturning) {
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Output), true));
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" values (")));
        for (i = 0; i < fields.size(); i++) {
            String comma = i + 1 < fields.size() ? "," : "";
            Field field = fields.elementAt(i);

            if (isIdentity(field) || field.isCalc)
                continue;

            if (isSequence(field)) {
                if (proc.hasReturning) {
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Sequence), true));
                }
            } else {
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line + ":"), new SQLProcFieldToken(field),new SQLProcStringToken(comma)));
            }
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" )")));
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
    }

    /**
     * Builds an insert proc generated as part of standard record class
     */
    public void buildBulkInsert(Proc proc) {
        proc.isMultipleInput = true;
        buildInsert(proc);
    }

    /**
     * Builds an identity proc generated as part of standard record class
     */
    public void buildIdentity(Proc proc) {
        String name = tableNameWithSchema();
        int i;
        String line;
        proc.isSql = true;
        proc.isSingle = true;
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.type != Field.IDENTITY)
                continue;
            proc.outputs.addElement(field);
            line = "select max(" + field.useLiteral() + ") " + field.useLiteral() + " from " + name;
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line)));
        }
    }

    /**
     * Builds an update proc generated as part of standard record class
     */
    public void buildUpdate(Proc proc) {
        String name = tableNameWithSchema();
        int i, j;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("update "), new SQLProcTableNameToken(this)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" set")));
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey || field.isCalc || field.isSequence)
                continue;
            proc.inputs.addElement(field);
            if (j == 0)
                line = "  ";
            else
                line = ", ";
            j++;
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken( " = "), new SQLProcFieldVariableToken(field)));

        }
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey) {
                proc.inputs.addElement(field);
                if (j == 0)
                    line = " where ";
                else
                    line = "   and ";
                j++;
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line),new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
            }
        }
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
    }

    /**
     * Builds an update proc generated as part of standard record class
     */
    public void buildUpdateFor(Proc proc) {
        String name = tableNameWithSchema();
        int i, j, k;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("update "), new SQLProcTableNameToken(this)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" set")));
        for (i = 0, j = 0; i < proc.fields.size(); i++) {
            String fieldName = (String) proc.fields.elementAt(i);
            for (k = 0; k < fields.size(); k++) {
                Field field = (Field) fields.elementAt(k);
                if (field.isPrimaryKey || field.isCalc || field.isSequence)
                    continue;
                if (field.name.equalsIgnoreCase(fieldName)) {
                    proc.inputs.addElement(field);
                    if (j == 0)
                        line = "  ";
                    else
                        line = ", ";
                    j++;
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line),new SQLProcFieldToken(field), new SQLProcStringToken(" = ") , new SQLProcFieldVariableToken(field)));
                }
            }
        }
        AddTimeStampUserStamp(proc);
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = (Field) fields.elementAt(i);
            if (field.isPrimaryKey) {
                proc.inputs.addElement(field);
                if (j == 0)
                    line = " where ";
                else
                    line = "   and ";
                j++;
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
            }
        }
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
    }

    /**
     * Builds an updateby proc generated as part of standard record class
     */
    public void buildUpdateBy(Proc proc) {
        String name = tableNameWithSchema();
        int i, j, k;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("update "), new SQLProcTableNameToken(this)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" set")));
        if (proc.fields.size() == 0) {
            for (k = 0; k < proc.updateFields.size(); k++) {
                String fieldName = (String) proc.updateFields.elementAt(k);
                for (i = 0, j = 0; i < fields.size(); i++) {

                    Field field = (Field) fields.elementAt(i);
                    if (field.isPrimaryKey || field.isCalc || field.name.equalsIgnoreCase(fieldName) || field.isSequence)
                        continue;
                    proc.inputs.addElement(field);
                    if (j == 0)
                        line = "  ";
                    else
                        line = ", ";
                    j++;
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken( " = "), new SQLProcFieldVariableToken(field)));


                }
            }
        } else {
            for (i = 0, j = 0; i < proc.fields.size(); i++) {
                String fieldName = (String) proc.fields.elementAt(i);
                for (k = 0; k < fields.size(); k++) {
                    Field field = (Field) fields.elementAt(k);
                    if (field.name.equalsIgnoreCase(fieldName)) {
                        proc.inputs.addElement(field);
                        if (j == 0)
                            line = "  ";
                        else
                            line = ", ";
                        j++;
                        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
                    }
                }
            }
        }
        AddTimeStampUserStamp(proc);
        for (i = 0, j = 0; i < proc.updateFields.size(); i++) {
            String fieldName = (String) proc.updateFields.elementAt(i);
            for (k = 0; k < fields.size(); k++) {
                Field field = fields.elementAt(k);
                if (field.name.equalsIgnoreCase(fieldName)) {
                    proc.inputs.addElement(field);
                    if (j == 0)
                        line = " where ";
                    else
                        line = "   and ";
                    j++;
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
                }
            }
        }
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
    }

    private void AddTimeStampUserStamp(Proc proc) {
        int k;
        String line;
        boolean tmAdded, unAdded;
        tmAdded = unAdded = false;
        for (k = 0; k < fields.size(); k++) {
            Field field = (Field) fields.elementAt(k);
            if (field.type == Field.USERSTAMP && !unAdded) {
                unAdded = true;
                if (!proc.inputs.contains(field)) {
                    proc.inputs.addElement(field);
                    line = ", ";

                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken( " = "), new SQLProcFieldVariableToken(field)));
                }
            } else if (field.type == Field.TIMESTAMP && !tmAdded) {
                tmAdded = true;
                if (!proc.inputs.contains(field)) {
                    proc.inputs.addElement(field);
                    line = ", ";
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = ") , new SQLProcFieldVariableToken(field)));
                }
            }
        }
    }

    /**
     * Builds an update proc generated as part of standard record class
     */
    public void buildBulkUpdate(Proc proc) {
        proc.isMultipleInput = true;
        buildUpdate(proc);
    }

    /**
     * Builds a delete by primary key proc
     */
    public void buildDeleteOne(Proc proc) {
        String name = tableNameWithSchema();
        int i, j;
        String line;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("delete from "), new SQLProcTableNameToken(this)));
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey) {
                proc.inputs.addElement(field);
                if (j == 0)
                    line = " where ";
                else
                    line = "   and ";
                j++;
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken( " = "), new SQLProcFieldVariableToken(field)));
            }
        }
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
    }

    /**
     * Builds a delete all rows proc
     */
    public void buildDeleteAll(Proc proc) {
        String name = tableNameWithSchema();
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("delete from "), new SQLProcTableNameToken(this)));
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
    }

    /**
     * Builds a count rows proc
     */
    public void buildCount(Proc proc) {
        String name = tableNameWithSchema();
        proc.isSql = true;
        proc.isSingle = true;
        Field field = new Field();
        field.name = "noOf";
        field.type = Field.INT;
        field.length = 4;
        proc.outputs.addElement(field);
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("select count(*) noOf from "), new SQLProcTableNameToken(this)));
    }

    /**
     * Builds a check for primary key existence proc
     */
    public void buildExists(Proc proc) {
        String name = tableNameWithSchema();
        int i, j;
        String line;
        proc.isSql = true;
        proc.isSingle = true;
        Field count = new Field();
        count.name = "noOf";
        count.type = Field.INT;
        count.length = 4;
        proc.outputs.addElement(count);
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("select count(*) noOf from "), new SQLProcTableNameToken(this)));
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey) {
                proc.inputs.addElement(field);
                if (j == 0)
                    line = " where ";
                else
                    line = "   and ";
                j++;
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
            }
        }
    }

    /**
     * Builds a select on primary key proc
     */
    public void buildSelectOne(Proc proc, boolean update, boolean readonly) {
        String name = tableNameWithSchema();
        int i, j;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.isSingle = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("select")));
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (!field.isPrimaryKey) {
                proc.outputs.addElement(field);
                if (j == 0)
                    line = "  ";
                else
                    line = ", ";
                j++;
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field)));
            }
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" from "), new SQLProcTableNameToken(this)));
        for (i = 0, j = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.isPrimaryKey) {
                proc.inputs.addElement(field);
                if (j == 0)
                    line = " where ";
                else
                    line = "   and ";
                j++;
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
            }
        }
        if (update)
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" for update")));
        else if (readonly)
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" for read only")));
    }

    /**
     * Builds a select on primary key proc
     */
    public void buildMaxTmStamp(Proc proc) {
        String name = tableNameWithSchema();
        int i;
        proc.isStd = true;
        proc.isSql = true;
        proc.isSingle = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("select")));
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            if (field.type == Field.TIMESTAMP) {
                proc.outputs.addElement(field);
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("max("), new SQLProcFieldToken(field), new SQLProcStringToken(")")));
            }
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" from "), new SQLProcTableNameToken(this)));
    }

    /**
     * Builds a select all rows proc
     */
    public void buildSelectAll(Proc proc, boolean update, boolean readonly, boolean inOrder, boolean descending) {
        String name = tableNameWithSchema();
        int i;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("select")));
        for (i = 0; i < fields.size(); i++) {
            Field field = fields.elementAt(i);
            proc.outputs.addElement(field);
            if (i == 0)
                line = "  ";
            else
                line = ", ";
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field)));
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" from "), new SQLProcTableNameToken(this)));
        selectFor(proc, update, readonly);
        selectOrderBy(proc, inOrder, descending);
    }

    private void selectOrderBy(Proc proc, boolean inOrder, boolean descending) {
        int i, n;
        String line, tail;
        if (inOrder == false)
            return;
        if (proc.orderFields.size() == 0) {
            for (i = 0; i < fields.size(); i++) {
                Field field = fields.elementAt(i);
                if (field.isPrimaryKey)
                    proc.orderFields.addElement(field);
            }
        }
        n = proc.orderFields.size();
        for (i = 0; i < n; i++) {
            Field field = proc.orderFields.elementAt(i);
            if (i == 0)
                line = " order by ";
            else
                line = ", ";
            if (descending == true && i + 1 == n)
                tail = " desc";
            else
                tail = "";
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(tail)));
        }
    }

    private void selectFor(Proc proc, boolean update, boolean readonly) {
        if (update)
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" for update")));
        else if (readonly)
            proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" for read only")));
    }

    public void buildDeleteBy(Proc proc) {
        String name = tableNameWithSchema();
        int i, j, k;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("Delete from "), new SQLProcTableNameToken(this)));
        for (i = 0, j = 0; i < proc.fields.size(); i++) {
            String fieldName = (String) proc.fields.elementAt(i);
            for (k = 0; k < fields.size(); k++) {
                Field field = fields.elementAt(k);
                if (field.name.equalsIgnoreCase(fieldName)) {
                    proc.inputs.addElement(field);
                    if (j == 0)
                        line = " where ";
                    else
                        line = "   and ";
                    j++;
                    line = line + field.useLiteral() + " = :" + field.useLiteral();
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
                }
            }
        }
        if (proc.hasReturning)
            proc.lines.add(new Line(database.templateOutputOptions, new SQLProcEngineSugarToken(SQLProcEngineSugarToken.EngineSugarType.Tail), true));
        if (j == 0) {
            throw new Error("Error generating buildDeleteBy");
        }
    }

    public void buildSelectBy(Proc proc, boolean forUpdate, boolean forReadOnly, boolean inOrder, boolean descending) {
        String name = tableNameWithSchema();
        int i, j, k;
        String line;
        proc.isStd = true;
        proc.isSql = true;
        proc.lines.addElement(new Line(database.templateOutputOptions, Misc.generateProcNameComment(proc)));
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken("select")));
        if (proc.outputs.size() > 0) {
            for (i = 0; i < proc.outputs.size(); i++) {
                Field fieldOut = (Field) proc.outputs.elementAt(i);
                for (k = 0; k < fields.size(); k++) {
                    Field field = (Field) fields.elementAt(k);
                    if (field.name.equalsIgnoreCase(fieldOut.name)) {
                        if (i == 0)
                            line = "  ";
                        else
                            line = ", ";
                        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field)));
                    }
                }
            }
        } else {
            for (i = 0; i < fields.size(); i++) {
                Field field = (Field) fields.elementAt(i);
                proc.outputs.addElement(field);
                if (i == 0)
                    line = "  ";
                else
                    line = ", ";
                proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line), new SQLProcFieldToken(field)));
            }
        }
        proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(" from "), new SQLProcTableNameToken(this)));
        selectFor(proc, forUpdate, forReadOnly);
        for (i = 0, j = 0; i < proc.fields.size(); i++) {
            String fieldName = (String) proc.fields.elementAt(i);
            for (k = 0; k < fields.size(); k++) {
                Field field = (Field) fields.elementAt(k);
                if (field.name.equalsIgnoreCase(fieldName)) {
                    proc.inputs.addElement(field);
                    if (j == 0)
                        line = " where ";
                    else
                        line = "   and ";
                    j++;
                    proc.lines.addElement(new Line(database.templateOutputOptions, new SQLProcStringToken(line),new SQLProcFieldToken(field), new SQLProcStringToken(" = "), new SQLProcFieldVariableToken(field)));
                }
            }
        }
        if (j == 0) {
            throw new Error("Error in SelectBy");
        }
        selectOrderBy(proc, inOrder, descending);
    }

//    public void buildSelectFrom(Proc proc, Table table) {
//        String name = tableName();
//        int i, j, k;
//        String line;
//        proc.extendsStd = false;
//        proc.isSql = true;
//        String preFix = "";
//        boolean doSelect = true;
//        Line first;
//        if (proc.lines.size() > 0) {
//            first = (Line) proc.lines.elementAt(0);
//            for (k = 0; k < proc.lines.size(); k++) {
//                first = (Line) proc.lines.elementAt(k);
//                if (first.line.toString().toLowerCase().indexOf("select ") > -1 && first.line.toString().toLowerCase().indexOf("select ") < 5) {
//                    doSelect = false;
//                    logger.warn("Select found not generating SELECT." + first.line.toString().toLowerCase().indexOf("select "));
//                    break;
//                }
//            }
//        }
//        if (doSelect) {
//            if (preFix == "") {
//                for (k = 0; k < proc.lines.size(); k++) {
//                    first = (Line) proc.lines.elementAt(k);
//                    if (first.line.toString().toLowerCase().indexOf(name.toLowerCase()) > -1) {
//                        preFix = first.line.toString().substring(first.line.toString().toLowerCase().indexOf(name.toLowerCase()) + name.length()).trim();
//                        int n = preFix.indexOf(' ');
//                        if (n > 0) {
//                            preFix = preFix.substring(0, n).trim();
//                        }
//                        if (!name.toLowerCase().startsWith(preFix.toLowerCase().substring(0, 1))) {
//                            logger.warn("PREFIX mismatch. Dropping PREFIX");
//                            preFix = "";
//                        }
//                        break;
//                    }
//                }
//            }
//            if (preFix.equals("")) {
//                logger.warn("Unable to determine PREFIX for table");
//            }
//            proc.lines.insertElementAt(new Line("SELECT "), 0);
//            for (j = 0; j < proc.outputs.size(); j++) {
//                Field fieldName = (Field) proc.outputs.elementAt(j);
//                if (table.hasField(fieldName.name)) {
//                    if (j == 0)
//                        if (preFix.length() > 0)
//                            line = "  " + preFix + "";
//                        else
//                            line = "  ";
//                    else if (preFix.length() > 0)
//                        line = ", " + preFix + "";
//                    else
//                        line = ", ";
//                } else {
//                    fieldName.isExtStd = true;
//                    fieldName.isExtStdOut = true;
//                    if (j == 0)
//                        line = "  ";
//                    else
//                        line = ", ";
//                }
//                proc.lines.insertElementAt(new Line(line + fieldName.useLiteral() + " "), j + 1);
//            }
//            if (proc.isStd) {
//                proc.isStd = false;
//                proc.extendsStd = true;
//                proc.useStd = true;
//            }
//        }
//    }

    public String toString() {
        return name;
    }

    private String set(String a, String b, String what) {
        if (a.length() == 0)
            a = b;
        else if (a.equalsIgnoreCase(b) == false)
            logger.warn("Import " + what + " name :" + a + " not the same as :" + b);
        return a;
    }

    private boolean set(boolean a, boolean b, String what) {
        if (a == false)
            a = b;
        else if (b == false)
            logger.warn("Import " + what + " is already true and is not set to false.");
        return a;
    }

    private void copy(Table addin) {
        name = addin.name;
        alias = addin.alias;
        check = addin.check;
        fields = addin.fields;
        keys = addin.keys;
        links = addin.links;
        grants = addin.grants;
        views = addin.views;
        procs = addin.procs;
        comments = addin.comments;
        options = addin.options;
        allUsers = addin.allUsers;
        hasPrimaryKey = addin.hasPrimaryKey;
        hasSequence = addin.hasSequence;
        hasTimeStamp = addin.hasTimeStamp;
        hasAutoTimeStamp = addin.hasAutoTimeStamp;
        hasUserStamp = addin.hasUserStamp;
        hasExecute = addin.hasExecute;
        hasSelect = addin.hasSelect;
        hasInsert = addin.hasInsert;
        hasDelete = addin.hasDelete;
        hasUpdate = addin.hasUpdate;
        hasStdProcs = addin.hasStdProcs;
        hasIdentity = addin.hasIdentity;
        start = addin.start;
    }

    private void merge(Table addin) {
        alias = set(alias, addin.alias, "alias");
        check = set(check, addin.check, "check");
        hasPrimaryKey = set(hasPrimaryKey, addin.hasPrimaryKey, "hasPrimaryKey");
        hasSequence = set(hasSequence, addin.hasSequence, "hasSequence");
        hasTimeStamp = set(hasTimeStamp, addin.hasTimeStamp, "hasTimeStamp");
        hasAutoTimeStamp = set(hasAutoTimeStamp, addin.hasAutoTimeStamp, "hasAutoTimeStamp");
        hasUserStamp = set(hasUserStamp, addin.hasUserStamp, "hasUserStamp");
        hasExecute = set(hasExecute, addin.hasExecute, "hasExecute");
        hasSelect = set(hasSelect, addin.hasSelect, "hasSelect");
        hasInsert = set(hasInsert, addin.hasInsert, "hasInsert");
        hasDelete = set(hasDelete, addin.hasDelete, "hasDelete");
        hasUpdate = set(hasUpdate, addin.hasUpdate, "hasUpdate");
        hasStdProcs = set(hasStdProcs, addin.hasStdProcs, "hasStdProcs");
        hasIdentity = set(hasIdentity, addin.hasIdentity, "hasIdentity");
    }

    public Table add(Table addin) {
        Table table = new Table();
        table.copy(this);
        table.merge(addin);
        return table;
    }

    public boolean hasOption(String value) {
        for (int i = 0; i < options.size(); i++) {
            String option = options.elementAt(i);
            if (option.toLowerCase().compareTo(value.toLowerCase()) == 0)
                return true;
        }
        return false;
    }
}
