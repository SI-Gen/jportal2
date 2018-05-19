package bbd.jportal2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class GeneratedFileGroup {
    public GeneratedFileGroup(String fileGroupName, ArrayList<Path> files) {
        this.fileGroupName = fileGroupName;
        Files.addAll(files);
    }

    public GeneratedFileGroup(String newFileGroupName) {
        this.fileGroupName = newFileGroupName;
    }

    private String fileGroupName;
    private Collection<Path> Files = new ArrayList<>();

    public Collection<Path> getFiles() {
        return Files;
    }

    public String getFileGroupName() {
        return fileGroupName;
    }

    public void setFileGroupName(String fileGroupName) {
        this.fileGroupName = fileGroupName;
    }
}
