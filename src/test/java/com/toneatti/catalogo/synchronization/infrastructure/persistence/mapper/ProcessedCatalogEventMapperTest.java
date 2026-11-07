package com.toneatti.catalogo.synchronization.infrastructure.persistence.mapper;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.ProcessedCatalogEventEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedCatalogEventMapperTest {

    private final ProcessedCatalogEventMapper mapper = new ProcessedCatalogEventMapper();

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainModel(null)).isNull();
    }

    @Test
    void mapsEveryFieldBothWays() {
        ProcessedCatalogEvent event = new ProcessedCatalogEvent(4L, "catalog-event-7-4f3b", 7,
                Instant.parse("2026-09-10T13:45:03Z"));

        ProcessedCatalogEventEntity entity = mapper.toEntity(event);

        assertThat(entity).usingRecursiveComparison().isEqualTo(event);
        assertThat(mapper.toDomainModel(entity)).isEqualTo(event);
    }
}
