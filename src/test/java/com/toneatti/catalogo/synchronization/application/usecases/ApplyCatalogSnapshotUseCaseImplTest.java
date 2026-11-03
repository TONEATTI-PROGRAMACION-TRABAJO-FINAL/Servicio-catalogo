package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplyCatalogSnapshotUseCaseImplTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-09-10T13:44:59Z");

    @Mock
    private ProfessionalCategoryRepository categories;
    @Mock
    private ProfessionalRepository professionals;
    @Mock
    private WeeklyScheduleRepository schedules;
    @Mock
    private CatalogVersionRepository versions;

    @InjectMocks
    private ApplyCatalogSnapshotUseCaseImpl useCase;

    private final List<ProfessionalCategory> categoryList = List.of(category(1L, "Clinica medica", false));
    private final List<Professional> professionalList = List.of(professional(15L, "Ana", true));
    private final List<WeeklySchedule> scheduleList = List.of(weeklySchedule(21L, DayOfWeek.MONDAY, true));
    private final CatalogSnapshot snapshot =
            new CatalogSnapshot(7L, GENERATED_AT, categoryList, professionalList, scheduleList);

    @Test
    void replacesTheThreeCollectionsAndThenSavesTheVersion() {
        Instant before = Instant.now();

        useCase.applyCatalogSnapshot(snapshot);

        InOrder order = inOrder(categories, professionals, schedules, versions);
        order.verify(categories).replaceAll(categoryList);
        order.verify(professionals).replaceAll(professionalList);
        order.verify(schedules).replaceAll(scheduleList);
        ArgumentCaptor<Instant> syncedAt = ArgumentCaptor.forClass(Instant.class);
        order.verify(versions).save(eq(7L), eq(GENERATED_AT), syncedAt.capture());
        assertThat(syncedAt.getValue()).isBetween(before, Instant.now());
    }

    @Test
    void doesNotSaveTheVersionWhenACollectionFails() {
        doThrow(new IllegalStateException("database down")).when(schedules).replaceAll(scheduleList);

        assertThatThrownBy(() -> useCase.applyCatalogSnapshot(snapshot)).isInstanceOf(IllegalStateException.class);

        verify(versions, never()).save(anyLong(), any(), any());
    }
}
