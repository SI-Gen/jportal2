// / ------------------------------------------------------------------
/// Copyright (c) from 1996 Vincent Risi
/// 
/// All rights reserved.
/// This program and the accompanying materials are made available
/// under the terms of the Common Public License v1.0
/// which accompanies this distribution and is available at
/// http://www.eclipse.org/legal/cpl-v10.html
/// Contributors:
/// Vincent Risi
/// ------------------------------------------------------------------

package bbd.jportal;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class Dataset
{
  public String name;
  public Vector<Table> tables;
  public String connectionStringName;
  public String connectionStringProperty;
  public Dataset(String name, String connectionStringName, String connectionStringProperty)
  {
    this.name = name;
    this.connectionStringName = connectionStringName;
    this.connectionStringProperty = connectionStringProperty;
    tables = new Vector<Table>();
  }
}

public class XsdCode extends Generator
{
  protected static Hashtable<?, ?> projectFlagsVector = null;
  protected static Vector<Flag> flagsVector;
  public static void main(String args[])
  {
    try
    {
      PrintWriter outLog = new PrintWriter(System.out);
      for (int i = 0; i < args.length; i++)
      {
        outLog.println(args[i] + ": Generate XSD for NET");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(args[i]));
        Database database = (Database) in.readObject();
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
  static boolean mSSqlStoredProcs;
  static boolean tableAdapters;
  static String dataSet;
  static String connectionString;
  static String connectionProperty;
  static String namespace;
  private static void flagDefaults()
  {
    mSSqlStoredProcs = false;
    tableAdapters = false;
    dataSet = "";
    connectionString = "";
    connectionProperty = "";
    namespace = "";
  }
  public static Vector<Flag> flags()
  {
    if (flagsVector == null)
    {
      flagsVector = new Vector<Flag>();
      flagDefaults();
      flagsVector.addElement(new Flag("mssql storedprocs", new Boolean(mSSqlStoredProcs), "Generate MSSql Stored Procedures"));
      flagsVector.addElement(new Flag("tableAdapters", new Boolean(tableAdapters), "Generate Table Adapter Code"));
      flagsVector.addElement(new Flag("dataset", dataSet, "DataSet Name"));
      flagsVector.addElement(new Flag("connectionstring", connectionString, "Connection String"));
      flagsVector.addElement(new Flag("connectionproperty", connectionProperty, "Connection Property"));
      flagsVector.addElement(new Flag("namespace", namespace, "Namespace"));
      flagsVector.addElement(new Flag("projectnamespace", namespace, "Project Namespace"));
    }
    return flagsVector;
  }

  public static String description()
  {
    return "Generate Xsd Code for NET";
  }

  public static String documentation()
  {
    return "Generate Xsd Code for NET" + "\r\nDATABASE name FLAGS flag" + "\r\n- \"mssql storedprocs\" generate stored procedures for MSSql"
        + "\r\n- \"tableAdapters\" generate table adapter code";
  }

  private static Hashtable<String, Dataset> datasets;

  static void markDatasets(Table table, String option, String connectionStringName, String connectionStringProperty)
  {
    String sets = option.substring(8);
    boolean found;
    int position;

    position = sets.indexOf(" ");
    if (position >= 0)
      sets = sets.substring(0, position);

    while (true)
    {
      String curr = sets;
      int n = sets.indexOf(":");
      if (n < 0)
        n = sets.indexOf(";");
      if (n > 0)
      {
        curr = sets.substring(0, n);
        sets = sets.substring(n + 1);
      }
      String set = curr.toLowerCase();
      Dataset dataset = (Dataset) datasets.get(set);
      if (dataset == null)
      {
        dataset = new Dataset(curr, connectionStringName, connectionStringProperty);
        datasets.put(set, dataset);
      }
      // Only add the table if it is not already in the data set
      found = false;
      for (int i = 0; i < dataset.tables.size(); i++)
      {
        Table theTable;

        theTable = (Table) dataset.tables.elementAt(i);
        if (theTable.name == table.name)
        {
          found = true;
          break;
        }
      }

      if (!found)
        dataset.tables.addElement(table);
      if (n <= 0)
        break;
    }
  }

  /**
   * Sets generation flags.
   */
  static void setFlags(Database database, PrintWriter outLog)
  {
    if (flagsVector != null)
    {
      mSSqlStoredProcs = toBoolean(((Flag) flagsVector.elementAt(0)).value);
    }
    else
      flagDefaults();
    for (int i = 0; i < database.flags.size(); i++)
    {
      String flag = (String) database.flags.elementAt(i);
      if (flag.equalsIgnoreCase("mssql storedprocs"))
        mSSqlStoredProcs = true;
      else if (flag.equalsIgnoreCase("tableAdapters"))
        tableAdapters = true;
    }
    if (mSSqlStoredProcs)
      outLog.println(" (mssql storedprocs)");
    if (tableAdapters)
      outLog.println(" (tableAdapters)");
  }

  public static void generate(Database database, String output, PrintWriter outLog)
  {
    setFlags(database, outLog);
    datasets = new Hashtable<String, Dataset>();

    if (projectFlagsVector != null)
    {
      if (projectFlagsVector.get("Generate Xsd Code for NET:dataset") != null)
      {
        String dataSetName;
        String connectionStringName = "";
        String connectionStringProperty = "";

        dataSetName = projectFlagsVector.get("Generate Xsd Code for NET:dataset").toString();
        if (projectFlagsVector.get("Generate Xsd Code for NET:connectionstring") != null)
          connectionStringName = projectFlagsVector.get("Generate Xsd Code for NET:connectionstring").toString();
        if (projectFlagsVector.get("Generate Xsd Code for NET:connectionproperty") != null)
          connectionStringProperty = projectFlagsVector.get("Generate Xsd Code for NET:connectionproperty").toString();

        Dataset dataset = new Dataset(dataSetName, connectionStringName, connectionStringProperty);
        datasets.put(dataSetName.toLowerCase(), dataset);

        for (int i = 0; i < database.tables.size(); i++)
        {
          Table table = (Table) database.tables.elementAt(i);

          dataset.tables.addElement(table);
        }
      }
    }

    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      String connectionStringName = "";
      String connectionStringProperty = "";
      int position1;
      int position2;

      for (int j = 0; j < table.options.size(); j++)
      {
        String option = (String) table.options.elementAt(j);
        if (option.toLowerCase().startsWith("storedproc") == true)
          table.isStoredProc = true;
      }

      for (int j = 0; j < table.options.size(); j++)
      {
        String option = (String) table.options.elementAt(j);
        if (option.toLowerCase().startsWith("dataset:") == true)
        {
          connectionStringName = "";
          connectionStringProperty = "";
          position1 = option.indexOf(" ");
          if (position1 >= 0)
          {
            position2 = option.indexOf(" ", position1 + 1);

            connectionStringName = option.substring(position1 + 1, position2);
            connectionStringProperty = option.substring(position2 + 1);
          }
          markDatasets(table, option, connectionStringName, connectionStringProperty);
        }
      }
    }
    if (datasets.size() == 0)
    {
      Dataset dataset = new Dataset(database.outName(), "", "");
      dataset.tables = database.tables;
      datasets.put(database.name, dataset);
    }
    Enumeration<Dataset> elements = datasets.elements();
    while (elements.hasMoreElements())
    {
      Dataset dataset = (Dataset) elements.nextElement();

      genDataset(dataset, output, outLog);
    }
  }

  static void genDataset(Dataset dataset, String output, PrintWriter outLog)
  {
    try
    {
      // if (output.startsWith(".") || output.startsWith(".."))
      // output = System.IO.Directory.GetCurrentDirectory() + "\\" + output;
      outLog.println("Code: " + output + dataset.name + ".xsd");
      OutputStream outFile = new FileOutputStream(output + dataset.name + ".xsd");
      try
      {
        PrintWriter outData = new PrintWriter(outFile);
        try
        {
          String namespace = "http://tempuri.org/";

          if (projectFlagsVector != null)
          {
            if (projectFlagsVector.get("Generate Xsd Code for NET:namespace") != null)
              namespace = projectFlagsVector.get("Generate Xsd Code for NET:namespace").toString();
          }

          outData.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
          outData.println("<xs:schema");
          outData.println(" id=\"" + dataset.name + "\"");
          outData.println(" targetNamespace=\"" + namespace + dataset.name + ".xsd\"");
          outData.println(" elementFormDefault=\"qualified\"");
          outData.println(" attributeFormDefault=\"qualified\"");
          outData.println(" xmlns=\"" + namespace + dataset.name + ".xsd\"");
          outData.println(" xmlns:mstns=\"" + namespace + dataset.name + ".xsd\"");
          outData.println(" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
          outData.println(" xmlns:msdata=\"urn:schemas-microsoft-com:xml-msdata\"");
          outData.println(" xmlns:msprop=\"urn:schemas-microsoft-com:xml-msprop\"");
          outData.println(" >");
          outData.println("  " + "<xs:annotation>");
          outData.println("    " + "<xs:appinfo");
          outData.println("    " + "  source=\"urn:schemas-microsoft-com:xml-msdatasource\"");
          outData.println("    " + "  >");
          outData.println("      " + "<DataSource");
          outData.println("      " + "  DefaultConnectionIndex=\"0\"");
          outData.println("      " + "  FunctionsComponentName=\"QueriesTableAdapter\"");
          outData.println("      " + "  Modifier=\"AutoLayout, AnsiClass, Class, Public\"");
          outData.println("      " + "  SchemaSerializationMode=\"IncludeSchema\"");
          outData.println("      " + "  xmlns=\"urn:schemas-microsoft-com:xml-msdatasource\"");
          outData.println("      " + "  >");
          if (tableAdapters == true)
          {
            outData.println("        " + "<Connections>");
            outData.println("          " + "<Connection");
            outData.println("          " + "  AppSettingsObjectName=\"Settings\"");
            outData.println("          " + "  AppSettingsPropertyName=\"" + dataset.connectionStringName + "\"");
            outData.println("          " + "  ConnectionStringObject=\"\"");
            outData.println("          " + "  IsAppSettingsProperty=\"True\"");
            outData.println("          " + "  Modifier=\"Assembly\"");
            outData.println("          " + "  Name=\"" + dataset.connectionStringName + " (Settings)\"");
            outData.println("          " + "  ParameterPrefix=\"@\"");
            outData.println("          " + "  PropertyReference=\"" + dataset.connectionStringProperty + "\"");
            outData.println("          " + "  Provider = \"System.Data.SqlClient\"");
            outData.println("          " + "  />");
            outData.println("        " + "</Connections>");
            outData.println("        " + "<Tables>");
            for (int i = 0; i < dataset.tables.size(); i++)
            {
              Table table = (Table) dataset.tables.elementAt(i);
              genDatasetTableAdapter(dataset.connectionStringName, dataset.connectionStringProperty, dataset, table, output, outData, outLog);
            }
            outData.println("        " + "</Tables>");
            outData.println("        " + "<Sources>");
            outData.println("        " + "</Sources>");
          }
          outData.println("      " + "</DataSource>");
          outData.println("    " + "</xs:appinfo>");
          outData.println("  " + "</xs:annotation>");
          outData.println("  " + "<xs:element name=\"" + dataset.name + "\" msdata:IsDataSet=\"true\">");
          outData.println("    " + "<xs:complexType>");
          outData.println("      " + "<xs:choice maxOccurs=\"unbounded\">");
          for (int i = 0; i < dataset.tables.size(); i++)
          {
            Table table = (Table) dataset.tables.elementAt(i);
            genDatasetTable(dataset, table, outData, outLog);
          }
          outData.println("      " + "</xs:choice>");
          outData.println("    " + "</xs:complexType>");
          for (int i = 0; i < dataset.tables.size(); i++)
          {
            Table table = (Table) dataset.tables.elementAt(i);
            for (int j = 0; j < table.keys.size(); j++)
            {
              Key key = (Key) table.keys.elementAt(j);
              // if (!table.isStoredProc)
              genDatasetTableKey(dataset, table, key, outData, outLog);
            }
          }
          // outData.println(" " + "</xs:element>");
          boolean hasLinks = false;
          String relationName;
          int relationCount = 0;
          Hashtable<String, String> relations = new Hashtable<String, String>();

          for (int i = 0; i < dataset.tables.size(); i++)
          {
            Table table = (Table) dataset.tables.elementAt(i);
            for (int j = 0; j < table.links.size(); j++)
            {
              Link link = (Link) table.links.elementAt(j);
              Table linkTable = getTable(dataset, link.name);
              if (linkTable != null)
              {
                if (hasLinks == false)
                {
                  hasLinks = true;
                  // outData.println(" " + "<xs:annotation>");
                  // outData.println(" " + "<xs:appinfo>");
                }
                /*
                 * outData.println(" " + "<msdata:Relationship");
                 * outData.println(" " + "name=\"" + linkTable.name + "_" +
                 * table.name + "\""); outData.println(" " + "msdata:parent=\""
                 * + table.name + "\""); outData.println(" " + "msdata:child=\""
                 * + linkTable.name + "\""); outData.print (" " +
                 * "msdata:parentkey=\"");
                 */

                outData.println("      " + "<xs:keyref");
                relationName = linkTable.name + "_" + table.name;
                if (relations.get(relationName) != null)
                {
                  relationName = relationName + relationCount;
                  relationCount++;
                }
                relations.put(relationName, new String());

                outData.println("        " + "name=\"" + relationName + "\"");

                for (int k = 0; k < linkTable.keys.size(); k++)
                {
                  Key key = (Key) linkTable.keys.elementAt(k);
                  if (key.isPrimary)
                  {
                    outData.println("        " + "refer=\"" + linkTable.name + key.name + "\"");
                    break;
                  }
                }

                outData.println("        " + "msdata:parent=\"" + table.name + "\"");
                outData.println("        " + "msdata:child=\"" + linkTable.name + "\"");
                outData.print("        " + "msdata:parentkey=\"");
                for (int f = 0; f < link.fields.size(); f++)
                {
                  String fieldName = (String) link.fields.elementAt(f);
                  if (f > 0)
                    outData.print(" ");
                  outData.print(fieldName);
                }
                outData.println("\"");
                outData.print("        " + "msdata:childkey=\"");
                for (int k = 0; k < linkTable.keys.size(); k++)
                {
                  Key key = (Key) linkTable.keys.elementAt(k);
                  if (key.isPrimary)
                  {
                    for (int f = 0; f < key.fields.size(); f++)
                    {
                      String fieldName = (String) key.fields.elementAt(f);
                      if (f > 0)
                        outData.print(" ");
                      outData.print(fieldName);
                    }
                  }
                }
                outData.println("\"");
                outData.println("        " + "msprop:Generator_UserRelationName=\"" + linkTable.name + "_" + table.name + "\"");
                outData.println("        " + "msprop:Generator_RelationVarName=\"relation" + linkTable.name + "_" + table.name + "\"");
                outData.println("        " + "msprop:Generator_UserChildTable=\"" + linkTable.name + "\"");
                outData.println("        " + "msprop:Generator_UserParentTable=\"" + table.name + "\"");
                outData.println("        " + "msprop:Generator_ParentPropName=\"" + table.name + "Row\"");
                outData.println("        " + "msprop:Generator_ChildPropName=\"Get" + linkTable.name + "Rows\"");
                outData.println("        " + "msdata:UpdateeRule=\"Cascade\"");
                outData.println("        " + "msdata:DeleteRule=\"Cascade\"");
                outData.println("      " + "  >");

                outData.println("      " + "<xs:selector xpath=\".//mstns:" + table.name + "\" />");

                for (int k = 0; k < link.fields.size(); k++)
                  outData.println("      " + "<xs:field xpath=\"mstns:" + ((String) link.fields.elementAt(k)) + "\" />");

                outData.println("  " + "</xs:keyref>");
              }
            }
          }
          /*
           * if (hasLinks == true) { //outData.println(" " + "</xs:appinfo>");
           * //outData.println(" " + "</xs:annotation>"); outData.println(" " +
           * "</xs:keyref>"); }
           */
          outData.println("  " + "</xs:element>");
          outData.println("</xs:schema>");
        }
        finally
        {
          outData.flush();
        }
      }
      finally
      {
        outFile.close();

        try
        {
          Runtime runtime = Runtime.getRuntime();
          String namespace = "";

          File dest = new File(output, dataset.name + ".Designer.cs");
          dest.delete();

          File source = new File("c:\\Temp", "");
          source.mkdir();

          source = new File("c:\\Temp", dataset.name + ".cs");

          if (projectFlagsVector != null)
          {
            if (projectFlagsVector.get("Generate Xsd Code for NET:projectnamespace") != null)
              namespace = "/namespace:" + projectFlagsVector.get("Generate Xsd Code for NET:projectnamespace").toString();
          }

          Process process = runtime.exec("xsd.exe \"" + output + dataset.name + ".xsd\" /outputdir:C:\\Temp /dataset " + namespace);
          process.waitFor();

          source.renameTo(dest);
        }
        catch (Exception e)
        {
        }
      }
    }
    catch (IOException e1)
    {
      outLog.println("Cannot write to " + output + dataset.name + ".xsd. Please check it out or switch off its read-only attribute");
    }
  }

  private static Table getTable(Dataset dataset, String name)
  {
    for (int i = 0; i < dataset.tables.size(); i++)
    {
      Table table = (Table) dataset.tables.elementAt(i);
      if (table.name.equalsIgnoreCase(name))
        return table;
    }
    return null;
  }

  private static void genProcedure(Proc proc, PrintWriter outData, String connectionStringName, String connectionStringProperty, Table table)
  {
    String commandType = "Text";
    boolean found;

    if (table.isStoredProc)
      commandType = "StoredProcedure";

    outData.println("             " + "<SelectCommand>");
    outData.println("               " + "<DbCommand CommandType=\"" + commandType + "\" ModifiedByUser=\"True\">");
    outData.print("                 " + "<CommandText>");
    if (!table.isStoredProc)
      outData.println("");

    // Go through the procedure options to determine the stored procedure name
    // from there
    // If the option is not found default to the content of the stored procedure
    found = false;
    for (int i = 0; i < proc.options.size(); i++)
    {
      String option = (String) proc.options.elementAt(i);
      if (option.toLowerCase().startsWith("storedproc:"))
      {
        outData.print(option.substring(11));
        found = true;
        break;
      }
    }

    if (!found)
    {
      for (int i = 0; i < proc.lines.size(); i++)
      {
        String line = ((Line) proc.lines.elementAt(i)).line.replace(':', '@');
        outData.print(line);
        if (!table.isStoredProc)
          outData.println("");
      }
    }

    outData.println("                 " + "</CommandText>");
    outData.println("                 " + "<Parameters>");

    // placeHolder = new PlaceHolder(proc, PlaceHolder.AT_NAMED, "");

    for (int i = 0; i < proc.inputs.size(); i++)
    {
      // PlaceHolderPairs pair =
      // (PlaceHolderPairs)placeHolder.pairs.elementAt(i);
      // Field field = pair.field;
      Field field = (Field) proc.inputs.elementAt(i);
      outData.println("                 " + "<Parameter");
      outData.println("                 " + "  AllowDbNull=\"" + adtNull(field) + "\"");
      outData.println("                 " + "  AutogeneratedName=\"\"");
      outData.println("                 " + "  DataSourceName=\"\"");
      outData.println("                 " + "  DbType=\"" + adtDbType(field) + "\"");
      outData.println("                 " + "  Direction=\"Input\"");
      outData.println("                 " + "  ParameterName=\"@" + field.useName() + "\"");
      outData.println("                 " + "  Precision=\"" + field.precision + "\"");
      outData.println("                 " + "  ProviderType=\"" + adtProviderType(field) + "\"");
      outData.println("                 " + "  Scale=\"" + field.scale + "\"");
      outData.println("                 " + "  Size=\"" + field.length + "\"");
      outData.println("                 " + "  SourceColumn=\"" + field.useName() + "\"");
      outData.println("                 " + "  SourceColumnNullMapping=\"" + adtNull(field) + "\"");
      outData.println("                 " + "  SourceVersion=\"Original\"");
      outData.println("                 " + "  />");
    }

    outData.println("                 " + "</Parameters>");
    outData.println("               " + "</DbCommand>");
    outData.println("             " + "</SelectCommand>");
  }

  static void genDatasetTableAdapter(String connectionStringName, String connectionStringProperty, Dataset dataset, Table table, String output,
      PrintWriter outData, PrintWriter outLog)
  {
    Proc defaultProc = null;
    String defaultProcName = "Fill";
    String defaultMethodName = "GetData";

    outData.println("         " + "<TableAdapter");
    outData.println("         " + "  BaseClass=\"System.ComponentModel.Component\"");
    outData.println("         " + "  DataAccessorModifier=\"AutoLayout, AnsiClass, Class, Public\"");
    outData.println("         " + "  DataAccessorName=\"" + table.name + "TableAdapter\"");
    outData.println("         " + "  GeneratorDataComponentClassName=\"" + table.name + "TableAdapter\"");
    outData.println("         " + "  Name=\"" + table.name + "\"");
    outData.println("         " + "  UserDataComponentName=\"" + table.name + "TableAdapter\"");
    outData.println("         " + "  >");
    outData.println("           " + "<MainSource>");

    // For a stored procedure make one of the procedures the default
    // procedure
    // Try the Fill procedure first
    if (table.isStoredProc)
    {
      for (int f = 0; f < table.procs.size(); f++)
      {
        Proc proc = ((Proc) table.procs.elementAt(f));

        if (proc.name.toLowerCase().equals("fill"))
        {
          defaultProc = proc;
          defaultProcName = proc.name;
          defaultMethodName = "GetData";
          break;
        }
      }

      if (table.procs.size() > 0)
      {
        Proc proc = ((Proc) table.procs.elementAt(0));

        defaultProc = proc;
        defaultProcName = proc.name;
        defaultMethodName = "GetData" + proc.name;
        if (defaultProcName.toLowerCase().startsWith("fill"))
          defaultMethodName = "GetData" + defaultProcName.substring(4);
      }
    }

    outData.println("             " + "<DbSource");
    outData.println("             " + "  ConnectionRef=\"" + connectionStringName + " (Settings)\"");

    if (table.isStoredProc)
      outData.println("             " + "  DbObjectType=\"StoredProcedure\"");
    else
      outData.println("             " + "  DbObjectType=\"Unknown\"");
    outData.println("             " + "  FillMethodModifier=\"Public\"");
    outData.println("             " + "  FillMethodName=\"" + defaultProcName + "\"");
    outData.println("             " + "  GenerateMethods=\"Both\"");
    outData.println("             " + "  GenerateShortCommands=\"True\"");
    outData.println("             " + "  GeneratorGetMethodName=\"" + defaultMethodName + "\"");
    outData.println("             " + "  GeneratorSourceName=\"" + defaultProcName + "\"");
    outData.println("             " + "  GetMethodModifier=\"Public\"");
    outData.println("             " + "  GetMethodName=\"" + defaultMethodName + "\"");
    outData.println("             " + "  QueryType=\"Rowset\"");
    outData.println("             " + "  ScalarCallRetval=\"System.Object, mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089\"");
    outData.println("             " + "  UseOptimisticConcurrency=\"False\"");
    outData.println("             " + "  UserGetMethodName=\"" + defaultMethodName + "\"");
    outData.println("             " + "  UserSourceName=\"" + defaultProcName + "\"");
    outData.println("             " + "  >");

    if (defaultProc != null)
      genProcedure(defaultProc, outData, connectionStringName, connectionStringProperty, table);

    Proc proc = new Proc();
    table.buildDeleteOne(proc);
    PlaceHolder placeHolder = genCommand(proc);

    String commandType = "Text";

    if (table.isStoredProc)
      commandType = "StoredProcedure";

    if (!table.isStoredProc)
    {
      genCommand(table, placeHolder, "DeleteCommand", "Text", outData, outLog);
      proc = new Proc();
      table.buildInsert(proc);
      placeHolder = genCommand(proc);
      if (!table.isStoredProc)
        genCommand(table, placeHolder, "InsertCommand", "Text", outData, outLog);
      proc = new Proc();
      table.buildSelectAll(proc, false, false, true, false);
      placeHolder = genCommand(proc);

      genCommand(table, placeHolder, "SelectCommand", commandType, outData, outLog);
      proc = new Proc();
      table.buildUpdate(proc);
      placeHolder = genCommand(proc);
      if (!table.isStoredProc)
        genCommand(table, placeHolder, "UpdateCommand", "Text", outData, outLog);
    }
    outData.println("             " + "</DbSource>");
    outData.println("           " + "</MainSource>");
    outData.println("           " + "<Mappings>");
    for (int f = 0; f < table.fields.size(); f++)
    {
      Field field = (Field) table.fields.elementAt(f);
      outData.println("             " + "<Mapping");
      outData.println("             " + "  SourceColumn=\"" + field.name + "\"");
      outData.println("             " + "  DataSetColumn=\"" + field.useName() + "\"");
      outData.println("             " + "  />");
    }
    outData.println("           " + "</Mappings>");
    outData.println("           " + "<Sources>");

    for (int f = 0; f < table.procs.size(); f++)
    {
      String procName;
      String methodName;
      String queryType = "Rowset";

      proc = ((Proc) table.procs.elementAt(f));
      for (int i = 0; i < proc.options.size(); i++)
      {
        String option = (String) proc.options.elementAt(i);
        if (option.toLowerCase().equals("scalar"))
        {
          queryType = "Scalar";
          break;
        }
      }

      if (defaultProc != null)
      {
        if (proc == defaultProc)
          continue;
      }

      if (proc.name == "Insert" || proc.name == "Update" || proc.name == "SelectOne" || proc.name == "")
        continue;

      procName = proc.name;
      methodName = proc.name;
      if (procName.length() >= 6)
      {
        if (procName.substring(0, 6).toLowerCase().equals("fillby"))
          procName = procName.substring(6);
      }
      if (proc.name.toLowerCase().startsWith("fill"))
        methodName = proc.name.substring(4);

      outData.println("           " + "<DbSource ConnectionRef=\"" + connectionStringName + " (Settings)\" DbObjectName=\"" + table.name
          + "\" DbObjectType=\"Table\" FillMethodModifier=\"Public\" FillMethodName=\"" + proc.name
          + "\" GenerateMethods=\"Both\" GenerateShortCommands=\"True\" GeneratorGetMethodName=\"GetData" + methodName + "\" GeneratorSourceName=\"" + proc.name
          + "\" GetMethodModifier=\"Public\" GetMethodName=\"GetData" + methodName + "\" QueryType=\"" + queryType
          + "\" ScalarCallRetval=\"System.Object, mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089\" UseOptimisticConcurrency=\"False\" UserGetMethodName=\"GetData"
          + methodName + "" + procName + "\" UserSourceName=\"" + procName + "\">");
      genProcedure(proc, outData, connectionStringName, connectionStringProperty, table);
      outData.println("           " + "</DbSource>");
    }
    outData.println("           " + "</Sources>");
    outData.println("         " + "</TableAdapter>");
  }

  static void genCommand(Table table, PlaceHolder placeHolder, String name, String commandType, PrintWriter outData, PrintWriter outLog)
  {
    Vector<Field> fields = new Vector<Field>();
    String line;
    boolean autoincrement;

    outData.println("           " + "<" + name + ">");
    outData.println("             " + "<DbCommand");
    outData.println("             " + "  CommandType=\"" + commandType + "\"");
    outData.println("             " + "  ModifiedByUser=\"False\"");
    outData.println("             " + "  >");

    outData.println("               " + "<CommandText>");

    autoincrement = false;
    for (int i = 0; i < table.options.size(); i++)
    {
      String option = (String) table.options.elementAt(i);

      if (option.toLowerCase().startsWith("autoincrement:"))
      {
        autoincrement = true;
        break;
      }
    }

    if (!table.isStoredProc)
    {
      if (name.equals("SelectCommand"))
      {
        outData.println("select");
        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          outData.println(line + field.name);
          line = ",";
        }
        outData.println("from " + table.name);
        outData.print("order by ");

        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (!field.isPrimaryKey)
            continue;
          outData.println(line + field.name);
          line = ",";
        }
      }

      if (name.equals("InsertCommand"))
      {
        outData.println("insert into " + table.name);
        outData.print("( ");

        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (field.isPrimaryKey && autoincrement)
            continue;
          outData.println(line + field.name);
          line = ",";
        }
        outData.println(")");
        outData.println("values");
        outData.print("( ");
        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (field.isPrimaryKey && autoincrement)
            continue;
          outData.println(line + "@" + field.name);
          line = ",";
        }
        outData.println(");");
        outData.println("select");
        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          outData.println(line + field.name);
          line = ",";
        }
        outData.println("from " + table.name);
        outData.println("where");

        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (!field.isPrimaryKey)
            continue;
          if (autoincrement)
            outData.println(field.name + "= SCOPE_IDENTITY()");
          else
            outData.println(field.name + "= @" + field.name);
          break;
        }

        // Determine the fields for the insert command
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (field.isPrimaryKey && autoincrement)
            continue;
          fields.addElement(field);
        }
      }

      if (name.equals("UpdateCommand"))
      {
        outData.println("update " + table.name);
        outData.println("set");

        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (field.isPrimaryKey)
            continue;
          outData.println(line + field.name + " = @" + field.name);
          line = ",";
        }
        outData.print("where ");
        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (!field.isPrimaryKey)
            continue;
          outData.println(line + field.name + " = @Original_" + field.name);
          line = ",";
        }
        outData.println(";");
        outData.println("select");

        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          outData.println(line + field.name);
          line = ",";
        }
        outData.println("from " + table.name);
        outData.print("where ");

        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (!field.isPrimaryKey)
            continue;
          outData.println(line + field.name + " = @" + field.name);
          line = ",";
        }

        // Determine the update command input fields
        fields = table.fields;
      }

      if (name.equals("DeleteCommand"))
      {
        outData.println("delete from " + table.name);
        outData.print("where ");

        line = "";
        for (int i = 0; i < table.fields.size(); i++)
        {
          Field field = (Field) table.fields.elementAt(i);

          if (!field.isPrimaryKey)
            continue;
          outData.println(line + field.name + " = @" + field.name);
          line = ",";
        }
      }

      // Determine the input fields for all the other commands except the
      // update and insert
      // commands
      if (!name.equals("InsertCommand") && !name.equals("UpdateCommand"))
      {
        for (int i = 0; i < placeHolder.pairs.size(); i++)
        {
          PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
          Field field = pair.field;
          fields.addElement(field);
        }
      }
    }
    else
    {
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc;

        proc = ((Proc) table.procs.elementAt(i));
        if (proc.name.equals(table.name))
        {
          fields = proc.inputs;
          break;
        }
      }

      // Determine the fields for the stored procedure
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc;

        proc = ((Proc) table.procs.elementAt(i));
        if (proc.name.equals(table.name))
        {
          fields = proc.inputs;
          break;
        }
      }
    }

    outData.println("               </CommandText>");
    outData.println("               " + "<Parameters>");

    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field) fields.elementAt(i);
      outData.println("                 " + "<Parameter");
      outData.println("                 " + "  AllowDbNull=\"" + adtNull(field) + "\"");
      outData.println("                 " + "  AutogeneratedName=\"\"");
      outData.println("                 " + "  DataSourceName=\"\"");
      outData.println("                 " + "  DbType=\"" + adtDbType(field) + "\"");
      outData.println("                 " + "  Direction=\"Input\"");
      outData.println("                 " + "  ParameterName=\"@" + field.useName() + "\"");
      outData.println("                 " + "  Precision=\"" + field.precision + "\"");
      outData.println("                 " + "  ProviderType=\"" + adtProviderType(field) + "\"");
      outData.println("                 " + "  Scale=\"" + field.scale + "\"");
      outData.println("                 " + "  Size=\"0\"");
      outData.println("                 " + "  SourceColumn=\"" + field.useName() + "\"");
      outData.println("                 " + "  SourceColumnNullMapping=\"false\"");
      outData.println("                 " + "  SourceVersion=\"Current\"");
      outData.println("                 " + "  />");
    }

    if (name.equals("UpdateCommand"))
    {
      for (int i = 0; i < fields.size(); i++)
      {
        Field field = (Field) fields.elementAt(i);
        if (!field.isPrimaryKey)
          continue;

        outData.println("                 " + "<Parameter");
        outData.println("                 " + "  AllowDbNull=\"" + adtNull(field) + "\"");
        outData.println("                 " + "  AutogeneratedName=\"\"");
        outData.println("                 " + "  DataSourceName=\"\"");
        outData.println("                 " + "  DbType=\"" + adtDbType(field) + "\"");
        outData.println("                 " + "  Direction=\"Input\"");
        outData.println("                 " + "  ParameterName=\"@Original_" + field.useName() + "\"");
        outData.println("                 " + "  Precision=\"" + field.precision + "\"");
        outData.println("                 " + "  ProviderType=\"" + adtProviderType(field) + "\"");
        outData.println("                 " + "  Scale=\"" + field.scale + "\"");
        outData.println("                 " + "  Size=\"0\"");
        outData.println("                 " + "  SourceColumn=\"" + field.useName() + "\"");
        outData.println("                 " + "  SourceColumnNullMapping=\"false\"");
        outData.println("                 " + "  SourceVersion=\"Original\"");
        outData.println("                 " + "  />");

        if (field.isNull)
        {
          outData.println("                 " + "<Parameter");
          outData.println("                 " + "  AllowDbNull=\"" + adtNull(field) + "\"");
          outData.println("                 " + "  AutogeneratedName=\"\"");
          outData.println("                 " + "  DataSourceName=\"\"");
          outData.println("                 " + "  DbType=\"" + adtDbType(field) + "\"");
          outData.println("                 " + "  Direction=\"Input\"");
          outData.println("                 " + "  ParameterName=\"@IsNull_" + field.useName() + "\"");
          outData.println("                 " + "  Precision=\"" + field.precision + "\"");
          outData.println("                 " + "  ProviderType=\"" + adtProviderType(field) + "\"");
          outData.println("                 " + "  Scale=\"" + field.scale + "\"");
          outData.println("                 " + "  Size=\"0\"");
          outData.println("                 " + "  SourceColumn=\"" + field.useName() + "\"");
          outData.println("                 " + "  SourceColumnNullMapping=\"true\"");
          outData.println("                 " + "  SourceVersion=\"Original\"");
          outData.println("                 " + "  />");
        }
      }
    }

    outData.println("               " + "</Parameters>");
    outData.println("             " + "</DbCommand>");
    outData.println("           " + "</" + name + ">");
  }

  static void genDatasetTableUseProcs(Dataset dataset, Table table, String output, PrintWriter outData, PrintWriter outLog)
  {
    OutputStream procFile = null;
    PrintWriter procData = null;
    try
    {
      try
      {
        if (mSSqlStoredProcs == true)
        {
          outLog.println("DDL: " + output + table.useName() + ".sproc.sql");
          procFile = new FileOutputStream(output + table.name + ".sproc.sql");
          procData = new PrintWriter(procFile);
          procData.println("use " + table.database.name);
          procData.println();
        }
        for (int p = 0; p < table.procs.size(); p++)
        {
          Proc proc = (Proc) table.procs.elementAt(p);
          if (proc.isData == true)
            continue;
          genStdCode(table, proc, outData, procData);
        }
      }
      finally
      {
        if (mSSqlStoredProcs == true)
        {
          if (procData != null)
            procData.flush();
          if (procFile != null)
            procFile.close();
        }
      }
    }
    catch (IOException e1)
    {
      outLog.println("Cannot write to " + output + table.useName() + ".sproc.sql. Please check it out or switch off its read-only attribute");
    }
  }

  static boolean doMSSqlStoredProcs(Proc proc)
  {
    return mSSqlStoredProcs == true && proc.dynamics.size() == 0;
  }

  static void genStdCode(Table table, Proc proc, PrintWriter outData, PrintWriter procData)
  {
    PlaceHolder placeHolder;
    if (doMSSqlStoredProcs(proc) == true)
      placeHolder = genStoredProcCommand(proc, outData, procData);
    else
      placeHolder = genCommand(proc);
    generateProcFunctions(proc, table.useName(), outData, placeHolder);
  }

  static PlaceHolder genCommand(Proc proc)
  {
    PlaceHolder placeHolder = new PlaceHolder(proc, PlaceHolder.AT_NAMED, "");
    return placeHolder;
  }

  static void genStoredProc(String storedProcName, Vector<?> lines, PrintWriter procData, PlaceHolder placeHolder)
  {
    procData.println("if exists (select * from sysobjects where id = object_id('dbo." + storedProcName + "') and sysstat & 0xf = 4)");
    procData.println("drop procedure dbo." + storedProcName);
    procData.println("GO");
    procData.println("");
    procData.println("CREATE PROCEDURE dbo." + storedProcName);
    String comma = "(";
    String done = "";
    for (int i = 0; i < placeHolder.pairs.size(); i++)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs) placeHolder.pairs.elementAt(i);
      Field field = pair.field;
      String lookFor = ":" + field.name + " ";
      if (done.indexOf(lookFor) < 0)
      {
        procData.println(comma + " @" + field.name + " " + sprocType(field));
        done = done + lookFor;
        comma = ",";
      }
    }
    if (placeHolder.pairs.size() > 0)
      procData.println(")");
    procData.println("AS");
    for (int i = 0; i < lines.size(); i++)
    {
      String line = (String) lines.elementAt(i);
      procData.println(line.substring(1, line.length() - 1));
    }
    procData.println("GO");
    procData.println("");
  }

  static PlaceHolder genStoredProcCommand(Proc proc, PrintWriter outData, PrintWriter procData)
  {
    PlaceHolder placeHolder = new PlaceHolder(proc, PlaceHolder.AT_NAMED, "");
    String storedProcName = proc.table.useName() + proc.upperFirst();
    Vector<?> lines = placeHolder.getLines();
    genStoredProc(storedProcName, lines, procData, placeHolder);
    // outData.println("<!-- genCommand");
    // outData.println(storedProcName);
    // outData.println("genCommand -->");
    return placeHolder;
  }

  static void genDatasetTable(Dataset dataset, Table table, PrintWriter outData, PrintWriter outLog)
  {
    outData.println("        " + "<xs:element name=\"" + table.name + "\">");
    outData.println("          " + "<xs:complexType>");
    outData.println("            " + "<xs:sequence>");

    Vector<?> fields;
    String isNullable = "";
    String autoIncrement = "";
    String dataType = "";

    for (int i = 0; i < table.options.size(); i++)
    {
      String option = (String) table.options.elementAt(i);
      // String[] options;

      if (option.toLowerCase().startsWith("autoincrement:"))
      {
        option = option.substring(14).trim();
        int n = option.indexOf(' ');
        // options = option.//Split(new char[] { ' ' });
        autoIncrement = "msdata:AutoIncrement=\"true\" ";
        if (n > 0)
        {
          autoIncrement = autoIncrement + "msdata:AutoIncrementSeed=\"" + option.substring(0, n).trim() + "\" ";
          autoIncrement = autoIncrement + "msdata:AutoIncrementStep=\"" + option.substring(n).trim() + "\" ";
        }
        else
          autoIncrement = autoIncrement + "msdata:AutoIncrementSeed=\"" + option + "\" ";
        break;
      }
    }

    fields = table.fields;

    if (table.isStoredProc)
    {
      for (int i = 0; i < table.procs.size(); i++)
      {
        Proc proc;

        proc = ((Proc) table.procs.elementAt(i));
        if (proc.name.equals(table.name))
        {
          fields = proc.outputs;
          break;
        }
      }
    }

    for (int i = 0; i < fields.size(); i++)
    {
      Field field = (Field) fields.elementAt(i);
      String theAutoIncrement;

      isNullable = "";
      if (field.isNull)
        isNullable = "minOccurs=\"0\"";

      theAutoIncrement = autoIncrement;
      if (!field.isPrimaryKey)
        theAutoIncrement = "";

      dataType = sdataType(field);
      if (!dataType.equals(""))
        dataType = " msdata:DataType=\"" + dataType + "\" ";

      if (field.type == Field.ANSICHAR || field.type == Field.CHAR || field.type == Field.DYNAMIC)
      {
        outData.println("              " + "<xs:element name=\"" + field.name + "\"" + " " + isNullable + " " + theAutoIncrement + ">");
        outData.println("                                <xs:simpleType>");
        outData.println("                                  <xs:restriction base=\"xs:string\">");
        outData.println("                                    <xs:maxLength value=\"" + field.length + "\" />");
        outData.println("                                  </xs:restriction>");
        outData.println("                                </xs:simpleType>");
        outData.println("              " + "</xs:element>");
      }
      else
        outData.println("              " + "<xs:element name=\"" + field.name + "\"" + " type=\"xs:" + xsdType(field) + "\" " + dataType + isNullable + " "
            + theAutoIncrement + " />");
    }
    outData.println("            " + "</xs:sequence>");
    outData.println("          " + "</xs:complexType>");
    outData.println("        " + "</xs:element>");
  }

  static void genDatasetTableKey(Dataset dataset, Table table, Key key, PrintWriter outData, PrintWriter outLog)
  {
    if (key.isPrimary)
      outData.println("    " + "<xs:unique name=\"" + table.name + key.name + "\" msdata:PrimaryKey=\"true\">");
    else if (key.isUnique)
      outData.println("    " + "<xs:unique name=\"" + table.name + key.name + "\" msdata:PrimaryKey=\"false\">");
    else
      return;
    outData.println("      " + "<xs:selector xpath=\".//mstns:" + table.name + "\" />");
    for (int i = 0; i < key.fields.size(); i++)
    {
      String field = (String) key.fields.elementAt(i);
      outData.println("      " + "<xs:field xpath=\"mstns:" + field + "\" />");
    }
    outData.println("    " + "</xs:unique>");
  }

  static void generateProcFunctions(Proc proc, String name, PrintWriter outData, PlaceHolder placeHolder)
  {
    if (proc.outputs.size() > 0 && !proc.isSingle)
      genFetchProc(proc, name, outData, placeHolder);
    else if (proc.outputs.size() > 0)
      genReadOneProc(proc, name, outData, placeHolder);
    else
      genNonQueryProc(proc, name, outData, placeHolder);
  }

  static void genFetchProc(Proc proc, String mainName, PrintWriter outData, PlaceHolder placeHolder)
  {
    outData.println("<!-- genFetchProc " + proc.name + " -->");
    // outData.println(" public void "+proc.upperFirst()+"(Connect
    // aConnect)");
    // outData.println(" {");
    // outData.println(" mCursor = new Cursor(aConnect);");
    // if (doMSSqlStoredProcs(proc))
    // outData.println(" mCursor.Procedure(Command"+proc.upperFirst()+");");
    // else
    // {
    // if (placeHolder.pairs.size() > 0)
    // outData.println(" // format command to change {n} to ?, @Pn or :Pn
    // placeholders depending on Vendor");
    // outData.println(" mCursor.Format(Command"+proc.upperFirst()+",
    // "+placeHolder.pairs.size()+");");
    // }
    // for (int i=0; i<placeHolder.pairs.size(); i++)
    // {
    // PlaceHolderPairs pair = (PlaceHolderPairs)
    // placeHolder.pairs.elementAt(i);
    // Field field = pair.field;
    // if (field.isNull)
    // outData.println(" mCursor.Parameter("+i+",
    // mRec."+field.useLowerName()+",
    // mRec."+field.useLowerName()+"IsNull);");
    // else
    // outData.println(" mCursor.Parameter("+i+",
    // mRec."+field.useLowerName()+");");
    // }
    // outData.println(" mCursor.Run();");
    // outData.println(" }");
    // outData.println(" public bool "+proc.upperFirst()+"Fetch()");
    // outData.println(" {");
    // outData.println(" bool wResult = (mCursor.HasReader() &&
    // mCursor.Read());");
    // outData.println(" if (wResult == true)");
    // outData.println(" {");
    // for (int i=0; i<proc.outputs.size(); i++)
    // {
    // Field field = (Field) proc.outputs.elementAt(i);
    // outData.println(" mRec."+field.useLowerName()+" =
    // mCursor."+cursorGet(field, i)+";");
    // }
    // outData.println(" }");
    // outData.println(" else if (mCursor.HasReader())");
    // outData.println(" mCursor.Close();");
    // outData.println(" return wResult;");
    // outData.println(" }");
    // outData.println(" public void "+proc.upperFirst()+"Load(Connect
    // aConnect)");
    // outData.println(" {");
    // outData.println(" "+proc.upperFirst()+"(aConnect);");
    // outData.println(" while ("+proc.upperFirst()+"Fetch())");
    // outData.println(" {");
    // outData.println(" mList.Add(mRec);");
    // outData.println(" mRec = new "+mainName+"Rec();");
    // outData.println(" }");
    // outData.println(" }");
    // if (useGenerics)
    // outData.println(" public List<" + mainName + "Rec> Loaded { get {
    // return mList; } }");
    // else
    // outData.println(" public ArrayList Loaded { get { return mList; }
    // }");
    // outData.println(" public class "+proc.upperFirst()+"Ord");
    // outData.println(" {");
    // int noInDataSet=0;
    // for (int i = 0; i < proc.inputs.size(); i++)
    // {
    // Field field = (Field)proc.inputs.elementAt(i);
    // outData.println(" public const int " + field.useLowerName() + " = " +
    // noInDataSet + ";");
    // noInDataSet++;
    // }
    // for (int i=0; i<proc.outputs.size(); i++)
    // {
    // Field field = (Field) proc.outputs.elementAt(i);
    // if (proc.hasInput(field.name))
    // continue;
    // outData.println(" public const int "+field.useLowerName()+" =
    // "+noInDataSet+";");
    // noInDataSet++;
    // }
    // outData.println(" public static string ToString(int ordinal)");
    // outData.println(" {");
    // outData.println(" switch (ordinal)");
    // outData.println(" {");
    // noInDataSet=0;
    // for (int i=0; i<proc.inputs.size(); i++)
    // {
    // Field field = (Field) proc.inputs.elementAt(i);
    // outData.println(" case "+noInDataSet+": return
    // \""+field.useLowerName()+"\";");
    // noInDataSet++;
    // }
    // for (int i = 0; i < proc.outputs.size(); i++)
    // {
    // Field field = (Field)proc.outputs.elementAt(i);
    // if (proc.hasInput(field.name))
    // continue;
    // outData.println(" case " + noInDataSet + ": return \"" +
    // field.useLowerName() + "\";");
    // noInDataSet++;
    // }
    // outData.println(" }");
    // outData.println(" return \"<??\"+ordinal+\"??>\";");
    // outData.println(" }");
    // outData.println(" }");
    // outData.println(" public
    // "+proc.table.useName()+proc.upperFirst()+"DataTable
    // "+proc.upperFirst()+"DataTable()");
    // outData.println(" {");
    // outData.println(" "+proc.table.useName()+proc.upperFirst()+"DataTable
    // wResult = new
    // "+proc.table.useName()+proc.upperFirst()+"DataTable(mList);");
    // outData.println(" return wResult;");
    // outData.println(" }");
    // outData.println(" public
    // "+proc.table.useName()+proc.upperFirst()+"DataTable
    // "+proc.upperFirst()+"DataTable(Connect aConnect)");
    // outData.println(" {");
    // outData.println(" "+proc.upperFirst()+"Load(aConnect);");
    // outData.println(" return "+proc.upperFirst()+"DataTable();");
    // outData.println(" }");
  }

  static void genReadOneProc(Proc proc, String mainName, PrintWriter outData, PlaceHolder placeHolder)
  {
    outData.println("<!-- genReadOneProc " + proc.name + " -->");
    // outData.println(" public bool "+proc.upperFirst()+"(Connect
    // aConnect)");
    // outData.println(" {");
    // outData.println(" Cursor wCursor = new Cursor(aConnect);");
    // if (doMSSqlStoredProcs(proc))
    // outData.println(" wCursor.Procedure(Command"+proc.upperFirst()+");");
    // else
    // {
    // if (placeHolder.pairs.size() > 0)
    // outData.println(" // format command to change {n} to ?, @Pn or :Pn
    // placeholders depending on Vendor");
    // outData.println(" wCursor.Format(Command"+proc.upperFirst()+",
    // "+placeHolder.pairs.size()+");");
    // }
    // for (int i=0; i<placeHolder.pairs.size(); i++)
    // {
    // PlaceHolderPairs pair = (PlaceHolderPairs)
    // placeHolder.pairs.elementAt(i);
    // Field field = pair.field;
    // if (field.isNull)
    // outData.println(" wCursor.Parameter("+i+",
    // mRec."+field.useLowerName()+",
    // mRec."+field.useLowerName()+"IsNull);");
    // else
    // outData.println(" wCursor.Parameter("+i+",
    // mRec."+field.useLowerName()+");");
    // }
    // outData.println(" wCursor.Run();");
    // outData.println(" bool wResult = (wCursor.HasReader() &&
    // wCursor.Read());");
    // outData.println(" if (wResult == true)");
    // outData.println(" {");
    // for (int i=0; i<proc.outputs.size(); i++)
    // {
    // Field field = (Field) proc.outputs.elementAt(i);
    // outData.println(" mRec."+field.useLowerName()+" =
    // wCursor."+cursorGet(field, i)+";");
    // }
    // outData.println(" }");
    // outData.println(" if (wCursor.HasReader())");
    // outData.println(" wCursor.Close();");
    // outData.println(" return wResult;");
    // outData.println(" }");
  }

  static void genNonQueryProc(Proc proc, String mainName, PrintWriter outData, PlaceHolder placeHolder)
  {
    outData.println("<!-- genNonQueryProc " + proc.name + " -->");
    // outData.println(" public void "+proc.upperFirst()+"(Connect
    // aConnect)");
    // outData.println(" {");
    // outData.println(" Cursor wCursor = new Cursor(aConnect);");
    // if (doMSSqlStoredProcs(proc))
    // outData.println(" wCursor.Procedure(Command"+proc.upperFirst()+");");
    // else
    // {
    // if (placeHolder.pairs.size() > 0)
    // outData.println(" // format command to change {n} to ?, @Pn or :Pn
    // placeholders depending on Vendor");
    // outData.println(" wCursor.Format(Command"+proc.upperFirst()+",
    // "+placeHolder.pairs.size()+");");
    // }
    // for (int i=0; i<placeHolder.pairs.size(); i++)
    // {
    // PlaceHolderPairs pair = (PlaceHolderPairs)
    // placeHolder.pairs.elementAt(i);
    // Field field = pair.field;
    // if (field.isNull)
    // outData.println(" wCursor.Parameter("+i+",
    // mRec."+field.useLowerName()+",
    // mRec."+field.useLowerName()+"IsNull);");
    // else
    // outData.println(" wCursor.Parameter("+i+",
    // mRec."+field.useLowerName()+");");
    // }
    // outData.println(" wCursor.Exec();");
    // outData.println(" }");
  }

  static String xsdType(Field field)
  {
    switch (field.type)
    {
    case Field.ANSICHAR:
      return "string";
    case Field.BLOB:
      return "base64Binary";
    case Field.BOOLEAN:
      return "boolean";
    case Field.BYTE:
      return "byte";
    case Field.CHAR:
      return "string";
    case Field.DATE:
      return "date";
    case Field.DATETIME:
      return "dateTime";
    case Field.DYNAMIC:
      return "string";
    case Field.DOUBLE:
      return "double";
    case Field.FLOAT:
      return "double";
    case Field.IDENTITY:
      return "int";
    case Field.INT:
      return "int";
    case Field.LONG:
      return "long";
    case Field.MONEY:
      return "decimal";
    case Field.SEQUENCE:
      return "int";
    case Field.SHORT:
      return "short";
    case Field.TIME:
      return "time";
    case Field.TIMESTAMP:
      return "dateTime";
    case Field.TLOB:
      return "string";
    case Field.USERSTAMP:
      return "string";
    case Field.UID:
      return "string";
    }
    return "<crap>";
  }
  /**
   * Translates field type to msdata:DataType in the data set This attribute is
   * only used for specific fields such as GUIDs This should be stored in a
   * configuration file
   */
  static String sdataType(Field field)
  {
    switch (field.type)
    {
    case Field.UID:
      return "System.Guid, mscorlib, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089";
    default:
      break;
    }
    return "";
  }
  /**
   * Translates field type to SQLServer SQL column types
   */
  static String sprocType(Field field)
  {
    switch (field.type)
    {
    case Field.BOOLEAN:
      return "bit";
    case Field.BYTE:
      return "tinyint";
    case Field.SHORT:
      return "smallint";
    case Field.INT:
    case Field.LONG:
      return "integer";
    case Field.SEQUENCE:
      return "integer";
    case Field.IDENTITY:
      return "integer";
    case Field.CHAR:
      return "varchar(" + String.valueOf(field.length) + ")";
    case Field.ANSICHAR:
      return "char(" + String.valueOf(field.length) + ")";
    case Field.DATE:
      return "datetime";
    case Field.DATETIME:
      return "datetime";
    case Field.TIME:
      return "datetime";
    case Field.TIMESTAMP:
      return "datetime";
    case Field.FLOAT:
      return "float";
    case Field.DOUBLE:
      return "float";
    case Field.BLOB:
      return "base64Binary";
    case Field.TLOB:
      return "text";
    case Field.MONEY:
      return "float";
    case Field.USERSTAMP:
      return "varchar(24)";
    case Field.UID:
      return "Int";
    default:
      break;
    }
    return "unknown";
  }
  private static String adtNull(Field field)
  {
    return field.isNull ? "True" : "False";
  }
  private static String adtProviderType(Field field)
  {
    switch (field.type)
    {
    case Field.ANSICHAR:
      return "Char";
    case Field.BOOLEAN:
      return "Bit";
    case Field.BYTE:
      return "Byte";
    case Field.DATE:
      return "Date";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "DateTime";
    case Field.DOUBLE:
    case Field.FLOAT:
      return "Float";
    case Field.IDENTITY:
    case Field.INT:
    case Field.SEQUENCE:
      return "Int";
    case Field.LONG:
      return "Long";
    case Field.MONEY:
      return "string";
    case Field.SHORT:
      return "Short";
    case Field.TIME:
      return "Time";
    case Field.BLOB:
      return "VarBinary";
    case Field.TLOB:
    case Field.CHAR:
      return "VarChar";
    case Field.UID:
      return "UniqueIdentifier";
    }
    return "<crap>";
  }
  private static String adtDbType(Field field)
  {
    switch (field.type)
    {
    case Field.ANSICHAR:
      return "AnsiString";
    case Field.BOOLEAN:
      return "Boolean";
    case Field.BYTE:
      return "Int8";
    case Field.CHAR:
    case Field.MONEY:
      return "string";
    case Field.BLOB:
      return "Binary";
    case Field.TLOB:
    case Field.USERSTAMP:
      return "AnsiString";
    case Field.DATE:
      return "Date";
    case Field.DATETIME:
    case Field.TIMESTAMP:
      return "DateTime";
    case Field.DYNAMIC:
      return "AnsiString";
    case Field.DOUBLE:
    case Field.FLOAT:
      return "Double";
    case Field.IDENTITY:
    case Field.SEQUENCE:
    case Field.INT:
      return "Int32";
    case Field.LONG:
      return "Int64";
    case Field.SHORT:
      return "Int16";
    case Field.TIME:
      return "Time";
    case Field.UID:
      return "Guid";
    }
    return "<crap>";
  }
}

/*
 * <TableAdapter BaseClass="System.ComponentModel.Component"
 * DataAccessorModifier="AutoLayout, AnsiClass, Class, Public"
 * DataAccessorName="UserOrganisationTableAdapter"
 * GeneratorDataComponentClassName="UserOrganisationTableAdapter"
 * Name="UserOrganisation" UserDataComponentName="UserOrganisationTableAdapter"
 * > <Mappings> <Mapping SourceColumn="UserOrganisationID"
 * DataSetColumn="UserOrganisationID" /> <Mapping SourceColumn="SystemUserID"
 * DataSetColumn="SystemUserID" /> <Mapping SourceColumn="EntityID"
 * DataSetColumn="EntityID" /> <Mapping SourceColumn="UserID"
 * DataSetColumn="UserID" /> <Mapping SourceColumn="TmStamp"
 * DataSetColumn="TmStamp" /> </Mappings> <Sources> <DbSource
 * ConnectionRef="SasfinFinanceConnectionString (Settings)"
 * DbObjectType="Unknown" FillMethodModifier="Public"
 * FillMethodName="FillBySystemUserID" GenerateMethods="Both"
 * GenerateShortCommands="True" GeneratorGetMethodName="GetDataBySystemUserID"
 * GeneratorSourceName="FillBySystemUserID" GetMethodModifier="Public"
 * GetMethodName="GetDataBySystemUserID" QueryType="Rowset"
 * ScalarCallRetval="System.Object, mscorlib, Version=2.0.0.0, Culture=neutral,
 * PublicKeyToken=b77a5c561934e089" UseOptimisticConcurrency="True"
 * UserGetMethodName="GetDataBySystemUserID"
 * UserSourceName="FillBySystemUserID"> <SelectCommand> <DbCommand
 * CommandType="StoredProcedure" ModifiedByUser="False">
 * <CommandText>dbo.GetOrganisationBySystemUserID</CommandText> <Parameters>
 * <Parameter AllowDbNull="True" AutogeneratedName="" DataSourceName=""
 * DbType="Int32" Direction="ReturnValue" ParameterName="@RETURN_VALUE"
 * Precision="10" ProviderType="Int" Scale="0" Size="4"
 * SourceColumnNullMapping="False" SourceVersion="Current"> </Parameter>
 * <Parameter AllowDbNull="True" AutogeneratedName="" DataSourceName=""
 * DbType="Int32" Direction="Input" ParameterName="@SystemUserID" Precision="10"
 * ProviderType="Int" Scale="0" Size="4" SourceColumnNullMapping="False"
 * SourceVersion="Current"> </Parameter> </Parameters> </DbCommand>
 * </SelectCommand> </DbSource> </Sources> </TableAdapter>
 */