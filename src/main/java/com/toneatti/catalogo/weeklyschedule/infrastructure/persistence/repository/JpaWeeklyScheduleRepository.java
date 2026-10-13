package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.repository;

import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaWeeklyScheduleRepository extends JpaRepository<WeeklyScheduleEntity, Long> {
}
