package bbd.jportal2.generators;

import bbd.jportal2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class OracleDDL extends BaseGenerator implements IBuiltInSIProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OracleDDL.class);

    public OracleDDL() {
        super(OracleDDL.class);
    }

    @Override
    public String description() {
        return "Generate Oracle DDL";
    }

    @Override
    public String documentation() {
        return "Generate Oracle DDL.";
    }

    @Override
    public void generate(Database database, String output) {
        try {
            String tableOwner = "";
            String fileName;
            if (database.output.length() > 0) {
                fileName = database.output;
            } else {
                fileName = database.name;
            }

            logger.info("DDL: " + output + fileName + ".sql");
            FileOutputStream outFile = new FileOutputStream(output + fileName + ".sql");

            try {
                PrintWriter outData = new PrintWriter(outFile);
                if (database.schema.length() > 0) {
                    tableOwner = database.schema + ".";
                } else if (database.userid.length() > 0) {
                    tableOwner = database.userid + ".";
                }

                if (database.password.length() > 0) {
                    outData.println("CONNECT " + database.userid + "/" + database.password + "@" + database.server);
                    outData.println();
                }

                int i;
                for (i = 0; i < database.tables.size(); ++i) {
                    generate((Table) database.tables.elementAt(i), outData);
                }

                for (i = 0; i < database.views.size(); ++i) {
                    generate((View) database.views.elementAt(i), outData, "", tableOwner);
                }

                for (i = 0; i < database.sequences.size(); ++i) {
                    generate((Sequence) database.sequences.elementAt(i), outData, tableOwner);
                }

                outData.flush();
            } finally {
                outFile.close();
            }
        } catch (IOException ex) {
            logger.error("Generate Oracle SQL IO Error", ex);
        }

    }

    static String bSO(int i) {
        String x = "" + (101 + i);
        return x.substring(1);
    }

    static void generate(Table table, PrintWriter outData) {
        String tableOwner = "";
        if (table.database.userid.length() > 0) {
            tableOwner = table.database.userid + ".";
        }

        String comma = "( ";
        boolean hasNotNull = false;
        int i;
        Field field;
        int j;
        if (table.fields.size() > 0) {
            outData.println("DROP TABLE " + tableOwner + table.name + " CASCADE CONSTRAINTS;");
            outData.println();
            outData.println("CREATE TABLE " + tableOwner + table.name);

            for (i = 0; i < table.fields.size(); comma = ", ") {
                field = (Field) table.fields.elementAt(i);
                outData.println(comma + field.name + " " + varType(field));
                if (field.defaultValue.length() > 0) {
                    hasNotNull = true;
                }

                if (field.checkValue.length() > 0) {
                    hasNotNull = true;
                } else if (!field.isNull) {
                    hasNotNull = true;
                }

                ++i;
            }

            outData.print(")");
            if (table.options.size() > 0) {
                for (i = 0; i < table.options.size(); ++i) {
                    String option = (String) table.options.elementAt(i);
                    outData.println();
                    outData.print(option);
                }
            }

            outData.println(";");
            outData.println();
            outData.println("DROP PUBLIC SYNONYM " + table.name + ";");
            outData.println();
            outData.println("CREATE PUBLIC SYNONYM " + table.name + " FOR " + tableOwner + table.name + ";");
            outData.println();

            for (i = 0; i < table.grants.size(); ++i) {
                Grant grant = table.grants.elementAt(i);
                generate(grant, outData, tableOwner + table.name);
            }

            if (table.hasSequence) {
                outData.println("DROP SEQUENCE " + tableOwner + table.name + "Seq;");
                outData.println();
                outData.println("CREATE SEQUENCE " + tableOwner + table.name + "Seq");
                outData.println("  MINVALUE 1");
                outData.println("  MAXVALUE 999999999");
                outData.println("  CYCLE");
                outData.println("  ORDER;");
                outData.println();
                outData.println("DROP PUBLIC SYNONYM " + table.name + "SEQ;");
                outData.println();
                outData.println("CREATE PUBLIC SYNONYM " + table.name + "SEQ FOR " + tableOwner + table.name + "SEQ;");
                outData.println();
                if (table.grants.size() > 0) {
                    Grant grant = table.grants.elementAt(0);

                    for (j = 0; j < grant.users.size(); ++j) {
                        String user = (String) grant.users.elementAt(j);
                        outData.println("GRANT SELECT ON " + tableOwner + table.name + "SEQ TO " + user + ";");
                        outData.println();
                    }
                }
            }

            for (i = 0; i < table.keys.size(); ++i) {
                Key key = table.keys.elementAt(i);
                if (!key.isPrimary && !key.isUnique) {
                    generateIndex(table, key, outData);
                }
            }
        }

        for (i = 0; i < table.views.size(); ++i) {
            View view = table.views.elementAt(i);
            generate(view, outData, table.name, tableOwner);
        }

        if (hasNotNull) {
            outData.println("ALTER TABLE " + tableOwner + table.name);
            outData.println("MODIFY");
            comma = "( ";

            for (i = 0; i < table.fields.size(); comma = ", ") {
                field = (Field) table.fields.elementAt(i);
                if (!field.isNull || field.defaultValue.length() != 0 || field.checkValue.length() != 0) {
                    outData.print(comma + field.name + " CONSTRAINT " + table.name + "_NN" + bSO(i));
                    if (field.defaultValue.length() > 0) {
                        outData.print(" DEFAULT " + field.defaultValue);
                    }

                    if (field.checkValue.length() > 0) {
                        outData.print(" CHECK (" + field.checkValue + ")");
                    } else {
                        outData.print(" NOT NULL");
                    }

                    outData.println();
                }

                ++i;
            }

            outData.println(");");
            outData.println();
        }

        String mComma;
        if (table.keys.size() > 0) {
            mComma = "( ";
            outData.println("ALTER TABLE " + tableOwner + table.name);
            outData.println("ADD");

            for (j = 0; j < table.keys.size(); ++j) {
                Key key = (Key) table.keys.elementAt(j);
                if (key.isPrimary) {
                    generatePrimary(table, key, outData, mComma);
                } else if (key.isUnique) {
                    generateUnique(table, key, outData, mComma);
                }

                mComma = ", ";
            }

            outData.println(");");
            outData.println();
        }

        if (table.links.size() > 0) {
            mComma = "( ";
            outData.println("ALTER TABLE " + tableOwner + table.name);
            outData.println("ADD");

            for (j = 0; j < table.links.size(); ++j) {
                Link link = table.links.elementAt(j);
                if (link.linkName.length() == 0) {
                    link.linkName = table.name + "_FK" + bSO(j);
                }

                generate(link, outData, mComma);
                mComma = ", ";
            }

            outData.println(");");
            outData.println();
        }

        for (i = 0; i < table.procs.size(); ++i) {
            Proc proc = (Proc) table.procs.elementAt(i);
            if (proc.isData) {
                generate(proc, outData);
            }
        }

    }

    static void generatePrimary(Table table, Key key, PrintWriter outData, String mcomma) {
        String comma = "  ( ";
        String keyname = key.name.toUpperCase();
        if (!keyname.contains(table.name.toUpperCase())) {
            keyname = table.name.toUpperCase() + "_" + keyname;
        }

        outData.println(mcomma + "CONSTRAINT " + keyname + " PRIMARY KEY");

        int i;
        String option;
        for (i = 0; i < key.fields.size(); comma = "  , ") {
            option = key.fields.elementAt(i);
            outData.println(comma + option);
            ++i;
        }

        outData.println("  )");

        for (i = 0; i < key.options.size(); ++i) {
            option = key.options.elementAt(i);
            outData.println("  " + option);
        }

    }

    static void generateUnique(Table table, Key key, PrintWriter outData, String mcomma) {
        String comma = "  ( ";
        String keyname = key.name.toUpperCase();
        if (!keyname.contains(table.name.toUpperCase())) {
            keyname = table.name.toUpperCase() + "_" + keyname;
        }

        outData.println(mcomma + "CONSTRAINT " + keyname + " UNIQUE");

        int i;
        String option;
        for (i = 0; i < key.fields.size(); comma = "  , ") {
            option = key.fields.elementAt(i);
            outData.println(comma + option);
            ++i;
        }

        outData.println("  )");

        for (i = 0; i < key.options.size(); ++i) {
            option = key.options.elementAt(i);
            outData.println("  " + option);
        }

    }

    private static void generateIndex(Table table, Key key, PrintWriter outData) {
        String tableOwner = "";
        if (table.database.userid.length() > 0) {
            tableOwner = table.database.userid + ".";
        }

        String comma = "( ";
        String keyname = key.name.toUpperCase();
        if (!keyname.contains(table.name.toUpperCase())) {
            keyname = table.name.toUpperCase() + "_" + keyname;
        }

        outData.println("DROP INDEX " + keyname + ";");
        outData.println("");
        outData.println("CREATE INDEX " + keyname + " ON " + tableOwner + table.name);

        int i;
        String option;
        for (i = 0; i < key.fields.size(); comma = ", ") {
            option = key.fields.elementAt(i);
            outData.println(comma + option);
            ++i;
        }

        outData.print(")");

        for (i = 0; i < key.options.size(); ++i) {
            outData.println();
            option = key.options.elementAt(i);
            outData.print(option);
        }

        outData.println(";");
        outData.println();
    }

    static void generate(Link link, PrintWriter outData, String mComma) {
        String comma = "  ( ";
        outData.println(mComma + "CONSTRAINT " + link.linkName + " FOREIGN KEY");

        for (int i = 0; i < link.fields.size(); comma = "  , ") {
            String name = link.fields.elementAt(i);
            outData.println(comma + name);
            ++i;
        }

        outData.println("  ) REFERENCES " + link.name);
    }

    static void generate(Grant grant, PrintWriter outData, String object) {
        for (int i = 0; i < grant.perms.size(); ++i) {
            String perm = grant.perms.elementAt(i);

            for (int j = 0; j < grant.users.size(); ++j) {
                String user = grant.users.elementAt(j);
                outData.println("GRANT " + perm + " ON " + object + " TO " + user + ";");
                outData.println();
            }
        }

    }

    static void generate(View view, PrintWriter outData, String tableName, String tableOwner) {
        outData.println("CREATE OR REPLACE FORCE VIEW " + tableName + view.name);
        String comma = "( ";

        int i;
        String user;
        for (i = 0; i < view.aliases.size(); ++i) {
            user = view.aliases.elementAt(i);
            outData.println(comma + user);
            comma = ", ";
        }

        outData.println(") AS");
        outData.println("(");

        for (i = 0; i < view.lines.size(); ++i) {
            user = view.lines.elementAt(i);
            outData.println(user);
        }

        outData.println(");");
        outData.println();

        for (i = 0; i < view.users.size(); ++i) {
            user = view.users.elementAt(i);
            outData.println("GRANT SELECT ON " + tableName + view.name + " TO " + user + ";");
        }

        outData.println();
        outData.println("DROP PUBLIC SYNONYM " + tableName + view.name + ";");
        outData.println();
        outData.println("CREATE PUBLIC SYNONYM " + tableName + view.name + " FOR " + tableOwner + tableName + view.name + ";");
        outData.println();
    }

    static void generate(Proc proc, PrintWriter outData) {

        for (Line line : proc.getLines()) {
            outData.println(line);
        }

        outData.println();
    }

    static void generate(Sequence sequence, PrintWriter outData, String tableOwner) {
        outData.println("DROP SEQUENCE " + sequence.name + ";");
        outData.println();
        outData.println("CREATE SEQUENCE " + sequence.name);
        outData.println("  MINVALUE  " + sequence.minValue);
        outData.println("  MAXVALUE  " + sequence.maxValue);
        outData.println("  INCREMENT BY " + sequence.increment);
        if (sequence.cycleFlag) {
            outData.println("  CYCLE");
        }

        if (sequence.orderFlag) {
            outData.println("  ORDER");
        }

        outData.println("  START WITH " + sequence.startWith + ";");
        outData.println();
        outData.println("DROP PUBLIC SYNONYM " + sequence.name + ";");
        outData.println();
        outData.println("CREATE PUBLIC SYNONYM " + sequence.name + " FOR " + tableOwner + sequence.name + ";");
        outData.println();
    }

    private static String varType(Field field) {
        switch (field.type) {
            case 1:
                return "BLOB";
            case 2:
            case 8:
            case 16:
            default:
                return "unknown";
            case 3:
                return "NUMBER(3)";
            case 4:
                return "VARCHAR2(" + field.length + ")";
            case 5:
                return "DATE";
            case 6:
                return "DATE";
            case 7:
            case 9:
                if (field.scale != 0) {
                    return "NUMBER(" + field.precision + ", " + field.scale + ")";
                } else {
                    if (field.precision != 0) {
                        return "NUMBER(" + field.precision + ")";
                    }

                    return "NUMBER";
                }
            case 10:
                return "<not supported>";
            case 11:
            case 14:
                return "NUMBER(10)";
            case 12:
                return "NUMBER(18)";
            case 13:
                return "NUMBER(15,2)";
            case 15:
                return "NUMBER(5)";
            case 17:
                return "DATE";
            case 18:
                return "DATE";
            case 19:
                return "CLOB";
            case 20:
                return "VARCHAR2(8)";
            case 21:
                return "CHAR(" + field.length + ")";
        }
    }
}
