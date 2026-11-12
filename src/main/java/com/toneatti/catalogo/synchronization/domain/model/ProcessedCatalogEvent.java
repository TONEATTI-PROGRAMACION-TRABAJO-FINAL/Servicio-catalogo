package com.toneatti.catalogo.synchronization.domain.model;

import java.time.Instant;

/**
 * A CatalogUpdated already processed (section 18.1 of the integration contract): its {@code eventId} is not
 * processed again. {@code newVersion} is the version it announced.
 */
public record ProcessedCatalogEvent(
        Long id,
        String eventId,
        long newVersion,
        Instant processedAt
) {
}
