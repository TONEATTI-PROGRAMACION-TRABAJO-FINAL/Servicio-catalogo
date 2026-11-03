package com.toneatti.catalogo.synchronization.infrastructure.startup;

import com.toneatti.catalogo.CatalogoApplication;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.support.FakeHttpServer;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;
import java.util.UUID;

import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_PATH;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Starts the whole application against a fake cátedra, scripted before the start because the snapshot is loaded
 * when the application is ready. Each start gets its own in-memory database and no web server.
 */
@ExtendWith(OutputCaptureExtension.class)
class SnapshotOnStartupIntegrationTest {

    private static final String SERVICE_UNAVAILABLE = "{\"status\":503}";
    private static final Duration WAIT = Duration.ofSeconds(10);

    private final FakeHttpServer catedra = FakeHttpServer.start();
    private ConfigurableApplicationContext context;

    @AfterEach
    void stop() {
        if (context != null) {
            context.close();
        }
        catedra.close();
    }

    @Test
    void theCatalogIsLoadedWhenTheApplicationStarts() {
        catedra.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        start(8);

        assertThat(versions().findCurrentVersion()).contains(SNAPSHOT_VERSION);
        assertThat(context.getBean(ProfessionalCategoryRepository.class).findAll()).hasSize(2);
        assertThat(catedra.requestCount(SNAPSHOT_PATH)).isEqualTo(1);
    }

    @Test
    void retriesWithBackoffUntilTheCatedraAnswers(CapturedOutput output) {
        catedra.respondOnce(SNAPSHOT_PATH, 503, SERVICE_UNAVAILABLE);
        catedra.respondOnce(SNAPSHOT_PATH, 503, SERVICE_UNAVAILABLE);
        catedra.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        start(8);

        await().atMost(WAIT).until(() -> versions().findCurrentVersion().isPresent());
        assertThat(versions().findCurrentVersion()).contains(SNAPSHOT_VERSION);
        assertThat(catedra.requestCount(SNAPSHOT_PATH)).isEqualTo(3);
        assertThat(output).contains("Catalog synchronization attempt 1/8 failed",
                "Catalog synchronization attempt 2/8 failed",
                "Catalog snapshot version 7 applied");
    }

    @Test
    void givesUpAfterTheLastAttemptAndKeepsRunning(CapturedOutput output) {
        catedra.respond(SNAPSHOT_PATH, 503, SERVICE_UNAVAILABLE);

        start(3);

        await().atMost(WAIT).until(() -> output.getOut().contains("Catalog not synchronized after 3 attempts"));
        assertThat(catedra.requestCount(SNAPSHOT_PATH)).isEqualTo(3);
        assertThat(context.isRunning()).isTrue();
        assertThat(versions().findCurrentVersion()).isEmpty();
    }

    @Test
    void doesNotRetryWhenTheJwtIsRejected(CapturedOutput output) {
        catedra.respond(SNAPSHOT_PATH, 401, "{\"status\":401,\"title\":\"Unauthorized\"}");

        start(8);

        assertThat(context.isRunning()).isTrue();
        assertThat(versions().findCurrentVersion()).isEmpty();
        assertThat(context.getBean(ProfessionalCategoryRepository.class).findAll()).isEmpty();
        assertThat(output).contains("check CATEDRA_JWT").doesNotContain("test-jwt");
        // Leaves time for a retry that must not happen
        await().pollDelay(Duration.ofMillis(300)).until(() -> true);
        assertThat(catedra.requestCount(SNAPSHOT_PATH)).isEqualTo(1);
    }

    private void start(int maxAttempts) {
        context = new SpringApplicationBuilder(CatalogoApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                // As arguments, so they win over application.properties (builder properties are only defaults)
                .run("--spring.datasource.url=jdbc:h2:mem:startup-" + UUID.randomUUID(),
                        "--catedra.api-base-url=" + catedra.baseUrl(),
                        "--catedra.sync.snapshot-on-startup=true",
                        "--catedra.sync.max-attempts=" + maxAttempts,
                        "--catedra.sync.initial-backoff=PT0.05S",
                        "--catedra.sync.max-backoff=PT0.1S");
    }

    private CatalogVersionRepository versions() {
        return context.getBean(CatalogVersionRepository.class);
    }
}
