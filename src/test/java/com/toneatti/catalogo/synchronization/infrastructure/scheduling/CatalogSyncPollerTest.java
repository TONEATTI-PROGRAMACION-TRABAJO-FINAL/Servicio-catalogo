package com.toneatti.catalogo.synchronization.infrastructure.scheduling;

import com.toneatti.catalogo.config.CatalogSyncProperties;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CatalogSyncPollerTest {

    private static final Duration INTERVAL = Duration.ofSeconds(45);

    private final CatalogSynchronizationService service = mock(CatalogSynchronizationService.class);
    private final CatalogSyncPoller poller = new CatalogSyncPoller(service, new CatalogSyncProperties(true, 8,
            Duration.ofSeconds(2), Duration.ofSeconds(60), true, INTERVAL));

    @Test
    void schedulesTheSynchronizationEveryIntervalStartingOneIntervalAfterStartup() {
        ScheduledTaskRegistrar registrar = new ScheduledTaskRegistrar();

        poller.configureTasks(registrar);

        assertThat(registrar.getFixedDelayTaskList()).singleElement().satisfies(task -> {
            assertThat(task.getIntervalDuration()).isEqualTo(INTERVAL);
            assertThat(task.getInitialDelayDuration()).isEqualTo(INTERVAL);
        });
        IntervalTask task = registrar.getFixedDelayTaskList().getFirst();
        task.getRunnable().run();
        verify(service).synchronizeCatalog();
    }

    // A failure must not stop the schedule: the next interval is the retry
    @ParameterizedTest
    @ValueSource(strings = {"unavailable", "authentication", "unexpected"})
    void keepsPollingAfterAFailure(String failure) {
        RuntimeException exception = switch (failure) {
            case "unavailable" -> new CatedraUnavailableException("Redis read failed", null);
            case "authentication" -> new CatedraAuthenticationException("Catedra rejected the technical JWT", null);
            default -> new IllegalStateException("database down");
        };
        doThrow(exception).when(service).synchronizeCatalog();

        assertThatCode(poller::poll).doesNotThrowAnyException();
        assertThatCode(poller::poll).doesNotThrowAnyException();

        verify(service, times(2)).synchronizeCatalog();
    }
}
