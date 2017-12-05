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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author vince
 */
public class ConnectorPostgre extends Connector
{
  String user;
  String server;
  /**
   * @throws Exception
   *  
   */
  public ConnectorPostgre(String server, String user, String password) throws Exception
  {
    this.user = user;
    this.server = server;
    String url = "jdbc:postgresql:" + server;
    connect(url, password);
  }
  void connect(String url, String password) throws Exception
  {
    Class<?> postgresqlDriver = Class.forName("org.postgresql.Driver");
    Driver driver = (Driver) postgresqlDriver.newInstance();
    DriverManager.registerDriver(driver);
    connection = DriverManager.getConnection(url, user, password);
    System.out.println(!connection.isClosed());
    connection.setAutoCommit(false);
  }
  public String getUserstamp() throws SQLException
  {
    return user;
  }
  public int getSequence(String table) throws SQLException
  {
    return 0;
  }
  public long getBigSequence(String table) throws SQLException
  {
    return 0;
  }
  public int getSequence(String table, String field) throws SQLException
  {
    int nextNo;
    PreparedStatement prep = connection.prepareStatement("select nextval("+table+"_"+field+"_seq)");
    ResultSet result = prep.executeQuery();
    result.next();
    nextNo =  result.getInt(1);
    result.close();
    prep.close();
    return nextNo;
  }
  public long getBigSequence(String table, String field) throws SQLException
  {
    long nextNo;
    PreparedStatement prep = connection.prepareStatement("select nextval("+table+"_"+field+"_seq)");
    ResultSet result = prep.executeQuery();
    result.next();
    nextNo =  result.getLong(1);
    result.close();
    prep.close();
    return nextNo;
  }
  public Returning getReturning(String table, String field) throws SQLException
  {
    Returning result = new Returning();
    result.head = "";
    result.output = "";
    result.sequence = "0,"; 
    result.tail = ""; 
    result.dropField = field;
    result.doesGeneratedKeys = false;
    return result;
  }
}
