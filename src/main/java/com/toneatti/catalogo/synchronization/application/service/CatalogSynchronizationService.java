package com.toneatti.catalogo.synchronization.application.service;

import com.toneatti.catalogo.synchronization.domain.exception.CatalogIncrementUnusableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSyncAction;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogIncrementUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogIncrementUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionWindowUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Brings the local catalog up to the version of the cátedra (section 16). Reads from the cátedra run outside any
 * transaction; each snapshot and each version is applied in its own one, by another bean, because @Transactional does
 * not work through self-invocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSynchronizationService {

    private final GetCatalogSnapshotUseCase getCatalogSnapshotUseCase;
    private final ApplyCatalogSnapshotUseCase applyCatalogSnapshotUseCase;
    private final GetCatalogVersionUseCase getCatalogVersionUseCase;
    private final GetCatalogVersionWindowUseCase getCatalogVersionWindowUseCase;
    private final GetCatalogIncrementUseCase getCatalogIncrementUseCase;
    private final ApplyCatalogIncrementUseCase applyCatalogIncrementUseCase;

    // Two synchronizations at once would apply the same versions twice
    private final ReentrantLock running = new ReentrantLock();

    /**
     * Without a local version, or when the changes in Redis cannot connect it to the current version, loads the
     * snapshot; otherwise applies changes:{local+1}..{current} in order. Unusable changes fall back to the snapshot
     * once per run. When a version fails, it is rolled back, the local version stays at the previous one and the
     * following versions are not applied.
     */
    public void synchronizeCatalog() {
        if (!running.tryLock()) {
            log.info("Catalog synchronization already running, skipped");
            return;
        }
        try {
            Optional<Long> localVersion = getCatalogVersionUseCase.getCatalogVersion();
            if (localVersion.isEmpty()) {
                synchronizeCatalogFromSnapshot();
                return;
            }
            synchronizeFrom(localVersion.get());
        } finally {
            running.unlock();
        }
    }

    /**
     * Fetches the snapshot outside any transaction and then applies it in its own one.
     */
    public CatalogSnapshot synchronizeCatalogFromSnapshot() {
        CatalogSnapshot snapshot = getCatalogSnapshotUseCase.getCatalogSnapshot();
        applyCatalogSnapshotUseCase.applyCatalogSnapshot(snapshot);
        log.info("Catalog snapshot version {} applied: {} categories, {} professionals, {} weekly schedules",
                snapshot.snapshotVersion(), snapshot.professionalCategories().size(),
                snapshot.professionals().size(), snapshot.weeklySchedules().size());
        return snapshot;
    }

    private void synchronizeFrom(long localVersion) {
        CatalogVersionWindow window;
        try {
            window = getCatalogVersionWindowUseCase.getCatalogVersionWindow();
        } catch (CatalogIncrementUnusableException e) {
            fallBackToSnapshot(e);
            return;
        }
        CatalogSyncAction action = window.decide(localVersion);
        if (action == CatalogSyncAction.UP_TO_DATE) {
            log.debug("Catalog already at version {}", localVersion);
        } else if (action.needsSnapshot()) {
            if (action == CatalogSyncAction.SNAPSHOT_AHEAD_OF_SOURCE) {
                log.warn("Local catalog version {} is newer than the current version {} of the catedra: "
                        + "replacing it with the snapshot", localVersion, window.currentVersion());
            } else {
                log.info("Local catalog version {} is older than the oldest available version {}: loading the "
                        + "snapshot", localVersion, window.oldestAvailableVersion());
            }
            synchronizeCatalogFromSnapshot();
        } else {
            applyIncrements(localVersion, window.currentVersion());
        }
    }

    private void applyIncrements(long localVersion, long currentVersion) {
        for (long version = localVersion + 1; version <= currentVersion; version++) {
            CatalogIncrement increment;
            try {
                increment = getCatalogIncrementUseCase.getCatalogIncrement(version);
            } catch (CatalogIncrementUnusableException e) {
                fallBackToSnapshot(e);
                return;
            }
            applyCatalogIncrementUseCase.applyCatalogIncrement(increment);
            log.info("Catalog version {} applied: {} categories, {} professionals, {} weekly schedules", version,
                    increment.professionalCategories().size(), increment.professionals().size(),
                    increment.weeklySchedules().size());
        }
    }

    private void fallBackToSnapshot(CatalogIncrementUnusableException cause) {
        log.warn("Catalog changes in Redis cannot be applied ({}): loading the snapshot", cause.getMessage());
        synchronizeCatalogFromSnapshot();
    }
}
