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

package bbd.jportal.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Query
{
  public PreparedStatement prep;
  public ResultSet result;
  public Query(PreparedStatement prep, ResultSet result)
  {
    this.prep = prep;
    this.result = result;
  }
  public void close() throws SQLException
  {
    result.close();
    prep.close();
  }
}
