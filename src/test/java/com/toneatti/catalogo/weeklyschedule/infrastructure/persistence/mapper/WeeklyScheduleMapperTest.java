package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.mapper;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklyScheduleEntity;
import static org.assertj.core.api.Assertions.assertThat;

class WeeklyScheduleMapperTest {

    private final WeeklyScheduleMapper mapper = new WeeklyScheduleMapper();

    @Test
    void toEntityCopiesAllFields() {
        WeeklySchedule domain = weeklySchedule(21L, DayOfWeek.MONDAY, true);

        assertThat(mapper.toEntity(domain)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomainModelCopiesAllFields() {
        WeeklyScheduleEntity entity = weeklyScheduleEntity(22L, DayOfWeek.FRIDAY, false);

        assertThat(mapper.toDomainModel(entity)).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
