package org.credibledoc.substitution.doc.log.buffered;

import com.credibledoc.substitution.exception.SubstitutionRuntimeException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * This class extends the {@link LogBufferedReader} and provides
 * an extra {@link #getReader()} method.
 *
 * @author Kyrylo Semenko
 */
public class LogBufferedReader extends BufferedReader {
    /**
     * A source of this {@link LogBufferedReader}.
     */
    private Reader reader;

    /**
     * Is 'true' when all lines has been read.
     */
    private boolean closed;

    /**
     * See the {@link BufferedReader#BufferedReader(Reader)}
     * constructor description.
     * @param reader a data source. This instance will be assigned to
     *               the {@link #reader} field.
     */
    public LogBufferedReader(Reader reader) {
        super(reader);
        this.reader = reader;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    /**
     * Calls the {@link BufferedReader#mark(int)} method.
     */
    @Override
    public void mark(int maxCharactersInOneLine) {
        try {
            super.mark(maxCharactersInOneLine);
        } catch (IOException e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * @return the {@link #reader} field value.
     */
    public Reader getReader() {
        return reader;
    }

    /**
     * Call the {@link BufferedReader#close()} method
     * and set {@link #closed} to 'true'.
     */
    @Override
    public void close() {
        try {
            this.closed = true;
            super.close();
        } catch (IOException e) {
            throw new SubstitutionRuntimeException(e);
        }
    }

    /**
     * @return 'False' if the {@link #closed} value is 'true'.
     */
    public boolean isNotClosed() {
        return !closed;
    }

    /**
     * @param closed see the {@link #closed} field
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
