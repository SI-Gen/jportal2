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

import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectorSqlServer extends Connector
{
  String userId;
  public ConnectorSqlServer(String server, String schema, String user, String password) throws SQLException, ClassNotFoundException
  {
    userId = user;
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    connection = DriverManager.getConnection("jdbc:sqlserver://" + server
        + (schema.length() > 0 ? ";database="+schema : "")
        + ";user=" + user 
        + ";password=" + password 
        );
    connection.setAutoCommit(false);
  }
  public int getSequence(String table) throws SQLException
  {
    return 0;
  }
  public int getSequence(String table, String field) throws SQLException
  {
    return getSequence(table);
  }
  public long getBigSequence(String table) throws SQLException
  {
    return 0;
  }
  public long getBigSequence(String table, String field) throws SQLException
  {
    return getBigSequence(table);
  }
  public String getUserstamp() throws SQLException
  {
    return userId;
  }
  public Returning getReturning(String table, String field) throws SQLException
  {
    Returning result = new Returning();
    result.head = "";
    result.output = "";
    result.sequence = ""; 
    result.tail = ""; 
    result.dropField = field;
    result.doesGeneratedKeys = true;
    return result;
  }
}
