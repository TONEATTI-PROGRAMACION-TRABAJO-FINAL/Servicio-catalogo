package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.adapter;

import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.mapper.WeeklyScheduleMapper;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.repository.JpaWeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklyScheduleEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaWeeklyScheduleRepositoryAdapterTest {

    @Mock
    private JpaWeeklyScheduleRepository jpaRepository;

    private JpaWeeklyScheduleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaWeeklyScheduleRepositoryAdapter(jpaRepository, new WeeklyScheduleMapper());
    }

    @Test
    void saveKeepsTheAssignedIdAndReturnsTheSavedWeeklySchedule() {
        WeeklySchedule weeklySchedule = weeklySchedule(21L, DayOfWeek.MONDAY, true);
        when(jpaRepository.save(any(WeeklyScheduleEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        WeeklySchedule saved = adapter.save(weeklySchedule);

        ArgumentCaptor<WeeklyScheduleEntity> captor = ArgumentCaptor.forClass(WeeklyScheduleEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(weeklySchedule);
        assertThat(saved).usingRecursiveComparison().isEqualTo(weeklySchedule);
    }

    @Test
    void findByIdReturnsTheWeeklyScheduleWhenItExists() {
        WeeklyScheduleEntity entity = weeklyScheduleEntity(21L, DayOfWeek.MONDAY, true);
        when(jpaRepository.findById(21L)).thenReturn(Optional.of(entity));

        assertThat(adapter.findById(21L)).get().usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void findByIdReturnsEmptyWhenItDoesNotExist() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllReturnsEveryWeeklySchedule() {
        List<WeeklyScheduleEntity> entities = List.of(
                weeklyScheduleEntity(21L, DayOfWeek.MONDAY, true), weeklyScheduleEntity(22L, DayOfWeek.TUESDAY, false));
        when(jpaRepository.findAll()).thenReturn(entities);

        assertThat(adapter.findAll()).usingRecursiveComparison().isEqualTo(entities);
    }

    @Test
    void findByProfessionalIdReturnsTheSchedulesOfThatProfessional() {
        List<WeeklyScheduleEntity> entities = List.of(weeklyScheduleEntity(21L, DayOfWeek.MONDAY, true));
        when(jpaRepository.findByProfessionalId(15L)).thenReturn(entities);

        assertThat(adapter.findByProfessionalId(15L)).usingRecursiveComparison().isEqualTo(entities);
    }
}
