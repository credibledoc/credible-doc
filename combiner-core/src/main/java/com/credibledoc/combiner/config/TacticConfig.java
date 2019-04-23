package com.credibledoc.combiner.config;

/**
 * This data object contains configuration of a single {@link com.credibledoc.combiner.tactic.Tactic}.
 *
 * @author Kyrylo Semenko
 */
public class TacticConfig {

    /**
     * (mandatory) the pattern of datetime searching in a log line
     */
    private String regex;

    /**
     * (optional) if defined, the first part of a line will be searched by matcher for datetime pattern.
     * If not set, the whole line will be searched by matcher.
     */
    private Integer maxIndexEndOfTime;

    /**
     * (mandatory) a pattern for parsing datetime string to `java.util.Date` object
     */
    private String simpleDateFormat;

    /**
     * (optional) if defined, each line in a merged file will be prefixed by this value.
     * It is useful for better readability of merged files, where logs from different applications
     * combined in a single file. In this case each line can be distinguished which application it belongs to.
     */
    private String applicationName;

    @Override
    public String toString() {
        return "TacticConfig{" +
            "regex='" + regex + '\'' +
            ", maxIndexEndOfTime=" + maxIndexEndOfTime +
            ", simpleDateFormat='" + simpleDateFormat + '\'' +
            ", applicationName='" + applicationName + '\'' +
            '}';
    }

    /**
     * @return The {@link #regex} field value.
     */
    public String getRegex() {
        return regex;
    }

    /**
     * @param regex see the {@link #regex} field description.
     */
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * @return The {@link #maxIndexEndOfTime} field value.
     */
    public Integer getMaxIndexEndOfTime() {
        return maxIndexEndOfTime;
    }

    /**
     * @param maxIndexEndOfTime see the {@link #maxIndexEndOfTime} field description.
     */
    public void setMaxIndexEndOfTime(Integer maxIndexEndOfTime) {
        this.maxIndexEndOfTime = maxIndexEndOfTime;
    }

    /**
     * @return The {@link #simpleDateFormat} field value.
     */
    public String getSimpleDateFormat() {
        return simpleDateFormat;
    }

    /**
     * @param simpleDateFormat see the {@link #simpleDateFormat} field description.
     */
    public void setSimpleDateFormat(String simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    /**
     * @return The {@link #applicationName} field value.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @param applicationName see the {@link #applicationName} field description.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

}
