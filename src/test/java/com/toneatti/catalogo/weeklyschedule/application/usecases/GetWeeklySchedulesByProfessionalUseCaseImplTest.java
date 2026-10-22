package com.toneatti.catalogo.weeklyschedule.application.usecases;

import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.List;

import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWeeklySchedulesByProfessionalUseCaseImplTest {

    @Mock
    private WeeklyScheduleRepository repository;

    @InjectMocks
    private GetWeeklySchedulesByProfessionalUseCaseImpl useCase;

    @Test
    void returnsOnlyTheEnabledSchedulesOfTheProfessional() {
        when(repository.findByProfessionalId(15L)).thenReturn(List.of(
                weeklySchedule(21L, DayOfWeek.MONDAY, false), weeklySchedule(22L, DayOfWeek.TUESDAY, true)));

        assertThat(useCase.getWeeklySchedulesByProfessional(15L))
                .extracting("id")
                .containsExactly(22L);
    }
}
