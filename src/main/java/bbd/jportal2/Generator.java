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

import java.nio.file.Path;
import java.util.Vector;

public interface Generator {
    void generate(Database database, String output) throws Exception;

    String description();

    String documentation();

    default Vector<Flag> flags() {
        return new Vector<>();
    }

    default Boolean toBoolean(Object value) {
        String s = value.toString();
        return s.toLowerCase().equals("true");
    }
}
