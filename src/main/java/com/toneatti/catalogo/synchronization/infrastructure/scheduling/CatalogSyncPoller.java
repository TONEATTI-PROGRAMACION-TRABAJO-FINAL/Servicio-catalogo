package com.toneatti.catalogo.synchronization.infrastructure.scheduling;

import com.toneatti.catalogo.config.CatalogSyncProperties;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Compares the local version with the cátedra every poll interval and applies what is missing. Kafka
 * (CatalogUpdatedListener) only notifies; this keeps a lost notification from leaving the catalog behind
 * (section 16). The first run waits one interval, so it does not race the synchronization at startup; two runs never
 * overlap, the service skips one.
 * A failure is only logged: the next interval is the retry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "catedra.sync", name = "poll-enabled", havingValue = "true", matchIfMissing = true)
public class CatalogSyncPoller implements SchedulingConfigurer {

    private final CatalogSynchronizationService catalogSynchronizationService;
    private final CatalogSyncProperties catalogSyncProperties;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Duration interval = catalogSyncProperties.pollInterval();
        taskRegistrar.addFixedDelayTask(new FixedDelayTask(this::poll, interval, interval));
    }

    void poll() {
        try {
            catalogSynchronizationService.synchronizeCatalog();
        } catch (CatedraAuthenticationException e) {
            log.error("Periodic catalog synchronization rejected, check CATEDRA_JWT ({})", e.getMessage());
        } catch (CatedraUnavailableException e) {
            log.warn("Periodic catalog synchronization failed, next try in {}: {}",
                    catalogSyncProperties.pollInterval(), e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Periodic catalog synchronization failed unexpectedly, next try in {}",
                    catalogSyncProperties.pollInterval(), e);
        }
    }
}
