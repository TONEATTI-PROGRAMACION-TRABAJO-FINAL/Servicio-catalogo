package com.toneatti.catalogo.synchronization.infrastructure.rest.mapper;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraSnapshotResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalCategoryResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.mapper.CatedraCatalogItemMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatedraSnapshotMapperTest {

    private static final Instant UPDATED = Instant.parse("2026-09-10T13:40:00Z");

    private final CatedraSnapshotMapper mapper = new CatedraSnapshotMapper(new CatedraCatalogItemMapper());

    @Test
    void mapsTheVersionAndLeavesOutNullEntries() {
        var category = new CatedraProfessionalCategoryResponse(1L, "Clinica medica", null, true,
                Instant.parse("2026-07-01T12:00:00Z"), UPDATED);
        var response = new CatedraSnapshotResponse(7L, UPDATED, Arrays.asList(category, null), List.of(), List.of());

        CatalogSnapshot snapshot = mapper.toDomainModel(response);

        assertThat(snapshot.snapshotVersion()).isEqualTo(7L);
        assertThat(snapshot.generatedAt()).isEqualTo(UPDATED);
        assertThat(snapshot.professionalCategories()).singleElement().usingRecursiveComparison().isEqualTo(category);
        assertThat(snapshot.professionals()).isEmpty();
        assertThat(snapshot.weeklySchedules()).isEmpty();
    }

    @Test
    void keepsAMissingCollectionAsNullSoTheAdapterCanRejectIt() {
        var response = new CatedraSnapshotResponse(7L, UPDATED, null, List.of(), List.of());

        assertThat(mapper.toDomainModel(response).professionalCategories()).isNull();
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
