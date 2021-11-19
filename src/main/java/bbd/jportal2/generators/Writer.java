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

package bbd.jportal2.generators;
import java.io.PrintWriter;

public class Writer
{
  public static PrintWriter writer;
  public static String format(String fmt, Object... objects)
  {
    return String.format(fmt,  objects);
  }
  public static void write(String value)
  {
    writer.print(value);
  }
  public static void write(int no, String value)
  {
    writer.print(indent(no)+value);
  }
  public static void writeln(int no, String value)
  {
    writer.println(indent(no)+value);
  }
  public static void writeln(String value)
  {
    writeln(0, value);
  }
  public static void writeln()
  {
    writer.println();
  }
  public static String indent_string = "                                                                                             ";
  public static int indent_size = 2;
  public static String indent(int no)
  {
     int max = indent_string.length();
     int to = no * indent_size;
     if (to > max)
       to = max;
     return indent_string.substring(0,  to);
  }
  //public static PrintWriter logger;
  //public static void logln(String line)
  //{
  //  logger.println(line);
  //}
}
