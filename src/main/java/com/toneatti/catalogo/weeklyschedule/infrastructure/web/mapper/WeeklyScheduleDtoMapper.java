package com.toneatti.catalogo.weeklyschedule.infrastructure.web.mapper;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.infrastructure.web.dto.WeeklyScheduleResponse;
import org.springframework.stereotype.Component;

@Component
public class WeeklyScheduleDtoMapper {

    public WeeklyScheduleResponse toResponse(WeeklySchedule domain) {
        if (domain == null) {
            return null;
        }
        return WeeklyScheduleResponse.builder()
                .id(domain.getId())
                .professionalId(domain.getProfessionalId())
                .dayOfWeek(domain.getDayOfWeek())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .slotDurationMinutes(domain.getSlotDurationMinutes())
                .build();
    }
}
