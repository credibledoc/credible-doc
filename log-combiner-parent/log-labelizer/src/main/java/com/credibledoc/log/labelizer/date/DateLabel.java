package com.credibledoc.log.labelizer.date;

import com.credibledoc.log.labelizer.exception.LabelizerRuntimeException;

/**
 * This enum represents a label of {@link org.nd4j.linalg.api.ndarray.INDArray} dimension.
 * 
 * @author Kyrylo Semenko
 */
public enum DateLabel {
    W_WITHOUT_DATE('w', 0),
    D_DATE('d', 1);

    DateLabel(char character, int index) {
        this.character = character;
        this.index = index;
    }
    
    private char character;
    
    private int index;

    public static int findIndex(char labelChar) {
        for (DateLabel dateLabel : values()) {
            if (dateLabel.getCharacter() == labelChar) {
                return dateLabel.getIndex();
            }
        }
        throw new LabelizerRuntimeException("Cannot find " + DateLabel.class.getSimpleName() +
            " with char '" + labelChar + "'.");
    }

    public static char findCharacter(int characterIndex) {
        for (DateLabel dateLabel : values()) {
            if (dateLabel.getIndex() == characterIndex) {
                return dateLabel.getCharacter();
            }
        }
        throw new LabelizerRuntimeException("Cannot find " + DateLabel.class.getSimpleName() +
            " with index '" + characterIndex + "'.");
    }

    /**
     * @return The {@link #character} field value.
     */
    public char getCharacter() {
        return character;
    }

    /**
     * @return The {@link #index} field value.
     */
    public int getIndex() {
        return index;
    }
}
