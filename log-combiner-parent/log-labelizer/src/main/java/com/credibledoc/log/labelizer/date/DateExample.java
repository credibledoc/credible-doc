package com.credibledoc.log.labelizer.date;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This data object represents an occurrence of Date - time pattern in a text line.
 * 
 * @author Kyrylo Semenko
 */
public class DateExample {
    /**
     * Serialized date, for example
     * <pre>2019.09.15 18:10:34 +0200</pre>.
     */
    private String source;

    /**
     * {@link java.text.SimpleDateFormat} pattern, for example
     * <pre>yyyy.MM.dd HH:mm:ss Z</pre>.
     */
    private String pattern;

    /**
     * Contains labels for every character in the {@link #source}, for example
     * <pre>yyyyoMModdoHHommozzoZZZZZ</pre>.
     * Length of this labels string equals with the {@link #source} string length.
     * 
     * Single characters are described in the {@link ProbabilityLabel} enumeration.
     */
    private String labels;

    /**
     * Specific example for training purposes.
     */
    private transient Date date;

    /**
     * Specific example for training purposes.
     */
    private transient Locale locale;

    /**
     * Specific example for training purposes.
     */
    private transient TimeZone timeZone;

    @Override
    public String toString() {
        return "DateExample{" +
            "source='" + source + '\'' +
            ", pattern='" + pattern + '\'' +
            ", labels='" + labels + '\'' +
            '}';
    }

    /**
     * @return The {@link #source} field value.
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source see the {@link #source} field description.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return The {@link #pattern} field value.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern see the {@link #pattern} field description.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return The {@link #labels} field value.
     */
    public String getLabels() {
        return labels;
    }

    /**
     * @param labels see the {@link #labels} field description.
     */
    public void setLabels(String labels) {
        this.labels = labels;
    }

    /**
     * @return The {@link #date} field value.
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date see the {@link #date} field description.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return The {@link #locale} field value.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale see the {@link #locale} field description.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return The {@link #timeZone} field value.
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone see the {@link #timeZone} field description.
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
