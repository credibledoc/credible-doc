package com.credibledoc.log.labelizer.date;

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
}
