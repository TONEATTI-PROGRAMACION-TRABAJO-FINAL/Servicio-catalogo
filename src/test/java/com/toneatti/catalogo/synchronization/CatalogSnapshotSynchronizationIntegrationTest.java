package com.toneatti.catalogo.synchronization;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.support.FakeHttpServer;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaCatalogSyncStateRepository;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_PATH;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_VERSION;
import static com.toneatti.catalogo.weeklyschedule.infrastructure.persistence.WeeklyScheduleFixtures.weeklySchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

/**
 * Full context on the real H2 database against a fake cátedra. Not transactional, so commits and rollbacks are the
 * real ones of applying the snapshot.
 */
@CatalogIntegrationTest
class CatalogSnapshotSynchronizationIntegrationTest {

    private static final FakeHttpServer CATEDRA = FakeHttpServer.start();
    private static final long PREVIOUS_VERSION = 5L;

    @DynamicPropertySource
    static void pointToTheFakeCatedra(DynamicPropertyRegistry registry) {
        registry.add("catedra.api-base-url", CATEDRA::baseUrl);
    }

    @AfterAll
    static void stopCatedra() {
        CATEDRA.close();
    }

    @Autowired
    private CatalogSynchronizationService service;
    @Autowired
    private ProfessionalCategoryRepository categories;
    @Autowired
    private ProfessionalRepository professionals;
    @MockitoSpyBean
    private WeeklyScheduleRepository schedules;
    @Autowired
    private CatalogVersionRepository versions;
    @Autowired
    private JpaCatalogSyncStateRepository syncStateRows;

    // A previous copy: id 1 with an outdated name, plus ids the snapshot no longer has
    @BeforeEach
    void loadAPreviousCatalog() {
        CATEDRA.reset();
        categories.replaceAll(List.of(category(1L, "Nombre viejo", true), category(90L, "Sobrante", true)));
        professionals.replaceAll(List.of(professional(91L, "Sobrante", true)));
        schedules.replaceAll(List.of(weeklySchedule(92L, DayOfWeek.SUNDAY, true)));
        versions.save(PREVIOUS_VERSION, Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2026-09-01T00:00:01Z"));
    }

    @Test
    void leavesTheLocalCopyEqualToTheSnapshot() {
        CATEDRA.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        service.synchronizeCatalogFromSnapshot();

        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId, ProfessionalCategory::getName,
                        ProfessionalCategory::getEnabled)
                .containsExactlyInAnyOrder(tuple(1L, "Clinica medica", true), tuple(2L, "Pediatria", false));
        assertThat(professionals.findAll()).extracting(Professional::getId, Professional::getEnabled)
                .containsExactlyInAnyOrder(tuple(15L, true), tuple(16L, false));
        assertThat(schedules.findAll()).extracting(WeeklySchedule::getId, WeeklySchedule::getEnabled)
                .containsExactlyInAnyOrder(tuple(21L, true), tuple(22L, false));
        assertThat(versions.findCurrentVersion()).contains(SNAPSHOT_VERSION);
    }

    @Test
    void rollsBackEverythingWhenTheSchedulesFailAfterTheOtherCollections() {
        CATEDRA.respond(SNAPSHOT_PATH, 200, SNAPSHOT);
        doThrow(new IllegalStateException("schedules failed")).when(schedules).replaceAll(anyList());

        assertThatThrownBy(service::synchronizeCatalogFromSnapshot).isInstanceOf(IllegalStateException.class);

        assertThePreviousCatalogIsIntact();
    }

    @Test
    void leavesTheDatabaseUntouchedWhenTheCatedraIsUnavailable() {
        CATEDRA.respond(SNAPSHOT_PATH, 503, "{\"status\":503}");

        assertThatThrownBy(service::synchronizeCatalogFromSnapshot).isInstanceOf(CatedraUnavailableException.class);

        assertThePreviousCatalogIsIntact();
    }

    @Test
    void synchronizingWithoutALocalVersionLoadsTheSnapshot() {
        syncStateRows.deleteAll();
        CATEDRA.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        service.synchronizeCatalog();

        assertThat(versions.findCurrentVersion()).contains(SNAPSHOT_VERSION);
        assertThat(professionals.findAll()).extracting(Professional::getId).containsExactlyInAnyOrder(15L, 16L);
    }

    private void assertThePreviousCatalogIsIntact() {
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId, ProfessionalCategory::getName)
                .containsExactlyInAnyOrder(tuple(1L, "Nombre viejo"), tuple(90L, "Sobrante"));
        assertThat(professionals.findAll()).extracting(Professional::getId).containsExactly(91L);
        assertThat(schedules.findAll()).extracting(WeeklySchedule::getId).containsExactly(92L);
        assertThat(versions.findCurrentVersion()).contains(PREVIOUS_VERSION);
    }
}
