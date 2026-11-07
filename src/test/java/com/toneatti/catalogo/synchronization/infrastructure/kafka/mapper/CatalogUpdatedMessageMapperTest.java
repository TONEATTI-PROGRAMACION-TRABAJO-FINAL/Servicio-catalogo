package com.toneatti.catalogo.synchronization.infrastructure.kafka.mapper;

import com.toneatti.catalogo.synchronization.domain.model.CatalogUpdated;
import com.toneatti.catalogo.synchronization.infrastructure.kafka.dto.CatalogUpdatedMessage;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogUpdatedMessageMapperTest {

    private static final Instant OCCURRED_AT = Instant.parse("2026-09-10T13:44:59Z");

    private final CatalogUpdatedMessageMapper mapper = new CatalogUpdatedMessageMapper();

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void mapsEveryField() {
        assertThat(mapper.toDomain(message("catalog-event-7-4f3b", "CatalogUpdated", 7L, 1)))
                .isEqualTo(new CatalogUpdated("catalog-event-7-4f3b", 7, "MANUAL", OCCURRED_AT));
    }

    // occurredAt and source are informative only
    @Test
    void acceptsAnEventWithoutOccurredAtNorSource() {
        assertThat(mapper.toDomain(new CatalogUpdatedMessage("catalog-event-7-4f3b", "CatalogUpdated", 7L, null,
                null, 1))).isEqualTo(new CatalogUpdated("catalog-event-7-4f3b", 7, null, null));
    }

    @Test
    void rejectsAnEventWithoutEventId() {
        assertRejected(message(null, "CatalogUpdated", 7L, 1), "Missing eventId");
        assertRejected(message(" ", "CatalogUpdated", 7L, 1), "Missing eventId");
    }

    @Test
    void rejectsAnotherEventType() {
        assertRejected(message("catalog-event-7-4f3b", "AppointmentConfirmed", 7L, 1), "eventType");
        assertRejected(message("catalog-event-7-4f3b", null, 7L, 1), "eventType");
    }

    @Test
    void rejectsAnotherSchemaVersion() {
        assertRejected(message("catalog-event-7-4f3b", "CatalogUpdated", 7L, 2), "schemaVersion 2");
        assertRejected(message("catalog-event-7-4f3b", "CatalogUpdated", 7L, null), "schemaVersion null");
    }

    @Test
    void rejectsAMissingOrNegativeNewVersion() {
        assertRejected(message("catalog-event-7-4f3b", "CatalogUpdated", null, 1), "newVersion null");
        assertRejected(message("catalog-event-7-4f3b", "CatalogUpdated", -1L, 1), "newVersion -1");
    }

    private void assertRejected(CatalogUpdatedMessage message, String reason) {
        assertThatThrownBy(() -> mapper.toDomain(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(reason);
    }

    private static CatalogUpdatedMessage message(String eventId, String eventType, Long newVersion,
                                                 Integer schemaVersion) {
        return new CatalogUpdatedMessage(eventId, eventType, newVersion, OCCURRED_AT, "MANUAL", schemaVersion);
    }
}
