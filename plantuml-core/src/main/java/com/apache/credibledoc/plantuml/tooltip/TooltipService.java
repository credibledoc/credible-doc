package com.apache.credibledoc.plantuml.tooltip;

public class TooltipService {

    private static final int MAX_TOOLTIP_LENGTH = 800;

    private static final String TOOLTIP_NEW_LINE = "\\\\\\\\n";

    private static final String TAB = "\t";

    private static final String FOUR_SPACES = "    ";

    private static final String SPACE_AND_BRACKET_BEFORE_TOOLTIP = " {";

    private static final String BRACKET_AFTER_TOOLTIP = "}";

    /**
     * Singleton.
     */
    private static TooltipService instance;

    private TooltipService() {
        // empty
    }

    /**
     * @return the {@link TooltipService} singleton.
     */
    public static TooltipService getInstance() {
        if (instance == null) {
            instance = new TooltipService();
        }
        return instance;
    }

    /**
     * Replace all {@link System#lineSeparator()}s with {@link #TOOLTIP_NEW_LINE}.
     * <p>
     * Replace all {@link #TAB}s with {@link #FOUR_SPACES}.
     *
     * @param source the tooltip content
     * @return the replaced source enclosed by {@link #SPACE_AND_BRACKET_BEFORE_TOOLTIP}
     * before and {@link #BRACKET_AFTER_TOOLTIP} after the replaced source.
     */
    public String generateTooltip(String source) {
        if (source.length() > MAX_TOOLTIP_LENGTH) {
            source = source.substring(0, MAX_TOOLTIP_LENGTH);
        }
        String result = source.replaceAll(TAB, FOUR_SPACES);
        result = result.replaceAll("\\r\\n|\\n", TOOLTIP_NEW_LINE);
        result = result.replaceAll("]]", "] ]");
        return SPACE_AND_BRACKET_BEFORE_TOOLTIP + result + BRACKET_AFTER_TOOLTIP;
    }

}
