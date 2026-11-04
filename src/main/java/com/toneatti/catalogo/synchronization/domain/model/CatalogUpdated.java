package com.toneatti.catalogo.synchronization.domain.model;

import java.time.Instant;

/**
 * Notification of the cátedra that the catalog reached {@code newVersion} (section 15.3 of the integration contract).
 * It carries no data: the catalog is read from Redis or the snapshot. {@code source} is the origin of the version,
 * such as MANUAL or SCHEDULED, and {@code occurredAt} may be missing.
 */
public record CatalogUpdated(
        String eventId,
        long newVersion,
        String source,
        Instant occurredAt
) {
}
