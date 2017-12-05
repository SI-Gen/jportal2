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

public class Padder
{
  int relativeOffset;
  int fillerNo;
  void padC(int padSize, int fieldSize, PrintWriter outData)
  {
     int n = relativeOffset % padSize;
     if (n > 0)
     {
       n = padSize-n;
       outData.println("  char   FILL"+fillerNo+"["+n+"];");
       fillerNo++;
       relativeOffset += n;
     }
     relativeOffset += fieldSize;
  }
  void padVB(int padSize, int fieldSize, PrintWriter outData)
  {
    int n = relativeOffset % padSize;
    if (n > 0)
    {
      n = padSize-n;
      fillerNo++;
      outData.println("  FILL"+fillerNo+" as String * "+n);
      relativeOffset += n;
    }
    relativeOffset += fieldSize;
  }
  void incOffset(int value)
  {
    relativeOffset += value;
  }
}

