package net.bitbylogic.kardia.docker.file;

import java.io.File;

public enum FileConstants {

    PACKAGES_DIRECTORY(File.separatorChar + "packages", FileType.DIRECTORY),
    RESOURCES_DIRECTORY(File.separatorChar + "resources", FileType.DIRECTORY);

    private final String relativePath;
    private final FileType fileType;

    FileConstants(String relativePath, FileType fileType) {
        this.relativePath = relativePath;
        this.fileType = fileType;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public FileType getFileType() {
        return fileType;
    }

    public enum FileType {
        DIRECTORY,
        FILE
    }

}
