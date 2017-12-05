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
* Views of table
*/
public class View implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public String getName() {
    return name;
  }

  public Vector<String> getAliases() {
    return aliases;
  }

  public Vector<String> getLines() {
    return lines;
  }

  public Vector<String> getUsers() {
    return users;
  }

  /** Name of view */
  public String name;
  /** List of aliases from view */
  public Vector<String> aliases;
  /** SQL Code for view */
  public Vector<String> lines;
  /** Users of the view */
  public Vector<String> users;
  /** Code starts at line */
  public int start;
  /** Constructs the view with proper defaults */
  public View()
  {
    name      = "";
    aliases   = new Vector<String>();
    lines     = new Vector<String>();
    users     = new Vector<String>();
    start     = 0;
  }
  public void reader(DataInputStream ids) throws IOException
  {
    name = ids.readUTF();
    int noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = ids.readUTF();
      aliases.addElement(value);
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = ids.readUTF();
      lines.addElement(value);
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = ids.readUTF();
      users.addElement(value);
    }
    start = ids.readInt();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeUTF(name);
    ods.writeInt(aliases.size());
    for (int i=0; i<aliases.size(); i++)
    {
      String value = (String) aliases.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(lines.size());
    for (int i=0; i<lines.size(); i++)
    {
      String value = (String) lines.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(users.size());
    for (int i=0; i<users.size(); i++)
    {
      String value = (String) users.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeInt(start);
  }
  /** Checks if view has alias */
  public boolean hasAlias(String s)
  {
    int i;
    for (i=0; i<aliases.size(); i++)
    {
      String alias = (String) aliases.elementAt(i);
      if (alias.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  /** Checks if view has user */
  public boolean hasUser(String s)
  {
    int i;
    for (i=0; i<users.size(); i++)
    {
      String name = (String) users.elementAt(i);
      if (name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  public String toString()
  {
    return name;
  }
}


