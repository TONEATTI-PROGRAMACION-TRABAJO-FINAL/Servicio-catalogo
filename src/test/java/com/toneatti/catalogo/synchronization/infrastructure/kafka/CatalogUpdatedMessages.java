package com.toneatti.catalogo.synchronization.infrastructure.kafka;

/**
 * CatalogUpdated as the cátedra publishes it (section 15.3), with a field added later that the consumer ignores.
 */
public final class CatalogUpdatedMessages {

    private CatalogUpdatedMessages() {
    }

    public static String json(String eventId, long newVersion) {
        return """
                {"eventId":"%s","eventType":"CatalogUpdated","newVersion":%d,\
                "occurredAt":"2026-09-10T13:44:59Z","source":"MANUAL","schemaVersion":1,"addedLater":true}"""
                .formatted(eventId, newVersion);
    }
}
