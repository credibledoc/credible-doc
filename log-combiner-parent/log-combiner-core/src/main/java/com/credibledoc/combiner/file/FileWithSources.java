package com.credibledoc.combiner.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a file collected by {@link FileService}. It has a single collected file and one or more its sources,
 * for example its directory and compressed archive file.
 */
public class FileWithSources {
    /**
     * Collected file returned by {@link FileService}. It can be decompressed.
     */
    private File file;

    /**
     * Source files, for example directories or archive files where the {@link #file} is stored.
     */
    private List<File> sources = new ArrayList<>();
    
    @Override
    public String toString() {
        return "FileWithSources{" +
            "file=" + file +
            ", sources=" + sources +
            '}';
    }

    /**
     * @return The {@link #file} field value.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file see the {@link #file} field description.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return The {@link #sources} field value.
     */
    public List<File> getSources() {
        return sources;
    }

    /**
     * @param sources see the {@link #sources} field description.
     */
    public void setSources(List<File> sources) {
        this.sources = sources;
    }
}
