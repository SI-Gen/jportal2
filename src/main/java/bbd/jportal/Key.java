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

/**
*  Keys and Indexes used for the database (if its not primary or unique then it is
*  an index)
*/
public class Key implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Name of index or key */
  public String name;
  /** List of fields used in the index or key */
  public Vector<String> fields;
  public Vector<String> options;
  /** Indicates the primary key */
  public boolean isPrimary;
  /** Indicates the index is unique (not defined if primary key) */
  public boolean isUnique;
  /** Contructs with default values */
  public Key()
  {
    name      = "";
    fields    = new Vector<String>();
    options   = new Vector<String>();
    isPrimary = false;
    isUnique  = false;
  }
  public void reader(DataInputStream ids) throws IOException
  {
    name = ids.readUTF();
    int noOf = ids.readInt();
    for (int i=0; i<noOf;i++)
    {
      String value = ids.readUTF();
      fields.addElement(value);
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf;i++)
    {
      String value = ids.readUTF();
      options.addElement(value);
    }
    isPrimary = ids.readBoolean();
    isUnique = ids.readBoolean();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeUTF(name);
    ods.writeInt(fields.size());
    for (int i=0; i<fields.size(); i++)
    {
      String value = (String) fields.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(options.size());
    for (int i=0; i<options.size(); i++)
    {
      String value = (String) options.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeBoolean(isPrimary);
    ods.writeBoolean(isUnique);
  }
  /** Checks if field is already used */
  public boolean hasField(String s)
  {
    int i;
    for (i=0; i<fields.size(); i++)
    {
      String name = (String) fields.elementAt(i);
      if (name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
}


