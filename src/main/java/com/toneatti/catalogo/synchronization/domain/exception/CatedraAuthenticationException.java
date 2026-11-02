package com.toneatti.catalogo.synchronization.domain.exception;

/**
 * The cátedra answered 401 or 403: the technical JWT is missing, expired, revoked or has no integration.
 * Retrying with the same JWT cannot succeed. The message never contains the JWT.
 */
public class CatedraAuthenticationException extends RuntimeException {
    public CatedraAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
