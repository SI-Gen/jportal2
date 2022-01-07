package bbd.jportal2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SingleFileCompiler extends BaseFileCompiler {
    public final Logger logger = LoggerFactory.getLogger(SingleFileCompiler.class);

    public Logger getLogger() {
        return this.logger;
    }
    protected int runAllGenerators(List<String> builtinSIProcessors, List<String> templateBasedSIProcessors, List<String> builtinPostProcessors, List<String> templateBasedPostProcessors, Database database) throws Exception {
        for (String generator : builtinSIProcessors) {
            if (!ExecuteBuiltinGenerator(database, generator)) return 1;
        }

        for (String templateGenerator : templateBasedSIProcessors) {
            if (!ExecuteTemplateGenerator(database, JPortal.table, templateGenerator)) return 1;
        }

        for (String generator : builtinPostProcessors) {
            if (!ExecuteBuiltinGenerator(database, generator)) return 1;
        }

        for (String templateGenerator : templateBasedPostProcessors) {
            if (!ExecuteTemplateGenerator(database, JPortal.table, templateGenerator)) return 1;
        }
        return 0;
    }
}
