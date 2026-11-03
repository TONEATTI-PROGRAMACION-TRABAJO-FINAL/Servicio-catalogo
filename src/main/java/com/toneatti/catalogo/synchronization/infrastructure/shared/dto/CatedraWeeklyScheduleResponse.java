package com.toneatti.catalogo.synchronization.infrastructure.shared.dto;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

/**
 * {@code startTime} and {@code endTime} are local times of attention, as HH:mm or HH:mm:ss (section 4).
 */
public record CatedraWeeklyScheduleResponse(
        Long id,
        Long professionalId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Integer slotDurationMinutes,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
