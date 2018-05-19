package bbd.jportal2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class GeneratedFiles {
    public GeneratedFiles(String generatorName) {
        this.generatorName = generatorName;
    }

    private String generatorName;
    private Collection<GeneratedFileGroup> fileGroups = new ArrayList<>();

    public String getGeneratorName() {
        return generatorName;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public Collection<GeneratedFileGroup> getFileGroups() {
        return fileGroups;
    }

    public void setFileGroups(Collection<GeneratedFileGroup> fileGroups) {
        this.fileGroups = fileGroups;
    }
}