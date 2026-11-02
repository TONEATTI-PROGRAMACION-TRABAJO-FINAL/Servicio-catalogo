package com.toneatti.catalogo.synchronization.infrastructure.persistence.adapter;

import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.CatalogSyncStateEntity;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.mapper.CatalogSyncStateMapper;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaCatalogSyncStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaCatalogVersionRepositoryAdapter implements CatalogVersionRepository {

    private final JpaCatalogSyncStateRepository jpaCatalogSyncStateRepository;
    private final CatalogSyncStateMapper catalogSyncStateMapper;

    @Override
    public Optional<Long> findCurrentVersion() {
        return jpaCatalogSyncStateRepository.findById(CatalogSyncStateEntity.SINGLE_ROW_ID)
                .map(catalogSyncStateMapper::toVersion);
    }

    // The row always has the same id, so save inserts it the first time and replaces it afterwards
    @Override
    @Transactional
    public void save(Long snapshotVersion, Instant generatedAt, Instant syncedAt) {
        jpaCatalogSyncStateRepository.save(catalogSyncStateMapper.toEntity(snapshotVersion, generatedAt, syncedAt));
    }
}
