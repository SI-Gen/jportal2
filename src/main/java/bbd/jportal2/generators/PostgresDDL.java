package bbd.jportal2.generators;

import bbd.jportal2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

public class PostgresDDL extends BaseGenerator implements IBuiltInSIProcessor {

    private String tableOwner;
    private static final Logger logger = LoggerFactory.getLogger(PostgresDDL.class);

    public PostgresDDL() {
        super(PostgresDDL.class);
    }

    @Override
    public String description() {
        return "Generate PostgreSQL DDL";
    }

    @Override
    public String documentation() {
        return "Generate PostgreSQL DDL.";
    }

    @Override
    public void generate(Database database, String output) {
        try {
            String fileName;
            if (database.output.length() > 0) {
                fileName = database.output;
            } else {
                fileName = database.name;
            }

            logger.info("DDL: {}{}.sql", output, fileName);

            try (PrintWriter outData = openOutputFileForGeneration("sql",
                    output + fileName + ".sql")) {

                for (int i = 0; i < database.tables.size(); ++i) {
                    generateTable(database, database.tables.elementAt(i), outData);
                }

                outData.flush();
            }

        } catch (IOException var11) {
            logger.info("Generate Posgre SQL IO Error");
        }

    }

    private void generateTable(Database database, Table table, PrintWriter outData) {
        if (database.schema.length() > 0) {
            tableOwner = database.schema + ".";
        } else {
            tableOwner = "";
        }

        String comma = "( ";
        boolean hasNotNull = false;
        int i;
        Key key;
        if (!table.fields.isEmpty()) {
            outData.println("DROP TABLE " + tableOwner + table.name + " CASCADE;");
            outData.println();
            outData.println("CREATE TABLE " + tableOwner + table.name);

            for (i = 0; i < table.fields.size(); comma = ", ", ++i) {
                Field field = table.fields.elementAt(i);
                outData.println(comma + field.name + " " + varType(field));
                if (field.defaultValue.length() > 0) {
                    hasNotNull = true;
                }

                if (field.checkValue.length() > 0) {
                    hasNotNull = true;
                } else if (!field.isNull) {
                    hasNotNull = true;
                }
            }

            outData.print(")");

            for (i = 0; i < table.options.size(); ++i) {
                String option = table.options.elementAt(i);
                if (option.toLowerCase().indexOf("tablespace") == 0) {
                    outData.println();
                    outData.print(option);
                }
            }

            outData.println(";");
            outData.println();

            for (i = 0; i < table.grants.size(); ++i) {
                Grant grant = table.grants.elementAt(i);
                generateGrant(grant, outData, tableOwner + table.name);
            }

            for (i = 0; i < table.keys.size(); ++i) {
                key = table.keys.elementAt(i);
                if (!key.isPrimary && !key.isUnique) {
                    generateIndex(table, key, outData);
                }
            }
        }

        for (i = 0; i < table.views.size(); ++i) {
            View view = table.views.elementAt(i);
            generateView(view, outData, table.name, tableOwner);
        }

        if (hasNotNull) {
            String alterTable = "ALTER TABLE " + tableOwner + table.name;

            for (int j = 0; j < table.fields.size(); ++j) {
                Field field = table.fields.elementAt(j);
                if (!field.isNull || field.defaultValue.length() != 0 || field.checkValue.length() != 0) {
                    outData.print(alterTable + " ALTER " + field.name + " SET");
                    if (field.defaultValue.length() > 0) {
                        outData.print(" DEFAULT " + field.defaultValue);
                    }

                    if (field.checkValue.length() > 0) {
                        outData.print(" CHECK (" + field.checkValue + ")");
                    } else {
                        outData.print(" NOT NULL");
                    }

                    outData.println(";");
                }
            }

            outData.println();
        }

        if (!table.keys.isEmpty()) {
            for (i = 0; i < table.keys.size(); ++i) {
                key = table.keys.elementAt(i);
                if (key.isPrimary) {
                    outData.println("ALTER TABLE " + tableOwner + table.name);
                    generatePrimary(table, key, outData);
                    outData.println(";");
                } else if (key.isUnique) {
                    outData.println("ALTER TABLE " + tableOwner + table.name);
                    generateUnique(table, key, outData);
                    outData.println(";");
                }
            }

            outData.println();
        }

        if (!table.links.isEmpty()) {
            for (i = 0; i < table.links.size(); ++i) {
                Link link = table.links.elementAt(i);
                outData.println("ALTER TABLE " + tableOwner + table.name);
                if (link.linkName.length() == 0) {
                    link.linkName = table.name.toUpperCase() + "_FK" + bSO(i);
                }

                generateLink(link, tableOwner, outData);
                outData.println(";");
            }

            outData.println();
        }

        for (i = 0; i < table.procs.size(); ++i) {
            Proc proc = table.procs.elementAt(i);
            if (proc.isData) {
                generateProc(proc, outData);
            }
        }

    }

    private void generateProc(Proc proc, PrintWriter outData) {

        for (Line line : proc.lines) {
            outData.println(line);
        }

        outData.println();
    }

    private void generateLink(Link link, String tableOwner, PrintWriter outData) {
        String comma = "  ( ";
        outData.println(" ADD CONSTRAINT " + link.linkName + " FOREIGN KEY");

        for (int i = 0; i < link.fields.size(); comma = "  , ", ++i) {
            String name = link.fields.elementAt(i);
            outData.println(comma + name);
        }

        outData.println("  ) REFERENCES " + tableOwner + link.name + " MATCH FULL");
    }

    private void generateUnique(Table table, Key key, PrintWriter outData) {
        String comma = "  ( ";
        String keyname = key.name.toUpperCase();
        if (!keyname.contains(table.name.toUpperCase())) {
            keyname = table.name.toUpperCase() + "_" + keyname;
        }

        outData.println(" ADD CONSTRAINT " + keyname + " UNIQUE");

        for (int i = 0; i < key.fields.size(); comma = "  , ", ++i) {
            String name = key.fields.elementAt(i);
            outData.println(comma + name);
        }

        outData.println("  )");
    }

    private void generatePrimary(Table table, Key key, PrintWriter outData) {
        String comma = "  ( ";
        String keyname = key.name.toUpperCase();
        if (!keyname.contains(table.name.toUpperCase())) {
            keyname = table.name.toUpperCase() + "_" + keyname;
        }

        outData.println(" ADD CONSTRAINT " + keyname + " PRIMARY KEY");

        int i;
        String option;
        for (i = 0; i < key.fields.size(); comma = "  , ", ++i) {
            option = key.fields.elementAt(i);
            outData.println(comma + option);
        }

        outData.print("  )");

        for (i = 0; i < key.options.size(); ++i) {
            option = key.options.elementAt(i);
            if (option.toLowerCase().indexOf("tablespace") == 0) {
                outData.print(" USING INDEX " + option);
            }
        }

        outData.println();
    }

    private String bSO(int i) {
        String x = "" + (101 + i);
        return x.substring(1);
    }

    private void generateView(View view, PrintWriter outData, String tableName, String tableOwner) {
        outData.println("CREATE OR REPLACE VIEW " + tableOwner + tableName + view.name);
        if (!view.aliases.isEmpty()) {
            String comma = "( ";

            for (int i = 0; i < view.aliases.size(); ++i) {
                String alias = view.aliases.elementAt(i);
                outData.println(comma + alias);
                comma = ", ";
            }

            outData.println(")");
        }

        outData.println("AS (");

        int i;
        String user;
        for (i = 0; i < view.lines.size(); ++i) {
            user = view.lines.elementAt(i);
            outData.println(user);
        }

        outData.println(");");
        outData.println();

        for (i = 0; i < view.users.size(); ++i) {
            user = view.users.elementAt(i);
            outData.println("GRANT SELECT ON " + tableOwner + tableName + view.name + " TO " + user + ";");
        }

        outData.println();
    }

    private void generateIndex(Table table, Key key, PrintWriter outData) {
        String comma = "( ";
        String keyname = key.name.toUpperCase();
        if (!keyname.contains(table.name.toUpperCase())) {
            keyname = table.name.toUpperCase() + "_" + keyname;
        }

        outData.println("-- DROP INDEX " + keyname + ";");
        outData.println("");
        outData.println("CREATE INDEX " + keyname + " ON " + tableOwner + table.name);

        int i;
        String option;
        for (i = 0; i < key.fields.size(); comma = ", ", ++i) {
            option = key.fields.elementAt(i);
            outData.println(comma + option);
        }

        outData.print(")");

        for (i = 0; i < key.options.size(); ++i) {
            option = key.options.elementAt(i);
            if (option.toLowerCase().indexOf("tablespace") == 0) {
                outData.println();
                outData.print(option);
            }
        }

        outData.println(";");
        outData.println();
    }

    private void generateGrant(Grant grant, PrintWriter outData, String on) {
        for (int i = 0; i < grant.perms.size(); ++i) {
            String perm = grant.perms.elementAt(i);

            for (int j = 0; j < grant.users.size(); ++j) {
                String user = grant.users.elementAt(j);
                outData.println("GRANT " + perm + " ON " + on + " TO " + user + ";");
                outData.println();
            }
        }

    }

    private static String varType(Field field) {
        switch (field.type) {
            case 1:
                return "bytea";
            case 2:
            case 8:
            case 16:
            default:
                return "unknown";
            case 3:
                return "smallint";
            case 4:
                return "varchar(" + field.length + ")";
            case 5:
                return "date";
            case 6:
                return "timestamp";
            case 7:
            case 9:
                if (field.precision == 0 && field.scale == 0) {
                    return "float8";
                }

                return "numeric(" + field.precision + ", " + field.scale + ")";
            case 10:
                return "<not supported>";
            case 11:
                return "integer";
            case 12:
                return "bigint";
            case 13:
                return "numeric(18,2)";
            case 14:
                return "serial";
            case 15:
                return "smallint";
            case 17:
                return "time";
            case 18:
                return "timestamp";
            case 19:
                return "text";
            case 20:
                return "VARCHAR(16)";
            case 21:
                return "character(" + field.length + ") -- beware char to varchar morph";
        }
    }
}
