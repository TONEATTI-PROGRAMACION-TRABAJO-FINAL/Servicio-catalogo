package com.toneatti.catalogo.synchronization.domain.ports.in;

public interface IsCatalogEventProcessedUseCase {
    boolean isCatalogEventProcessed(String eventId);
}
