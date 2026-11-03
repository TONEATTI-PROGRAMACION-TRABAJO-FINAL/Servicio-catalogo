package com.toneatti.catalogo.synchronization.application.service;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSynchronizationService {

    private final GetCatalogSnapshotUseCase getCatalogSnapshotUseCase;
    private final ApplyCatalogSnapshotUseCase applyCatalogSnapshotUseCase;
    private final GetCatalogVersionUseCase getCatalogVersionUseCase;

    /**
     * Fetches the snapshot outside any transaction and then applies it in its own one. Applying lives in another
     * bean because @Transactional does not work through self-invocation.
     */
    public CatalogSnapshot synchronizeCatalogFromSnapshot() {
        CatalogSnapshot snapshot = getCatalogSnapshotUseCase.getCatalogSnapshot();
        applyCatalogSnapshotUseCase.applyCatalogSnapshot(snapshot);
        log.info("Catalog snapshot version {} applied: {} categories, {} professionals, {} weekly schedules",
                snapshot.snapshotVersion(), snapshot.professionalCategories().size(),
                snapshot.professionals().size(), snapshot.weeklySchedules().size());
        return snapshot;
    }

    // False until the first snapshot is applied, which is the normal state at the first start
    public boolean hasCatalogVersion() {
        return getCatalogVersionUseCase.getCatalogVersion().isPresent();
    }
}
