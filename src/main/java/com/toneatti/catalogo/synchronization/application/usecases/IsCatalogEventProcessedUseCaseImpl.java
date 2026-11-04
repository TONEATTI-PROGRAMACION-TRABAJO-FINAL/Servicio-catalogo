package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.ports.in.IsCatalogEventProcessedUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.ProcessedCatalogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IsCatalogEventProcessedUseCaseImpl implements IsCatalogEventProcessedUseCase {

    private final ProcessedCatalogEventRepository processedCatalogEventRepository;

    @Override
    public boolean isCatalogEventProcessed(String eventId) {
        return processedCatalogEventRepository.existsByEventId(eventId);
    }
}
