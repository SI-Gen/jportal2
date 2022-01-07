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
import java.nio.file.Paths;

public interface ITemplateBasedPostProcessor extends IPostProcessor, ITemplateBasedGenerator {

    //Location of the generator templates for the FreeMarkerGenerator
    static final String POST_PROCESSOR_TEMPLATE_LOCATION = "/postprocessor_templates";

    public default Path getPostProcessorTemplateFilesLocation() {
        return Paths.get(ITemplateBasedPostProcessor.POST_PROCESSOR_TEMPLATE_LOCATION);
    }
}
