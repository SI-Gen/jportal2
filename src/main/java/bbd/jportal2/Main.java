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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Parameters(separators = "=")
public class Main
{
    //===============================================================================================
    //Command-line parameters
    @Parameter(names = { "--log", "-l"}, description = "Logfile name i.e. --log=jportal2.log")
    private String logFileName = null;

    @Parameter(names = { "--inputdir", "-d"}, description = "Input dir")
    private List<String> inputDirs = new ArrayList<>();

    @Parameter(description = "InputFiles")
    private List<String> inputFiles = new ArrayList<>();

    @Parameter(names = {"--builtin-generator", "-o"}, description = "Built-In (Java-based) generatorName to run. Format is <generator_name>:<dest_dir> i.e. --builtin-generator=CSNetCode:./cs")
    private List<String> builtinSIProcessors = new ArrayList<>();

    @Parameter(names = {"--template-generator", "-t"},
            description =
                    "FreeMarker-based generator to run."
                            + "Format is <free_marker_generator_name>:<dest_dir> i.e. "
                            + "'--template-generator=MyCustomGenerator:./output'. "
                            + "The template must exist as a directory under the location specified by --template-location.")
    private List<String> templateSIProcessors = new ArrayList<>();

    @Parameter(names = {"--builtin-postprocessor", "-bpp"}, description = "Built-In (Java-based) generatorName to run. Format is <generator_name>:<dest_dir> i.e. --builtin-postprocessor=CSNetCode:./cs")
    private List<String> builtinPostProcessors = new ArrayList<>();

    @Parameter(names = {"--template-postprocessor", "-tpp"},
            description =
                    "FreeMarker-based post-processor to run."
                            + "Format is <free_marker_generator_name>:<dest_dir> i.e. "
                            + "'--template-postprocessor=MyCustomGenerator:./output'. "
                            + "The template must exist as a directory under the location specified by --template-location.")
    private List<String> templatePostProcessors = new ArrayList<>();

    @Parameter(names = {"--template-location", "-tl"}, description = "Freemarker template location. Default is <current_working_directory>/jportal2_templates")
    private List<String> templateLocations = Arrays.asList(Paths.get(System.getProperty("user.dir"), "jportal2_templates").toString());

    @Parameter(names = { "--flag", "-F"}, description = "Flags to pass to the generator")
    private List<String> flags = new ArrayList<>();

    @Parameter(names = { "--help", "-h", "-?" }, help = true)
    private boolean help;    
    //===============================================================================================  


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private BufferedReader bufferedReader;


    /**
     * Reads input from stored repository
     */
    public static void main(String args[])
    {
        Main main = new Main();

        try 
        {
            JCommander jCommander = JCommander.newBuilder()
                                        .addObject(main)
                                        .build();

            jCommander.setProgramName("JPortal2");
            jCommander.parse(args);

            if (main.help || args.length == 0) {
                jCommander.usage();
                return;
            }            
        }
        catch (ParameterException exc)
        {
            logger.error("Error", exc);
            exc.getJCommander().usage();
            System.exit(1);
        }

        //Set this before the logger starts.
        if (main.logFileName != null)
            System.setProperty("log.name", main.logFileName);

        try
        {
            ProjectCompiler pj = new ProjectCompiler();
            pj.addInputDirs(main.inputDirs);
            pj.addInputFiles(main.inputFiles);
            pj.addCompilerFlags(main.flags);
            pj.addBuiltinSIProcessors(main.builtinSIProcessors);
            pj.addTemplateBasedSIProcessors(main.templateSIProcessors);
            pj.addTemplateLocations(main.templateLocations);
            pj.addBuiltinPostProcessors(main.builtinPostProcessors);
            pj.addTemplateBasedPostProcessors(main.templatePostProcessors);
            int rc = pj.compileAll();
            System.exit(rc);
        } catch (Exception e)
        {
            logger.error("General Exception caught", e);        
            System.exit(3);
        }
    }






//    private static String abbreviate(List<String> sources)
//    {
//        if (sources.size() > 5)
//            return sources.get(0) + " ... " + sources.get(sources.size() - 1);
//        return sources;
//    }


}
