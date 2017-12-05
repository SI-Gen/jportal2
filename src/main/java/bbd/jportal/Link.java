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
* Foreign keys used in database
*/
public class Link implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /** Name of foreign table */
  public String name;
  public String linkName;
  public Vector<String> fields;
  public Vector<String> linkFields;
  public Vector<String> options;
  public boolean isDeleteCascade;
  public boolean isUpdateCascade;
  public boolean isProc;
  public boolean isDProc;
  public Link()
  {
    name      = "";
    linkName = "";
    fields    = new Vector<String>();
    linkFields = new Vector<String>();
    options = new Vector<String>();
    isDeleteCascade = false;
    isUpdateCascade = false;
    isProc = false;
    isDProc = false;
  }
  public void reader(DataInputStream ids) throws IOException
  {
    name = ids.readUTF();
    linkName = ids.readUTF();
    int noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = ids.readUTF();
      fields.addElement(value);
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = ids.readUTF();
      linkFields.addElement(value);
    }
    isDeleteCascade = ids.readBoolean();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeUTF(name);
    ods.writeUTF(linkName);
    ods.writeInt(fields.size());
    for (int i=0; i<fields.size(); i++)
    {
      String value = (String) fields.elementAt(i);
      ods.writeUTF(value);
    }
    for (int i=0; i<linkFields.size(); i++)
    {
      String value = (String) linkFields.elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeBoolean(isDeleteCascade);
  }
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
  /** If there is an alias uses that else returns name */
  public String useName()
  {
    String n = name;
    n = replaceAll(n, "", "_");
    return n;
  }

  public String replaceAll(
    String haystack,              // String to search in
    String needle,                // Substring to find
    String replacement)
  {         // Substring to replace with

    int i = haystack.lastIndexOf(needle);
    if (i != -1)
    {
      StringBuffer buffer = new StringBuffer(haystack);
      buffer.replace(i, i + needle.length(), replacement);
      while ((i = haystack.lastIndexOf(needle, i - 1)) != -1)
      {
        buffer.replace(i, i + needle.length(), replacement);
      }
      haystack = buffer.toString();
    }

    return haystack;
  }
}



