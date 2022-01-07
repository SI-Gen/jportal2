package bbd.jportal2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MultiFileCompiler extends BaseFileCompiler {
    public final Logger logger = LoggerFactory.getLogger(MultiFileCompiler.class);

    public Logger getLogger() {
        return this.logger;
    }
    protected int runAllGenerators(List<String> builtinSIProcessors, List<String> templateBasedSIProcessors, List<String> builtinPostProcessors, List<String> templateBasedPostProcessors, Database database) throws Exception {
        for (String generator : builtinSIProcessors) {
            if (!ExecuteBuiltinGenerator(database, generator)) return 1;
        }

        for (Table table : database.tables) {
            for (String templateGenerator : templateBasedSIProcessors) {
                if (!ExecuteTemplateGenerator(database, table, templateGenerator)) return 1;
            }
        }

        for (String generator : builtinPostProcessors) {
            if (!ExecuteBuiltinGenerator(database, generator)) return 1;
        }

        for (Table table : database.tables) {
            for (String templateGenerator : templateBasedPostProcessors) {
                if (!ExecuteTemplateGenerator(database, table, templateGenerator)) return 1;
            }
        }
        return 0;
    }
}
