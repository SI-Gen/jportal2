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

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Vector;

public interface ITemplateBasedGenerator extends IGenerator {
    void generateTemplate(Database database, Table table, String templateBaseDir, String generatorName, File outputDirectory) throws Exception;
}
