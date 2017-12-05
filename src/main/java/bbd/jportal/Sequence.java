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

public class Sequence implements Serializable
{
  private static final long serialVersionUID = 9000568162957080666L;
  public String  name;

  public String getName() {
    return name;
  }

  public long getMinValue() {
    return minValue;
  }

  public long getMaxValue() {
    return maxValue;
  }

  public int getIncrement() {
    return increment;
  }

  public boolean isCycleFlag() {
    return cycleFlag;
  }

  public boolean isOrderFlag() {
    return orderFlag;
  }

  public long getStartWith() {
    return startWith;
  }

  public long    minValue;
  public long    maxValue;
  public int     increment;
  public boolean cycleFlag;
  public boolean orderFlag;
  public long    startWith;
  /** Code starts at line */
  public int     start;
  public Sequence()
  {
    name        = "";
    minValue    = 1;
    maxValue    = 999999999L;
    increment   = 1;
    cycleFlag   = true;
    orderFlag   = true;
    startWith   = 1;
    start       = 0;
  }
}

