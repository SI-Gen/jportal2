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

import java.io.Serializable;
import java.util.ArrayList;

public class Limit implements Serializable
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  public int count;
  public String variable;
  public int size;
  public Limit()
  {
    this.count = 0;
    this.variable = null;
    this.size = 0;
  }
  public int fetchRowsSize()
  {
    int result = 0;
    if (variable == null)
      result += String.format("\"\\nFETCH FIRST %d ROWS ONLY\"", count).length();
    else
    {
      result += "\"\\nFETCH FIRST \"".length();
      result += size;
      result += "\"\\nROWS ONLY\\n\"".length();
    }
    return result;
  }
  public String[] fetchRowsLines()
  {
    ArrayList<String> code;
    code = new ArrayList<>();
    if (variable == null)
    {
      code.add(String.format("    strcat(q_.command, \"\\nFETCH FIRST %d ROWS ONLY\");", count));
      var strings = code.toArray(new String[1]);
      return strings;
    }
    else
    {
      code.add("    strcat(q_.command, \"\\nFETCH FIRST \");");
      code.add(String.format("    strcat(q_.command, %s);", variable));
      code.add("    strcat(q_.command, \" ROWS ONLY\\n\");");
      var strings = code.toArray(new String[3]);
      return strings;
    }
  }
  public String[] fetchRowsLinesDBApi()
  {
    ArrayList<String> code;
    code = new ArrayList<>();
    if (variable == null)
    {
      code.add(String.format("FETCH FIRST %d ROWS ONLY", count));
      var strings = code.toArray(new String[1]);
      return strings;
    }
    else
    {
      code.add("FETCH FIRST ");
      code.add(String.format("{self.%s}", variable));
      code.add(" ROWS ONLY");
      var strings = code.toArray(new String[3]);
      return strings;
    }
  }
  public int topRowsSize()
  {
    int result = 0;
    if (variable == null)
      result += String.format("\"TOP %d \");", count).length();
    else
    {
      result += "\"TOP \");".length();
      result += size;
    }
    return result;
  }
  public String[] topRowsLines()
  {
    ArrayList<String> code;
    code = new ArrayList<>();
    if (variable == null)
    {
      code.add(String.format("    strcat(q_.command, \"TOP %d \");", count));
      var strings = code.toArray(new String[1]);
      return strings;
    }
    else
    {
      code.add("    strcat(q_.command, \"TOP \");");
      code.add(String.format("    strcat(q_.command, %s);", variable));
      code.add("    strcat(q_.command, \"\\n\");");
      var strings = code.toArray(new String[3]);
      return strings;
    }
  }
  public String[] topRowsLinesDBApi()
  {
    ArrayList<String> code;
    code = new ArrayList<>();
    if (variable == null)
    {
      code.add(String.format("TOP %d ", count));
      var strings = code.toArray(new String[1]);
      return strings;
    }
    else
    {
      code.add("TOP ");
      code.add(String.format("{self.%s}", variable));
      var strings = code.toArray(new String[2]);
      return strings;
    }
  }
  public int limitRowsSize()
  {
    int result = 0;
    if (variable == null)
      result += String.format("\"\\nLIMIT %d\"", count).length();
    else
    {
      result += "\"\\nLIMIT \"".length();
      result += size;
    }
    return result;
  }
  public String[] limitRowsLines()
  {
    ArrayList<String> code;
    code = new ArrayList<>();
    if (variable == null)
    {
      code.add(String.format("    strcat(q_.command, \"\\nLIMIT %d \");", count));
      var strings = code.toArray(new String[1]);
      return strings;
    }
    else
    {
      code.add("    strcat(q_.command, \"\\nLIMIT \");");
      code.add(String.format("    strcat(q_.command, %s);", variable));
      code.add("    strcat(q_.command, \"\\n\");");
      var strings = code.toArray(new String[3]);
      return strings;
    }
  }
  public String[] limitRowsLinesDBApi()
  {
    ArrayList<String> code;
    code = new ArrayList<>();
    if (variable == null)
    {
      code.add(String.format("LIMIT %d ", count));
      var strings = code.toArray(new String[1]);
      return strings;
    }
    else
    {
      code.add("LIMIT ");
      code.add(String.format("{self.%s} ", variable));
      var strings = code.toArray(new String[2]);
      return strings;
    }
  }
}
