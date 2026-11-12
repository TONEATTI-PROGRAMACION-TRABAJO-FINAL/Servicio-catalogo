package com.toneatti.catalogo.synchronization.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * A CatalogUpdated of the topic catedra.catalog.{groupId} (section 15.3 of the integration contract). Any field
 * added in v1 is ignored (section 15.1).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogUpdatedMessage(
        String eventId,
        String eventType,
        Long newVersion,
        Instant occurredAt,
        String source,
        Integer schemaVersion
) {
}
