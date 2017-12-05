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

import java.io.PrintWriter;
import java.util.Map;
import java.util.Vector;

public abstract class AdvancedGenerator
{
  public static void generateAdvanced(Database database, Map<String,String> parameters, String output, PrintWriter outLog)
  {
  }
  public static String description()
  {
    return "This description should be implemented in the generator";
  }
  public static String documentation()
  {
    return "This documentation should be implemented in the generator";
  }
  public static Vector<?> flags()
  {
    return null;
  }
  public static boolean toBoolean(Object value) 
  {
    String s = value.toString();
	  return s.toLowerCase().equals("true");
  }
}
