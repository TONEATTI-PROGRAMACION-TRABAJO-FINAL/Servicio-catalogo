package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

// Fields of the same type get different values, so a swapped constructor argument fails the tests
public final class WeeklyScheduleFixtures {

    private static final Long PROFESSIONAL_ID = 15L;
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(12, 30);
    private static final Integer SLOT_DURATION_MINUTES = 30;
    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    private WeeklyScheduleFixtures() {
    }

    public static WeeklySchedule weeklySchedule(Long id, DayOfWeek dayOfWeek, boolean enabled) {
        return new WeeklySchedule(id, PROFESSIONAL_ID, dayOfWeek, START_TIME, END_TIME, SLOT_DURATION_MINUTES,
                enabled, CREATED_AT, UPDATED_AT);
    }

    public static WeeklyScheduleEntity weeklyScheduleEntity(Long id, DayOfWeek dayOfWeek, boolean enabled) {
        return new WeeklyScheduleEntity(id, PROFESSIONAL_ID, dayOfWeek, START_TIME, END_TIME, SLOT_DURATION_MINUTES,
                enabled, CREATED_AT, UPDATED_AT);
    }
}
