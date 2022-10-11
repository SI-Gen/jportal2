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

/** Lines of SQL Code */
public class SQLProcTableNameToken implements ISQLProcToken, Serializable
{
  private static final long serialVersionUID = 1L;
  public Table value;
  public boolean isVar() {
    return isVar;
  }

  public boolean isVar;
  /** Constructs line needed to be enclosed in double quotes */
  public SQLProcTableNameToken(Table t)
  {
    value = t;
    isVar = false;
  }
  /** Constructs line used in variable substitution */
  public SQLProcTableNameToken(Table t, boolean b)
  {
    value = t;
    isVar = b;
  }
//  public void reader(DataInputStream ids) throws IOException
//  {
//    value = ids.readUTF();
//    isVar = ids.readBoolean();
//  }
//  public void writer(DataOutputStream ods) throws IOException
//  {
//    ods.writeUTF(value);
//    ods.writeBoolean(isVar);
//  }
  public String getlineval()
  {
    String tname = value.useLiteral();
    tname = tname.replaceAll("\\:{1}\\w*", "?");
    tname = tname.replace("\"", "\\\"");
    return tname;
  }

  public String getUnformattedLine()
  {
    return value.tableNameWithSchema();
  }

  public String getDecoratedLine(JPortalTemplateOutputOptions options) {
    return options.TableNamePrefix + value.tableNameWithSchema() + options.TableNameSuffix;
  }

  public String toString() {
    return getlineval();
  }

}

