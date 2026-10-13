package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "weekly_schedules")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyScheduleEntity {
    @Id
    private Long id; // Assigned by the cátedra, not generated locally
    private Long professionalId; // Plain id, no JPA relation to the professional slice
    @Enumerated(EnumType.STRING) // Stored as MONDAY..SUNDAY, same values as the cátedra
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
