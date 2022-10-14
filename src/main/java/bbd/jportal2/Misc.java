package bbd.jportal2;


import java.util.ArrayList;

public class Misc {
    public static ArrayList<ISQLProcToken> generateProcNameComment(Proc proc) {
        ArrayList<ISQLProcToken> al = new ArrayList<ISQLProcToken>();
        al.add(new SQLProcStringToken("/* PROC "));
        al.add(new SQLProcTableNameToken(proc.table));
        al.add(new SQLProcStringToken("."));
        al.add(new SQLProcProcNameToken(proc.name));
        al.add(new SQLProcStringToken(" */"));
        return al;
    }
}
