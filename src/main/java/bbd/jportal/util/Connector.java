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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

abstract public class Connector
{
  class Calendar extends GregorianCalendar
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    long AsMillis()
    {
      return getTimeInMillis();
    }
  }
  public Connection connection;
  public PreparedStatement prepareStatement(String sql) throws SQLException
  {
    return connection.prepareStatement(sql);
  }
  public PreparedStatement prepareStatement(String sql, boolean generated) throws SQLException
  {
    int gen = generated?Statement.RETURN_GENERATED_KEYS:Statement.NO_GENERATED_KEYS;
    return connection.prepareStatement(sql, gen);
  }
  public CallableStatement prepareCall(String sql) throws SQLException
  {
    return connection.prepareCall(sql);
  }
  abstract public String getUserstamp() throws SQLException;
  public Timestamp getTimestamp() throws SQLException
  {
    Calendar now = new Calendar();
    return new Timestamp(now.AsMillis());
  }
  abstract public int getSequence(String table) throws SQLException;
  abstract public int getSequence(String table, String field) throws SQLException;
  abstract public long getBigSequence(String table) throws SQLException;
  abstract public long getBigSequence(String table, String field) throws SQLException;
  public static class Returning
  {
    public String head = "";
    public String output = "";
    public String tail = "";
    public String sequence = "";
    public String dropField = "";
    public boolean doesGeneratedKeys = false;
    public String checkUse(String line)
    {
        // if (dropField.equalsIgnoreCase(line))
        //     return "";
        // return line;
        return checkExclude(line,dropField);
    }
  }
  abstract public Returning getReturning(String table, String field) throws SQLException;
  public static String checkExclude(String line, String field)
  {
    if (field.length() > 0)
    {
      int n = line.indexOf(field);
      int m = n + field.length();
      if (n > 0 && line.charAt(n - 1) == ' ' && m < line.length() && line.charAt(m) == ',')
        return "";
    }
    return line;
  }
  public void setAutoCommit(boolean cond) throws SQLException
  {
    connection.setAutoCommit(cond);
  }
  public void commit() throws SQLException
  {
    if (inTransaction == true)
      doCommit = doRollback = inTransaction = false;
    connection.commit();
  }
  public void rollback() throws SQLException
  {
    if (inTransaction == true)
      doCommit = doRollback = inTransaction = false;
    connection.rollback();
  }
  private boolean inTransaction = false;
  private boolean doCommit = false;
  private boolean doRollback = false;
  public void startTran() throws SQLException
  {
    if (inTransaction == true)
      endTran();
    inTransaction = true;
    doCommit = false;
    doRollback = false;
  }
  public void endTran() throws SQLException
  {
    if (inTransaction == true)
    {
      if (doRollback == true)
        connection.rollback();
      else if (doCommit == true)
        connection.commit();
    }
    inTransaction = false;
    doCommit = false;
    doRollback = false;
  }
  public void flagCommit() throws SQLException
  {
    if (inTransaction == true)
      doCommit = true;
    else
      connection.commit();
  }
  public void flagRollback() throws SQLException
  {
    if (inTransaction == true)
      doRollback = true;
    else
      connection.rollback();
  }
}
