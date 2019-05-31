package bbd.jportal2.util;

import bbd.jportal2.Proc;

public class Misc {
    public static String GenerateProcNameComment(Proc proc) {
        return "/* PROC " + proc.table.database.getSchema() + "." + proc.table.name + "." + proc.name + " */";
    }
}
