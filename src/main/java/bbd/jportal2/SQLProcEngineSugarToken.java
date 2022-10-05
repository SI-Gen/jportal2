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
import java.util.Map;


/** Lines of SQL Code */
public class SQLProcEngineSugarToken implements ISQLProcToken, Serializable
{
  public enum EngineSugarType {
    Output,
    Tail,
    Sequence
  }
  private static final long serialVersionUID = 1L;
  public EngineSugarType value;
  public boolean isVar() {
    return isVar;
  }

  public boolean isVar;
  /** Constructs line needed to be enclosed in double quotes */
  public SQLProcEngineSugarToken(EngineSugarType t)
  {
    value = t;
    isVar = false;
  }
  /** Constructs line used in variable substitution */
  public SQLProcEngineSugarToken(EngineSugarType t, boolean b)
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

  public String getUnformattedLine()
  {
    return "_ret." + value.name().toLowerCase();
  }

  @Override
  public String getDecoratedLine(JPortalTemplateOutputOptions options) {
    return options.EngineSugarPrefix + value.name().toLowerCase() + options.EngineSugarSuffix;
  }

  public String toString() {
    return getUnformattedLine();
  }

}

