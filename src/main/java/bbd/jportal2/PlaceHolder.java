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

package bbd.jportal2;
import java.io.Serializable;
import java.util.Vector;
public class PlaceHolder implements Serializable
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  public static final byte COLON = 0, QUESTION = 1, AT = 2, CURLY = 3, AT_NAMED = 4, PERC_STRING = 5, DOLLAR_NO = 6
          , COLON_NO = 7, FORMAT = 8, PYFORMAT = 9, ORACLE = 10, DB2 = 11, MSSQL = 12, POSTGRE = 13
          , MYSQL = 14, LITE3 = 15;
  private final Proc proc;
  public Vector<PlaceHolderPairs> pairs;
  private StringBuffer command, upper;
  private int questionsSeen;
  private final String varPrefix;
  public int totalDynamicSize;
  public String codeLine;
  public Limit limit;

  public PlaceHolder(Proc proc, byte useMark, String varPrefix)
  {
    this.proc = proc;
    this.varPrefix = varPrefix;
    totalDynamicSize = 0;
    limit = null;
    makeCommand();
    makePairs();
    switch (useMark)
    {
      case COLON -> makeColonNamed();
      case QUESTION -> makeQuestionMarks();
      case AT -> makeAtMarks();
      case CURLY -> makeCurlyMarks();
      case AT_NAMED -> makeAtNamed();
      case PERC_STRING -> makePercString();
      case DOLLAR_NO -> makeDollarNo();
      case COLON_NO -> makeColonNo();
      case FORMAT -> makeFormat(proc.hasReturning);
      case PYFORMAT -> makePyFormat(proc.hasReturning);
    }
  }
  private static final String BEGIN = "\uFFBB", END = "\uFFEE";
  public Vector<String> getLines()
  {
    Vector<String> result = new Vector<>();
    int anchor = 0, beg, end;
    beg = indexOf(command, BEGIN, anchor);
    if (beg > 0)
    {
      String start = substring(command, 0, beg, ' ');
      result.addElement(start);
    }
    end = beg;
    while (beg != -1)
    {
      end = indexOf(command, END, anchor);
      if (end < beg)
        break;
      anchor = end + 1;
      String line = substring(command, beg + 1, end, '"');
      result.addElement(line);
      beg = indexOf(command, BEGIN, anchor);
      if (beg - end > 1)
      {
        line = substring(command, end + 1, beg, ' ');
        result.addElement(line);
      }
    }
    if (end + 1 < command.length())
    {
      String tail = substring(command, end + 1, command.length(), ' ');
      result.addElement(tail);
    }
    return result;
  }
  public Vector<PlaceHolderPairs> getPairs()
  {
    return pairs;
  }
  public int getTotalDynamicSize()
  {
    return totalDynamicSize;
  }
  private int checkFetch(Proc proc, int index)
  {
    Line l = proc.lines.elementAt(index);
    String line = l.line;
    String work = line.trim().toUpperCase();
    String[] parts;
    codeLine = line;
    int state;
    if (work.contains("FETCH"))
    {
      parts = line.split("\s+");
      codeLine = "";
      state = 0;
      for (int i=0; i < parts.length; i++)
      {
        String part = parts[i];
        switch (state)
        {
          case 0:
            if (part.equalsIgnoreCase("FETCH"))
            {
              limit = new Limit();
              if (parts.length == 2)
              {
                Line l2 = proc.lines.elementAt(index+1);
                if (l2.isVar)
                {
                  limit.variable = l2.line;
                  limit.size = proc.getDynamicSize(limit.variable);
                  return 2;
                }
                state = 1;
                continue;
              }
              if (parts.length - i >= 5)
              {
                String count = parts[i+2];
                if (count.charAt(0) == ':')
                {
                  limit.count = 1;
                  return 0;
                }
                limit.count = Integer.valueOf(count);
                return 0;
              }
            }
            codeLine += part;
            codeLine += " ";
            break;
          case 1:
            limit.count = Integer.valueOf(part);
            return 0;
        }
      }
    }
    else if (work.contains("TOP "))
    {
      parts = line.split("\s+");
      codeLine = "";
      state = 0;
      for (int i=0; i < parts.length; i++)
      {
        String part = parts[i];
        switch (state)
        {
          case 0:
            if (part.equalsIgnoreCase("TOP"))
            {
              limit = new Limit();
              if (parts.length == 1)
              {
                Line l2 = proc.lines.elementAt(index+1);
                if (l2.isVar)
                {
                  limit.variable = l2.line;
                  limit.size = proc.getDynamicSize(limit.variable);
                  return 1;
                }
              }
              state = 1;
              continue;
            }
            codeLine += part;
            codeLine += " ";
            break;
          case 1:
            state = 2;
            limit.count = Integer.valueOf(part);
            break;
          case 2:
            codeLine += part;
            codeLine += " ";
            break;
        }
      }
    }
    return 0;
  }

  private void makeCommand()
  {
    command = new StringBuffer();
    for (int i = 0; i < proc.lines.size(); i++)
    {
      Line l = proc.lines.elementAt(i);
      String line = l.line;
      if (l.isVar)
      {
        command.append(varPrefix);
        command.append(line);
        totalDynamicSize += proc.getDynamicSize(line);
      }
      else
      {
          int incr = checkFetch(proc, i);
          if (incr >= 0)
          {
            i += incr; // 0 fetch was one line 2 fetch was over 3 lines
            command.append(BEGIN);
            command.append(question(new StringBuffer(codeLine)));
            command.append(END);
          }
        }
      }
    }
  }

  private void makePairs()
  {
    pairs = new Vector<>();
    upper = new StringBuffer(command.toString().toUpperCase());
    for (int i = 0; i < proc.inputs.size(); )
    {
      Field field = proc.inputs.elementAt(i);
      int pos = colonIndexOf(field);
      if (pos != -1)
      {
        int j = 0;
        while (j < pairs.size())
        {
          PlaceHolderPairs jPair = pairs.elementAt(j);
          if (jPair.pos > pos)
            break;
          else
            j++;
        }
        pairs.insertElementAt(new PlaceHolderPairs(field, pos), j);
      } else
        i++;
    }
  }
  private void makeColonNamed()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = ":" + field.name;
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }

  private void makeQuestionMarks()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "?";
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }
  private void makeFormat(boolean returning)
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = returning ? "%%s" : "%s";
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }

  private void makeAtMarks()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "@P" + i;
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }
  private void makeAtNamed()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "@" + field.name;
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }
  private void makeCurlyMarks()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String curly = "{" + i + "}";
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, curly);
      }
    }
  }
  private void makePercString()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "%(" + field.name + ")s";
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }
  private void makePyFormat(boolean returning)
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm;
        if (returning)
          parm = "%%(" + field.name + ")s";
        else
          parm = "%(" + field.name + ")s";
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }

  private void makeDollarNo()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "$" + (i + 1);
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }
  private void makeColonNo()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = ":" + (i + 1);
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }

  static private void delete(StringBuffer buffer, int from, int size)
  {
    int len = buffer.length() - size;
    for (int i = from; i < len; i++)
      buffer.setCharAt(i, buffer.charAt(i + size));
    buffer.setLength(len);
  }
  static private String substring(StringBuffer buffer, int from, int to, char ch)
  {
    int len = to - from + 2;
    char[] work = new char[len];
    work[0] = ch;
    buffer.getChars(from, to, work, 1);
    work[len - 1] = ch;
    return new String(work);
  }
  static private int indexOf(StringBuffer buffer, String key, int offset)
  {
    for (int i = offset; i < buffer.length(); i++)
    {
      int pos = i;
      for (int j = 0; j < key.length(); j++)
      {
        if (buffer.charAt(i + j) != key.charAt(j))
        {
          pos = -1;
          break;
        }
      }
      if (pos != -1)
        return pos;
    }
    return -1;
  }
  private String question(StringBuffer line)
  {
    int anchor = 0;
    int findPos, startQuote, endQuote;
    while ((findPos = indexOf(line, "?", anchor)) >= 0)
    {
      startQuote = indexOf(line, "'", anchor);
      if (startQuote >= 0)
      {
        endQuote = indexOf(line, "'", startQuote + 1);
        if (findPos > startQuote && findPos < endQuote)
        {
          anchor = endQuote + 1;
          continue;
        }
      }
      delete(line, findPos, 1);
      if (findPos < line.length() && line.charAt(findPos) == '?')
      {
        anchor = findPos + 1;
        continue;
      }
      if (questionsSeen < proc.inputs.size())
      {
        Field field = proc.inputs.elementAt(questionsSeen++);
        if (field.type == Field.IDENTITY && proc.isInsert)
          field = proc.inputs.elementAt(questionsSeen++);
        line.insert(findPos, ":" + field.name);
        anchor = findPos + field.name.length() + 1;
      } else
      {
        line.insert(findPos, ":<UNKNOWN(" + questionsSeen + ")>");
        anchor = findPos + 12;
      }
    }
    return line.toString();
  }
  private int colonIndexOf(Field field)
  {
    int anchor = 0;
    String holder = ":" + field.name.toUpperCase();
    int pos, len = holder.length();
    while (true)
    {
      pos = indexOf(upper, holder, anchor);
      if (pos == -1)
        break;
      char ch = upper.charAt(pos + len);
      if ((ch >= 'A' && ch <= 'Z')
          || (ch >= '0' && ch <= '9')
          || (ch == '_'))
      {
        anchor = pos + 1;
        continue;
      }
      for (int i = 0; i < len; i++)
        upper.setCharAt(pos + i, '?');
      return pos;
    }
    return pos;
  }
}
