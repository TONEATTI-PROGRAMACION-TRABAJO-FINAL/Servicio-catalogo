package com.toneatti.catalogo.synchronization.infrastructure.startup;

import com.toneatti.catalogo.config.CatalogSyncProperties;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SnapshotOnStartupListenerTest {

    private final CatalogSynchronizationService service = mock(CatalogSynchronizationService.class);

    @Test
    void doesNothingWhenTheLoadIsDisabled() {
        listener(false).loadSnapshotOnStartup();

        verifyNoInteractions(service);
    }

    @Test
    void doesNotSynchronizeWhenThereIsAlreadyALocalVersion() {
        when(service.hasCatalogVersion()).thenReturn(true);

        listener(true).loadSnapshotOnStartup();

        verify(service, never()).synchronizeCatalogFromSnapshot();
    }

    private SnapshotOnStartupListener listener(boolean snapshotOnStartup) {
        return new SnapshotOnStartupListener(service,
                new CatalogSyncProperties(snapshotOnStartup, 8, Duration.ofSeconds(2), Duration.ofSeconds(60)));
    }
}
