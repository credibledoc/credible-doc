package org.credibledoc.substitution.doc.line;

import java.io.Reader;

/**
 * This enum contains states of the line from a log file,
 * for example {@link #IS_NULL}, {@link #WITHOUT_DATE}
 * and {@link #WITH_DATE}
 *
 * @author Kyrylo Semenko
 */
public enum LineState {
    
    /** The line is 'null', so the {@link Reader} is empty (the last line has been returned from the {@link Reader}) */
    IS_NULL,
    
    /** The line is not 'null', but the line has no date and time record */
    WITHOUT_DATE,
    
    /**
     * The line contains date and time records, for example
     * <pre>29.09.2018 22:53:42.494|https-jsse-nio-15443-exec-1|DEBUG...</pre>
     * for {@link org.credibledoc.substitution.doc.module.tactic.TacticHolder#SUBSTITUTION}
     */
    WITH_DATE

}
