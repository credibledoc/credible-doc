package com.credibledoc.log.labelizer.date;

import java.util.Calendar;

/**
 * This data object represents an occurrence of Date - time pattern in a text line.
 * 
 * @author Kyrylo Semenko
 */
public class DateLabel {
    /**
     * For example <pre>01.07.2019;00:00:33.038</pre>.
     */
    private String source;

    /**
     * Begin index of the {@link #source} in a line.
     */
    private int beginIndex;

    /**
     * End index of the {@link #source} in a line.
     */
    private int endIndex;

    /**
     * Parsed {@link #source}
     */
    private Calendar calendar;

    /**
     * {@link java.text.SimpleDateFormat} pattern.
     */
    private String pattern;

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
     * @return The {@link #beginIndex} field value.
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * @param beginIndex see the {@link #beginIndex} field description.
     */
    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    /**
     * @return The {@link #endIndex} field value.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * @param endIndex see the {@link #endIndex} field description.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * @return The {@link #calendar} field value.
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * @param calendar see the {@link #calendar} field description.
     */
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
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
}
