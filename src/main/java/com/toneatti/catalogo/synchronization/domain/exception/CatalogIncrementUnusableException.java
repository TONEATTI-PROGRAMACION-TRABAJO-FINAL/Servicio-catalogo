package com.toneatti.catalogo.synchronization.domain.exception;

/**
 * The versions or the changes in Redis cannot be applied: a missing key, an id missing from its Hash, invalid JSON
 * or data that does not match the version. Retrying does not fix it; the full snapshot does.
 */
public class CatalogIncrementUnusableException extends RuntimeException {
    public CatalogIncrementUnusableException(String message, Throwable cause) {
        super(message, cause);
    }
}
