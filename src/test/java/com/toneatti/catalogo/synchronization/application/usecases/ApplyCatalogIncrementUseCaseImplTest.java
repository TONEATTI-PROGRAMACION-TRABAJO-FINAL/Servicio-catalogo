package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApplyCatalogIncrementUseCaseImplTest {

    private final ProfessionalCategoryRepository categoryRepository = mock(ProfessionalCategoryRepository.class);
    private final ProfessionalRepository professionalRepository = mock(ProfessionalRepository.class);
    private final WeeklyScheduleRepository scheduleRepository = mock(WeeklyScheduleRepository.class);
    private final CatalogVersionRepository versionRepository = mock(CatalogVersionRepository.class);
    private ApplyCatalogIncrementUseCaseImpl useCase;

    private final ProfessionalCategory disabledCategory = category(1L, "Clinica medica", false);
    private final Professional ana = professional(15L, "Ana", true);
    private final Professional luis = professional(16L, "Luis", false);
    private final WeeklySchedule monday = weeklySchedule(21L, DayOfWeek.MONDAY, true);

    @BeforeEach
    void setUp() {
        useCase = new ApplyCatalogIncrementUseCaseImpl(categoryRepository, professionalRepository, scheduleRepository,
                versionRepository);
    }

    @Test
    void upsertsEveryEntityAndThenAdvancesTheVersionWithoutGenerationInstant() {
        Instant before = Instant.now();

        useCase.applyCatalogIncrement(new CatalogIncrement(6, List.of(disabledCategory), List.of(ana, luis),
                List.of(monday)));

        InOrder order = inOrder(categoryRepository, professionalRepository, scheduleRepository,
                versionRepository);
        order.verify(categoryRepository).save(disabledCategory);
        order.verify(professionalRepository).save(ana);
        order.verify(professionalRepository).save(luis);
        order.verify(scheduleRepository).save(monday);
        ArgumentCaptor<Instant> syncedAt = ArgumentCaptor.forClass(Instant.class);
        order.verify(versionRepository).save(eq(6L), isNull(), syncedAt.capture());
        assertThat(syncedAt.getValue()).isBetween(before, Instant.now());
    }

    @Test
    void advancesTheVersionEvenWhenItChangesNothingLocally() {
        useCase.applyCatalogIncrement(new CatalogIncrement(7, List.of(), List.of(), List.of()));

        verifyNoInteractions(categoryRepository, professionalRepository, scheduleRepository);
        verify(versionRepository).save(eq(7L), isNull(), any());
    }

    @Test
    void doesNotAdvanceTheVersionWhenAnEntityFails() {
        when(scheduleRepository.save(monday)).thenThrow(new IllegalStateException("database down"));

        assertThatThrownBy(() -> useCase.applyCatalogIncrement(
                new CatalogIncrement(6, List.of(), List.of(ana), List.of(monday))))
                .isInstanceOf(IllegalStateException.class);

        verify(versionRepository, never()).save(anyLong(), any(), any());
    }
}
