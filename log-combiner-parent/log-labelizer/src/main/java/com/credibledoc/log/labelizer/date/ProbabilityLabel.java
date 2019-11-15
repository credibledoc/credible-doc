package com.credibledoc.log.labelizer.date;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;

import java.util.Arrays;
import java.util.List;

/**
 * This enum represents a label of a single character in an arbitrary string.
 * 
 * For example this string
 * <pre>'some text 2019/09/28 '</pre>
 * having the next labels
 * <pre>'nnnnnnnnnnyyyycMMcddn'</pre>
 * 
 * @author Kyrylo Semenko
 */
public enum ProbabilityLabel {
    N_WITHOUT_DATE('n', "n", 0),
    C_CALENDAR_DATE_FILLER('c', "c", 1),
    T_THREAD('t', "t", 2),
    G_ERA_DESIGNATOR('G', "G", 3),
    Y_YEAR('y', "y", 4),
    Y_WEEK_YEAR('Y', "Y", 5),
    M_MONTH_IN_YEAR('M', "M", 6),
    W_WEAK_IN_YEAR('w', "w", 7),
    W_WEAK_IN_MONTH('W', "W", 8),
    DAY_IN_WEEK('D', "D", 9),
    DAY_IN_MONTH('d', "d", 10),
    DAY_OF_WEEK_IN_MONTH('F', "F", 11),
    DAY_NAME_IN_WEEK('E', "E", 12),
    DAY_NUMBER_OF_WEEK_1_MONDAY('u', "u", 13),
    AM_PM_MARKER('a', "a", 14),
    HOUR_IN_DAY_0_23('H', "H", 15),
    HOUR_IN_DAY_1_24('k', "k", 16),
    HOUR_IN_AM_PM_0_11('K', "K", 17),
    HOUR_IN_AM_PM_1_12('h', "h", 18),
    MINUTE_OF_HOUR('m', "m", 19),
    SECOND_IN_MINUTE('s', "s", 20),
    MILLISECOND('S', "S", 21),
    TIME_ZONE_GENERAL('z', "z", 22),
    TIME_ZONE_RFC_822('Z', "Z", 23),
    TIME_ZONE_ISO_8601('X', "X", 24),
    I_IP_ADDRESS_AND_PORT('i', "i", 25),
    L_LOG_LEVEL('l', "l", 26);

    public static final List<ProbabilityLabel> dates = Arrays.asList(
        G_ERA_DESIGNATOR,
        Y_YEAR,
        Y_WEEK_YEAR,
        M_MONTH_IN_YEAR,
        W_WEAK_IN_YEAR,
        W_WEAK_IN_MONTH,
        DAY_IN_WEEK,
        DAY_IN_MONTH,
        DAY_OF_WEEK_IN_MONTH,
        DAY_NAME_IN_WEEK,
        DAY_NUMBER_OF_WEEK_1_MONDAY,
        AM_PM_MARKER,
        HOUR_IN_DAY_0_23,
        HOUR_IN_DAY_1_24,
        HOUR_IN_AM_PM_0_11,
        HOUR_IN_AM_PM_1_12,
        MINUTE_OF_HOUR,
        SECOND_IN_MINUTE,
        MILLISECOND,
        TIME_ZONE_GENERAL,
        TIME_ZONE_RFC_822,
        TIME_ZONE_ISO_8601
    );

    ProbabilityLabel(char character, String string, int index) {
        this.character = character;
        this.string = string;
        this.index = index;
    }
    
    private char character;
    
    private String string;
    
    private int index;

    public static int findIndex(char labelChar) {
        for (ProbabilityLabel probabilityLabel : values()) {
            if (probabilityLabel.getCharacter() == labelChar) {
                return probabilityLabel.getIndex();
            }
        }
        throw new LabelizerRuntimeException("Cannot find " + ProbabilityLabel.class.getSimpleName() +
            " with char '" + labelChar + "'.");
    }

    public static ProbabilityLabel find(Character character) {
        for (ProbabilityLabel probabilityLabel : values()) {
            if (probabilityLabel.getCharacter() == character) {
                return probabilityLabel;
            }
        }
        return null;
    }

    /**
     * @return The {@link #character} field value.
     */
    public char getCharacter() {
        return character;
    }

    /**
     * @return The {@link #string} field value.
     */
    public String getString() {
        return string;
    }

    /**
     * @return The {@link #index} field value.
     */
    public int getIndex() {
        return index;
    }
}
