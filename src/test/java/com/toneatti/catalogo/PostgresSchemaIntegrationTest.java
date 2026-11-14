package com.toneatti.catalogo;

import com.toneatti.catalogo.professional.infrastructure.persistence.adapter.JpaProfessionalRepositoryAdapter;
import com.toneatti.catalogo.professional.infrastructure.persistence.repository.JpaProfessionalRepository;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.adapter.JpaProcessedCatalogEventRepositoryAdapter;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaProcessedCatalogEventRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static com.toneatti.catalogo.synchronization.infrastructure.persistence.ProcessedCatalogEventFixtures.processed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The Flyway migrations against a real PostgreSQL, the same image as the compose.yaml of Servicio-turnos: the context
 * only starts when they apply and Hibernate validates the schema they create. The other tests run them on H2 in
 * PostgreSQL mode. Not transactional, like the H2 test of the eventId. Skipped where Docker is not available.
 */
@CatalogIntegrationTest
@Testcontainers(disabledWithoutDocker = true)
class PostgresSchemaIntegrationTest {

    private static final Instant PROCESSED_AT = Instant.parse("2026-09-10T13:45:03Z");

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JpaProfessionalRepositoryAdapter professionalAdapter;

    @Autowired
    private JpaProfessionalRepository jpaProfessionalRepository;

    @Autowired
    private JpaProcessedCatalogEventRepositoryAdapter processedEventAdapter;

    @Autowired
    private JpaProcessedCatalogEventRepository jpaProcessedEventRepository;

    @AfterEach
    void deleteAll() {
        jpaProfessionalRepository.deleteAll();
        jpaProcessedEventRepository.deleteAll();
    }

    @Test
    void appliesTheMigrationsThatHibernateValidates() {
        MigrationInfo current = flyway.info().current();

        assertThat(current.getVersion().getVersion()).isEqualTo("1");
        assertThat(current.getState()).isEqualTo(MigrationState.SUCCESS);
    }

    // An empty snapshot: deleteByIdNotIn with no ids must delete every row in PostgreSQL too
    @Test
    void replaceAllWithAnEmptyListDeletesEveryRow() {
        professionalAdapter.replaceAll(List.of(professional(15L, "Ana", true), professional(16L, "Juan", true)));

        professionalAdapter.replaceAll(List.of());

        assertThat(professionalAdapter.findAll()).isEmpty();
    }

    @Test
    void rejectsTheSameEventIdTwice() {
        processedEventAdapter.create(processed("catalog-event-7-4f3b", PROCESSED_AT));

        assertThatThrownBy(() -> processedEventAdapter.create(processed("catalog-event-7-4f3b",
                PROCESSED_AT.plusSeconds(1)))).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jpaProcessedEventRepository.count()).isEqualTo(1);
    }
}
