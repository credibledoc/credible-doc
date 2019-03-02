package org.credibledoc.substitution.doc.log.buffered;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Extends the {@link InputStreamReader} and provides an additional
 * {@link #getInputStream()} method.
 */
public class LogInputStreamReader extends InputStreamReader {
    /**
     * This reader data source.
     */
    private InputStream inputStream;

    /**
     * Calls the {@link InputStreamReader#InputStreamReader(InputStream, Charset)} method.
     *
     * @param in will be set to extended class and assigned to the {@link #inputStream} field
     * @param cs will be set to extended class
     */
    public LogInputStreamReader(InputStream in, Charset cs) {
        super(in, cs);
        this.inputStream = in;
    }

    /**
     * @return the {@link #inputStream} value
     */
    public InputStream getInputStream() {
        return inputStream;
    }
}
