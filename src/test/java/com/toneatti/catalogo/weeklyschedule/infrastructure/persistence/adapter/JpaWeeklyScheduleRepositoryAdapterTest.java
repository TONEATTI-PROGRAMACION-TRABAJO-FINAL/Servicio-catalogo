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
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaWeeklyScheduleRepositoryAdapterTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    @Mock
    private JpaWeeklyScheduleRepository jpaRepository;

    private JpaWeeklyScheduleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaWeeklyScheduleRepositoryAdapter(jpaRepository, new WeeklyScheduleMapper());
    }

    @Test
    void saveKeepsTheAssignedIdAndReturnsTheSavedWeeklySchedule() {
        WeeklySchedule weeklySchedule = WeeklySchedule.builder()
                .id(21L)
                .professionalId(15L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
        when(jpaRepository.save(any(WeeklyScheduleEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        WeeklySchedule saved = adapter.save(weeklySchedule);

        ArgumentCaptor<WeeklyScheduleEntity> captor = ArgumentCaptor.forClass(WeeklyScheduleEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(21L);
        assertThat(saved.getId()).isEqualTo(21L);
        assertThat(saved.getProfessionalId()).isEqualTo(15L);
        assertThat(saved.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(saved.getSlotDurationMinutes()).isEqualTo(30);
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void findByIdReturnsTheWeeklyScheduleWhenItExists() {
        when(jpaRepository.findById(21L)).thenReturn(Optional.of(entity(21L, DayOfWeek.MONDAY)));

        Optional<WeeklySchedule> result = adapter.findById(21L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(21L);
        assertThat(result.get().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void findByIdReturnsEmptyWhenItDoesNotExist() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllReturnsEveryWeeklySchedule() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity(21L, DayOfWeek.MONDAY), entity(22L, DayOfWeek.TUESDAY)));

        List<WeeklySchedule> result = adapter.findAll();

        assertThat(result).extracting(WeeklySchedule::getId).containsExactly(21L, 22L);
        assertThat(result).extracting(WeeklySchedule::getDayOfWeek).containsExactly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
    }

    private static WeeklyScheduleEntity entity(Long id, DayOfWeek dayOfWeek) {
        return WeeklyScheduleEntity.builder()
                .id(id)
                .professionalId(15L)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }
}
