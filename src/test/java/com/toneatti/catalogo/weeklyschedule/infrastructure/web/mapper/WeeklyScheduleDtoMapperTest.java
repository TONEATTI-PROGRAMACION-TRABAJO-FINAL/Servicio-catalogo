package com.toneatti.catalogo.weeklyschedule.infrastructure.web.mapper;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static org.assertj.core.api.Assertions.assertThat;

class WeeklyScheduleDtoMapperTest {

    private final WeeklyScheduleDtoMapper mapper = new WeeklyScheduleDtoMapper();

    @Test
    void mapsTheFieldsServicioTurnosNeedsToBuildSlots() {
        assertThat(mapper.toResponse(weeklySchedule(21L, DayOfWeek.MONDAY, true)))
                .extracting("id", "professionalId", "dayOfWeek", "startTime", "endTime", "slotDurationMinutes")
                .containsExactly(21L, 15L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 30), 30);
    }

    @Test
    void returnsNullForNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
