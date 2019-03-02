package com.credibledoc.substitution.exception;

/**
 * Application runtime exception. Extends the {@link RuntimeException}.
 *
 * @author Kyrylo Semenko
 */
public class SubstitutionRuntimeException extends RuntimeException {

    /**
     * Call a super {@link RuntimeException#RuntimeException()} constructor
     */
    public SubstitutionRuntimeException() {
        super();
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String)} method description
     */
    public SubstitutionRuntimeException(String message) {
        super(message);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor
     *
     * @param throwable see the {@link RuntimeException#RuntimeException(Throwable)} method description
     */
    public SubstitutionRuntimeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     * @param cause see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     */
    public SubstitutionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
