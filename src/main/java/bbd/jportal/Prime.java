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

import java.util.Date;

public class Prime
{
  public static void main(String args[])
  {
    int Primes[] = new int[100001];
    int DivIndex;
    int Index;
    int NoPrimes;
    Date d1, d2;

    Index = 3;
    NoPrimes = 0;
    Primes[0] = 3;

    d1 = new Date();
    for(;NoPrimes < 100000;)
    {
      for(DivIndex = 0; DivIndex <= NoPrimes; DivIndex++)
      {
        if (Primes[DivIndex] > Index/2)
        {
          NoPrimes++;
          Primes[NoPrimes] = Index;
          if(NoPrimes % 1000 == 0)
          {
             d2 = new Date();
             System.out.println(""+ NoPrimes + "th Prime : " + Primes[NoPrimes] + " Time : " + (d2.getTime() - d1.getTime()));
          }
          break;
        }
        if (Index % Primes[DivIndex] == 0)
          break;
      }
      Index += 2;
    }
    d2 = new Date();
    System.out.println(""+ NoPrimes + "th Prime : " + Primes[NoPrimes] + " Time : " + (d2.getTime() - d1.getTime()));
  }
}

