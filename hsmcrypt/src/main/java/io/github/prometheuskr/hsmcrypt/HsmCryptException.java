package io.github.prometheuskr.hsmcrypt;

/**
 * Exception thrown when encryption or decryption operations fail.
 * 
 * @author Prometheus
 */
public class HsmCryptException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new HsmCryptException with the specified detail message.
     * 
     * @param message
     *            the detail message
     */
    public HsmCryptException(String message) {
        super(message);
    }

    /**
     * Constructs a new HsmCryptException with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public HsmCryptException(String message, Throwable cause) {
        super(message, cause);
    }
}
