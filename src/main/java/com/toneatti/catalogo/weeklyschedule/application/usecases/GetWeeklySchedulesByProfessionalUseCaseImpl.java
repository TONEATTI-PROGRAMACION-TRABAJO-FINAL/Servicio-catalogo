package com.toneatti.catalogo.weeklyschedule.application.usecases;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.in.GetWeeklySchedulesByProfessionalUseCase;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetWeeklySchedulesByProfessionalUseCaseImpl implements GetWeeklySchedulesByProfessionalUseCase {

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    @Override
    public List<WeeklySchedule> getWeeklySchedulesByProfessional(Long professionalId) {
        // Only the schedules in force are used to build slots. An unknown professional gives an empty list:
        // checking that the professional exists would need the professional slice
        return weeklyScheduleRepository.findByProfessionalId(professionalId).stream()
                .filter(schedule -> Boolean.TRUE.equals(schedule.getEnabled()))
                .toList();
    }
}
