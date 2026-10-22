package com.toneatti.catalogo.weeklyschedule.application.service;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.in.GetWeeklySchedulesByProfessionalUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyScheduleService {

    private final GetWeeklySchedulesByProfessionalUseCase getWeeklySchedulesByProfessionalUseCase;

    public List<WeeklySchedule> getWeeklySchedulesByProfessional(Long professionalId) {
        return getWeeklySchedulesByProfessionalUseCase.getWeeklySchedulesByProfessional(professionalId);
    }
}
