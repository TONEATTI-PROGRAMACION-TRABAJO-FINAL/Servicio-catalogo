package com.toneatti.catalogo.synchronization.infrastructure.persistence.mapper;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.ProcessedCatalogEventEntity;
import org.springframework.stereotype.Component;

@Component
public class ProcessedCatalogEventMapper {

    public ProcessedCatalogEventEntity toEntity(ProcessedCatalogEvent event) {
        if (event == null) {
            return null;
        }
        return ProcessedCatalogEventEntity.builder()
                .id(event.id())
                .eventId(event.eventId())
                .newVersion(event.newVersion())
                .processedAt(event.processedAt())
                .build();
    }

    public ProcessedCatalogEvent toDomainModel(ProcessedCatalogEventEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProcessedCatalogEvent(
                entity.getId(),
                entity.getEventId(),
                entity.getNewVersion(),
                entity.getProcessedAt());
    }
}
