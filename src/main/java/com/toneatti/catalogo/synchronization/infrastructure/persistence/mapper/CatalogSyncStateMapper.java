package com.toneatti.catalogo.synchronization.infrastructure.persistence.mapper;

import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.CatalogSyncStateEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CatalogSyncStateMapper {

    public CatalogSyncStateEntity toEntity(Long snapshotVersion, Instant generatedAt, Instant syncedAt) {
        return CatalogSyncStateEntity.builder()
                .id(CatalogSyncStateEntity.SINGLE_ROW_ID)
                .snapshotVersion(snapshotVersion)
                .generatedAt(generatedAt)
                .syncedAt(syncedAt)
                .build();
    }

    public Long toVersion(CatalogSyncStateEntity entity) {
        if (entity == null) {
            return null;
        }
        return entity.getSnapshotVersion();
    }
}
