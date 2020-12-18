package bbd.jportal2.test;

import bbd.jportal2.ProjectCompiler;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class JavaJCCodeGeneratorTest {

    private final String SI_DIR = "src/main/resources/example_si_files/";
    private final String OUTPUT_DIR = "target/test-all-generators-basic-test-out/";
    private final String SI_PROCESSOR_TEMPLATE_DIR = "src/main/resources/si_processor_templates";

    @Test
    public void generateJavaCodeWithExtensiveEnumUtilisation() throws Exception {
        ProjectCompiler pj = new ProjectCompiler(false);
        pj.addCompilerFlags(Collections.singletonList("utilizeEnums"));
        pj.addTemplateLocation(SI_PROCESSOR_TEMPLATE_DIR);
        Path outputPath = Paths.get(OUTPUT_DIR, "JavaJCCode", "JavaJCCodeWithEnum");
        pj.addBuiltinSIProcessor("JavaJCCode:" + outputPath);
        pj.addInputDir(SI_DIR);
        pj.compileAll();
    }

    @Test
    public void generateJavaCodeWithLombokAnnotations() throws Exception {
        ProjectCompiler pj = new ProjectCompiler(false);
        pj.addCompilerFlags(Collections.singletonList("generateLombok"));
        pj.addTemplateLocation(SI_PROCESSOR_TEMPLATE_DIR);
        Path outputPath = Paths.get(OUTPUT_DIR, "JavaJCCode", "JavaJCCodeWithLombok");
        pj.addBuiltinSIProcessor("JavaJCCode:" + outputPath);
        pj.addInputDir(SI_DIR);
        pj.compileAll();
    }

}

