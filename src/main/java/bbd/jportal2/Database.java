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
package bbd.jportal2;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Database identified by name, holds a list of tables for a
 * server, userid and password.
 */
public class Database implements Serializable
{
  private static final Logger logger = LoggerFactory.getLogger(Database.class);

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public String getName() {
    return name;
  }

  public String getOutput() {
    return output;
  }

  public String getServer() {
    return server;
  }

  public String getSchema() {
    return schema;
  }

  public String getUserid() {
    return userid;
  }

  public String getPassword() {
    return password;
  }

  public String getPackageName() {
    return packageName;
  }

  public Vector<Table> getTables() {
    return tables;
  }

  public Vector<String> getFlags() {
    return flags;
  }

  public Vector<Sequence> getSequences() {
    return sequences;
  }

  public Vector<View> getViews() {
    return views;
  }

  public Vector<String> getImports() {
    return imports;
  }

  public String name;
  public String output;
  public String server;
  public String schema;
  public String userid;
  public String password;
  public String packageName;
  public Vector<Table> tables;
  public Vector<String> flags;
  public Vector<Sequence> sequences;
  public Vector<View> views;
  public Vector<String> imports;
  public Database()
  {
    server = "";
    schema = "";
    output = "";
    name = "";
    userid = "";
    password = "";
    packageName = "";
    tables = new Vector<Table>();
    flags = new Vector<String>();
    sequences = new Vector<Sequence>();
    views = new Vector<View>();
    imports = new Vector<String>();
  }
  /**
   * Check for the existance of a table
   */
  public boolean hasTable(String s)
  {
    int i;
    for (i = 0; i < tables.size(); i++)
    {
      Table table = (Table) tables.elementAt(i);
      if (table.name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  /**
   * Return repository name root
   */
  public String outName()
  {
    if (output.length() > 0)
      return output;
    return name;
  }
  public static Database readRepository(String name)
      throws Exception
  {
    logger.info("Inputting " + name + ".repository");
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(name + ".repository"));
    try
    {
      Database database = (Database) in.readObject();
      return database;
    }
    finally
    {
      in.close();
    }
  }
  private void addinTable(Vector<Table> tables, Table addin)
  {
    for (int i = 0; i < tables.size(); i++)
    {
      Table table = (Table) tables.elementAt(i);
      if (table.name.equalsIgnoreCase(addin.name))
      {
        logger.info("Import table name :" + addin.name + " to merge with existing.");
        table = table.add(addin);
        tables.setElementAt(table, i);
        return;
      }
    }
    tables.addElement(addin);
  }
  private void addinView(Vector<View> views, View addin)
  {
    for (int i = 0; i < views.size(); i++)
    {
      View view = (View) views.elementAt(i);
      if (view.name.equalsIgnoreCase(addin.name))
      {
        logger.warn("Import view name :" + addin.name + " already exists");
        return;
      }
    }
    views.addElement(addin);
  }
  private static void addinSequence(Vector<Sequence> sequences, Sequence addin)
  {
    for (int i = 0; i < sequences.size(); i++)
    {
      Sequence sequence = (Sequence) sequences.elementAt(i);
      if (sequence.name.equalsIgnoreCase(addin.name))
      {
        logger.warn("Import sequence name :" + addin.name + " already exists");
        return;
      }
    }
    sequences.addElement(addin);
  }
  private String set(String a, String b, String what)
  {
    if (a.length() == 0)
      a = b;
    else if (a.equalsIgnoreCase(b) == false)
      logger.warn("Import " + what + " name :" + a + " not the same as :" + b);
    return a;
  }
  public void add(Database database)
  {
    name = set(name, database.name, "name");
    output = set(output, database.output, "output");
    server = set(server, database.server, "server");
    userid = set(userid, database.userid, "userid");
    password = set(password, database.password, "password");
    packageName = set(packageName, database.packageName, "packageName" );
    for (int i = 0; i < database.tables.size(); i++)
    {
      Table table = (Table) database.tables.elementAt(i);
      addinTable(tables, table);
    }
    for (int i = 0; i < database.views.size(); i++)
    {
      View view = (View) database.views.elementAt(i);
      addinView(views, view);
    }
    for (int i = 0; i < database.sequences.size(); i++)
    {
      Sequence sequence = (Sequence) database.sequences.elementAt(i);
      addinSequence(sequences, sequence);
    }
    for (int i = 0; i < database.imports.size(); i++)
    {
      String addinName = "";
      try
      {
        addinName = (String) database.imports.elementAt(i);
        logger.warn("Addin name " + addinName);
        boolean addIt = true;
        for (int j = 0; j < imports.size(); j++)
        {
          String already = (String) imports.elementAt(j);
          if (already.equalsIgnoreCase(addinName) == true)
          {
            addIt = false;
            logger.warn("Already imported: " + addinName);
            break;
          }
        }
        if (addIt == true)
        {
          imports.addElement(addinName);
          Database next = readRepository(addinName);
          add(next);
        }
      }
      catch (Exception ex)
      {
        logger.error("Import name :" + addinName + " failed.",ex);
      }
    }
  }
  public Database doImports()
  {
    if (imports.size() > 0)
    {
      Database database = new Database();
      database.add(this);
      return database;
    }
    return this;
  }
  public String packageMerge(String output)
  {
    Vector<String> ov = new Vector<String>();
    Vector<String> pv = new Vector<String>();
    if (packageName.length() == 0)
      return output;
    int length = output.length();
    int sep = output.lastIndexOf('/');
    char sepChar = '\\';
    if (sep != -1)
      sepChar = '/';
    else
      sep = output.lastIndexOf('\\');
    if (sep < length - 1)
      output = output + sepChar;
    String work = packageName + '.';
    int p = work.indexOf('.');
    while (p >= 0)
    {
      if (p > 0)
        pv.addElement(work.substring(0, p));
      work = work.substring(p + 1);
      p = work.indexOf('.');
    }
    work = output;
    p = work.indexOf(sepChar);
    while (p >= 0)
    {
      if (p > 0)
        ov.addElement(work.substring(0, p));
      work = work.substring(p + 1);
      p = work.indexOf(sepChar);
    }
    String pw = (String) pv.elementAt(0);
    int oi, pi;
    for (oi = ov.size() - 1; oi >= 0; oi--)
    {
      String ow = (String) ov.elementAt(oi);
      if (ow.compareTo(pw) == 0)
        break;
    }
    if (oi == -1)
      return output;
    for (pi = 0; oi < ov.size(); pi++, oi++)
    {
      pw = (String) pv.elementAt(pi);
      String ow = (String) ov.elementAt(oi);
      if (ow.compareTo(pw) != 0)
        break;
    }
    if (oi < ov.size())
      return output;
    for (; pi < pv.size(); pi++)
    {
      pw = (String) pv.elementAt(pi);
      output = output + pw + sepChar;
    }
    return output;
  }
}


