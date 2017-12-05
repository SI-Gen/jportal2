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
import java.util.Vector;
/**
 * @author vince
 */
class PlaceHolderPairs implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public Field field;
  public int pos;
  PlaceHolderPairs(Field field, int pos)
  {
    this.field = field;
    this.pos = pos;
  }
}
public class PlaceHolder implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public static final byte COLON=0, QUESTION=1, AT=2, CURLY=3, AT_NAMED=4, PERC_STRING=5, DOLLAR_NO=6;
  private Proc proc;
  public Vector<PlaceHolderPairs> pairs;
  private StringBuffer command, upper;
  private int questionsSeen;
  private String varPrefix;
  public int totalDynamicSize;
  public PlaceHolder(Proc proc, byte useMark, String varPrefix)
  {
    this.proc = proc;
    this.varPrefix = varPrefix;
    totalDynamicSize = 0;
    makeCommand();
    makePairs();
    switch (useMark)
    {
    case QUESTION:
      makeQuestionMarks();
      break;
    case AT:
      makeAtMarks();
      break;
    case CURLY:
      makeCurlyMarks();
      break;
    case AT_NAMED:
      makeAtNamed();
      break;
    case PERC_STRING:
      makePercString();
      break;
    case DOLLAR_NO:
      makeDollarNo();
      break;
  }
  }
	private static final String BEGIN = "\uFFBB", END = "\uFFEE";
  public Vector<String> getLines()
  {
    Vector<String> result = new Vector<String>();
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
  private void makeCommand()
  {
    command = new StringBuffer();
		for (int i = 0; i < proc.lines.size(); i++)
		{
			Line l = (Line) proc.lines.elementAt(i);
      if (l.isVar)
      {
        command.append(varPrefix);
        command.append(l.line);
        totalDynamicSize += proc.getDynamicSize(l.line);
      }
      else
      {
        command.append(BEGIN);
        command.append(question(new StringBuffer(l.line)));
        command.append(END);
      }
		}
  }
  private void makePairs()
  {
		pairs = new Vector<PlaceHolderPairs>();
		upper = new StringBuffer(command.toString().toUpperCase());
		for (int i=0; i<proc.inputs.size();)
		{
			Field field = (Field) proc.inputs.elementAt(i);
			int pos = colonIndexOf(field);
			if (pos != -1)
			{
				int j = 0;
				while(j < pairs.size())
				{
					PlaceHolderPairs jPair = (PlaceHolderPairs) pairs.elementAt(j);
					if (jPair.pos > pos)
						break;
					else
						j++;
				}
				pairs.insertElementAt(new PlaceHolderPairs(field, pos), j);
			}
			else
				i++;
		}
  }
  private void makeQuestionMarks()
  {
		for (int i=pairs.size()-1; i>=0; i--)
		{
			PlaceHolderPairs pair = (PlaceHolderPairs) pairs.elementAt(i);
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
  private void makeAtMarks()
  {
    for (int i=pairs.size()-1; i>=0; i--)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs) pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "@P"+i;
        delete(command, pos, field.name.length()+1);
        command.insert(pos, parm);
      }
    }
  }
  private void makeAtNamed()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)pairs.elementAt(i);
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
    for (int i=pairs.size()-1; i>=0; i--)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs) pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String curly = "{" + i + "}";
        delete(command, pos, field.name.length()+1);
        command.insert(pos, curly);
      }
    }
  }
  private void makePercString()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)pairs.elementAt(i);
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
  private void makeDollarNo()
  {
    for (int i = pairs.size() - 1; i >= 0; i--)
    {
      PlaceHolderPairs pair = (PlaceHolderPairs)pairs.elementAt(i);
      Field field = pair.field;
      int pos = pair.pos;
      if (pos != -1)
      {
        String parm = "$" + (i+1);
        delete(command, pos, field.name.length() + 1);
        command.insert(pos, parm);
      }
    }
  }
  static private void delete(StringBuffer buffer, int from, int size)
  {
    int len = buffer.length()-size;
    for (int i=from; i<len; i++)
      buffer.setCharAt(i, buffer.charAt(i+size));
    buffer.setLength(len);
  }
  static private String substring(StringBuffer buffer, int from, int to, char ch)
  {
    int len = to-from+2;
    char work[] = new char[len];
    work[0] = ch;
    buffer.getChars(from, to, work, 1);
    work[len-1] = ch;
    return new String(work);
  }
  static private int indexOf(StringBuffer buffer, String key, int offset)
  {
    for (int i=offset; i<buffer.length(); i++)
    {
      int pos = i;
      for (int j=0; j<key.length(); j++)
      {
        if (buffer.charAt(i+j) != key.charAt(j))
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
        Field field = (Field) proc.inputs.elementAt(questionsSeen++);
        if (field.type == Field.IDENTITY && proc.isInsert)
          field = (Field) proc.inputs.elementAt(questionsSeen++);
        line.insert(findPos, ":" + field.name);
        anchor = findPos + field.name.length() + 1;
      }
      else
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
    String holder = ":"+field.name.toUpperCase();
    int pos, len = holder.length();
    while(true)
    {
      pos = indexOf(upper, holder, anchor);
      if (pos == -1)
        break;
      char ch = upper.charAt(pos+len);
      if ((ch >= 'A' && ch <= 'Z') 
      ||  (ch >= '0' && ch <= '9')
      ||  (ch == '_'))
      {
      	anchor = pos+1;
      	continue;
      }
      for (int i=0; i<len; i++)
        upper.setCharAt(pos+i, '?');
      return pos;
		}
    return pos;
  }
}
