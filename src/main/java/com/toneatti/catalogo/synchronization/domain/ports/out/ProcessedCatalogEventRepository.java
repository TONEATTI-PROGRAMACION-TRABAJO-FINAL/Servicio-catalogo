package com.toneatti.catalogo.synchronization.domain.ports.out;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;

import java.time.Instant;

public interface ProcessedCatalogEventRepository {

    ProcessedCatalogEvent create(ProcessedCatalogEvent event);
    boolean existsByEventId(String eventId);
    // Returns how many were deleted
    int deleteProcessedBefore(Instant cutoff);

}
