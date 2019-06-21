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

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Reads input from stored repository
     */
    public static void main(String[] args) {
        JPortal2Arguments arguments = JPortal2ArgumentParser.parse(args);

        if (arguments == null)
            System.exit(0);

        if (arguments.mustDebug()) {
            ((ch.qos.logback.classic.Logger) logger).setLevel(Level.DEBUG);
        }

        //Set this before the logger starts.
        if (arguments.getLogFileName() != null){
            System.setProperty("log.name", arguments.getLogFileName());
        }

        try {
            ProjectCompiler pj = ProjectCompilerBuilder.build(arguments);

            if (Objects.nonNull(pj)) {
                int rc = pj.compileAll();
                System.exit(rc);
            } else {
                System.exit(1);
            }

        } catch (Exception e) {
            logger.error("General Exception caught", e);
            System.exit(3);
        }
    }

}
