package com.toneatti.catalogo.weeklyschedule.domain.ports.out;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;

import java.util.List;
import java.util.Optional;

public interface WeeklyScheduleRepository {

    WeeklySchedule save(WeeklySchedule weeklySchedule);
    Optional<WeeklySchedule> findById(Long id);
    List<WeeklySchedule> findAll();
    List<WeeklySchedule> findByProfessionalId(Long professionalId);
    // Leaves exactly these schedules: saves them with their ids and deletes the ones not in the list
    void replaceAll(List<WeeklySchedule> weeklySchedules);

}
