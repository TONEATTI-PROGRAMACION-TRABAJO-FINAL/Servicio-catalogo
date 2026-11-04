package com.toneatti.catalogo.synchronization.domain.exception;

/**
 * After synchronizing, the local catalog is still older than the version a CatalogUpdated announced: another
 * synchronization was running and this one was skipped, or Redis does not show that version yet. Retrying later may
 * succeed.
 */
public class CatalogSynchronizationPendingException extends RuntimeException {
    public CatalogSynchronizationPendingException(String message) {
        super(message);
    }
}
