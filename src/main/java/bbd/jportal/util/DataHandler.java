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

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author vince
 *
 */
public class DataHandler
{
  public static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddhhmmss");
  public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
  public static SimpleDateFormat timeFormat = new SimpleDateFormat("hhmmss");
  public static String timeStamp(java.sql.Timestamp value)
  {
    return dateTimeFormat.format(value);
  }
  public static java.sql.Timestamp timeStamp(String value)
  {
    try
    {
      java.util.Date date = dateTimeFormat.parse(value); 
      return new java.sql.Timestamp(date.getTime());
    } catch (ParseException e)
    {
      return new java.sql.Timestamp(0);
    }
  }
  public static String dateTime(java.sql.Timestamp value)
  {
    return dateTimeFormat.format(value);
  }
  public static java.sql.Timestamp dateTime(String value)
  {
    try
    {
      java.util.Date date = dateTimeFormat.parse(value); 
      return new java.sql.Timestamp(date.getTime());
    } catch (ParseException e)
    {
      return new java.sql.Timestamp(0);
    }
  }
  public static String date(java.sql.Date value)
  {
    return dateFormat.format(value);
  }
  public static java.sql.Date date(String value)
  {
    try
    {
      java.util.Date date = dateFormat.parse(value); 
      return new java.sql.Date(date.getTime());
    } catch (ParseException e)
    {
      return new java.sql.Date(0);
    }
  }
  public static String time(java.sql.Time value)
  {
    return timeFormat.format(value);
  }
  public static java.sql.Time time(String value)
  {
    try
    {
      java.util.Date date = timeFormat.parse(value); 
      return new java.sql.Time(date.getTime());
    } catch (ParseException e)
    {
      return new java.sql.Time(0);
    }
  }
}
