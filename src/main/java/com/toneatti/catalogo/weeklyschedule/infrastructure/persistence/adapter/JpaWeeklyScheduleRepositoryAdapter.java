package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.adapter;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.mapper.WeeklyScheduleMapper;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.repository.JpaWeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaWeeklyScheduleRepositoryAdapter implements WeeklyScheduleRepository {

    private final JpaWeeklyScheduleRepository jpaWeeklyScheduleRepository;
    private final WeeklyScheduleMapper weeklyScheduleMapper;

    @Override
    public WeeklySchedule save(WeeklySchedule weeklySchedule) {
        // The id comes from the cátedra, so save inserts or replaces the existing row
        WeeklyScheduleEntity weeklyScheduleEntity = weeklyScheduleMapper.toEntity(weeklySchedule);
        WeeklyScheduleEntity savedEntity = jpaWeeklyScheduleRepository.save(weeklyScheduleEntity);
        return weeklyScheduleMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<WeeklySchedule> findById(Long id) {
        return jpaWeeklyScheduleRepository.findById(id)
                .map(weeklyScheduleMapper::toDomainModel);
    }

    @Override
    public List<WeeklySchedule> findAll() {
        return jpaWeeklyScheduleRepository.findAll().stream()
                .map(weeklyScheduleMapper::toDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<WeeklySchedule> findByProfessionalId(Long professionalId) {
        return jpaWeeklyScheduleRepository.findByProfessionalId(professionalId).stream()
                .map(weeklyScheduleMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
