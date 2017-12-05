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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectorOracle extends Connector
{
  String userId;
  /**
  ** @param driverType - get inserted into  jdbc:oracle:----:user/password[@server]
  ** @param server     - get inserted into  jdbc:oracle:driverType:user/password[@----]
  ** @param user       - get inserted into  jdbc:oracle:driverType:----/password[@server]
  ** @param password   - get inserted into  jdbc:oracle:driverType:user/----[@server]
  */
  public ConnectorOracle(String driverType, String server,
                  String user, String password) throws ClassNotFoundException, SQLException
  {
    String url = "jdbc:oracle:"+driverType+":";
    if (server.length() > 0)
      url = url+"@"+server;
    connect(url, user, password);
  }
  void connect(String url,
               String user, String password) throws ClassNotFoundException, SQLException
  {
    userId = user;
    connection = DriverManager.getConnection(url, user, password);    
    System.out.println(!connection.isClosed ());
    connection.setAutoCommit(false);
  }
  public String getUserstamp() throws SQLException
  {
    return userId;
  }
  public int getSequence(String table) throws SQLException
  {
    int nextNo;
    PreparedStatement prep = connection.prepareStatement("select "+table+"Seq.NextVal from dual");
    ResultSet result = prep.executeQuery();
    result.next();
    nextNo =  result.getInt(1);
    result.close();
    prep.close();
    return nextNo;
  }
  public long getBigSequence(String table) throws SQLException
  {
    long nextNo;
    PreparedStatement prep = connection.prepareStatement("select "+table+"Seq.NextVal from dual");
    ResultSet result = prep.executeQuery();
    result.next();
    nextNo =  result.getLong(1);
    result.close();
    prep.close();
    return nextNo;
  }
  public int getSequence(String table, String field) throws SQLException
  {
    return getSequence(table);
  }
  public long getBigSequence(String table, String field) throws SQLException
  {
    return getBigSequence(table);
  }
  public Returning getReturning(String table, String field) throws SQLException
  {
    Returning result = new Returning();
    result.head = "";
    result.output = "";
    result.sequence = table + "Seq.NextVal,"; 
    result.tail = ""; 
    result.dropField = "";
    result.doesGeneratedKeys = false;
    return result;
  }
}

