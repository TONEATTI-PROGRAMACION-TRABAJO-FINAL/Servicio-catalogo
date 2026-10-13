package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.mapper;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyScheduleMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    private final WeeklyScheduleMapper mapper = new WeeklyScheduleMapper();

    @Test
    void toEntityCopiesAllFields() {
        WeeklySchedule domain = WeeklySchedule.builder()
                .id(21L)
                .professionalId(15L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        WeeklyScheduleEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(21L);
        assertThat(entity.getProfessionalId()).isEqualTo(15L);
        assertThat(entity.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(entity.getSlotDurationMinutes()).isEqualTo(30);
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void toDomainModelCopiesAllFields() {
        WeeklyScheduleEntity entity = WeeklyScheduleEntity.builder()
                .id(22L)
                .professionalId(16L)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(18, 0))
                .slotDurationMinutes(20)
                .enabled(false)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        WeeklySchedule domain = mapper.toDomainModel(entity);

        assertThat(domain.getId()).isEqualTo(22L);
        assertThat(domain.getProfessionalId()).isEqualTo(16L);
        assertThat(domain.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(domain.getStartTime()).isEqualTo(LocalTime.of(14, 30));
        assertThat(domain.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(domain.getSlotDurationMinutes()).isEqualTo(20);
        assertThat(domain.getEnabled()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(domain.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void toEntityReturnsNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomainModelReturnsNullForNullInput() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
