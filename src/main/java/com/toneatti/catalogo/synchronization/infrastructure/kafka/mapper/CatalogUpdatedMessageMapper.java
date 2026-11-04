package com.toneatti.catalogo.synchronization.infrastructure.kafka.mapper;

import com.toneatti.catalogo.synchronization.domain.model.CatalogUpdated;
import com.toneatti.catalogo.synchronization.infrastructure.kafka.dto.CatalogUpdatedMessage;
import org.springframework.stereotype.Component;

/**
 * Throws IllegalArgumentException for a message that is not a v1 CatalogUpdated: without its eventId it cannot be
 * deduplicated, without its newVersion nothing tells when it was processed, and another eventType or schemaVersion
 * has no meaning in v1.
 */
@Component
public class CatalogUpdatedMessageMapper {

    private static final String EVENT_TYPE = "CatalogUpdated";
    private static final int SCHEMA_VERSION = 1;

    public CatalogUpdated toDomain(CatalogUpdatedMessage message) {
        if (message == null) {
            return null;
        }
        if (message.eventId() == null || message.eventId().isBlank()) {
            throw new IllegalArgumentException("Missing eventId");
        }
        if (!EVENT_TYPE.equals(message.eventType())) {
            throw new IllegalArgumentException("Unexpected eventType " + message.eventType());
        }
        if (message.schemaVersion() == null || message.schemaVersion() != SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported schemaVersion " + message.schemaVersion());
        }
        if (message.newVersion() == null || message.newVersion() < 0) {
            throw new IllegalArgumentException("Invalid newVersion " + message.newVersion());
        }
        return new CatalogUpdated(message.eventId(), message.newVersion(), message.source(), message.occurredAt());
    }
}
