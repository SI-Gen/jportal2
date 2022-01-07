package bbd.jportal2;

public class Misc {
    public static String generateProcNameComment(Proc proc) {
        return "/* PROC " + proc.table.database.getSchema() + "." + proc.table.name + "." + proc.name + " */";
    }
}
