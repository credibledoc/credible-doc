package com.apache.credibledoc.plantuml.exception;

/**
 * Application runtime exception. Extends the {@link RuntimeException}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class PlantumlRuntimeException extends RuntimeException {

    /** Call a super {@link RuntimeException#RuntimeException()} constructor */
    public PlantumlRuntimeException() {
        super();
    }

    /** Call a super {@link RuntimeException#RuntimeException(String)} constructor */
    public PlantumlRuntimeException(String message) {
         super(message);
    }

    /** Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor */
    public PlantumlRuntimeException(Throwable e) {
         super(e);
    }

    /** Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor */
    public PlantumlRuntimeException(String message, Throwable cause) {
         super(message, cause);
    }
    
}
