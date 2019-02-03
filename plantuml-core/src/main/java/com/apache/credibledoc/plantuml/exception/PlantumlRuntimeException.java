package com.apache.credibledoc.plantuml.exception;

/**
 * Application runtime exception. Extends the {@link RuntimeException}.
 *
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class PlantumlRuntimeException extends RuntimeException {

    /**
     * Call a super {@link RuntimeException#RuntimeException()} constructor
     */
    public PlantumlRuntimeException() {
        super();
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String)} method description
     */
    public PlantumlRuntimeException(String message) {
        super(message);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor
     *
     * @param throwable see the {@link RuntimeException#RuntimeException(Throwable)} method description
     */
    public PlantumlRuntimeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     * @param cause see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     */
    public PlantumlRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
