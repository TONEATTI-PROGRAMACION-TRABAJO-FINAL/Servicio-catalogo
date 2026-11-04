package com.toneatti.catalogo.synchronization.application.service;

import com.toneatti.catalogo.synchronization.domain.exception.CatalogSynchronizationPendingException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogEventOutcome;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.model.CatalogUpdated;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogIncrementUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogIncrementUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionWindowUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.IsCatalogEventProcessedUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.in.RecordProcessedCatalogEventUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The handling of a CatalogUpdated with every port mocked; the synchronization itself is covered by the integration
 * tests of the synchronization.
 */
class CatalogSynchronizationServiceProcessCatalogUpdatedTest {

    private static final CatalogUpdated VERSION_5 = new CatalogUpdated("catalog-event-5-4f3b", 5, "MANUAL",
            Instant.parse("2026-09-10T13:44:59Z"));
    private static final CatalogIncrement INCREMENT_5 = new CatalogIncrement(5, List.of(), List.of(), List.of());

    private final GetCatalogSnapshotUseCase getSnapshot = mock(GetCatalogSnapshotUseCase.class);
    private final ApplyCatalogSnapshotUseCase applySnapshot = mock(ApplyCatalogSnapshotUseCase.class);
    private final GetCatalogVersionUseCase getVersion = mock(GetCatalogVersionUseCase.class);
    private final GetCatalogVersionWindowUseCase getWindow = mock(GetCatalogVersionWindowUseCase.class);
    private final GetCatalogIncrementUseCase getIncrement = mock(GetCatalogIncrementUseCase.class);
    private final ApplyCatalogIncrementUseCase applyIncrement = mock(ApplyCatalogIncrementUseCase.class);
    private final IsCatalogEventProcessedUseCase isProcessed = mock(IsCatalogEventProcessedUseCase.class);
    private final RecordProcessedCatalogEventUseCase recordProcessed = mock(RecordProcessedCatalogEventUseCase.class);
    private final CatalogSynchronizationService service = new CatalogSynchronizationService(getSnapshot,
            applySnapshot, getVersion, getWindow, getIncrement, applyIncrement, isProcessed, recordProcessed);

    @Test
    void synchronizesAndThenRecordsTheEvent() {
        when(getVersion.getCatalogVersion()).thenReturn(Optional.of(4L), Optional.of(5L));
        when(getWindow.getCatalogVersionWindow()).thenReturn(new CatalogVersionWindow(5, 1));
        when(getIncrement.getCatalogIncrement(5)).thenReturn(INCREMENT_5);
        Instant before = Instant.now();

        assertThat(service.processCatalogUpdated(VERSION_5)).isEqualTo(CatalogEventOutcome.SYNCHRONIZED);

        InOrder order = inOrder(applyIncrement, recordProcessed);
        order.verify(applyIncrement).applyCatalogIncrement(INCREMENT_5);
        ArgumentCaptor<ProcessedCatalogEvent> recorded = ArgumentCaptor.forClass(ProcessedCatalogEvent.class);
        order.verify(recordProcessed).recordProcessedCatalogEvent(recorded.capture());
        assertThat(recorded.getValue().id()).isNull();
        assertThat(recorded.getValue().eventId()).isEqualTo("catalog-event-5-4f3b");
        assertThat(recorded.getValue().newVersion()).isEqualTo(5);
        assertThat(recorded.getValue().processedAt()).isBetween(before, Instant.now());
    }

    // Section 18.1
    @Test
    void aRecordedEventIdDoesNothing() {
        when(isProcessed.isCatalogEventProcessed("catalog-event-5-4f3b")).thenReturn(true);

        assertThat(service.processCatalogUpdated(VERSION_5)).isEqualTo(CatalogEventOutcome.DUPLICATE);

        verifyNoInteractions(getVersion, getWindow, getIncrement, applyIncrement, getSnapshot, applySnapshot,
                recordProcessed);
    }

    // A redelivery after the catalog moved on only compares the versions
    @Test
    void anEventOlderThanTheLocalVersionIsRecordedWithoutApplyingAnything() {
        when(getVersion.getCatalogVersion()).thenReturn(Optional.of(9L));
        when(getWindow.getCatalogVersionWindow()).thenReturn(new CatalogVersionWindow(9, 1));

        assertThat(service.processCatalogUpdated(VERSION_5)).isEqualTo(CatalogEventOutcome.SYNCHRONIZED);

        verifyNoInteractions(getIncrement, applyIncrement, getSnapshot, applySnapshot);
        verify(recordProcessed).recordProcessedCatalogEvent(any());
    }

    @Test
    void aFailedSynchronizationLeavesTheEventUnrecorded() {
        when(getVersion.getCatalogVersion()).thenReturn(Optional.of(4L));
        when(getWindow.getCatalogVersionWindow()).thenThrow(new CatedraUnavailableException("redis down", null));

        assertThatThrownBy(() -> service.processCatalogUpdated(VERSION_5))
                .isInstanceOf(CatedraUnavailableException.class);

        verify(recordProcessed, never()).recordProcessedCatalogEvent(any());
    }

    // Redis does not show the announced version yet
    @Test
    void aLocalVersionStillBehindLeavesTheEventUnrecorded() {
        when(getVersion.getCatalogVersion()).thenReturn(Optional.of(4L));
        when(getWindow.getCatalogVersionWindow()).thenReturn(new CatalogVersionWindow(4, 1));

        assertThatThrownBy(() -> service.processCatalogUpdated(VERSION_5))
                .isInstanceOf(CatalogSynchronizationPendingException.class)
                .hasMessageContaining("catalog-event-5-4f3b")
                .hasMessageContaining("version 5")
                .hasMessageContaining("at 4");

        verify(recordProcessed, never()).recordProcessedCatalogEvent(any());
    }

    // The poller holds the lock: the event does not count as processed until a synchronization reaches its version
    @Test
    void anEventSkippedByARunningSynchronizationLeavesItUnrecorded() throws Exception {
        CountDownLatch insidePoll = new CountDownLatch(1);
        CountDownLatch releasePoll = new CountDownLatch(1);
        when(getVersion.getCatalogVersion()).thenReturn(Optional.of(4L));
        when(getWindow.getCatalogVersionWindow()).thenAnswer(invocation -> {
            insidePoll.countDown();
            assertThat(releasePoll.await(10, TimeUnit.SECONDS)).isTrue();
            return new CatalogVersionWindow(4, 1);
        });
        Thread poll = Thread.ofVirtual().start(service::synchronizeCatalog);
        assertThat(insidePoll.await(10, TimeUnit.SECONDS)).isTrue();

        try {
            assertThatThrownBy(() -> service.processCatalogUpdated(VERSION_5))
                    .isInstanceOf(CatalogSynchronizationPendingException.class);
        } finally {
            releasePoll.countDown();
            poll.join(10_000);
        }

        verify(getWindow).getCatalogVersionWindow();
        verify(recordProcessed, never()).recordProcessedCatalogEvent(any());
    }

    // The apply of the snapshot is a mock, so the local version stays empty
    @Test
    void noLocalVersionAfterSynchronizingLeavesTheEventUnrecorded() {
        when(getVersion.getCatalogVersion()).thenReturn(Optional.empty());
        when(getSnapshot.getCatalogSnapshot()).thenReturn(new CatalogSnapshot(5L,
                Instant.parse("2026-09-10T13:44:59Z"), List.of(), List.of(), List.of()));

        assertThatThrownBy(() -> service.processCatalogUpdated(VERSION_5))
                .isInstanceOf(CatalogSynchronizationPendingException.class)
                .hasMessageContaining("no version");

        verify(recordProcessed, never()).recordProcessedCatalogEvent(any());
    }
}
