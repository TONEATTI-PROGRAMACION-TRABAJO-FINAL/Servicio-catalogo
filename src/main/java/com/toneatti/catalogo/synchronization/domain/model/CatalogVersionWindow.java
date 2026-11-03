package com.toneatti.catalogo.synchronization.domain.model;

/**
 * Versions the cátedra keeps in Redis (section 14.2): the current one and the oldest whose changes are still
 * available.
 */
public record CatalogVersionWindow(
        long currentVersion,
        long oldestAvailableVersion) {
}
