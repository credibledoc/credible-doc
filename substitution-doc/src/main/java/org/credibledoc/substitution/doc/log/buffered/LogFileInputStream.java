package org.credibledoc.substitution.doc.log.buffered;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Extends the {@link FileInputStream} and provides an extra
 * {@link #getFile()} method.
 *
 * @author Kyrylo Semenko
 */
public class LogFileInputStream extends FileInputStream {

    /**
     * This stream data source.
     */
    private File file;

    public LogFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /**
     * @return the {@link #file} value.
     */
    public File getFile() {
        return file;
    }
}
