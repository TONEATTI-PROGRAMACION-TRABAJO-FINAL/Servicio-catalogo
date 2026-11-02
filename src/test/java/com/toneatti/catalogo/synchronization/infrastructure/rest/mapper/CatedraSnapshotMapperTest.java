package com.toneatti.catalogo.synchronization.infrastructure.rest.mapper;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraProfessionalCategoryResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraProfessionalResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraSnapshotResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraWeeklyScheduleResponse;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatedraSnapshotMapperTest {

    private static final Instant CREATED = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED = Instant.parse("2026-09-10T13:40:00Z");

    private final CatedraSnapshotMapper mapper = new CatedraSnapshotMapper();

    @Test
    void mapsEveryFieldOfACategory() {
        var response = new CatedraProfessionalCategoryResponse(1L, "Clinica medica", "General", false,
                CREATED, UPDATED);

        assertThat(mapper.toDomainModel(response)).usingRecursiveComparison().isEqualTo(response);
    }

    @Test
    void mapsEveryFieldOfAProfessional() {
        var response = new CatedraProfessionalResponse(15L, 1L, "Ana", "Gomez", true, CREATED, UPDATED);

        assertThat(mapper.toDomainModel(response)).usingRecursiveComparison().isEqualTo(response);
    }

    @Test
    void mapsEveryFieldOfAWeeklySchedule() {
        var response = new CatedraWeeklyScheduleResponse(21L, 15L, DayOfWeek.MONDAY, LocalTime.of(9, 0),
                LocalTime.of(12, 0), 30, true, CREATED, UPDATED);

        assertThat(mapper.toDomainModel(response)).usingRecursiveComparison().isEqualTo(response);
    }

    @Test
    void mapsTheVersionAndLeavesOutNullEntries() {
        var category = new CatedraProfessionalCategoryResponse(1L, "Clinica medica", null, true, CREATED, UPDATED);
        var response = new CatedraSnapshotResponse(7L, UPDATED, Arrays.asList(category, null), List.of(), List.of());

        CatalogSnapshot snapshot = mapper.toDomainModel(response);

        assertThat(snapshot.snapshotVersion()).isEqualTo(7L);
        assertThat(snapshot.generatedAt()).isEqualTo(UPDATED);
        assertThat(snapshot.professionalCategories()).singleElement().usingRecursiveComparison().isEqualTo(category);
        assertThat(snapshot.professionals()).isEmpty();
        assertThat(snapshot.weeklySchedules()).isEmpty();
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toDomainModel((CatedraSnapshotResponse) null)).isNull();
        assertThat(mapper.toDomainModel((CatedraProfessionalCategoryResponse) null)).isNull();
        assertThat(mapper.toDomainModel((CatedraProfessionalResponse) null)).isNull();
        assertThat(mapper.toDomainModel((CatedraWeeklyScheduleResponse) null)).isNull();
    }
}
