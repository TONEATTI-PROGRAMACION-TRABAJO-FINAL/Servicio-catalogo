package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.ports.out.ProcessedCatalogEventRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IsCatalogEventProcessedUseCaseImplTest {

    private final ProcessedCatalogEventRepository repository = mock(ProcessedCatalogEventRepository.class);
    private final IsCatalogEventProcessedUseCaseImpl useCase = new IsCatalogEventProcessedUseCaseImpl(repository);

    @Test
    void answersWhatTheRepositoryKnows() {
        when(repository.existsByEventId("catalog-event-7-4f3b")).thenReturn(true);

        assertThat(useCase.isCatalogEventProcessed("catalog-event-7-4f3b")).isTrue();
        assertThat(useCase.isCatalogEventProcessed("catalog-event-8-9a1c")).isFalse();
    }
}
