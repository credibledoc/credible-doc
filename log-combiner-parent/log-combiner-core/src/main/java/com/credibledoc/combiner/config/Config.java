package com.credibledoc.combiner.config;

import java.util.ArrayList;
import java.util.List;

/**
 * This data object contains a configuration of the
 * {@value com.credibledoc.combiner.log.reader.ReaderService#COMBINER_CORE_MODULE_NAME} module.
 *
 * @author Kyrylo Semenko
 */
public class Config {

    private static final String DEFAULT_TARGET_FILE_NAME_COMBINED_TXT = "combined.txt";
    /**
     * Contains values parsed from a config file
     */
    private List<TacticConfig> tacticConfigs = new ArrayList<>();

    /**
     * (optional, default false, allowed value `true`) if defined as `true`,
     * System.lineSeparator() will be inserted after each file except the last one
     */
    private boolean insertLineSeparatorBetweenFiles;

    /**
     * (optional, default true) if defined as `true`, log lines from different sub-folders will be prefixed
     * by sub-folder name. It is useful in case when the same application is installed on multiple nodes and each
     * node generates
     * its own logs. In this case each node files should be places in sub-folder next to each other.
     */
    private boolean printNodeName = true;

    /**
     * (optional, default value "combined.txt") file name where all source log files will be combined.
     */
    private String targetFileName = DEFAULT_TARGET_FILE_NAME_COMBINED_TXT;

    @Override
    public String toString() {
        return "Config{" +
            "tacticConfigs=" + tacticConfigs +
            ", insertLineSeparatorBetweenFiles=" + insertLineSeparatorBetweenFiles +
            ", printNodeName=" + printNodeName +
            ", targetFileName=" + targetFileName +
            '}';
    }

    /**
     * @return The {@link #tacticConfigs} field value.
     */
    public List<TacticConfig> getTacticConfigs() {
        return tacticConfigs;
    }

    /**
     * @return The {@link #insertLineSeparatorBetweenFiles} field value.
     */
    public boolean isInsertLineSeparatorBetweenFiles() {
        return insertLineSeparatorBetweenFiles;
    }

    /**
     * @param insertLineSeparatorBetweenFiles see the {@link #insertLineSeparatorBetweenFiles} field description.
     */
    public void setInsertLineSeparatorBetweenFiles(boolean insertLineSeparatorBetweenFiles) {
        this.insertLineSeparatorBetweenFiles = insertLineSeparatorBetweenFiles;
    }

    /**
     * @return The {@link #printNodeName} field value.
     */
    public boolean isPrintNodeName() {
        return printNodeName;
    }

    /**
     * @param printNodeName see the {@link #printNodeName} field description.
     */
    public void setPrintNodeName(boolean printNodeName) {
        this.printNodeName = printNodeName;
    }

    /**
     * @return The {@link #targetFileName} field value.
     */
    public String getTargetFileName() {
        return targetFileName;
    }

    /**
     * @param targetFileName see the {@link #targetFileName} field description.
     */
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
}
