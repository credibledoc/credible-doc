package com.credibledoc.log.labelizer.exception;

/**
 * Application runtime exception. Extends the {@link RuntimeException}.
 *
 * @author Kyrylo Semenko
 */
public class LabelizerRuntimeException extends RuntimeException {

    /**
     * Call a super {@link RuntimeException#RuntimeException()} constructor
     */
    public LabelizerRuntimeException() {
        super();
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String)} method description
     */
    public LabelizerRuntimeException(String message) {
        super(message);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor
     *
     * @param throwable see the {@link RuntimeException#RuntimeException(Throwable)} method description
     */
    public LabelizerRuntimeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     * @param cause see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     */
    public LabelizerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
