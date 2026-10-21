package com.toneatti.catalogo.weeklyschedule.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class WeeklySchedule {
    private Long id;
    private Long professionalId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
