package bbd.jportal2;

import com.google.common.base.Strings;

import java.util.Objects;

public class ProjectCompilerBuilder {

    private static final String WHITE_SPACE_PATTERN = "\\s+";

    public static ProjectCompiler build(JPortal2Arguments arguments, String additionalArgumentString) {

        if (Strings.isNullOrEmpty(additionalArgumentString)) {
            return build(arguments);
        }

        return build(JPortal2ArgumentParser.parse(additionalArgumentString.split(WHITE_SPACE_PATTERN), arguments));
    }

    public static ProjectCompiler build(JPortal2Arguments allArguments) {

        Objects.requireNonNull(allArguments, "No arguments provided for JPortal2 compiler");

        ProjectCompiler pj = new ProjectCompiler();
        pj.addInputDirs(allArguments.getInputDirs());
        pj.addInputFiles(allArguments.getInputFiles());
        pj.addCompilerFlags(allArguments.getFlags());
        pj.addBuiltinSIProcessors(allArguments.getBuiltinSIProcessors());
        pj.addTemplateBasedSIProcessors(allArguments.getTemplateSIProcessors());
        pj.addTemplateLocations(allArguments.getTemplateLocations());
        pj.addBuiltinPostProcessors(allArguments.getBuiltinPostProcessors());
        pj.addTemplateBasedPostProcessors(allArguments.getTemplatePostProcessors());

        return pj;

    }
}
