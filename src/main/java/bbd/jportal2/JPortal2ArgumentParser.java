package bbd.jportal2;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

public class JPortal2ArgumentParser {

    private static final Logger logger = LoggerFactory.getLogger(JPortal2ArgumentParser.class);

    private static void printListOfBuiltinGenerators() {
        System.out.println("The following Builtin Generators are available (use with --builtin-generator <name>:<outputdir>:");
        BuiltInGeneratorHelpers helper = new BuiltInGeneratorHelpers();
        Vector<String> generators = helper.findAllBuiltInGeneratorsOfType(IBuiltInSIProcessor.class);
        System.out.println("          SI Generators:");
        for (String generator : generators) {
            System.out.println("                        "+ generator);
        }

        generators = helper.findAllBuiltInGeneratorsOfType(IBuiltInPostProcessor.class);
        System.out.println("The following Builtin Post Processors are available (use with --builtin-postprocessor <name>:<outputdir>:");
        for (String generator : generators) {
            System.out.println("                        "+ generator);
        }

    }

    public static JPortal2Arguments parse(String[] args, JPortal2Arguments arguments) {

        try
        {
            JCommander jCommander = JCommander.newBuilder()
                    .addObject(arguments)
                    .build();

            jCommander.setProgramName("JPortal2");
            jCommander.parse(args);

            if (arguments.isHelp() || args.length == 0) {
                jCommander.usage();
                printListOfBuiltinGenerators();
                return null;
            }

            return arguments;
        }
        catch (ParameterException exc)
        {
            logger.error("Error", exc);
            exc.getJCommander().usage();
            return null;
        }
    }

    public static JPortal2Arguments parse(String[] args) {
        JPortal2Arguments jpArgs = new JPortal2Arguments();
        return parse(args, jpArgs);
    }

}
