package com.toneatti.catalogo.weeklyschedule.domain.ports.in;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;

import java.util.List;

public interface GetWeeklySchedulesByProfessionalUseCase {
    List<WeeklySchedule> getWeeklySchedulesByProfessional(Long professionalId);
}
