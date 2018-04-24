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

public abstract interface TemplateBasedGenerator {
    void generateTemplate(Database database, String templateBaseDir, String generatorName, File outputDirectory) throws Exception;

    String documentation();

    String description();

    Vector<?> flags();

    Path getTemplateFilesLocation();
}
