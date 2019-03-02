package org.credibledoc.substitution.doc.log.buffered;

import com.credibledoc.substitution.core.exception.SubstitutionRuntimeException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

/**
 * Provides concatenated streams from
 * the {@link #enumeration} field.
 *
 * @author Kyrylo Semenko
 */
public class LogConcatenatedInputStream extends InputStream {

    /**
     * This stream data source.
     */
    private Enumeration<LogFileInputStream> enumeration;

    /**
     * Current input stream.
     */
    private LogFileInputStream currentInputStream;

    /**
     * Last opened input stream. This value is not removed
     * when the last {@link #currentInputStream} has been closed.
     */
    private LogFileInputStream lastInputStream;

    /**
     * Calls the {@link #closeAndGetNextStream()} method.
     *
     * @param enumeration will be assigned to the {@link #enumeration} field.
     */
    public LogConcatenatedInputStream(Enumeration<LogFileInputStream> enumeration) {
        this.enumeration = enumeration;
        try {
            closeAndGetNextStream();
        } catch (IOException e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * Calls the {@link FileInputStream#available()} method.
     *
     * @return 'null' if the {@link #currentInputStream} is 'null'
     */
    @Override
    public int available() throws IOException {
        if (currentInputStream == null) {
            return 0;
        }
        return currentInputStream.available();
    }

    /**
     * Calls the {@link FileInputStream#read()} method.
     *
     * @return -1 if the {@link #currentInputStream} is null and no
     * streams in the {@link #enumeration} available.
     */
    @Override
    public int read() throws IOException {
        while (currentInputStream != null) {
            int read = currentInputStream.read();
            if (read != -1) {
                return read;
            }
            closeAndGetNextStream();
        }
        return -1;
    }

    /**
     * Calls the {@link FileInputStream#read(byte[], int, int)} method.
     *
     * @param bytes  see the {@link FileInputStream#read(byte[], int, int)} method
     * @param offset see the {@link FileInputStream#read(byte[], int, int)} method
     * @param length see the {@link FileInputStream#read(byte[], int, int)} method
     * @return -1 if the {@link #currentInputStream} is null and no
     * streams in the {@link #enumeration} available.
     */
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (currentInputStream == null) {
            return -1;
        } else if (bytes == null) {
            throw new SubstitutionRuntimeException("Parameter 'bytes' is null");
        } else if (offset < 0 || length < 0 || length > bytes.length - offset) {
            throw new IndexOutOfBoundsException();
        } else if (length == 0) {
            return 0;
        }
        do {
            int read = currentInputStream.read(bytes, offset, length);
            if (read > 0) {
                return read;
            }
            closeAndGetNextStream();
        } while (currentInputStream != null);
        return -1;
    }

    /**
     * Close all streams.
     */
    @Override
    public void close() throws IOException {
        do {
            closeAndGetNextStream();
        } while (currentInputStream != null);
    }

    /**
     * Close the {@link #currentInputStream} and assign a next
     * stream from the {@link #enumeration} to the
     * {@link #currentInputStream} field.
     */
    private void closeAndGetNextStream() throws IOException {
        if (currentInputStream != null) {
            currentInputStream.close();
        }

        if (enumeration.hasMoreElements()) {
            currentInputStream = enumeration.nextElement();
            lastInputStream = currentInputStream;
            if (currentInputStream == null) {
                throw new NullPointerException();
            }
        }
        else currentInputStream = null;
    }

    /**
     * @return the {@link #lastInputStream} field value
     */
    public LogFileInputStream getCurrentStream() {
        return lastInputStream;
    }
}
