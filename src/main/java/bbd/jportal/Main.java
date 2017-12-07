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

package bbd.jportal;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;

import bbd.jportal.generators.FreeMarker;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.JCommander;

import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Parameters(separators = "=")
public class Main
{
    //===============================================================================================
    //Command-line parameters
    @Parameter(names = { "--log", "-l"}, description = "Logfile name i.e. --log=jportal.log")
    private String logFileName;// = "jportal2.log";
  
    @Parameter(names = { "--nubdir", "-n"}, description = "Nubdir")
    private String nubDir = "";
  
    @Parameter(names = { "--inputdir", "-d"}, description = "Input dir")
    private String inputDir;// = "";

    @Parameter(description = "InputFiles")
    private List<String> inputFiles = new ArrayList<>();

    @Parameter(names = { "--generator", "-o"}, description = "Generator to run (format is generator_name:dest_dir i.e. --generator=CSNetCode:./cs")
    private List<String> generators = new ArrayList<>();
        
    @Parameter(names = { "--flag", "-F"}, description = "Flags to pass to the generator")
    private List<String> flags = new ArrayList<>();

    @Parameter(names = { "--help", "-h" }, help = true)
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
            //If inputdir is specified, add its contents to inputFiles list
            if (main.inputDir != null && main.inputDir.isEmpty())
            {
                File folder = new File(main.inputDir);
                File[] listOfFiles = folder.listFiles();
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        Path path = Paths.get( main.inputDir,file.getName());
                        main.inputFiles.add(path.toString());
                    }
                }
            }


            for (String filename : main.inputFiles)
            {
                logger.info("Generating for: " + filename);
                int rc = main.compile(filename);
                if (rc != 0)
                    System.exit(2);
            }
            System.exit(0);
        } catch (Exception e)
        {
            logger.error("General Exception caught", e);        
            System.exit(3);
        }
    }    






    public int compile(String source)
            throws FileNotFoundException, ClassNotFoundException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        String[] pieces = source.split("\\+");
        Database database = null;
        boolean hasErrors = false;
        for (int p = 0; p < pieces.length; p++)
        {
            Database db = JPortal.run(pieces[p], nubDir);
            if (db == null)
            {
                logger.error("::>" + pieces[p] + "<:: compile has errors");
                hasErrors = true;
                continue;
            }
            if (database == null)
                database = db;
            else
            {
                for (int t = 0; t < db.tables.size(); t++)
                    database.tables.addElement(db.tables.elementAt(t));
                for (int s = 0; s < db.sequences.size(); s++)
                    database.sequences.addElement(db.sequences.elementAt(s));
                for (int v = 0; v < db.views.size(); v++)
                    database.views.addElement(db.views.elementAt(v));
            }
        }
        if (hasErrors == true)
            return 1;
        
        for (int i = 0; i < flags.size(); i++)
        {
                String flag = flags.get(i);
                database.flags.addElement(flag);
        }        

        for (String generator :  generators)
        {
            String generatorName = generator.split(":")[0];
            String generatorDirectory = generator.split(":")[1];

            File theDir = new File(generatorDirectory);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                logger.info("creating directory: " + theDir.getName());
                boolean result = false;

                try{
                    theDir.mkdirs();
                    result = true;
                }
                catch(SecurityException se){
                    //handle it
                    logger.error("A Security Exception occurred:",se);
                }
            }
            char term = File.separatorChar;
            char ch = generatorDirectory.charAt(generatorDirectory.length() - 1);
            if (ch != term)
                generatorDirectory = generatorDirectory + term;


            logger.info("Executing: " + generatorName);
            //try {
                Class<?> c = Class.forName("bbd.jportal.generators." + generatorName);
//TODO: Support for FreeMarker template
                //            }
//            catch (ClassNotFoundException cnf)
//            {
//                //Assume it's a freemarker template
//                File file = new File(generatorName);
//                if (file.isFile()) {
//                    Configuration cfg = FreeMarker.configure(templateDir);
//                    FreeMarker.generateAdvanced(database, );
//                    Path path = Paths.get( main.inputDir,file.getName());
//                    main.inputFiles.add(path.toString());
//                }
//            }
            Class<?> d[] = { database.getClass(), generatorDirectory.getClass() };
            Method m = c.getMethod("generate", d);
            Object o[] = { database, generatorDirectory};
            m.invoke(database, o);                    
        }

    
        return 0;
    }

    // private static String[] frontSwitches(String[] args) throws IOException
    // {
    //     String log = "";
    //     int i = 0;
    //     while (true)
    //     {
    //         if (args.length > i && args[i].equals("-l"))
    //         {
    //             if (i + 1 < args.length)
    //             {
    //                 log = args[++i];
    //                 OutputStream outFile = new FileOutputStream(log);
    //                 outLog.flush();
    //                 outLog = new PrintWriter(outFile);
    //             }
    //             i++;
    //             continue;
    //         }
    //         if (args.length > i && args[i].equals("-n"))
    //         {
    //             if (i + 1 < args.length)
    //                 nubDir = args[++i];
    //             i++;
    //             continue;
    //         }
    //         if (args.length > i && args[i].equals("-d"))
    //         {
    //             if (i + 1 < args.length) {
    //                 String dirName = args[++i];
    //                 File folder = new File(dirName);
    //                 File[] listOfFiles = folder.listFiles();
    //                 for (File file : listOfFiles) {
    //                     if (file.isFile()) {
    //                         Path path = Paths.get( dirName,file.getName());
    //                         inputs = inputs + path.toString() + ";";
    //                     }
    //                 }
    //             }
    //             i++;
    //             continue;
    //         }

    //         if (args.length > i && args[i].equals("-f"))
    //         {
    //             if (i + 1 < args.length)
    //             {
    //                 String fileName = args[++i];
    //                 FileReader fileReader = new FileReader(fileName);
    //                 bufferedReader = new BufferedReader(fileReader);
    //                 try
    //                 {
    //                     String semicolon = inputs.length() > 0 ? ";" : "";
    //                     while (bufferedReader.ready())
    //                     {
    //                         String line = bufferedReader.readLine();
    //                         inputs = inputs + semicolon + line;
    //                         semicolon = ";";
    //                     }
    //                 } catch (NullPointerException e2)
    //                 {
    //                 }
    //             }
    //             i++;
    //             continue;
    //         }
    //         break;
    //     }
    //     if (args.length > i && inputs.length() == 0)
    //     {
    //         inputs = args[i];
    //         i++;
    //     }
    //     String[] newargs = new String[args.length - i];
    //     System.arraycopy(args, i, newargs, 0, newargs.length);
    //     return newargs;
    // }

//    private static String abbreviate(List<String> sources)
//    {
//        if (sources.size() > 5)
//            return sources.get(0) + " ... " + sources.get(sources.size() - 1);
//        return sources;
//    }


}
