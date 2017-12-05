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

/** Lines of SQL Code */
public class Line implements Serializable
{
  private static final long serialVersionUID = 1L;
  public String line;
  public boolean isVar;
  /** Constructs line needed to be enclosed in double quotes */
  public Line(String l)
  {
    line = l;
    isVar = false;
  }
  /** Constructs line used in variable substitution */
  public Line(String l, boolean t)
  {
    line = l;
    isVar = t;
  }
  public void reader(DataInputStream ids) throws IOException
  {
    line = ids.readUTF();
    isVar = ids.readBoolean();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ods.writeUTF(line);
    ods.writeBoolean(isVar);
  }
}



