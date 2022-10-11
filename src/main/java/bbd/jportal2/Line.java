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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Lines of SQL Code */
public class Line implements Serializable
{
  private static final long serialVersionUID = 1L;
  private ArrayList<ISQLProcToken> line;
  public boolean isVar() {
    return isVar;
  }
  public JPortalTemplateOutputOptions options;

  public boolean isVar;
  /** Constructs line needed to be enclosed in double quotes */
  public Line(JPortalTemplateOutputOptions options, ISQLProcToken... args)
  {
    line = new ArrayList<>();
    this.options = options;
    Collections.addAll(line, args);
    isVar = false;
  }

  public Line(JPortalTemplateOutputOptions options, List<ISQLProcToken> args)
  {
    this.options = options;
    line = new ArrayList<>();
    line.addAll(args);
    isVar = false;
  }
  /** Constructs line used in variable substitution */
  public Line(JPortalTemplateOutputOptions options, ISQLProcToken l, boolean t)
  {
    this.options = options;
    line = new ArrayList<>();
    line.add(l);
    isVar = t;
  }
  public void reader(DataInputStream ids) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(ids);
    line = (ArrayList<ISQLProcToken>) ois.readObject();
    isVar = ois.readBoolean();
  }
  public void writer(DataOutputStream ods) throws IOException
  {
    ObjectOutputStream oos = new ObjectOutputStream(ods);
    oos.writeObject(line);
    oos.writeBoolean(isVar);
  }
  public String getlineval()
  {
    // Convert elements to strings and concatenate them, separated by commas
    String joined = line.stream()
            .map(ISQLProcToken::getUnformattedLine)
            .collect(Collectors.joining());
    joined = joined.replaceAll("\\:{1}\\w*", "?");
    joined = joined.replace("\"", "\\\"");
    return joined;
  }

  public String getUnformattedLine()
  {
    return line.stream()
          .map(ISQLProcToken::getUnformattedLine)
          .collect(Collectors.joining());
  }

  public String getDecoratedLine()
  {
    return line.stream()
            .map(s -> s.getDecoratedLine(this.options))
            .collect(Collectors.joining());
  }

  public String getDecoratedLine(JPortalTemplateOutputOptions options)
  {
    return line.stream()
            .map(s -> s.getDecoratedLine(options))
            .collect(Collectors.joining());
  }

  public String toString() {
    //return "XXX" + getlineval();
    throw new RuntimeException();
  }

}

