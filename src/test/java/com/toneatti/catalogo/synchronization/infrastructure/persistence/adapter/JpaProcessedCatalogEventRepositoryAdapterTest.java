package com.toneatti.catalogo.synchronization.infrastructure.persistence.adapter;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.ProcessedCatalogEventEntity;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaProcessedCatalogEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Against the H2 schema Hibernate generates, so the unique constraint on the eventId is the real one. Not
 * transactional: a failed flush marks the transaction for rollback, and here it must leave the first row.
 */
@CatalogIntegrationTest
class JpaProcessedCatalogEventRepositoryAdapterTest {

    private static final Instant PROCESSED_AT = Instant.parse("2026-09-10T13:45:03Z");

    @Autowired
    private JpaProcessedCatalogEventRepositoryAdapter adapter;

    @Autowired
    private JpaProcessedCatalogEventRepository jpaRepository;

    @AfterEach
    void deleteEvents() {
        jpaRepository.deleteAll();
    }

    @Test
    void recordsAnEventWithIdAndFindsItByItsEventId() {
        ProcessedCatalogEvent created = adapter.create(processed("catalog-event-7-4f3b", PROCESSED_AT));

        assertThat(created.id()).isNotNull();
        assertThat(created).usingRecursiveComparison().ignoringFields("id")
                .isEqualTo(processed("catalog-event-7-4f3b", PROCESSED_AT));
        assertThat(adapter.existsByEventId("catalog-event-7-4f3b")).isTrue();
        assertThat(adapter.existsByEventId("catalog-event-8-9a1c")).isFalse();
    }

    // Two consumers processing the same event at the same time: the second one fails and rolls back
    @Test
    void rejectsTheSameEventIdTwice() {
        adapter.create(processed("catalog-event-7-4f3b", PROCESSED_AT));

        assertThatThrownBy(() -> adapter.create(processed("catalog-event-7-4f3b", PROCESSED_AT.plusSeconds(1))))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jpaRepository.count()).isEqualTo(1);
    }

    @Test
    void deletesOnlyTheEventsProcessedBeforeTheCutoff() {
        adapter.create(processed("catalog-event-5-old", PROCESSED_AT.minusSeconds(1)));
        adapter.create(processed("catalog-event-6-edge", PROCESSED_AT));
        adapter.create(processed("catalog-event-7-new", PROCESSED_AT.plusSeconds(1)));

        assertThat(adapter.deleteProcessedBefore(PROCESSED_AT)).isEqualTo(1);

        assertThat(jpaRepository.findAll()).extracting(ProcessedCatalogEventEntity::getEventId)
                .containsExactlyInAnyOrder("catalog-event-6-edge", "catalog-event-7-new");
    }

    private static ProcessedCatalogEvent processed(String eventId, Instant processedAt) {
        return new ProcessedCatalogEvent(null, eventId, 7, processedAt);
    }
}
