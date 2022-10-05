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
public class SQLProcFieldVariableToken implements ISQLProcToken, Serializable
{
  private static final long serialVersionUID = 1L;
  public Field value;
  public boolean isVar() {
    return isVar;
  }

  public boolean isVar;
  /** Constructs line needed to be enclosed in double quotes */
  public SQLProcFieldVariableToken(Field l)
  {
    value = l;
    isVar = false;
  }
  /** Constructs line used in variable substitution */
  public SQLProcFieldVariableToken(Field l, boolean t)
  {
    value = l;
    isVar = t;
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

  public String getUnformattedLine()
  {
    return ":" + value.useLiteral();
  }

  public String getDecoratedLine(JPortalTemplateOutputOptions options) {
    return options.FieldVariablePrefix + value.useLiteral() + options.FieldVariableSuffix;
  }

  @Override
  public String toString() {
    throw new RuntimeException("Don't use toString. Use getDecorateLine instead");
  }

}

