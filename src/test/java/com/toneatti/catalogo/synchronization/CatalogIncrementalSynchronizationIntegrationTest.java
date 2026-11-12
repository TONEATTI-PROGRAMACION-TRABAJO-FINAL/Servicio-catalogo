package com.toneatti.catalogo.synchronization;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.support.FakeHttpServer;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatalogIncrementUnusableException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogChangesRepository;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The incremental synchronization on the real H2 database, without Docker: the changes in Redis come from a mock of
 * the port and the fallback snapshot from a fake cátedra. Not transactional, so commits and rollbacks are real.
 */
@CatalogIntegrationTest
class CatalogIncrementalSynchronizationIntegrationTest {

    private static final FakeHttpServer CATEDRA = FakeHttpServer.start();
    private static final CatalogVersionWindow CURRENT_7_OLDEST_4 = new CatalogVersionWindow(7, 4);

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
    @MockitoBean
    private CatalogChangesRepository changes;
    @Autowired
    private ProfessionalCategoryRepository categories;
    @Autowired
    private ProfessionalRepository professionals;
    @MockitoSpyBean
    private WeeklyScheduleRepository schedules;
    @Autowired
    private CatalogVersionRepository versions;

    // Version 5 adds, 6 disables Ana and adds a schedule, 7 renames what 5 added
    private final CatalogIncrement version5 = new CatalogIncrement(5, List.of(category(3L, "Nutricion", true)),
            List.of(professional(17L, "Lucia", true)), List.of());
    private final CatalogIncrement version6 = new CatalogIncrement(6, List.of(),
            List.of(professional(15L, "Ana", false)), List.of(weeklySchedule(31L, DayOfWeek.FRIDAY, true)));
    private final CatalogIncrement version7 = new CatalogIncrement(7,
            List.of(category(3L, "Nutricion infantil", true)), List.of(), List.of());

    @BeforeEach
    void loadALocalCatalog() {
        CATEDRA.reset();
        categories.replaceAll(List.of(category(1L, "Clinica medica", true)));
        professionals.replaceAll(List.of(professional(15L, "Ana", true)));
        schedules.replaceAll(List.of(weeklySchedule(21L, DayOfWeek.MONDAY, true)));
        when(changes.getVersionWindow()).thenReturn(CURRENT_7_OLDEST_4);
        when(changes.getIncrement(5)).thenReturn(version5);
        when(changes.getIncrement(6)).thenReturn(version6);
        when(changes.getIncrement(7)).thenReturn(version7);
        CATEDRA.respond(SNAPSHOT_PATH, 200, SNAPSHOT);
    }

    // Section 18.2: local 3, current 7, oldest 4
    @Test
    void loadsTheSnapshotWithoutTryingChanges4WhenTheLocalVersionIsOlderThanTheOldest() {
        localVersion(3);

        service.synchronizeCatalog();

        verify(changes, never()).getIncrement(4);
        verify(changes, never()).getIncrement(anyLong());
        assertTheSnapshotWasLoaded();
    }

    @Test
    void appliesEveryMissingVersionInOrder() {
        localVersion(4);

        service.synchronizeCatalog();

        InOrder order = inOrder(changes);
        order.verify(changes).getIncrement(5);
        order.verify(changes).getIncrement(6);
        order.verify(changes).getIncrement(7);
        verify(changes, never()).getIncrement(4);
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId, ProfessionalCategory::getName)
                .containsExactlyInAnyOrder(tuple(1L, "Clinica medica"), tuple(3L, "Nutricion infantil"));
        assertThat(professionals.findAll()).extracting(Professional::getId, Professional::getEnabled)
                .containsExactlyInAnyOrder(tuple(15L, false), tuple(17L, true));
        assertThat(schedules.findAll()).extracting(WeeklySchedule::getId).containsExactlyInAnyOrder(21L, 31L);
        assertThat(versions.findCurrentVersion()).contains(7L);
        assertThat(CATEDRA.requestCount(SNAPSHOT_PATH)).isZero();
    }

    @Test
    void aFailureInsideAVersionLeavesNothingOfItAndStopsTheFollowingOnes() {
        localVersion(4);
        doThrow(new IllegalStateException("schedule failed"))
                .when(schedules).save(argThat(schedule -> schedule.getId() == 31L));

        assertThatThrownBy(service::synchronizeCatalog).isInstanceOf(IllegalStateException.class);

        // Version 5 is kept; Ana, disabled by version 6 before its schedule failed, is enabled again
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId, ProfessionalCategory::getName)
                .containsExactlyInAnyOrder(tuple(1L, "Clinica medica"), tuple(3L, "Nutricion"));
        assertThat(professionals.findAll()).extracting(Professional::getId, Professional::getEnabled)
                .containsExactlyInAnyOrder(tuple(15L, true), tuple(17L, true));
        assertThat(schedules.findAll()).extracting(WeeklySchedule::getId).containsExactly(21L);
        assertThat(versions.findCurrentVersion()).contains(5L);
        verify(changes, never()).getIncrement(7);
    }

    @Test
    void doesNothingWhenTheLocalVersionIsTheCurrentOne() {
        localVersion(7);

        service.synchronizeCatalog();

        verify(changes, never()).getIncrement(anyLong());
        assertThat(CATEDRA.requestCount(SNAPSHOT_PATH)).isZero();
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId).containsExactly(1L);
        assertThat(versions.findCurrentVersion()).contains(7L);
    }

    @Test
    void replacesALocalVersionNewerThanTheCurrentOneWithTheSnapshot() {
        localVersion(9);

        service.synchronizeCatalog();

        verify(changes, never()).getIncrement(anyLong());
        assertTheSnapshotWasLoaded();
    }

    @Test
    void fallsBackToTheSnapshotOnceWhenTheChangesOfAVersionAreMissing() {
        localVersion(4);
        when(changes.getIncrement(6)).thenThrow(new CatalogIncrementUnusableException("changes:6 is missing", null));

        service.synchronizeCatalog();

        verify(changes, never()).getIncrement(7);
        assertTheSnapshotWasLoaded();
    }

    @Test
    void fallsBackToTheSnapshotWhenTheVersionsInRedisAreUnusable() {
        localVersion(4);
        when(changes.getVersionWindow()).thenThrow(
                new CatalogIncrementUnusableException("current-version is missing", null));

        service.synchronizeCatalog();

        assertTheSnapshotWasLoaded();
    }

    @Test
    void leavesTheLocalCopyWhenRedisIsUnavailable() {
        localVersion(4);
        when(changes.getVersionWindow()).thenThrow(new CatedraUnavailableException("Redis read failed", null));

        assertThatThrownBy(service::synchronizeCatalog).isInstanceOf(CatedraUnavailableException.class);

        assertThat(CATEDRA.requestCount(SNAPSHOT_PATH)).isZero();
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId).containsExactly(1L);
        assertThat(versions.findCurrentVersion()).contains(4L);
    }

    private void localVersion(long version) {
        versions.save(version, null, Instant.parse("2026-09-01T00:00:00Z"));
    }

    private void assertTheSnapshotWasLoaded() {
        assertThat(CATEDRA.requestCount(SNAPSHOT_PATH)).isEqualTo(1);
        assertThat(versions.findCurrentVersion()).contains(SNAPSHOT_VERSION);
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(professionals.findAll()).extracting(Professional::getId).containsExactlyInAnyOrder(15L, 16L);
    }
}
