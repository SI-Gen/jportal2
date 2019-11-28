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
    linkName  = "";
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
      getFields().addElement(value);
    }
    noOf = ids.readInt();
    for (int i=0; i<noOf; i++)
    {
      String value = ids.readUTF();
      getLinkFields().addElement(value);
    }
    isDeleteCascade = ids.readBoolean();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeUTF(name);
    ods.writeUTF(linkName);
    ods.writeInt(getFields().size());
    for (int i = 0; i< getFields().size(); i++)
    {
      String value = (String) getFields().elementAt(i);
      ods.writeUTF(value);
    }
    for (int i = 0; i< getLinkFields().size(); i++)
    {
      String value = (String) getLinkFields().elementAt(i);
      ods.writeUTF(value);
    }
    ods.writeBoolean(isDeleteCascade);
  }
  public boolean hasField(String s)
  {
    int i;
    for (i=0; i< getFields().size(); i++)
    {
      String name = (String) getFields().elementAt(i);
      if (name.equalsIgnoreCase(s))
        return true;
    }
    return false;
  }
  /** If there is an alias uses that else returns name */
  public String useName()
  {
    String n = name;
    n = replaceAll(n, " ", "_");
    n = replaceAll(n, ".", "_");
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

  public String getSchema() {
    String[] results = name.split("\\.");
    if (results.length > 1)
      return results[0];
    return "";
  }

  public String getName() {
    String[] results = name.split("\\.");
    if (results.length > 1)
      return results[1];
    return name;
  }

  public String getFullName() {
    return name;
  }

  public String getLinkName() {
    return linkName;
  }

  public Vector<String> getFields() {
    return fields;
  }

  public Vector<String> getLinkFields() {
    return linkFields;
  }

  public String getFirstLinkField() {
    if (getLinkFields().size() > 0) {
      return getLinkFields().get(0);
    }
    return null;
  }

  public Vector<String> getOptions() {
    return options;
  }

}



