package com.toneatti.catalogo.synchronization.infrastructure.rest.adapter;

import com.toneatti.catalogo.config.CatedraIntegrationProperties;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.support.FakeHttpServer;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.infrastructure.rest.config.CatedraRestClientConfig;
import com.toneatti.catalogo.synchronization.infrastructure.rest.mapper.CatedraSnapshotMapper;
import com.toneatti.catalogo.synchronization.infrastructure.shared.mapper.CatedraCatalogItemMapper;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.EMPTY_SNAPSHOT;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_PATH;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(OutputCaptureExtension.class)
class RestCatedraRepositoryAdapterTest {

    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);
    private static final String JWT = "test-technical-jwt";

    private FakeHttpServer catedra;
    private RestCatedraRepositoryAdapter adapter;

    @BeforeEach
    void startCatedra() {
        catedra = FakeHttpServer.start();
        adapter = adapterFor(catedra.baseUrl());
    }

    @AfterEach
    void stopCatedra() {
        catedra.close();
    }

    @Test
    void mapsTheThreeCollectionsIncludingTheDisabledEntities() {
        catedra.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        CatalogSnapshot snapshot = adapter.getSnapshot();

        assertThat(snapshot.snapshotVersion()).isEqualTo(SNAPSHOT_VERSION);
        assertThat(snapshot.generatedAt()).isEqualTo(Instant.parse("2026-09-10T13:44:59Z"));
        assertThat(snapshot.professionalCategories()).extracting(ProfessionalCategory::getId,
                ProfessionalCategory::getEnabled).containsExactly(tuple(1L, true), tuple(2L, false));
        assertThat(snapshot.professionals()).extracting(Professional::getId, Professional::getEnabled)
                .containsExactly(tuple(15L, true), tuple(16L, false));
        assertThat(snapshot.weeklySchedules()).extracting(WeeklySchedule::getId, WeeklySchedule::getEnabled)
                .containsExactly(tuple(21L, true), tuple(22L, false));
    }

    @Test
    void acceptsStartTimesWithAndWithoutSecondsAndIgnoresUnknownFields() {
        catedra.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        WeeklySchedule friday = adapter.getSnapshot().weeklySchedules().get(1);

        assertThat(friday.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(friday.getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(friday.getEndTime()).isEqualTo(LocalTime.of(18, 30));
    }

    @Test
    void acceptsASnapshotWithEmptyCollections() {
        catedra.respond(SNAPSHOT_PATH, 200, EMPTY_SNAPSHOT);

        CatalogSnapshot snapshot = adapter.getSnapshot();

        assertThat(snapshot.snapshotVersion()).isEqualTo(8L);
        assertThat(snapshot.professionalCategories()).isEmpty();
        assertThat(snapshot.professionals()).isEmpty();
        assertThat(snapshot.weeklySchedules()).isEmpty();
    }

    @Test
    void sendsTheTechnicalJwt() {
        catedra.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        adapter.getSnapshot();

        assertThat(catedra.lastRequestHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + JWT);
    }

    @Test
    void failsAsAuthenticationWhenTheJwtIsRejectedWithoutLeakingIt(CapturedOutput output) {
        catedra.respond(SNAPSHOT_PATH, 401, "{\"status\":401,\"title\":\"Unauthorized\"}");

        assertThatGettingTheSnapshotFails()
                .isInstanceOf(CatedraAuthenticationException.class)
                .hasCauseInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("GET " + SNAPSHOT_PATH)
                .hasMessageContaining("401")
                .message().doesNotContain(JWT);
        assertThat(output).doesNotContain(JWT);
    }

    @Test
    void failsAsAuthenticationWhenAccessIsForbiddenWithoutACode() {
        catedra.respond(SNAPSHOT_PATH, 403, "{\"status\":403,\"title\":\"Forbidden\"}");

        assertThatGettingTheSnapshotFails().isInstanceOf(CatedraAuthenticationException.class);
    }

    @Test
    void failsAsAuthenticationWhenTheJwtHasNoIntegration() {
        catedra.respondProblem(SNAPSHOT_PATH, 403, "STUDENT_INTEGRATION_NOT_FOUND");

        assertThatGettingTheSnapshotFails()
                .isInstanceOf(CatedraAuthenticationException.class)
                .hasMessageContaining("403 STUDENT_INTEGRATION_NOT_FOUND");
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 502, 503})
    void failsAsUnavailableOnAServerError(int status) {
        catedra.respond(SNAPSHOT_PATH, status, "{\"status\":" + status + "}");

        assertThatGettingTheSnapshotFails()
                .isInstanceOf(CatedraUnavailableException.class)
                .hasMessageContaining("GET " + SNAPSHOT_PATH)
                .hasCauseInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void failsAsUnavailableOnAnUnexpectedClientError() {
        catedra.respondProblem(SNAPSHOT_PATH, 404, "NOT_FOUND");

        assertThatGettingTheSnapshotFails()
                .isInstanceOf(CatedraUnavailableException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void failsAsUnavailableWhenTheCatedraTakesLongerThanTheReadTimeout() {
        catedra.respondAfter(SNAPSHOT_PATH, READ_TIMEOUT.multipliedBy(4), 200, SNAPSHOT);

        assertThatGettingTheSnapshotFails()
                .isInstanceOf(CatedraUnavailableException.class)
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @Test
    void failsAsUnavailableWhenTheConnectionIsRefused() throws IOException {
        RestCatedraRepositoryAdapter unreachable = adapterFor(FakeHttpServer.unreachableBaseUrl());

        assertThatThrownBy(unreachable::getSnapshot)
                .isInstanceOf(CatedraUnavailableException.class)
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not json",
            "{\"snapshotVersion\":7,\"professionalCategories\":[],\"professionals\":[],\"weeklySchedules\":["
                    + "{\"id\":21,\"dayOfWeek\":\"LUNES\"}]}"
    })
    void failsAsUnavailableWhenTheBodyIsUnreadable(String body) {
        catedra.respond(SNAPSHOT_PATH, 200, body);

        assertThatGettingTheSnapshotFails().isInstanceOf(CatedraUnavailableException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "{\"professionalCategories\":[],\"professionals\":[],\"weeklySchedules\":[]}",
            "{\"snapshotVersion\":7,\"professionals\":[],\"weeklySchedules\":[]}",
            "{\"snapshotVersion\":7,\"professionalCategories\":[],\"weeklySchedules\":[]}",
            "{\"snapshotVersion\":7,\"professionalCategories\":[],\"professionals\":[],\"weeklySchedules\":null}"
    })
    void failsAsUnavailableWhenTheSnapshotIsIncomplete(String body) {
        catedra.respond(SNAPSHOT_PATH, 200, body);

        assertThatGettingTheSnapshotFails()
                .isInstanceOf(CatedraUnavailableException.class)
                .hasMessageContaining("without a complete snapshot");
    }

    private AbstractThrowableAssert<?, ? extends Throwable> assertThatGettingTheSnapshotFails() {
        return assertThatThrownBy(adapter::getSnapshot);
    }

    private static RestCatedraRepositoryAdapter adapterFor(String baseUrl) {
        CatedraIntegrationProperties properties = new CatedraIntegrationProperties(baseUrl, JWT,
                Duration.ofSeconds(1), READ_TIMEOUT,
                new CatedraIntegrationProperties.Redis("localhost", 6379, "grp_test", "test-redis-password",
                        "catedra:sync:*", Duration.ofSeconds(2), Duration.ofSeconds(2)),
                new CatedraIntegrationProperties.Kafka("localhost:9092", "alumnos-test", "catedra.catalog.test"));
        return new RestCatedraRepositoryAdapter(new CatedraRestClientConfig().catedraRestClient(properties),
                new CatedraSnapshotMapper(new CatedraCatalogItemMapper()));
    }
}
