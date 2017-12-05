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
* This is used to hold the grants and users of Table and Procedures
*/
public class Grant implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** List of Permissions */
  public Vector<String> perms;
  /** List of Users for the List of Permissions */
  public Vector<String> users;
  /** Construct default values */
  public Grant()
  {
    perms = new Vector<String>();
    users = new Vector<String>();
  }
  public void reader(DataInputStream ids) throws IOException
  {
    int noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = (String) ids.readUTF();
      perms.addElement(value);
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = (String) ids.readUTF();
      users.addElement(value);
    }
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeInt(perms.size());
    for (int i=0; i<perms.size(); i++)
    {
      String value = (String) perms.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(users.size());
    for (int i=0; i<users.size(); i++)
    {
      String value = (String) users.elementAt(i);
      ods.writeUTF(value);
    }
  }
  /** Check if permission is used */
  public boolean hasPerm(String s)
  {
    for (int i=0; i<perms.size(); i++)
    {
      String name = (String) perms.elementAt(i);
      if (name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  /** Check if user has been defined */
  public boolean hasUser(String s)
  {
    for (int i=0; i<users.size(); i++)
    {
      String name = (String) users.elementAt(i);
      if (name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
}


