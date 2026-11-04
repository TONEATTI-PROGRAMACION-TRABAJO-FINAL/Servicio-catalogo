package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.domain.ports.out.ProcessedCatalogEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecordProcessedCatalogEventUseCaseImplTest {

    private static final Instant PROCESSED_AT = Instant.parse("2026-09-10T13:45:03Z");

    private final ProcessedCatalogEventRepository repository = mock(ProcessedCatalogEventRepository.class);
    private final RecordProcessedCatalogEventUseCaseImpl useCase =
            new RecordProcessedCatalogEventUseCaseImpl(repository, Duration.ofDays(7));

    @Test
    void recordsTheEventAndThenDeletesTheOnesOlderThanTheRetention() {
        ProcessedCatalogEvent event = new ProcessedCatalogEvent(null, "catalog-event-7-4f3b", 7, PROCESSED_AT);
        ProcessedCatalogEvent recorded = new ProcessedCatalogEvent(1L, "catalog-event-7-4f3b", 7, PROCESSED_AT);
        when(repository.create(event)).thenReturn(recorded);

        assertThat(useCase.recordProcessedCatalogEvent(event)).isEqualTo(recorded);

        InOrder order = inOrder(repository);
        order.verify(repository).create(event);
        order.verify(repository).deleteProcessedBefore(Instant.parse("2026-09-03T13:45:03Z"));
    }

    @Test
    void rejectsARetentionThatIsNotPositive() {
        assertThatThrownBy(() -> new RecordProcessedCatalogEventUseCaseImpl(repository, Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("processed-events-retention");
        assertThatThrownBy(() -> new RecordProcessedCatalogEventUseCaseImpl(repository, Duration.ofDays(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
