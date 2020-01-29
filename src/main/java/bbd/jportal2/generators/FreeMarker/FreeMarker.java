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

package bbd.jportal2.generators.FreeMarker;

import bbd.jportal2.*;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class FreeMarker extends BaseGenerator implements ITemplateBasedSIProcessor, ITemplateBasedPostProcessor {

    public FreeMarker() {
        super(FreeMarker.class);
    }


    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public String description() {
        return "Generate according to a given FreeMarker template.";
    }

    public String documentation() {
        return "Generate according to a given FreeMarker template. Usage is TODO";
    }

    public Vector<Flag> getFlags() {
        return new Vector<>();
    }

    /**
     * Generates code using a given FreeMarker template
     */
    //public static void generateTemplate(Database database, Map<String,String> parameters, File outputDirectory) throws IOException, TemplateException
    public void generateTemplate(Database database, String templateBaseDir, String generatorName, File outputDirectory) throws Exception {

        //Set the generator name correctly in the generatedOutputFiles member
        this.getGeneratedOutputFiles().setGeneratorName(generatorName);

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.ftl");
        Path fullGeneratorPath = Paths.get(templateBaseDir, generatorName);
        //Path fullGeneratorPath = findTemplateDirectory(templateBaseDir, generatorName);
        Files.walkFileTree(Paths.get(fullGeneratorPath.toString()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (matcher.matches(file)) {
                    //Subtract source directory from found path
                    Path pathToTemplateBaseDir = Paths.get(templateBaseDir);
                    Path relativePathToFTL = fullGeneratorPath.relativize(file);
                    try {
                        GenerateSingleFTLFile(fullGeneratorPath.toString(), generatorName, relativePathToFTL.toString(), outputDirectory, database);
                    } catch (Exception te) {
                        throw new RuntimeException(te);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
            //Empty string

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

    }

    private Configuration configure(File templateDir) throws IOException {
        // Create your Configuration instance, and specify if up to what FreeMarker
        // version (here 2.3.25) do you want to apply the fixes that are not 100%
        // backward-compatible. See the Configuration JavaDoc for details.
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);


        // Specify the source where the template files come from. We allow use of the built-in
        //templates (inside of src/main/resources/si_processor_templates/ and src/main/resources/postprocessor_templates/
        //as well as the location specified by the user
        FileTemplateLoader ftl1 = new FileTemplateLoader(templateDir);
        FileTemplateLoader ftl2 = new FileTemplateLoader(new File(Paths.get("").toAbsolutePath().toString()));
        ClassTemplateLoader ctl1 = new ClassTemplateLoader(FreeMarker.class, this.getSIProcessorTemplateFilesLocation().toString());
        ClassTemplateLoader ctl2 = new ClassTemplateLoader(FreeMarker.class, this.getPostProcessorTemplateFilesLocation().toString());
        MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[]{ftl1, ctl1, ctl2, ftl2});
        cfg.setTemplateLoader(mtl);


        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
        cfg.setLogTemplateExceptions(false);
        return cfg;
    }

    private void GenerateSingleFTLFile(String templateBaseDir, String generatorName, String templateName, File outputDir, Database database) throws TemplateException, IOException, ClassNotFoundException {

        Configuration cfg = configure(new File(templateBaseDir));


        //Set up FreeMarker object maps
        java.util.Map<String, Object> root = new HashMap<>();
        root.put("database", database);
        root.put("table", database.tables.get(0));

        // Create the builder:
        //BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_23);
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28);
        // Set desired BeansWrapper configuration properties:
        builder.setUseModelCache(true);
        builder.setExposeFields(true);
        builder.setExposureLevel(BeansWrapper.EXPOSE_ALL);
        BeansWrapper wrapper = builder.build();

        //This is a bit crappy of FreeMarker. It doesn't expose static and enum members of classes,
        //so we need to manually expose them. To access, use such as:
        //${STATICS.Field.BLOB} to access the static member BLOB, defined in the Field class.
        //${ENUMS.Field.BLOB} to access the static member BLOB, defined in the Field class.

        //Expose static variables
        root.put("STATICS", new HashMap<String, TemplateModel>());
        TemplateHashModel staticModels = wrapper.getStaticModels();
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("Database", staticModels.get("bbd.jportal2.Database"));
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("Table", staticModels.get("bbd.jportal2.Table"));
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("Field", staticModels.get("bbd.jportal2.Field"));
        ((HashMap<String, TemplateModel>) root.get("STATICS")).put("PlaceHolder", staticModels.get("bbd.jportal2.PlaceHolder"));


        //Expose enums
        root.put("ENUMS", new HashMap<String, TemplateModel>());
        TemplateHashModel enumModels = wrapper.getEnumModels();
        ((HashMap<String, TemplateModel>) root.get("ENUMS")).put("Database", enumModels.get("bbd.jportal2.Database"));
        ((HashMap<String, TemplateModel>) root.get("ENUMS")).put("Table", enumModels.get("bbd.jportal2.Table"));
        ((HashMap<String, TemplateModel>) root.get("ENUMS")).put("Field", enumModels.get("bbd.jportal2.Field"));
        ((HashMap<String, Object>) root.get("ENUMS")).put("PlaceHolder", enumModels.get("bbd.jportal2.PlaceHolder"));


        String destFileName;
        Path templateRelativePath = Paths.get(templateName);
        String strRelativePath = templateRelativePath.toString();

        HashSet<String> doneFiles = new HashSet<>();

        if (database.tables.get(0).procs.isEmpty()) {
            logger.warn("\t [{}]: Table: [{}] Has no procs defined. Skipping...", generatorName, database.tables.get(0).name);
            return;
        }

        for (Proc proc : database.tables.get(0).procs)
        {
            root.put("proc", proc);
            if (strRelativePath.endsWith(".ftl"))
                strRelativePath = strRelativePath.substring(0, strRelativePath.length() - 4);

            destFileName = strRelativePath;

            Template fileNameTemplate = new Template("fileNameTemplate_" + templateRelativePath.toString(), new StringReader(destFileName), cfg);
            Writer fileNameOut = new StringWriter();
            fileNameTemplate.process(root, fileNameOut);

            destFileName = fileNameOut.toString();

            if (doneFiles.contains(destFileName))
                continue; //File template is less specific than proc/table level, already generated.

            Path fullDestinationFile = Paths.get(outputDir.toString(), destFileName);

            fullDestinationFile.getParent().toFile().mkdirs();

            Template temp = cfg.getTemplate(templateName);
            logger.info("\t [{}]: Generating [{}]", generatorName, fullDestinationFile.toString());
            doneFiles.add(destFileName);
            try (PrintWriter outData = openOutputFileForGeneration(generatorName, fullDestinationFile.toString())) {
                temp.process(root, outData);
            }

        }

    }


    //Find directories in a JAR on on the disk
    private Path findTemplateDirectory(String templateBaseDir, String generatorName) throws Exception {

        Path templateFilesLocation = Paths.get(templateBaseDir, generatorName);
        if (Files.exists(templateFilesLocation))
            return templateFilesLocation;

        //On windows, we need to convert the windows path to URL format
        URI uri = getClass().getResource(templateFilesLocation.toString().replace(File.separator, "/")).toURI();
        Path template_generatorsPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            template_generatorsPath = fileSystem.getPath(templateFilesLocation.toString());
        } else {
            template_generatorsPath = Paths.get(uri);
        }
        return template_generatorsPath;
    }
}
