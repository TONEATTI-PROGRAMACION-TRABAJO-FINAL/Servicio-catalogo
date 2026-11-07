package com.toneatti.catalogo.synchronization.infrastructure.kafka.listener;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogChangesRepository;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.ProcessedCatalogEventEntity;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaProcessedCatalogEventRepository;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static com.toneatti.catalogo.synchronization.infrastructure.kafka.CatalogUpdatedMessages.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The consumer against an embedded broker, never the environment of the cátedra, and the real H2 database. The
 * changes in Redis come from a mock of the port, so neither Docker nor Redis are needed. Every test starts with the
 * local catalog at version 4 and uses its own eventIds.
 */
@SpringBootTest(properties = {
        "catedra.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "catedra.kafka.listener-enabled=true",
        // One retry after a second, then the event is skipped
        "catedra.kafka.retry-max-elapsed-time=PT1S"})
@EmbeddedKafka(partitions = 1, topics = CatalogUpdatedListenerIntegrationTest.TOPIC)
@ActiveProfiles("test")
class CatalogUpdatedListenerIntegrationTest {

    // From application-test.properties
    static final String TOPIC = "catedra.catalog.test";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    // Version 5 adds a category, 6 renames it, 7 adds another one
    private static final CatalogIncrement VERSION_5 = new CatalogIncrement(5,
            List.of(category(3L, "Nutricion", true)), List.of(), List.of());
    private static final CatalogIncrement VERSION_6 = new CatalogIncrement(6,
            List.of(category(3L, "Nutricion infantil", true)), List.of(), List.of());
    private static final CatalogIncrement VERSION_7 = new CatalogIncrement(7,
            List.of(category(4L, "Pediatria", true)), List.of(), List.of());

    @Autowired
    private EmbeddedKafkaBroker broker;
    @MockitoBean
    private CatalogChangesRepository changes;
    @MockitoSpyBean
    private CatalogSynchronizationService service;
    @Autowired
    private ProfessionalCategoryRepository categories;
    @Autowired
    private CatalogVersionRepository versions;
    @Autowired
    private JpaProcessedCatalogEventRepository processedEvents;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void startAtVersion4() {
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                KafkaTestUtils.producerProps(broker), new StringSerializer(), new StringSerializer()));
        categories.replaceAll(List.of(category(1L, "Clinica medica", true)));
        versions.save(4L, null, Instant.now());
        when(changes.getIncrement(5)).thenReturn(VERSION_5);
        when(changes.getIncrement(6)).thenReturn(VERSION_6);
        when(changes.getIncrement(7)).thenReturn(VERSION_7);
    }

    // The in-memory database is shared with the other tests of the JVM
    @AfterEach
    void cleanUp() {
        kafkaTemplate.destroy();
        processedEvents.deleteAll();
    }

    @Test
    void anEventSynchronizesTheCatalogAndRecordsItsEventId() {
        when(changes.getVersionWindow()).thenReturn(new CatalogVersionWindow(5, 1));
        String eventId = newEventId();

        publish(eventId, 5);

        awaitRecorded(eventId);
        assertThat(versions.findCurrentVersion()).contains(5L);
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getName)
                .containsExactlyInAnyOrder("Clinica medica", "Nutricion");
    }

    // Section 18.1: the redelivered event does not even compare versions; the last one is a barrier
    @Test
    void aRepeatedEventIdRepeatsNoEffects() {
        when(changes.getVersionWindow()).thenReturn(new CatalogVersionWindow(5, 1));
        String repeated = newEventId();
        String barrier = newEventId();

        publish(repeated, 5);
        publish(repeated, 5);
        publish(barrier, 5);

        awaitRecorded(barrier);
        verify(service, times(3)).processCatalogUpdated(any());
        verify(changes, times(2)).getVersionWindow();
        verify(changes, times(1)).getIncrement(5);
        assertThat(processedEvents.findAll()).extracting(ProcessedCatalogEventEntity::getEventId)
                .containsExactlyInAnyOrder(repeated, barrier);
    }

    // Section 16: the notifications of 5 and 6 were lost; the one of 7 applies every missing version in order
    @Test
    void theNextNotificationRecoversTheVersionsOfTheLostOnes() {
        when(changes.getVersionWindow()).thenReturn(new CatalogVersionWindow(7, 4));
        String eventId = newEventId();

        publish(eventId, 7);

        awaitRecorded(eventId);
        InOrder order = inOrder(changes);
        order.verify(changes).getIncrement(5);
        order.verify(changes).getIncrement(6);
        order.verify(changes).getIncrement(7);
        assertThat(versions.findCurrentVersion()).contains(7L);
        assertThat(categories.findAll()).extracting(ProfessionalCategory::getName)
                .containsExactlyInAnyOrder("Clinica medica", "Nutricion infantil", "Pediatria");
    }

    @Test
    void skipsInvalidMessagesWithoutBlockingTheValidOneBehindThem() {
        when(changes.getVersionWindow()).thenReturn(new CatalogVersionWindow(5, 1));
        String eventId = newEventId();

        publishRaw("5", "not json");
        publishRaw("5", null);
        publishRaw("5", json(" ", 5));
        publishRaw("5", json(newEventId(), 5).replace("\"schemaVersion\":1", "\"schemaVersion\":2"));
        publish(eventId, 5);

        awaitRecorded(eventId);
        verify(service, times(1)).processCatalogUpdated(any());
        assertThat(versions.findCurrentVersion()).contains(5L);
        assertThat(processedEvents.count()).isEqualTo(1);
    }

    // Redis down once: the offset is not committed and the event is retried, then recorded once
    @Test
    void retriesAnEventWhoseSynchronizationFailedOnce() {
        when(changes.getVersionWindow())
                .thenThrow(new CatedraUnavailableException("redis down", null))
                .thenReturn(new CatalogVersionWindow(5, 1));
        String eventId = newEventId();

        publish(eventId, 5);

        awaitRecorded(eventId);
        verify(changes, atLeast(2)).getVersionWindow();
        verify(changes, times(1)).getIncrement(anyLong());
        assertThat(versions.findCurrentVersion()).contains(5L);
        assertThat(processedEvents.count()).isEqualTo(1);
    }

    private static String newEventId() {
        return "catalog-event-" + UUID.randomUUID();
    }

    // The message key is the version as text (section 15.1)
    private void publish(String eventId, long newVersion) {
        publishRaw(String.valueOf(newVersion), json(eventId, newVersion));
    }

    private void publishRaw(String key, String value) {
        try {
            kafkaTemplate.send(TOPIC, key, value).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Could not publish to the embedded broker", e);
        }
    }

    private void awaitRecorded(String eventId) {
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(processedEvents.existsByEventId(eventId)).isTrue());
    }
}
