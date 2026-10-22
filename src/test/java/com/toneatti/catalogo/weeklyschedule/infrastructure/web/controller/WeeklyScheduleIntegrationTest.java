package com.toneatti.catalogo.weeklyschedule.infrastructure.web.controller;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.entity.WeeklyScheduleEntity;
import com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.repository.JpaWeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.util.List;

import static com.toneatti.catalogo.ProblemDetailsMatchers.problem;
import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklyScheduleEntity;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CatalogIntegrationTest
class WeeklyScheduleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaWeeklyScheduleRepository jpaWeeklyScheduleRepository;

    @BeforeEach
    void cleanDatabase() {
        jpaWeeklyScheduleRepository.deleteAll();
    }

    @Test
    void listsTheEnabledSchedulesOfTheProfessionalInTheContractFormat() throws Exception {
        WeeklyScheduleEntity otherProfessional = weeklyScheduleEntity(23L, DayOfWeek.MONDAY, true);
        otherProfessional.setProfessionalId(16L);
        jpaWeeklyScheduleRepository.saveAll(List.of(weeklyScheduleEntity(21L, DayOfWeek.MONDAY, true),
                weeklyScheduleEntity(22L, DayOfWeek.TUESDAY, false), otherProfessional));

        mockMvc.perform(get("/weekly-schedules").param("professionalId", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(21)))
                .andExpect(jsonPath("$[0].professionalId").value(15))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("12:30:00"))
                .andExpect(jsonPath("$[0].slotDurationMinutes").value(30))
                .andExpect(jsonPath("$[0].enabled").doesNotExist())
                .andExpect(jsonPath("$[0].createdAt").doesNotExist());
    }

    @Test
    void returnsAnEmptyListForAProfessionalWithoutSchedules() throws Exception {
        mockMvc.perform(get("/weekly-schedules").param("professionalId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void returnsBadRequestWithoutProfessionalId() throws Exception {
        mockMvc.perform(get("/weekly-schedules"))
                .andExpect(problem(HttpStatus.BAD_REQUEST));
    }
}
