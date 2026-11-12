package com.toneatti.catalogo.synchronization.infrastructure.scheduling;

import com.toneatti.catalogo.CatalogoApplication;
import com.toneatti.catalogo.support.FakeHttpServer;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_PATH;
import static com.toneatti.catalogo.synchronization.CatalogSnapshotFixtures.SNAPSHOT_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The real scheduler of the whole application, against a fake cátedra, with its own in-memory database and no web
 * server. Without a local version the poll loads the snapshot, so Redis is not needed.
 */
class CatalogSyncPollerIntegrationTest {

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
    void synchronizesTheCatalogOnEachIntervalWithoutTheStartupSynchronization() {
        catedra.respond(SNAPSHOT_PATH, 200, SNAPSHOT);

        context = start("--catedra.sync.poll-enabled=true", "--catedra.sync.poll-interval=PT0.2S");

        CatalogVersionRepository versions = context.getBean(CatalogVersionRepository.class);
        await().atMost(Duration.ofSeconds(10)).until(() -> versions.findCurrentVersion().isPresent());
        assertThat(versions.findCurrentVersion()).contains(SNAPSHOT_VERSION);
    }

    @Test
    void theTestProfileDoesNotPoll() {
        context = start();

        assertThat(context.getBeansOfType(CatalogSyncPoller.class)).isEmpty();
        await().pollDelay(Duration.ofMillis(300)).until(() -> true);
        assertThat(catedra.requestCount(SNAPSHOT_PATH)).isZero();
    }

    // As arguments, so they win over application.properties; the test profile turns off both triggers
    private ConfigurableApplicationContext start(String... overrides) {
        List<String> arguments = new ArrayList<>(List.of(
                "--spring.datasource.url=jdbc:h2:mem:poller-" + UUID.randomUUID(),
                "--catedra.api-base-url=" + catedra.baseUrl()));
        arguments.addAll(List.of(overrides));
        return new SpringApplicationBuilder(CatalogoApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run(arguments.toArray(String[]::new));
    }
}
