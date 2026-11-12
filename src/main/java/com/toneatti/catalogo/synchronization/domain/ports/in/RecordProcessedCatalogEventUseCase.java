package com.toneatti.catalogo.synchronization.domain.ports.in;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;

public interface RecordProcessedCatalogEventUseCase {
    ProcessedCatalogEvent recordProcessedCatalogEvent(ProcessedCatalogEvent event);
}
