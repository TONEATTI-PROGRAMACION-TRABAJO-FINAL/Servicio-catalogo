package com.toneatti.catalogo.synchronization.infrastructure.startup;

import com.toneatti.catalogo.config.CatalogSyncProperties;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CatalogSyncOnStartupListenerTest {

    private final CatalogSynchronizationService service = mock(CatalogSynchronizationService.class);

    @Test
    void doesNothingWhenTheLoadIsDisabled() {
        listener(false).synchronizeOnStartup();

        verifyNoInteractions(service);
    }

    // The service decides between the snapshot and the changes in Redis
    @Test
    void synchronizesThroughTheServiceDecision() {
        listener(true).synchronizeOnStartup();

        verify(service).synchronizeCatalog();
        verify(service, never()).synchronizeCatalogFromSnapshot();
    }

    private CatalogSyncOnStartupListener listener(boolean onStartup) {
        return new CatalogSyncOnStartupListener(service,
                new CatalogSyncProperties(onStartup, 8, Duration.ofSeconds(2), Duration.ofSeconds(60), false,
                        Duration.ofMinutes(1)));
    }
}
