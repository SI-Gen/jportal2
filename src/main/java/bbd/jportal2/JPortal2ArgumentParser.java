package bbd.jportal2;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPortal2ArgumentParser {

    private static final Logger logger = LoggerFactory.getLogger(JPortal2ArgumentParser.class);

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
        return parse(args, new JPortal2Arguments());
    }

}
