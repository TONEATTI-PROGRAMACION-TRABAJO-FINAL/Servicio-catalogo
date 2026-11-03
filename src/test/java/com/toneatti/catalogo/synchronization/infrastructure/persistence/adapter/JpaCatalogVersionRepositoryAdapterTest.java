package com.toneatti.catalogo.synchronization.infrastructure.persistence.adapter;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.CatalogSyncStateEntity;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaCatalogSyncStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

// Real H2 database, rolled back after each test
@CatalogIntegrationTest
@Transactional
class JpaCatalogVersionRepositoryAdapterTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-09-10T13:44:59Z");
    private static final Instant SYNCED_AT = Instant.parse("2026-09-10T13:45:03Z");

    @Autowired
    private JpaCatalogVersionRepositoryAdapter adapter;

    @Autowired
    private JpaCatalogSyncStateRepository jpaRepository;

    @BeforeEach
    void startWithoutVersion() {
        jpaRepository.deleteAll();
    }

    @Test
    void hasNoVersionBeforeTheFirstSnapshot() {
        assertThat(adapter.findCurrentVersion()).isEmpty();
    }

    @Test
    void savesTheVersionWithItsInstants() {
        adapter.save(7L, GENERATED_AT, SYNCED_AT);

        assertThat(adapter.findCurrentVersion()).contains(7L);
        assertThat(jpaRepository.findAll()).singleElement().satisfies(row -> {
            assertThat(row.getGeneratedAt()).isEqualTo(GENERATED_AT);
            assertThat(row.getSyncedAt()).isEqualTo(SYNCED_AT);
        });
    }

    @Test
    void aNewVersionReplacesThePreviousOneInTheSameRow() {
        adapter.save(7L, GENERATED_AT, SYNCED_AT);
        adapter.save(9L, GENERATED_AT.plusSeconds(60), SYNCED_AT.plusSeconds(60));

        assertThat(adapter.findCurrentVersion()).contains(9L);
        assertThat(jpaRepository.findAll()).extracting(CatalogSyncStateEntity::getId)
                .containsExactly(CatalogSyncStateEntity.SINGLE_ROW_ID);
    }
}
