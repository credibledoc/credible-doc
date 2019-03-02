package org.credibledoc.substitution.doc.log.buffered;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Extends the {@link FileReader} and provides an extra
 * {@link #getFile()} method.
 *
 * @author Kyrylo Semenko
 */
public class LogFileReader extends FileReader {

    /**
     * This reader source file.
     */
    private File file;

    /**
     * Calls the {@link FileReader#FileReader(File)} constructor.
     * @param file will be assigned to the {@link #file} field
     */
    public LogFileReader(File file) throws FileNotFoundException {
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
