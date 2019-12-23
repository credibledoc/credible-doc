package com.credibledoc.iso8583packer.exception;

/**
 * Application runtime exception. Extends the {@link RuntimeException}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class PackerRuntimeException extends RuntimeException {

    /**
     * Call a super {@link RuntimeException#RuntimeException()} constructor
     */
    public PackerRuntimeException() {
        super();
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String)} method description
     */
    public PackerRuntimeException(String message) {
        super(message);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor
     *
     * @param throwable see the {@link RuntimeException#RuntimeException(Throwable)} method description
     */
    public PackerRuntimeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor
     *
     * @param message see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     * @param cause see the {@link RuntimeException#RuntimeException(String, Throwable)} method description
     */
    public PackerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
