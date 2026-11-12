package com.toneatti.catalogo.synchronization.infrastructure.persistence;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;

import java.time.Instant;

// A CatalogUpdated of version 7 to record, still without the id the database assigns
public final class ProcessedCatalogEventFixtures {

    private ProcessedCatalogEventFixtures() {
    }

    public static ProcessedCatalogEvent processed(String eventId, Instant processedAt) {
        return new ProcessedCatalogEvent(null, eventId, 7, processedAt);
    }
}
