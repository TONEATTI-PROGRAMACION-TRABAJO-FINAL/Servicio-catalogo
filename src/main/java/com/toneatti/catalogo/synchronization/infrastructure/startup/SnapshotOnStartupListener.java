package com.toneatti.catalogo.synchronization.infrastructure.startup;

import com.toneatti.catalogo.config.CatalogSyncProperties;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Synchronizes the catalog when the application is ready: the snapshot without a local version, otherwise the
 * changes in Redis or the snapshot, as the service decides.
 * The service starts even if the cátedra cannot be reached: the first attempt runs right away and the retries,
 * with exponential backoff and a limit, run in a background thread. A rejected JWT is not retried.
 * Reading the catalog has no effects in the cátedra, so retrying it is safe (section 18.4).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotOnStartupListener {

    private final CatalogSynchronizationService catalogSynchronizationService;
    private final CatalogSyncProperties catalogSyncProperties;

    private volatile Thread retries;

    @EventListener(ApplicationReadyEvent.class)
    public void loadSnapshotOnStartup() {
        if (!catalogSyncProperties.snapshotOnStartup()) {
            return;
        }
        if (!attempt(1)) {
            retries = Thread.ofVirtual().name("catalog-snapshot-retries").start(this::retry);
        }
    }

    @PreDestroy
    void stopRetrying() {
        Thread thread = retries;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void retry() {
        int maxAttempts = catalogSyncProperties.maxAttempts();
        Duration backoff = catalogSyncProperties.initialBackoff();
        for (int attempt = 2; attempt <= maxAttempts; attempt++) {
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (attempt(attempt)) {
                return;
            }
            backoff = min(backoff.multipliedBy(2), catalogSyncProperties.maxBackoff());
        }
        log.error("Catalog not synchronized after {} attempts: the API answers the last local copy until the next "
                + "synchronization", maxAttempts);
    }

    // True when there is nothing left to retry: the catalog was synchronized or the JWT was rejected.
    private boolean attempt(int attempt) {
        try {
            catalogSynchronizationService.synchronizeCatalog();
            return true;
        } catch (CatedraAuthenticationException e) {
            log.error("Catalog not synchronized, check CATEDRA_JWT ({}). Not retrying", e.getMessage());
            return true;
        } catch (CatedraUnavailableException e) {
            log.warn("Catalog synchronization attempt {}/{} failed: {}", attempt, catalogSyncProperties.maxAttempts(),
                    e.getMessage());
            return false;
        } catch (RuntimeException e) {
            log.warn("Catalog synchronization attempt {}/{} failed unexpectedly", attempt,
                    catalogSyncProperties.maxAttempts(), e);
            return false;
        }
    }

    private static Duration min(Duration a, Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
