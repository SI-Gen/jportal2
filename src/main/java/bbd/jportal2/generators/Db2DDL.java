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

public class Db2DDL extends BaseGenerator implements IBuiltInSIProcessor {
    private static final Logger logger = LoggerFactory.getLogger(Db2DDL.class);
    public Db2DDL() {
        super(Db2DDL.class);
    }

    /**
     * Generates the SQL for DB2 Table creation.
     */
    public String description() {
        return "Generate DB2 DDL";
    }

    public String documentation() {
        return "Generate DB2 DDL.";
    }

    public boolean hasData;

    public void generate(Database database, String output) {
        try {
            String fileName;
            if (database.output.length() > 0)
                fileName = database.output;
            else
                fileName = database.name;
            hasData = false;

            try (PrintWriter outData = this.openOutputFileForGeneration("sql", output + fileName + ".sql")) {
                for (int i = 0; i < database.tables.size(); i++)
                    generate((Table) database.tables.elementAt(i), outData);
                outData.flush();
            }
            if (hasData == true) {


                try (PrintWriter outData = this.openOutputFileForGeneration("_data.sql", output + fileName + "_data_.sql")) {
                    for (int i = 0; i < database.tables.size(); i++)
                        generateData((Table) database.tables.elementAt(i), outData);
                    outData.flush();
                }
            }
        } catch (IOException e1) {
            logger.error("Generate DB2 SQL IO Error");
        }
    }

    String bSO(int i) {
        String x = "" + (101 + i);
        return x.substring(1);
    }

    void generate(Table table, PrintWriter outData) {
        String tableOwner = "";
        if (table.database.schema.length() > 0)
            tableOwner = table.database.schema + "";
        else if (table.database.server.length() > 0)
            tableOwner = table.database.server + "";
        String tableName = tableOwner + table.name;
        String comma = "( ";
        boolean useBigSequence = false;
        if (table.fields.size() > 0) {
            outData.println("DROP TABLE " + tableName + ";");
            outData.println();
            outData.println("CREATE TABLE " + tableName);
            for (int i = 0; i < table.fields.size(); i++, comma = ", ") {
                Field field = (Field) table.fields.elementAt(i);
                if (field.type == Field.BIGSEQUENCE)
                    useBigSequence = true;
                outData.print(comma + field.name + " " + varType(field));
                if (field.defaultValue.length() > 0)
                    outData.print(" DEFAULT " + field.defaultValue);
                if (field.checkValue.length() > 0)
                    outData.print(" CHECK (" + field.checkValue + ")");
                else if (field.isNull == false)
                    outData.print(" NOT NULL");
                if (field.type == Field.TIMESTAMP)
                    outData.print(" GENERATED BY DEFAULT FOR EACH ROW ON UPDATE AS ROW CHANGE TIMESTAMP");
                outData.println();
            }
            outData.print(")");
            if (table.options.size() > 0) {
                for (int i = 0; i < table.options.size(); i++) {
                    String option = (String) table.options.elementAt(i);
                    if (option.toLowerCase().indexOf("tablespace") == 0) {
                        outData.println();
                        outData.print("  IN " + option.substring(11));
                        continue;
                    } else if (option.toLowerCase().indexOf("distribute") == 0) {
                        outData.println();
                        outData.print("  " + option);
                        continue;
                    } else if (option.toLowerCase().indexOf("partition") == 0) {
                        outData.println();
                        outData.print("  " + option);
                        continue;
                    }
                }
            }
            outData.println(";");
            outData.println();
            if (table.database.server.length() > 0) {
                outData.println("DROP ALIAS " + table.name + ";");
                outData.println();
                outData.println("CREATE ALIAS " + table.name + " FOR " + tableName + ";");
                outData.println();
            }
            for (int i = 0; i < table.grants.size(); i++) {
                Grant grant = (Grant) table.grants.elementAt(i);
                generate(grant, outData, tableName);
            }
            if (table.hasSequence) {
                outData.println("DROP SEQUENCE " + tableName + "Seq;");
                outData.println();
                outData.println("CREATE SEQUENCE " + tableName + "Seq");
                if (useBigSequence == true)
                    outData.println("  AS BIGINT");
                else
                    outData.println("  AS INT");
                outData.println("  START WITH 1");
                outData.println("  INCREMENT BY 1");
                outData.println("  NO MAXVALUE");
                outData.println("  NO CYCLE");
                outData.println("  CACHE 200");
                outData.println("  ORDER;");
                outData.println();
                if (table.grants.size() > 0) {
                    Grant grant = (Grant) table.grants.elementAt(0);
                    for (int j = 0; j < grant.users.size(); j++) {
                        String user = (String) grant.users.elementAt(j);
                        outData.println("GRANT SELECT ON " + tableName + "SEQ TO " + user + ";");
                        outData.println();
                    }
                }
            }
            for (int i = 0; i < table.keys.size(); i++) {
                Key key = (Key) table.keys.elementAt(i);
                boolean PSH = false;
                String fieldName = (String) key.fields.elementAt(0);
                int no = table.getFieldIndex(fieldName);
                if (no != -1) {
                    Field field = (Field) table.fields.elementAt(no);
                    if (field.type == Field.BIGIDENTITY
                            || field.type == Field.BIGSEQUENCE
                            || field.type == Field.IDENTITY
                            || field.type == Field.SEQUENCE)
                        PSH = true;
                }
                if ((!key.isPrimary && !key.isUnique) || PSH == true)
                    generateIndexPSH(table, key, outData, PSH);
            }
        }
        for (int i = 0; i < table.views.size(); i++) {
            View view = (View) table.views.elementAt(i);
            generate(view, outData, table.name, tableOwner);
        }
        if (table.keys.size() > 0) {
            for (int i = 0; i < table.keys.size(); i++) {
                Key key = (Key) table.keys.elementAt(i);
                if (key.isPrimary) {
                    outData.println("ALTER TABLE " + tableName);
                    generatePrimary(table, key, outData);
                    outData.println(";");
                } else if (key.isUnique) {
                    outData.println("ALTER TABLE " + tableName);
                    generateUnique(table, key, outData);
                    outData.println(";");
                }
            }
            outData.println("");
        }
        if (table.links.size() > 0) {
            for (int i = 0; i < table.links.size(); i++) {
                Link link = (Link) table.links.elementAt(i);
                if (link.linkName.length() == 0)
                    link.linkName = table.name + "_FK" + bSO(i);
                generate(link, outData, tableName, tableOwner, i);
            }
        }
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData)
                hasData = true;
        }
    }

    void generateData(Table table, PrintWriter outData) {
        for (int i = 0; i < table.procs.size(); i++) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData)
                generate(proc, outData);
        }
    }

    String makeMaxName(String data, int size) {
        if (data.length() <= size)
            return data;
        String x = "_UOIEAY";
        for (int i = 0; i < x.length(); i++) {
            char lookup = x.charAt(i);
            int n = data.indexOf(lookup);
            while (n != -1) {
                if (n == 0)
                    data = data.substring(1);
                else if (n == data.length() - 1)
                    data = data.substring(0, n);
                else
                    data = data.substring(0, n) + data.substring(n + 1);
                if (data.length() <= size)
                    return data;
                n = data.indexOf(lookup);
            }
        }
        return data.substring(0, size);
    }

    /**
     * Generates SQL code for DB2 Primary Key create
     */
    void generatePrimary(Table table, Key key, PrintWriter outData) {
        String comma = "( ";
        String keyname = key.name.toUpperCase();
        if (keyname.indexOf(table.name.toUpperCase()) == -1)
            keyname = table.name.toUpperCase() + "_" + keyname;
        outData.println("ADD CONSTRAINT " + makeMaxName(keyname, 128) + " PRIMARY KEY");
        for (int i = 0; i < key.fields.size(); i++, comma = "  , ") {
            String name = (String) key.fields.elementAt(i);
            outData.println(comma + name);
        }
        outData.print(")");
        for (int i = 0; i < key.options.size(); i++) {
            String option = (String) key.options.elementAt(i);
            if (option.toLowerCase().indexOf("tablespace") == 0) {
                outData.print(" IN " + option.substring(11));
                break;
            }
        }
    }

    /**
     * Generates SQL code for DB2 Unique Key create
     */
    void generateUnique(Table table, Key key, PrintWriter outData) {
        String comma = "( ";
        String keyname = key.name.toUpperCase();
        if (keyname.indexOf(table.name.toUpperCase()) == -1)
            keyname = table.name.toUpperCase() + "_" + keyname;
        outData.println("ADD CONSTRAINT " + makeMaxName(keyname, 128) + " UNIQUE");
        for (int i = 0; i < key.fields.size(); i++, comma = "  , ") {
            String name = (String) key.fields.elementAt(i);
            outData.println(comma + name);
        }
        outData.print(")");
        for (int i = 0; i < key.options.size(); i++) {
            String option = (String) key.options.elementAt(i);
            if (option.toLowerCase().indexOf("tablespace") == 0) {
                outData.print(" IN " + option.substring(11));
                break;
            }
        }
    }

    /**
     * Generates SQL code for DB2 Index create
     */
    void generateIndex(Table table, Key key, PrintWriter outData) {
        generateIndexPSH(table, key, outData, false);
    }

    void generateIndexPSH(Table table, Key key, PrintWriter outData, boolean withPSH) {
        String tableOwner = "";
        if (table.database.schema.length() > 0)
            tableOwner = table.database.schema + "";
        else if (table.database.server.length() > 0)
            tableOwner = table.database.server + "";
        String comma = "( ";
        String keyname = key.name.toUpperCase();
        if (keyname.indexOf(table.name.toUpperCase()) == -1)
            keyname = makeMaxName(table.name.toUpperCase() + "_" + keyname, 128);
        outData.println("DROP INDEX " + tableOwner + keyname + ";");
        outData.println("");
        outData.println("CREATE INDEX " + tableOwner + keyname + " ON " + tableOwner + table.name);
        for (int i = 0; i < key.fields.size(); i++, comma = ", ") {
            String name = (String) key.fields.elementAt(i);
            outData.println(comma + name);
        }
        outData.print(")");
        for (int i = 0; i < key.options.size(); i++) {
            String option = (String) key.options.elementAt(i);
            if (option.toLowerCase().indexOf("tablespace") == 0) {
                outData.print(" IN " + option.substring(11));
                break;
            }
        }
        if (withPSH == true)
            outData.print(" PAGE SPLIT HIGH ");
        outData.println(";");
        outData.println();
    }

    /**
     * Generates foreign key SQL Code for DB2
     */
    void generate(Link link, PrintWriter outData, String tableName, String owner, int no) {
        outData.println("ALTER TABLE " + tableName);
        String comma = "( ";
        String linkname = "FK" + no + link.linkName.toUpperCase();
        outData.println("ADD CONSTRAINT " + makeMaxName(linkname, 128) + " FOREIGN KEY");
        for (int i = 0; i < link.fields.size(); i++, comma = "    , ") {
            String name = (String) link.fields.elementAt(i);
            outData.println(comma + name);
        }
        outData.print(") REFERENCES " + owner + link.name);
        if (link.linkFields.size() > 0) {
            comma = "(";
            for (int i = 0; i < link.linkFields.size(); i++) {
                String name = (String) link.linkFields.elementAt(i);
                outData.print(comma + name);
                comma = ", ";
            }
            outData.print(")");
        }
        if (link.isDeleteCascade)
            outData.print(" ON DELETE CASCADE");
        outData.println(";");
        outData.println();
    }

    /**
     * Generates grants for DB2
     */
    void generate(Grant grant, PrintWriter outData, String object) {
        for (int i = 0; i < grant.perms.size(); i++) {
            String perm = (String) grant.perms.elementAt(i);
            for (int j = 0; j < grant.users.size(); j++) {
                String user = (String) grant.users.elementAt(j);
                outData.println("GRANT " + perm + " ON " + object + " TO " + user + ";");
                outData.println();
            }
        }
    }

    /**
     * Generates views for DB2
     */
    void generate(View view, PrintWriter outData, String tableName, String tableOwner) {
        outData.println("DROP VIEW " + tableOwner + tableName + view.name + ";");
        outData.println();
        outData.println("CREATE VIEW " + tableOwner + tableName + view.name);
        if (view.aliases.size() > 0) {
            String comma = "( ";
            for (int i = 0; i < view.aliases.size(); i++) {
                String alias = (String) view.aliases.elementAt(i);
                outData.println(comma + alias);
                comma = ", ";
            }
            outData.print(") ");
        }
        outData.println("AS");
        outData.println("(");
        for (int i = 0; i < view.lines.size(); i++) {
            String line = (String) view.lines.elementAt(i);
            outData.println(line);
        }
        outData.println(");");
        outData.println();
        for (int i = 0; i < view.users.size(); i++) {
            String user = (String) view.users.elementAt(i);
            outData.println("GRANT SELECT ON " + tableName + view.name + " TO " + user + ";");
        }
        outData.println();
    }

    /**
     * Generates pass through data for DB2
     */
    void generate(Proc proc, PrintWriter outData) {
        for (int i = 0; i < proc.lines.size(); i++) {
            Line l = proc.lines.elementAt(i);
            outData.println(l.getDecoratedLine());
        }
        outData.println();
    }

    /**
     * Translates field type to DB2 SQL column types
     */
    String varType(Field field) {
        switch (field.type) {
            case Field.BYTE:
                return "SMALLINT";
            case Field.SHORT:
                return "SMALLINT";
            case Field.INT:
            case Field.SEQUENCE:
                return "INT";
            case Field.LONG:
            case Field.BIGSEQUENCE:
                return "BIGINT";
            case Field.CHAR:
                if (field.length > 32762)
                    return "CLOB(" + String.valueOf(field.length) + ")";
                else
                    return "VARCHAR(" + String.valueOf(field.length) + ")";
            case Field.ANSICHAR:
                return "CHAR(" + String.valueOf(field.length) + ")";
            case Field.DATE:
                return "DATE";
            case Field.DATETIME:
                return "TIMESTAMP";
            case Field.TIME:
                return "TIME";
            case Field.TIMESTAMP:
                return "TIMESTAMP";
            case Field.FLOAT:
            case Field.DOUBLE:
                if (field.scale != 0)
                    return "DECIMAL(" + String.valueOf(field.precision) + ", " + String.valueOf(field.scale) + ")";
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
                return "JSON";
            case Field.IDENTITY:
                return "<not supported>";
        }
        return "unknown";
    }
}
