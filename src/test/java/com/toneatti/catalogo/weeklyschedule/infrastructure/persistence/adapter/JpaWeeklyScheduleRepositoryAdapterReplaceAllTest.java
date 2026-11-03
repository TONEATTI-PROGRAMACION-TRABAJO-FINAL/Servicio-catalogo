package com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.adapter;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * replaceAll on the real H2 database, so the behaviour of deleteByIdNotIn in this Hibernate version is covered,
 * empty list included. Each test is rolled back.
 */
@CatalogIntegrationTest
@Transactional
class JpaWeeklyScheduleRepositoryAdapterReplaceAllTest {

    @Autowired
    private JpaWeeklyScheduleRepositoryAdapter adapter;

    @Test
    void emptyListLeavesNoSchedules() {
        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY)
                .forEach(day -> adapter.save(weeklySchedule((long) day.getValue(), day, true)));

        adapter.replaceAll(List.of());

        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    void schedulesMissingFromTheListAreDeletedAndTheOthersReplaced() {
        adapter.save(weeklySchedule(21L, DayOfWeek.MONDAY, true));
        adapter.save(weeklySchedule(22L, DayOfWeek.WEDNESDAY, true));
        WeeklySchedule disabled = weeklySchedule(22L, DayOfWeek.THURSDAY, false);
        WeeklySchedule added = weeklySchedule(23L, DayOfWeek.SATURDAY, true);

        adapter.replaceAll(List.of(disabled, added));

        assertThat(adapter.findAll()).usingRecursiveComparison().ignoringCollectionOrder()
                .isEqualTo(List.of(disabled, added));
    }
}
