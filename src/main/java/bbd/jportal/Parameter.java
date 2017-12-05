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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

public class Parameter implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public Table  table;
  public Proc   reader;
  public Proc   insert;
  public Proc   delete;
  public Proc   update;
  public String title;
  public Proc   cache;
  public Vector<Field> supplied;
  public Vector<Field> shows;
  public Vector<String> cacheExtras;
  public boolean isViewOnly;
  public Parameter()
  {
    table       = null;
    reader      = null;
    insert      = null;
    delete      = null;
    update      = null;
    title       = "";
    cache       = null;
    supplied    = new Vector<Field>();
    shows       = new Vector<Field>();
    cacheExtras = new Vector<String>();
    isViewOnly  = false;
  }
  public void reader(DataInputStream ids) throws IOException
  {
    boolean isUsed = ids.readBoolean();
    reader = null;
    if (isUsed == true)
    {
      String s = ids.readUTF();
      Proc p = table.getProc(s);
      if (p != null)
        reader = p;
    }
    isUsed = ids.readBoolean();
    insert = null;
    if (isUsed == true)
    {
      String s = ids.readUTF();
      Proc p = table.getProc(s);
      if (p != null)
        insert = p;
    }
    isUsed = ids.readBoolean();
    delete = null;
    if (isUsed == true)
    {
      String s = ids.readUTF();
      Proc p = table.getProc(s);
      if (p != null)
        delete = p;
    }
    isUsed = ids.readBoolean();
    update = null;
    if (isUsed == true)
    {
      String s = ids.readUTF();
      Proc p = table.getProc(s);
      if (p != null)
        update = p;
    }
    title = ids.readUTF();
    isUsed = ids.readBoolean();
    cache = null;
    if (isUsed == true)
    {
      String s = ids.readUTF();
      Proc p = table.getProc(s);
      if (p != null)
        cache = p;
    }
    int noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String s = ids.readUTF();
      if (table.hasField(s))
        supplied.addElement(table.getField(s));
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String s = ids.readUTF();
      if (table.hasField(s))
        shows.addElement(table.getField(s));
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String s = ids.readUTF();
      cacheExtras.addElement(s);
    }
    isViewOnly = ids.readBoolean();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeBoolean(reader != null);
    if (reader != null)
      ods.writeUTF(reader.name);
    ods.writeBoolean(insert != null);
    if (insert != null)
      ods.writeUTF(insert.name);
    ods.writeBoolean(delete != null);
    if (delete != null)
      ods.writeUTF(delete.name);
    ods.writeBoolean(update != null);
    if (update != null)
      ods.writeUTF(update.name);
    ods.writeUTF(title);
    ods.writeBoolean(cache != null);
    if (cache != null)
      ods.writeUTF(cache.name);
    ods.writeInt(supplied.size());
    for (int i=0; i<supplied.size(); i++)
    {
      Field f = (Field) supplied.elementAt(i);
      ods.writeUTF(f.name);
    }
    ods.writeInt(shows.size());
    for (int i=0; i<shows.size(); i++)
    {
      Field f = (Field) shows.elementAt(i);
      ods.writeUTF(f.name);
    }
    ods.writeInt(cacheExtras.size());
    for (int i=0; i<cacheExtras.size(); i++)
    {
      String s = (String) cacheExtras.elementAt(i);
      ods.writeUTF(s);
    }
    ods.writeBoolean(isViewOnly);
  }
}
