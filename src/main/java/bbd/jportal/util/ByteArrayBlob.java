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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class ByteArrayBlob implements Blob
{
  private byte[]  buffer;
  private Blob    blob;
  private boolean freed;
  private void checkFreed() throws SQLException
  {
    if (freed == true)
      throw new SQLException("The blob has already been freed");
  }
  /**
   * @param buffer
   */
  public ByteArrayBlob(byte[] buffer)
  {
    blob = null;
    freed = false;
    this.buffer = new byte[buffer.length];
    System.arraycopy(buffer, 0, this.buffer, 0, buffer.length);
  }
  public ByteArrayBlob(Blob blob) throws SQLException
  {
    if (blob == null)
      throw new SQLException("Cannot instantiate a SerialBlob "
          + "object with a null Blob object");
    this.blob = blob;
    freed = false;
    buffer = blob.getBytes(1, (int) blob.length());
  }
  private int checkPos(long pos) throws SQLException
  {
    if (pos < 1)
      throw new SQLException("Supplied pos is less than 1");
    if (pos > buffer.length)
      throw new SQLException("Supplied pos is greater than current buffer size");
    return (int) pos - 1;
  }
  private int checkLength(long pos, long length) throws SQLException
  {
    checkPos(pos);
    int left = buffer.length - ((int) pos - 1);
    if ((int) length > left)
      return left;
    return (int) length;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#free()
   */
  //  @Override
  public void free() throws SQLException
  {
    if (freed == true)
      return;
    freed = true;
    buffer = null;
    blob = null;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#getBinaryStream()
   */
  //  @Override
  public InputStream getBinaryStream() throws SQLException
  {
    checkFreed();
    return new ByteArrayInputStream(buffer);
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#getBinaryStream(long, long)
   */
  //  @Override
  public InputStream getBinaryStream(long pos, long length) throws SQLException
  {
    checkFreed();
    int inPos = checkPos(pos);
    int len = checkLength(pos, length);
    if (pos == 1 && len == buffer.length)
      return getBinaryStream();
    byte arr[] = new byte[len];
    System.arraycopy(buffer, inPos, arr, 0, len);
    return new ByteArrayInputStream(arr);
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#getBytes(long, int)
   */
  //  @Override
  public byte[] getBytes(long pos, int length) throws SQLException
  {
    checkFreed();
    int len = checkLength(pos, length);
    byte arr[] = new byte[len];
    System.arraycopy(buffer, (int) pos - 1, arr, 0, len);
    return arr;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#length()
   */
  //  @Override
  public long length() throws SQLException
  {
    checkFreed();
    return (long) buffer.length;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#position(byte[], long)
   */
  //  @Override
  public long position(byte[] pattern, long start) throws SQLException
  {
    checkFreed();
    int blen = buffer.length;
    int plen = pattern.length;
    outer: for (int i = (int) start - 1; i < blen - plen; i++)
    {
      for (int j = 0; j < plen; j++)
      {
        if (pattern[j] != buffer[i + j])
          continue outer;
      }
      return (long) i;
    }
    return -1;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#position(java.sql.Blob, long)
   */
  //  @Override
  public long position(Blob pattern, long start) throws SQLException
  {
    byte[] pat = pattern.getBytes(1, (int) pattern.length());
    return position(pat, start);
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#setBinaryStream(long)
   */
  //  @Override
  public OutputStream setBinaryStream(long pos) throws SQLException
  {
    checkFreed();
    if (blob != null && blob.setBinaryStream(pos) != null)
      return blob.setBinaryStream(pos);
    throw new SQLException(
        "Unsupported operation. SerialBlob cannot "
            + "return a writable binary stream, unless instantiated with a Blob object "
            + "that provides a setBinaryStream() implementation");
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#setBytes(long, byte[])
   */
  //  @Override
  public int setBytes(long pos, byte[] bytes) throws SQLException
  {
    return setBytes(pos, bytes, 0, bytes.length);
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#setBytes(long, byte[], int, int)
   */
  //  @Override
  public int setBytes(long pos, byte[] bytes, int offset, int len)
      throws SQLException
  {
    checkFreed();
    int opos = checkPos(pos);
    int blen = buffer.length;
    int nlen = opos + len;
    if (nlen > blen)
    {
      byte[] buff2 = new byte[nlen];
      System.arraycopy(buffer, 0, buff2, 0, opos);
      buffer = buff2;
    }
    System.arraycopy(bytes, offset, buffer, opos, len);
    return len;
  }
  /*
   * (non-Javadoc)
   * 
   * @see java.sql.Blob#truncate(long)
   */
  //  @Override
  public void truncate(long length) throws SQLException
  {
    int blen = buffer.length;
    if (blen > length)
      throw new SQLException("Length more than what can be truncated");
    if ((int) blen == 0)
      buffer = new byte[0];
    else
      buffer = this.getBytes(1, (int) length);
  }
}
