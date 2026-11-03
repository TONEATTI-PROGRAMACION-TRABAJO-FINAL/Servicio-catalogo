package com.toneatti.catalogo.synchronization.infrastructure.persistence.mapper;

import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.CatalogSyncStateEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogSyncStateMapperTest {

    private final CatalogSyncStateMapper mapper = new CatalogSyncStateMapper();

    @Test
    void buildsTheSingleRowWithEveryField() {
        Instant generatedAt = Instant.parse("2026-09-10T13:44:59Z");
        Instant syncedAt = Instant.parse("2026-09-10T13:45:03Z");

        CatalogSyncStateEntity entity = mapper.toEntity(7L, generatedAt, syncedAt);

        assertThat(entity.getId()).isEqualTo(CatalogSyncStateEntity.SINGLE_ROW_ID);
        assertThat(entity.getSnapshotVersion()).isEqualTo(7L);
        assertThat(entity.getGeneratedAt()).isEqualTo(generatedAt);
        assertThat(entity.getSyncedAt()).isEqualTo(syncedAt);
        assertThat(mapper.toVersion(entity)).isEqualTo(7L);
    }

    @Test
    void returnsNullVersionForNullEntity() {
        assertThat(mapper.toVersion(null)).isNull();
    }
}
