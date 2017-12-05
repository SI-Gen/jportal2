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

import java.io.Serializable;

/**
 * @author vince
 */
public class Flag implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  String name;
  Object value;
  String description;
  public Flag(String name, Object value, String description)
  {
    this.name = name;
    this.value = value;
    this.description = description;
  }
}