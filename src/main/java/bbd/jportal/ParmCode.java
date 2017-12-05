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

package bbd.jportal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.*;

public class ParmCode extends Generator
{

  private static class ParmOptions
  {
    public String descr;
    public String lookup;
    public String show;
    public boolean viewOnly;
    public boolean domain;
    public boolean nullEnabled;
    public ParmOptions()
    {
      descr = "";
      lookup= "";
      show="";
      viewOnly = false;
      domain = true;
      nullEnabled=false;
    }
  }
  static final String reservedWords=":"
      +"all:" 
      +"ansi:"
      +"ansichar:"
      +"autotimestamp:"
      +"bigidentity:"
      +"bigsequence:"
      +"bigxml:"
      +"bit:"
      +"blob:"
      +"boolean:"
      +"bulkinsert:"
      +"bulkupdate:"
      +"byte:"
      +"cascade:"
      +"char:"
      +"check:"
      +"clob:"
      +"const:"
      +"constant:"
      +"count:"
      +"cursor:"
      +"database:"
      +"date:"
      +"datetime:"
      +"declare:"
      +"default:"
      +"delete:"
      +"deleteall:"
      +"deleteone:"
      +"double:"
      +"dynamic:"
      +"execute:"
      +"exists:"
      +"flags:"
      +"float:"
      +"for:"
      +"grant:"
      +"identity:"
      +"import:"
      +"in:"
      +"inout:"
      +"input:"
      +"insert:"
      +"int:"
      +"integer:"
      +"key:"
      +"link:"
      +"long:"
      +"lookup:"
      +"merge:"
      +"money:"
      +"multiple:"
      +"names:"
      +"not:"
      +"null:"
      +"options:"
      +"order:"
      +"output:"
      +"package:"
      +"password:"
      +"primary:"
      +"proc:"
      +"readonly:"
      +"returning:"
      +"schema:"
      +"select:"
      +"selectall:"
      +"selectone:"
      +"sequence:"
      +"server:"
      +"short:"
      +"single:"
      +"sorted:"
      +"sproc:"
      +"sql:"
      +"storedproc:"
      +"table:"
      +"time:"
      +"timestamp:"
      +"tlob:"
      +"to:"
      +"uid:"
      +"unique:"
      +"update:"
      +"userid:"
      +"userstamp:"
      +"utf8:"
      +"value:"
      +"view:"
      +"viewonly:"
      +"wansi:"
      +"wansichar:"
      +"wchar:"
      +"xml:";
  private static String checkReserved(String name)
  {
    String work=String.format(":%s:", name.toLowerCase());
    if (reservedWords.indexOf(work) != -1)
      return String.format("L'%s'", name);
    return name;
  }
  private static String commentOf(Field field)
  {
    String result = "  ";
    switch (field.type)
    {
    case Field.BLOB:
      result = "//";
      break;
    }
    return result;
  }
  private static int countOf(Table table)
  {
    int result = 0;
    for (int i=0; i<table.fields.size(); i++)
    {
      Field field = table.fields.get(i);
      if (field.name.equalsIgnoreCase("USId") == true)
        continue;
      if (field.type == Field.TIMESTAMP)
        continue;
      result++;
    }
    return result;
  }
  public static String description()
  {
    return "generate Parm application code";
  }
  public static String documentation()
  {
    return "generate Parm application code";
  }
  public static void generate(Database database, String output, PrintWriter outLog)
  {
    output = database.packageMerge(output);
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = database.tables.get(i);
      generateTable(table, output, outLog);
    }
  }
  private static void generateKeys(Table table, ParmOptions opts, PrintWriter outData, PrintWriter outLog)
  {
    for (Key key : table.keys)
    {
      outData.print(String.format("KEY %s", checkReserved(key.name)));
      String param="(";
      for (String field : key.fields)
      {
        outData.print(String.format("%s%s", param, checkReserved(field)));
        param=" ";
      }
      outData.print(")");
      if (key.isPrimary)
        outData.print(" PRIMARY");
      else if (key.isUnique)
        outData.print(" UNIQUE");
      outData.println();
    }
  }
  private static void generateLinks(Table table, ParmOptions opts, PrintWriter outData, PrintWriter outLog)
  {
    for (Link link : table.links)
    {
      outData.print(String.format("LINK %s", checkReserved(link.name)));
      String param="(";
      for (String field : link.fields)
      {
        outData.print(String.format("%s%s", param, checkReserved(field)));
        param=" ";
      }
      outData.println(")");
    }
  }
  private static void generateRelation(Table table, ParmOptions opts, PrintWriter outData, PrintWriter outLog)
  {
    outData.print(String.format("RELATION %s", table.name));
    if (opts.descr.length() > 0)
      outData.print(String.format(" '%s'", opts.descr));
    outData.println();
    Link link0 = table.links.get(0);
    Link link1 = table.links.get(1);
    outData.print(String.format("  %s", link0.name));
    // if (link0.linkFields.size() > 0)
    // {
    // for (int i=0; i < link0.linkFields.size(); i++)
    // {
    // String field = link0.linkFields.get(i);
    // outData.print(String.format("%s%s", i==0?"(":" ", field));
    // }
    // }
    // else
    // {
    for (int i = 0; i < link0.fields.size(); i++)
    {
      String field = link0.fields.get(i);
      outData.print(String.format("%s%s", i == 0 ? "(" : " ", field));
    }
    // }
    outData.println(")");
    outData.print(String.format("  %s", link1.name));
    // if (link1.linkFields.size() > 0)
    // {
    // for (int i=0; i < link1.linkFields.size(); i++)
    // {
    // String field = link1.linkFields.get(i);
    // outData.print(String.format("%s%s", i==0?"(":" ", field));
    // }
    // }
    // else
    // {
    for (int i = 0; i < link1.fields.size(); i++)
    {
      String field = link1.fields.get(i);
      outData.print(String.format("%s%s", i == 0 ? "(" : " ", field));
    }
    // }
    outData.println(")");
    outData.println();
  }
  private static void generateTable(Table table, PrintWriter outData, PrintWriter outLog)
  {
    ParmOptions opts = loadOptions(table);
    if (table.links.size() > 0)
    {
      if (generateUses(table, opts, outData, outLog) == true)
      {
        generateRelation(table, opts, outData, outLog);
        opts.viewOnly = true;
      }
    }
    outData.print(String.format("TABLE %s", checkReserved(table.name)));
    if (opts.descr.length() > 0)
      outData.print(String.format(" '%s'", opts.descr));
    if (opts.nullEnabled == true)
      outData.print(" NULL");
    if (opts.domain == false)
      outData.print(" NODOMAIN");
    if (opts.viewOnly == true)
      outData.print(" VIEWONLY");
    outData.println();
    if (table.check.length() > 0)
      outData.println(String.format("  CHECK \"%s\"", table.check));
    for (Field field : table.fields)
    {
      if (field.name.equalsIgnoreCase("USid") == true
      ||  field.name.equalsIgnoreCase("TmStamp") == true)
        continue;
      String checker="";
      if (field.checkValue.length() > 0)
        checker = String.format(" CHECK \"%s\"", field.checkValue);
      
      outData.println(String.format("%s%-28s %s%s%s"
          , commentOf(field)
          , nameOf(field)
          , typeOf(field)
          , enumListOf(field)
          , field.isNull ? " NULL" : ""
          , checker
          ));
    }
    if (opts.lookup.length() > 0)
      outData.println(String.format("LOOKUP(%s)", opts.lookup));
    else
    {
      for (Key key : table.keys)
      {
        if (key.isPrimary)
        {
          String lookup = "";
          for (String field : key.fields)
            lookup = String.format("%s %s", lookup, field);
          outData.println(String.format("LOOKUP(%s)", lookup));
          break;
        }
      }
    }
    if (opts.show.length() > 0)
      outData.println(String.format("SHOW(%s)", opts.show));
    if (table.keys.size() > 0)
      generateKeys(table, opts, outData, outLog);
    if (table.links.size() > 0)
      generateLinks(table, opts, outData, outLog);
  }
  private static String enumListOf(Field field)
  {
    String result = "";
    String d1="(", d2=")";
    if (field.type == Field.CHAR)
    {
      d1="{"; 
      d2="}";
    }
    if (field.enums.size() > 0)
    {
      for (int i=0; i<field.enums.size(); i++)
      {
        Enum en = field.enums.get(i);
        result = String.format("%s%s%s=%d", result, i==0?d1:", ", checkReserved(en.name), en.value);
      }
      result += d2;
    }
    else if (field.valueList.size() > 0)
    {
      for (int i=0; i<field.valueList.size(); i++)
      {
        String en = field.valueList.get(i);
        String comma = d1;
        String[] ens = en.split("=");
        if (ens.length == 2)
        {
          result = String.format("%s%s%s=%s", result, comma, checkReserved(ens[0]), ens[1]);
          comma = ", ";
        }
      }
      result += d2;
    }
    return result;
  }
  private static void generateTable(Table table, String output, PrintWriter outLog)
  {
    try
    {
      outLog.println("Code: " + output + table.useName().toLowerCase() + ".pi");
      OutputStream outFile = new FileOutputStream(output + table.useName().toLowerCase() + ".pi");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        generateTable(table, outData, outLog);
        outData.flush();
      }
      finally
      {
        outFile.close();
      }
    }
    catch (IOException e1)
    {
      outLog.println("Generate Procs IO Error");
    }
  }
  private static boolean generateUses(Table table, ParmOptions opts, PrintWriter outData, PrintWriter outLog)
  {
    outData.print("//USES:");
    int n = table.links.size();
    for (int i=0; i<n; i++)
    {
      Link link = table.links.get(i);
      outData.print(link.name+":");
    }
    outData.println();
    outData.println();
    if (n != 2)
      return false;
    Link link0 = table.links.get(0);
    Link link1 = table.links.get(1);
    if (link0.name.equalsIgnoreCase(link1.name) == true)
      return false;
    n = link0.linkFields.size()+link1.linkFields.size();
    if (n != countOf(table))
      return false;
    for (int i=0; i<link0.fields.size(); i++)
    {
      String field0 = link0.fields.get(i);
      for (int j=0; j<link1.fields.size(); j++)
      {
        String field1 = link1.fields.get(j);
        if (field0.equalsIgnoreCase(field1) == true)
          return false;
      }
    }
    return true;
  }
  private static ParmOptions loadOptions(Table table)
  {
    ParmOptions opts = new ParmOptions();
    for (String option : table.options)
    {
      if (option.toLowerCase().indexOf("descr=") == 0)
        opts.descr = option.substring(6).trim();
      else if (option.equalsIgnoreCase("nodomain") == true)
        opts.domain = false;
      else if (option.equalsIgnoreCase("viewonly") == true)
        opts.viewOnly = true;
      else if (option.equalsIgnoreCase("null") == true)
        opts.nullEnabled = true;
      else if (option.toLowerCase().indexOf("lookup=") == 0)
        opts.lookup = option.substring(7).trim();
      else if (option.toLowerCase().indexOf("show=") == 0)
        opts.show = option.substring(5).trim();
    }
    return opts;
  }
  public static void main(String[] args) 
	{
	  try
	  {
	    PrintWriter outLog = new PrintWriter(System.out);
	    for (int i = 0; i < args.length; i++)
	    {
	      outLog.println(args[i] + ": generate Parm application code");
	      ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
	      Database database = (Database)in.readObject();
	      in.close();
	      generate(database, "", outLog);
	    }
	    outLog.flush();
	  }
	  catch (Exception e)
	  {
	    e.printStackTrace();
	  }
	}
  private static String nameOf(Field field)
  {
    String result = checkReserved(field.name);
    if (field.alias.length() > 0)
      result = String.format("%s (%s)", checkReserved(field.name), checkReserved(field.alias));
    return result;
  }
  private static String typeOf(Field field)
  {
    switch (field.type)
    {
    case Field.ANSICHAR:
      return String.format("ansichar(%s)", field.length);
    case Field.AUTOTIMESTAMP:
      return "timestamp";
    case Field.BIGIDENTITY:
      return "bigidentity";
    case Field.BIGSEQUENCE:
      return "bigsequence";
    case Field.BIGXML:
      return String.format("bigxml(%s)", field.length);
    case Field.BLOB:
      return String.format("blob(%s)", field.length);
    case Field.BOOLEAN:
      return "boolean";
    case Field.BYTE:
      return "byte";
    case Field.CHAR:
      return String.format("char(%s)", field.length);
    case Field.DATE:
      return "date";
    case Field.DATETIME:
      return "datetime";
    case Field.DOUBLE:
      if (field.precision == 0 && field.scale == 0)
        return String.format("double");
      else if (field.scale == 0)
        return String.format("double(%d", field.precision);
      return String.format("double(%d, %d)", field.precision, field.scale);
    case Field.DYNAMIC:
      return "dynamic";
    case Field.FLOAT:
      return "double";
    case Field.IDENTITY:
      return "identity";
    case Field.INT:
      return "int";
    case Field.LONG:
      return "long";
    case Field.MONEY:
      return "money";
    case Field.SEQUENCE:
      return "sequence";
    case Field.SHORT:
      return "short";
    case Field.STATUS:
      return "status";
    case Field.TIME:
      return "time";
    case Field.TIMESTAMP:
      return "timestamp";
    case Field.TLOB:
      return String.format("tlob(%s)", field.length);
    case Field.UID:
      return "uid";
    case Field.USERSTAMP:
      return "userstamp";
    case Field.UTF8:
      return String.format("utf8(%s)", field.length);
    case Field.WANSICHAR:
      return String.format("wansichar(%s)", field.length);
    case Field.WCHAR:
      return String.format("wchar(%s)", field.length);
    case Field.XML:
      return String.format("xml(%s)", field.length);
    }
    return "?typeOf?";
  }
}
