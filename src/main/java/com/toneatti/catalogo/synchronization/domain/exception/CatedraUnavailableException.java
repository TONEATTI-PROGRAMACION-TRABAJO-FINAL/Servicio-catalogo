package com.toneatti.catalogo.synchronization.domain.exception;

/**
 * The cátedra could not answer: server error, timeout, refused connection, unreadable or incomplete response,
 * or an unexpected rejection. Retrying later may succeed.
 */
public class CatedraUnavailableException extends RuntimeException {
    public CatedraUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
