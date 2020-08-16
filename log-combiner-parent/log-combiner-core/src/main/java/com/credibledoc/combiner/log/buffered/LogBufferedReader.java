package com.credibledoc.combiner.log.buffered;

import com.credibledoc.combiner.exception.CombinerRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Date;

/**
 * This class extends the {@link BufferedReader} and provides
 * an extra {@link #getReader()} method.
 *
 * @author Kyrylo Semenko
 */
public class LogBufferedReader extends BufferedReader {
    /**
     * A source of this {@link LogBufferedReader}.
     */
    private final Reader reader;

    /**
     * Is 'true' when all lines has been read.
     */
    private boolean closed;

    /**
     * A user of the {@link LogBufferedReader} can set the value when he parsed a date from a read line.
     * <p>
     * The value serves as a cache of the parsed date to prevent repeated parsing.
     * <p>
     * The date can be used until the next {@link #readLine()}, {@link #read()},
     * {@link #read(char[])}, {@link #read(CharBuffer)} or {@link #read(char[], int, int)} methods called.
     * <p>
     * After calling each of these methods the value will be set to 'null'.
     */
    private Date lineDate;

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
        return "LogBufferedReader{" +
            "reader=\"" + reader +
            "\", lineDate=\"" + lineDate +
            "\", closed=\"" + closed +
            "\"}";
    }

    /**
     * Calls the {@link BufferedReader#mark(int)} method.
     */
    @Override
    public void mark(int maxCharactersInOneLine) {
        try {
            super.mark(maxCharactersInOneLine);
        } catch (IOException e) {
            throw new CombinerRuntimeException(e);
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
            throw new CombinerRuntimeException(e);
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

    /**
     * See the {@link BufferedReader#readLine()} method description.
     * Sets the {@link #lineDate} value to 'null'.
     */
    @Override
    public String readLine() throws IOException {
        lineDate = null;
        return super.readLine();
    }

    /**
     * See the {@link BufferedReader#read()} method description.
     * Sets the {@link #lineDate} value to 'null'.
     */
    @Override
    public int read() throws IOException {
        lineDate = null;
        return super.read();
    }

    /**
     * See the {@link BufferedReader#read(char[], int, int)}  method description.
     * Sets the {@link #lineDate} value to 'null'.
     */
    @Override
    public int read(char [] cbuf, int off, int len) throws IOException {
        lineDate = null;
        return super.read(cbuf, off, len);
    }

    /**
     * See the {@link BufferedReader#read(CharBuffer)}  method description.
     * Sets the {@link #lineDate} value to 'null'.
     */
    @Override
    public int read(CharBuffer target) throws IOException {
        lineDate = null;
        return super.read(target);
    }

    /**
     * See the {@link BufferedReader#read(char[])}  method description.
     * Sets the {@link #lineDate} value to 'null'.
     */
    @Override
    public int read(char[] cbuf) throws IOException {
        lineDate = null;
        return super.read(cbuf);
    }

    /**
     * @return The {@link #lineDate} field value.
     */
    public Date getLineDate() {
        return lineDate;
    }

    /**
     * @param lineDate see the {@link #lineDate} field description.
     */
    public void setLineDate(Date lineDate) {
        this.lineDate = lineDate;
    }
}
