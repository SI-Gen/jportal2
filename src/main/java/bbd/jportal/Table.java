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

package bbd.jportal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Vector;

/**
 * Table identified by name holds fields, keys, links, grants, views and
 * procedures associated with the table.
 */
public class Table implements Serializable
{
  private static final long serialVersionUID = 1L;

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

  public boolean isHasBigXML() {
    return hasBigXML;
  }

  public boolean isStoredProc() {
    return isStoredProc;
  }

  public boolean isLiteral() {
    return isLiteral;
  }

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
  public boolean isStoredProc;
  public boolean isLiteral;
  public int start;
  public Table()
  {
    name = "";
    alias = "";
    check = "";
    literalName = "";
    fields = new Vector<Field>();
    keys = new Vector<Key>();
    links = new Vector<Link>();
    grants = new Vector<Grant>();
    views = new Vector<View>();
    procs = new Vector<Proc>();
    comments = new Vector<String>();
    options = new Vector<String>();
    allUsers = new Vector<String>();
    parameters = new Vector<Parameter>();
    consts = new Vector<Const>();
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
    isStoredProc = false;
    isLiteral = false;
    start = 0;
  }
  public void reader(DataInputStream ids) throws IOException
  {
    reader(ids, null);
  }
  public void reader(DataInputStream ids, Vector<?> useProcs) throws IOException
  {
    int signature = ids.readInt();
    if (signature != 0xBABA00D)
      return;
    name = ids.readUTF();
    literalName = ids.readUTF();
    alias = ids.readUTF();
    check = ids.readUTF();
    int noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      Field value = new Field();
      value.reader(ids);
      fields.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      Key value = new Key();
      value.reader(ids);
      keys.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      Link value = new Link();
      value.reader(ids);
      links.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      Grant value = new Grant();
      value.reader(ids);
      grants.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      View value = new View();
      value.reader(ids);
      views.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      Proc value = new Proc();
      value.reader(ids);
      if (useProcs == null)
        procs.addElement(value);
      else
        for (int p = 0; p < useProcs.size(); p++)
        {
          String name = (String) useProcs.elementAt(p);
          if (value.name.compareToIgnoreCase(name) == 0)
          {
            procs.addElement(value);
            break;
          }
        }
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      String value = ids.readUTF();
      comments.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      String value = ids.readUTF();
      options.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
      Parameter value = new Parameter();
      value.table = this;
      value.reader(ids);
      parameters.addElement(value);
    }
    noOf = ids.readInt();
    for (int i = 0; i < noOf; i++)
    {
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
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeInt(0xBABA00D);
    ods.writeUTF(name);
    ods.writeUTF(literalName);
    ods.writeUTF(alias);
    ods.writeUTF(check);
    ods.writeInt(fields.size());
    for (int i = 0; i < fields.size(); i++)
    {
      Field value = fields.elementAt(i);
      value.writer(ods);
    }
    ods.writeInt(keys.size());
    for (int i = 0; i < keys.size(); i++)
    {
      Key value = keys.elementAt(i);
      value.writer(ods);
    }
    ods.writeInt(links.size());
    for (int i = 0; i < links.size(); i++)
    {
      Link value = links.elementAt(i);
      value.writer(ods);
    }
    ods.writeInt(grants.size());
    for (int i = 0; i < grants.size(); i++)
    {
      Grant value = grants.elementAt(i);
      value.writer(ods);
    }
    ods.writeInt(views.size());
    for (int i = 0; i < views.size(); i++)
    {
      View value = views.elementAt(i);
      value.writer(ods);
    }
    int noProcs = 0;
    for (int i = 0; i < procs.size(); i++)
    {
      Proc value = procs.elementAt(i);
      if (value.isData == true)
        continue;
      noProcs++;
    }
    ods.writeInt(noProcs);
    for (int i = 0; i < procs.size(); i++)
    {
      Proc value = procs.elementAt(i);
      if (value.isData == true)
        continue;
      value.writer(ods);
    }
    ods.writeInt(comments.size());
    for (int i = 0; i < comments.size(); i++)
    {
      String value = comments.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(options.size());
    for (int i = 0; i < options.size(); i++)
    {
      String value = options.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(parameters.size());
    for (int i = 0; i < parameters.size(); i++)
    {
      Parameter value = parameters.elementAt(i);
      value.table = this;
      value.writer(ods);
    }
    ods.writeInt(consts.size());
    for (int i = 0; i < consts.size(); i++)
    {
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
  /** If there is an literal uses that else returns name */
  public String useLiteral()
  {
    if (isLiteral)
      return literalName;
    return name;
  }
  /**
   * If there is an alias uses that else returns name
   */
  public String useName()
  {
    if (alias.length() > 0)
      return alias;
    return name;
  }
  /**
   * Checks for the existence of a field
   */
  public boolean hasField(String s)
  {
    int i;
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  public Field getField(String s)
  {
    int i;
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.name.equalsIgnoreCase(s))
        return field;
    }
    return null;
  }
  public int getFieldIndex(String s)
  {
    int i;
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.name.equalsIgnoreCase(s))
        return i;
    }
    return -1;
  }
  /**
   * Checks if table field is declared as null
   */
  public boolean hasFieldAsNull(String s)
  {
    int i;
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.name.equalsIgnoreCase(s))
        return field.isNull;
    }
    return false;
  }
  /**
   * Checks for the existence of a proc
   */
  public boolean hasProc(Proc p)
  {
    for (int i = 0; i < procs.size(); i++)
    {
      Proc proc = procs.elementAt(i);
      if (proc.name.equalsIgnoreCase(p.name))
        return true;
    }
    return false;
  }
  /**
   * Returns proc or null
   */
  public Proc getProc(String name)
  {
    for (int i = 0; i < procs.size(); i++)
    {
      Proc proc = procs.elementAt(i);
      if (proc.name.equalsIgnoreCase(name))
        return proc;
    }
    return null;
  }
  /**
   * Checks for the existence of a proc
   */
  public boolean hasCursorStdProc()
  {
    for (int i = 0; i < procs.size(); i++)
    {
      Proc proc = procs.elementAt(i);
      if (proc.isStd == true && proc.isSingle == false && proc.outputs.size() > 0)
        return true;
    }
    return false;
  }
  /**
   * Sets a field to be primary key
   */
  public void setPrimary(String s)
  {
    int i;
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.name.equalsIgnoreCase(s))
      {
        field.isPrimaryKey = true;
        return;
      }
    }
  }
  public String tableName()
  {
    if (database.schema.length() == 0)
      if (isLiteral)
        return literalName;
      else
        return name;
    if (isLiteral)
      return database.schema + "" + literalName;
    return database.schema + "" + name;
  }
  /**
   * Builds a merge proc generated as part of standard record class
   */
  public void buildMerge(Proc proc)
  {
    String name = tableName();
    int i;
    String comma = "  ";
    String front = "  ";
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));    
    proc.lines.addElement(new Line("merge into " + name));
    proc.lines.addElement(new Line("using table"));
    proc.lines.addElement(new Line("      ("));
    proc.lines.addElement(new Line("        values"));
    proc.lines.addElement(new Line("        ("));
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      proc.lines.addElement(new Line("        " + comma + "cast(? as " + varType(field) + ")"));
      proc.inputs.addElement(field);
      comma = ", ";
    }
    proc.lines.addElement(new Line("        )"));
    proc.lines.addElement(new Line("      )"));
    proc.lines.addElement(new Line("      temp_" + proc.table.name));
    proc.lines.addElement(new Line("      ("));
    comma = "  ";
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      proc.lines.addElement(new Line("      " + comma + field.useLiteral()));
      comma = ", ";
    }
    proc.lines.addElement(new Line("      )"));
    front = "on  ";
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey == true)
      {
        proc.lines.addElement(new Line(front + name + "" + field.useLiteral() + " = temp_" + proc.table.name + "." + field.useLiteral()));
        front = "and ";
      }
    }
    proc.lines.addElement(new Line("when matched then"));
    proc.lines.addElement(new Line("  update set"));
    comma = "    ";
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey == false)
      {
        proc.lines.addElement(new Line(comma + name + "" + field.useLiteral() + " = temp_" + proc.table.name + "." + field.useLiteral()));
        comma = "  , ";
      }
    }
    proc.lines.addElement(new Line("when not matched then"));
    proc.lines.addElement(new Line("  insert"));
    proc.lines.addElement(new Line("  ("));
    comma = "    ";
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      proc.lines.addElement(new Line(comma + field.useLiteral()));
      comma = "  , ";
    }
    proc.lines.addElement(new Line("  )"));
    proc.lines.addElement(new Line("  values"));
    proc.lines.addElement(new Line("  ("));
    comma = "    ";
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      proc.lines.addElement(new Line(comma + "temp_" + proc.table.name + "" + field.useLiteral()));
      comma = "  , ";
    }
    proc.lines.addElement(new Line("  )"));
  }
  static boolean isIdentity(Field field)
  {
    return field.type == Field.BIGIDENTITY || field.type == Field.IDENTITY;
  }
  static boolean isSequence(Field field)
  {
    return field.type == Field.BIGSEQUENCE || field.type == Field.SEQUENCE;
  }
  /**
   * Builds an insert proc generated as part of standard record class
   */
  public void buildInsert(Proc proc)
  {
    String name = tableName();
    int i;
    String line = "  ";
    proc.isStd = true;
    proc.isSql = true;
    proc.isInsert = true;
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.head", true));
    String identityName = "";
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("insert into " + name + " ("));
    for (i = 0; i < fields.size(); i++)
    {
      String comma = i + 1 < fields.size() ? "," : "";
      Field field = fields.elementAt(i);
      if (field.isCalc)
        continue;
      if (isIdentity(field) == true)
      {
        hasIdentity = true;
        identityName = field.name;
        proc.outputs.addElement(field);
        proc.isSingle = true;
        proc.hasUpdates = true;
        continue;
      }
      if (isSequence(field) == true && proc.hasReturning == true)
      {
        proc.outputs.addElement(field);
        proc.isSingle = true;
        proc.hasUpdates = true;
        proc.lines.addElement(new Line("_ret.checkUse(\"" + line + field.useLiteral() + comma + "\")", true));
        continue;
      }
      else if (isSequence(field) == true && proc.isMultipleInput == true)
      {
        continue;
      }
      else if (isSequence(field) == true && proc.hasReturning == false)
      {
        proc.outputs.addElement(field);
        proc.isSingle = true;
        proc.hasUpdates = true;
        continue;
      }
      proc.inputs.addElement(field);
      proc.lines.addElement(new Line(line + field.useLiteral() + comma));
    }
    proc.lines.addElement(new Line(" ) "));
    if (hasIdentity == true)
      proc.lines.addElement(new Line(" output inserted." + identityName));
    else if (proc.hasReturning)
      proc.lines.addElement(new Line("_ret.output", true));
    proc.lines.addElement(new Line(" values ("));
    for (i = 0; i < fields.size(); i++)
    {
      String comma = i + 1 < fields.size() ? "," : "";
      Field field = fields.elementAt(i);
      if (isIdentity(field) == true || field.isCalc)
        continue;
      if (isSequence(field) == true && proc.hasReturning == true)
        proc.lines.addElement(new Line("_ret.sequence", true));
      else if (isSequence(field) == true && proc.isMultipleInput == true)
        continue;
      else if (isSequence(field) == true && proc.hasReturning == false)
        continue;
      else
        proc.lines.addElement(new Line(line + "?" + comma));
      line = "  ";
    }
    proc.lines.addElement(new Line(" )"));
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
  }
  /**
   * Builds an insert proc generated as part of standard record class
   */
  public void buildBulkInsert(Proc proc)
  {
    proc.isMultipleInput = true;
    buildInsert(proc);
  }
  /**
   * Builds an identity proc generated as part of standard record class
   */
  public void buildIdentity(Proc proc)
  {
    String name = tableName();
    int i;
    String line;
    proc.isSql = true;
    proc.isSingle = true;
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.type != Field.IDENTITY)
        continue;
      proc.outputs.addElement(field);
      line = "select max(" + field.useLiteral() + ") " + field.useLiteral() + " from " + name;
      proc.lines.addElement(new Line(line));
    }
  }
  /**
   * Builds an update proc generated as part of standard record class
   */
  public void buildUpdate(Proc proc)
  {
    String name = tableName();
    int i, j;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("update " + name));
    proc.lines.addElement(new Line(" set"));
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey || field.isCalc || field.isSequence)
        continue;
      proc.inputs.addElement(field);
      if (j == 0)
        line = "  ";
      else
        line = ", ";
      j++;
      proc.lines.addElement(new Line(line + field.useLiteral() + " = ?"));

    }
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey)
      {
        proc.inputs.addElement(field);
        if (j == 0)
          line = " where ";
        else
          line = "   and ";
        j++;
        line = line + field.useLiteral() + " = ?";
        proc.lines.addElement(new Line(line));
      }
    }
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
  }
  /**
   * Builds an update proc generated as part of standard record class
   */
  public void buildUpdateFor(Proc proc, PrintWriter outLog)
  {
    String name = tableName();
    int i, j, k;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("update " + name));
    proc.lines.addElement(new Line(" set"));
    for (i = 0, j = 0; i < proc.fields.size(); i++)
    {
      String fieldName = (String) proc.fields.elementAt(i);
      for (k = 0; k < fields.size(); k++)
      {
        Field field = (Field) fields.elementAt(k);
        if (field.isPrimaryKey || field.isCalc || field.isSequence)
          continue;
        if (field.name.equalsIgnoreCase(fieldName))
        {
          proc.inputs.addElement(field);
          if (j == 0)
            line = "  ";
          else
            line = ", ";
          j++;
          proc.lines.addElement(new Line(line + field.useLiteral() + " = ?"));
        }
      }
    }
    AddTimeStampUserStamp(proc);
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = (Field) fields.elementAt(i);
      if (field.isPrimaryKey)
      {
        proc.inputs.addElement(field);
        if (j == 0)
          line = " where ";
        else
          line = "   and ";
        j++;
        line = line + field.useLiteral() + " = ?";
        proc.lines.addElement(new Line(line));
      }
    }
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
  }

  /**
   * Builds an updateby proc generated as part of standard record class
   */
  public void buildUpdateBy(Proc proc, PrintWriter outLog)
  {
    String name = tableName();
    int i, j, k;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("update " + name));
    proc.lines.addElement(new Line(" set"));
    if (proc.fields.size() == 0)
    {
      for (k = 0; k < proc.updateFields.size(); k++)
      {
        String fieldName = (String) proc.updateFields.elementAt(k);
        for (i = 0, j = 0; i < fields.size(); i++)
        {

          Field field = (Field) fields.elementAt(i);
          if (field.isPrimaryKey || field.isCalc || field.name.equalsIgnoreCase(fieldName) || field.isSequence)
            continue;
          proc.inputs.addElement(field);
          if (j == 0)
            line = "  ";
          else
            line = ", ";
          j++;
          proc.lines.addElement(new Line(line + field.useLiteral() + " = ?"));

        }
      }
    }
    else
    {
      for (i = 0, j = 0; i < proc.fields.size(); i++)
      {
        String fieldName = (String) proc.fields.elementAt(i);
        for (k = 0; k < fields.size(); k++)
        {
          Field field = (Field) fields.elementAt(k);
          if (field.name.equalsIgnoreCase(fieldName))
          {
            proc.inputs.addElement(field);
            if (j == 0)
              line = "  ";
            else
              line = ", ";
            j++;
            proc.lines.addElement(new Line(line + field.useLiteral() + " = ?"));
          }
        }
      }
    }
    AddTimeStampUserStamp(proc);
    for (i = 0, j = 0; i < proc.updateFields.size(); i++)
    {
      String fieldName = (String) proc.updateFields.elementAt(i);
      for (k = 0; k < fields.size(); k++)
      {
        Field field = fields.elementAt(k);
        if (field.name.equalsIgnoreCase(fieldName))
        {
          proc.inputs.addElement(field);
          if (j == 0)
            line = " where ";
          else
            line = "   and ";
          j++;
          line = line + field.useLiteral() + " = ?";
          proc.lines.addElement(new Line(line));
        }
      }
    }
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
  }
  private void AddTimeStampUserStamp(Proc proc)
  {
    int k;
    String line;
    boolean tmAdded, unAdded;
    tmAdded = unAdded = false;
    for (k = 0; k < fields.size(); k++)
    {
      Field field = (Field) fields.elementAt(k);
      if (field.type == Field.USERSTAMP && !unAdded)
      {
        unAdded = true;
        if (!proc.inputs.contains(field))
        {
          proc.inputs.addElement(field);
          line = ", ";

          proc.lines.addElement(new Line(line + field.useLiteral() + " = ?"));
        }
      }
      else if (field.type == Field.TIMESTAMP && !tmAdded)
      {
        tmAdded = true;
        if (!proc.inputs.contains(field))
        {
          proc.inputs.addElement(field);
          line = ", ";
          proc.lines.addElement(new Line(line + field.useLiteral() + " = ?"));
        }
      }
    }
  }
  /**
   * Builds an update proc generated as part of standard record class
   */
  public void buildBulkUpdate(Proc proc)
  {
    proc.isMultipleInput = true;
    buildUpdate(proc);
  }
  /**
   * Builds a delete by primary key proc
   */
  public void buildDeleteOne(Proc proc)
  {
    String name = tableName();
    int i, j;
    String line;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("delete from " + name));
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey)
      {
        proc.inputs.addElement(field);
        if (j == 0)
          line = " where ";
        else
          line = "   and ";
        j++;
        line = line + field.useLiteral() + " = ?";
        proc.lines.addElement(new Line(line));
      }
    }
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
  }
  /**
   * Builds a delete all rows proc
   */
  public void buildDeleteAll(Proc proc)
  {
    String name = tableName();
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("delete from " + name));
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
  }
  /**
   * Builds a count rows proc
   */
  public void buildCount(Proc proc)
  {
    String name = tableName();
    proc.isSql = true;
    proc.isSingle = true;
    Field field = new Field();
    field.name = "noOf";
    field.type = Field.INT;
    field.length = 4;
    proc.outputs.addElement(field);
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("select count(*) noOf from " + name));
  }
  /**
   * Builds a check for primary key existance proc
   */
  public void buildExists(Proc proc)
  {
    String name = tableName();
    int i, j;
    String line;
    proc.isSql = true;
    proc.isSingle = true;
    Field count = new Field();
    count.name = "noOf";
    count.type = Field.INT;
    count.length = 4;
    proc.outputs.addElement(count);
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("select count(*) noOf from " + name));
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey)
      {
        proc.inputs.addElement(field);
        if (j == 0)
          line = " where ";
        else
          line = "   and ";
        j++;
        line = line + field.useLiteral() + " = ?";
        proc.lines.addElement(new Line(line));
      }
    }
  }
  /**
   * Builds a select on primary key proc
   */
  public void buildSelectOne(Proc proc, boolean update, boolean readonly)
  {
    String name = tableName();
    int i, j;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.isSingle = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("select"));
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (!field.isPrimaryKey)
      {
        proc.outputs.addElement(field);
        if (j == 0)
          line = "  ";
        else
          line = ", ";
        j++;
        proc.lines.addElement(new Line(line + field.useLiteral()));
      }
    }
    proc.lines.addElement(new Line(" from " + name));
    for (i = 0, j = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.isPrimaryKey)
      {
        proc.inputs.addElement(field);
        if (j == 0)
          line = " where ";
        else
          line = "   and ";
        j++;
        line = line + field.useLiteral() + " = ?";
        proc.lines.addElement(new Line(line));
      }
    }
    if (update)
      proc.lines.addElement(new Line(" for update"));
    else if (readonly)
      proc.lines.addElement(new Line(" for read only"));
  }
  /**
   * Builds a select on primary key proc
   */
  public void buildMaxTmStamp(Proc proc)
  {
    String name = tableName();
    int i;
    proc.isStd = true;
    proc.isSql = true;
    proc.isSingle = true;
    proc.lines.addElement(new Line("select"));
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      if (field.type == Field.TIMESTAMP)
      {
        proc.outputs.addElement(field);
        proc.lines.addElement(new Line("max(" + field.useLiteral() + ")"));
      }
    }
    proc.lines.addElement(new Line(" from " + name));
  }
  /**
   * Builds a select all rows proc
   */
  public void buildSelectAll(Proc proc, boolean update, boolean readonly, boolean inOrder, boolean descending)
  {
    String name = tableName();
    int i;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("select"));
    for (i = 0; i < fields.size(); i++)
    {
      Field field = fields.elementAt(i);
      proc.outputs.addElement(field);
      if (i == 0)
        line = "  ";
      else
        line = ", ";
      proc.lines.addElement(new Line(line + field.useLiteral()));
    }
    proc.lines.addElement(new Line(" from " + name));
    selectFor(proc, update, readonly);
    selectOrderBy(proc, inOrder, descending);
  }
  private void selectOrderBy(Proc proc, boolean inOrder, boolean descending)
  {
    int i, n;
    String line, tail;
    if (inOrder == false)
      return;
    if (proc.orderFields.size() == 0)
    {
      for (i=0; i < fields.size(); i++)
      {
        Field field = fields.elementAt(i);
        if (field.isPrimaryKey)
          proc.orderFields.addElement(field.useLiteral());
      }
    }
    n = proc.orderFields.size();
    for (i = 0; i < n; i++)
    {
      String fieldName = proc.orderFields.elementAt(i);
      if (i == 0)
        line = " order by ";
      else
        line = ", ";
      if (descending == true && i+1 == n)
        tail = " desc";
      else
        tail = "";
      proc.lines.addElement(new Line(line + fieldName + tail));
    }
  }
  private void selectFor(Proc proc, boolean update, boolean readonly)
  {
    if (update)
      proc.lines.addElement(new Line(" for update"));
    else if (readonly)
      proc.lines.addElement(new Line(" for read only"));
  }
  public void buildDeleteBy(Proc proc, PrintWriter outLog)
  {
    String name = tableName();
    int i, j, k;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("Delete from " + name));
    for (i = 0, j = 0; i < proc.fields.size(); i++)
    {
      String fieldName = (String) proc.fields.elementAt(i);
      for (k = 0; k < fields.size(); k++)
      {
        Field field = fields.elementAt(k);
        if (field.name.equalsIgnoreCase(fieldName))
        {
          proc.inputs.addElement(field);
          if (j == 0)
            line = " where ";
          else
            line = "   and ";
          j++;
          line = line + field.useLiteral() + " = ?";
          proc.lines.addElement(new Line(line));
        }
      }
    }
    if (proc.hasReturning)
      proc.lines.add(new Line("_ret.tail", true));
    if (j == 0)
    {
      throw new Error("Error generating buildDeleteBy");
    }
  }
  public void buildSelectBy(Proc proc, boolean forUpdate, boolean forReadOnly, boolean inOrder, boolean descending, PrintWriter outLog)
  {
    String name = tableName();
    int i, j, k;
    String line;
    proc.isStd = true;
    proc.isSql = true;
    proc.lines.addElement(new Line("/* PROC " + proc.name + " */"));
    proc.lines.addElement(new Line("select"));
    if (proc.outputs.size() > 0)
    {
      for (i = 0; i < proc.outputs.size(); i++)
      {
        Field fieldOut = (Field) proc.outputs.elementAt(i);
        for (k = 0; k < fields.size(); k++)
        {
          Field field = (Field) fields.elementAt(k);
          if (field.name.equalsIgnoreCase(fieldOut.name))
          {
            if (i == 0)
              line = "  ";
            else
              line = ", ";
            proc.lines.addElement(new Line(line + field.useLiteral()));
          }
        }
      }
    }
    else
    {
      for (i = 0; i < fields.size(); i++)
      {
        Field field = (Field) fields.elementAt(i);
        proc.outputs.addElement(field);
        if (i == 0)
          line = "  ";
        else
          line = ", ";
        proc.lines.addElement(new Line(line + field.useLiteral()));
      }
    }
    proc.lines.addElement(new Line(" from " + name));
    selectFor(proc, forUpdate, forReadOnly);
    for (i = 0, j = 0; i < proc.fields.size(); i++)
    {
      String fieldName = (String) proc.fields.elementAt(i);
      for (k = 0; k < fields.size(); k++)
      {
        Field field = (Field) fields.elementAt(k);
        if (field.name.equalsIgnoreCase(fieldName))
        {
          proc.inputs.addElement(field);
          if (j == 0)
            line = " where ";
          else
            line = "   and ";
          j++;
          line = line + field.useLiteral() + " = ?";
          proc.lines.addElement(new Line(line));
        }
      }
    }
    if (j == 0)
    {
      throw new Error("Error in SelectBy");
    }
    selectOrderBy(proc, inOrder, descending);
  }
  public void buildSelectFrom(Proc proc, Table table, PrintWriter outLog)
  {
    String name = tableName();
    int i, j, k;
    String line;
    proc.extendsStd = false;
    proc.isSql = true;
    String preFix = "";
    boolean doSelect = true;
    Line first;
    if (proc.lines.size() > 0)
    {
      first = (Line) proc.lines.elementAt(0);
      for (k = 0; k < proc.lines.size(); k++)
      {
        first = (Line) proc.lines.elementAt(k);
        if (first.line.toLowerCase().indexOf("select ") > -1 && first.line.toLowerCase().indexOf("select ") < 5)
        {
          doSelect = false;
          outLog.println("Select found not generating SELECT." + first.line.toLowerCase().indexOf("select "));
          break;
        }
      }
    }
    if (doSelect)
    {
      if (preFix == "")
      {
        for (k = 0; k < proc.lines.size(); k++)
        {
          first = (Line) proc.lines.elementAt(k);
          if (first.line.toLowerCase().indexOf(name.toLowerCase()) > -1)
          {
            preFix = first.line.substring(first.line.toLowerCase().indexOf(name.toLowerCase()) + name.length()).trim();
            int n = preFix.indexOf(' ');
            if (n > 0)
            {
              preFix = preFix.substring(0, n).trim();
            }
            if (!name.toLowerCase().startsWith(preFix.toLowerCase().substring(0, 1)))
            {
              outLog.println("PREFIX mismatch. Dropping PREFIX");
              preFix = "";
            }
            break;
          }
        }
      }
      if (preFix.equals(""))
      {
        outLog.println("Unable to determine PREFIX for table");
      }
      proc.lines.insertElementAt(new Line("SELECT "), 0);
      for (j = 0; j < proc.outputs.size(); j++)
      {
        Field fieldName = (Field) proc.outputs.elementAt(j);
        if (table.hasField(fieldName.name))
        {
          if (j == 0)
            if (preFix.length() > 0)
              line = "  " + preFix + "";
            else
              line = "  ";
          else if (preFix.length() > 0)
            line = ", " + preFix + "";
          else
            line = ", ";
        }
        else
        {
          fieldName.isExtStd = true;
          fieldName.isExtStdOut = true;
          if (j == 0)
            line = "  ";
          else
            line = ", ";
        }
        proc.lines.insertElementAt(new Line(line + fieldName.useLiteral() + " "), j + 1);
      }
      if (proc.isStd)
      {
        proc.isStd = false;
        proc.extendsStd = true;
        proc.useStd = true;
      }
    }
  }
  public String toString()
  {
    return name;
  }
  private String set(String a, String b, String what, PrintWriter outLog)
  {
    if (a.length() == 0)
      a = b;
    else if (a.equalsIgnoreCase(b) == false)
      outLog.println("Import " + what + " name :" + a + " not the same as :" + b);
    return a;
  }
  private boolean set(boolean a, boolean b, String what, PrintWriter outLog)
  {
    if (a == false)
      a = b;
    else if (b == false)
      outLog.println("Import " + what + " is already true and is not set to false.");
    return a;
  }
  private void copy(Table addin, PrintWriter outLog)
  {
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
  private void merge(Table addin, PrintWriter outLog)
  {
    alias = set(alias, addin.alias, "alias", outLog);
    check = set(check, addin.check, "check", outLog);
    hasPrimaryKey = set(hasPrimaryKey, addin.hasPrimaryKey, "hasPrimaryKey", outLog);
    hasSequence = set(hasSequence, addin.hasSequence, "hasSequence", outLog);
    hasTimeStamp = set(hasTimeStamp, addin.hasTimeStamp, "hasTimeStamp", outLog);
    hasAutoTimeStamp = set(hasAutoTimeStamp, addin.hasAutoTimeStamp, "hasAutoTimeStamp", outLog);
    hasUserStamp = set(hasUserStamp, addin.hasUserStamp, "hasUserStamp", outLog);
    hasExecute = set(hasExecute, addin.hasExecute, "hasExecute", outLog);
    hasSelect = set(hasSelect, addin.hasSelect, "hasSelect", outLog);
    hasInsert = set(hasInsert, addin.hasInsert, "hasInsert", outLog);
    hasDelete = set(hasDelete, addin.hasDelete, "hasDelete", outLog);
    hasUpdate = set(hasUpdate, addin.hasUpdate, "hasUpdate", outLog);
    hasStdProcs = set(hasStdProcs, addin.hasStdProcs, "hasStdProcs", outLog);
    hasIdentity = set(hasIdentity, addin.hasIdentity, "hasIdentity", outLog);
  }
  public Table add(Table addin, PrintWriter outLog)
  {
    Table table = new Table();
    table.copy(this, outLog);
    table.merge(addin, outLog);
    return table;
  }
  public boolean hasOption(String value)
  {
    for (int i = 0; i < options.size(); i++)
    {
      String option = options.elementAt(i);
      if (option.toLowerCase().compareTo(value.toLowerCase()) == 0)
        return true;
    }
    return false;
  }
  /**
   * Translates field type to DB2 SQL column types
   */
  static String varType(Field field)
  {
    switch (field.type)
    {
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
    case Field.AUTOTIMESTAMP:
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
    }
    return "unknown";
  }
}