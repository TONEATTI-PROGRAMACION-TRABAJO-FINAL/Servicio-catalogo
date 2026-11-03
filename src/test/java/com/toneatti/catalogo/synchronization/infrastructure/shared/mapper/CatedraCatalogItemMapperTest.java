package com.toneatti.catalogo.synchronization.infrastructure.shared.mapper;

import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalCategoryResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraWeeklyScheduleResponse;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class CatedraCatalogItemMapperTest {

    private static final Instant CREATED = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED = Instant.parse("2026-09-10T13:40:00Z");

    private final CatedraCatalogItemMapper mapper = new CatedraCatalogItemMapper();

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
    void returnsNullForNullInput() {
        assertThat(mapper.toDomainModel((CatedraProfessionalCategoryResponse) null)).isNull();
        assertThat(mapper.toDomainModel((CatedraProfessionalResponse) null)).isNull();
        assertThat(mapper.toDomainModel((CatedraWeeklyScheduleResponse) null)).isNull();
    }
}
