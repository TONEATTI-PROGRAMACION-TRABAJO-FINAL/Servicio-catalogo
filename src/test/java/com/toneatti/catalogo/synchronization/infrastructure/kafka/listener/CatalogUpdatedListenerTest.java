package com.toneatti.catalogo.synchronization.infrastructure.kafka.listener;

import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogUpdated;
import com.toneatti.catalogo.synchronization.infrastructure.kafka.mapper.CatalogUpdatedMessageMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static com.toneatti.catalogo.synchronization.infrastructure.kafka.CatalogUpdatedMessages.json;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CatalogUpdatedListenerTest {

    private static final CatalogUpdated VERSION_7 = new CatalogUpdated("catalog-event-7-4f3b", 7, "MANUAL",
            Instant.parse("2026-09-10T13:44:59Z"));

    private final CatalogSynchronizationService service = mock(CatalogSynchronizationService.class);
    private final CatalogUpdatedListener listener = new CatalogUpdatedListener(service,
            new CatalogUpdatedMessageMapper(), JsonMapper.builder().build());

    // The JSON of the contract, with a field added later that is ignored
    @Test
    void processesTheEventOfTheContract() {
        listener.onCatalogUpdated(recordOf(json("catalog-event-7-4f3b", 7)));

        verify(service).processCatalogUpdated(VERSION_7);
    }

    // Logged and skipped, without throwing: retrying it would fail the same way and block the partition
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "not json", "null", "[]", "{\"eventType\":\"CatalogUpdated\"}",
            "{\"eventId\":\"e-1\",\"eventType\":\"CatalogUpdated\",\"newVersion\":\"seven\",\"schemaVersion\":1}",
            "{\"eventId\":\"e-1\",\"eventType\":\"CatalogUpdated\",\"newVersion\":7,\"schemaVersion\":2}",
            "{\"eventId\":\"e-1\",\"eventType\":\"CatalogUpdated\",\"newVersion\":7,\"schemaVersion\":1,"
                    + "\"occurredAt\":\"yesterday\"}"})
    void skipsAnUnreadableMessage(String value) {
        listener.onCatalogUpdated(recordOf(value));

        verifyNoInteractions(service);
    }

    // The error handler of the container retries it
    @Test
    void throwsWhenProcessingAReadableEventFails() {
        RuntimeException failure = new CatedraUnavailableException("redis down", null);
        when(service.processCatalogUpdated(VERSION_7)).thenThrow(failure);

        assertThatThrownBy(() -> listener.onCatalogUpdated(recordOf(json("catalog-event-7-4f3b", 7))))
                .isSameAs(failure);
    }

    // The message key is the version as text (section 15.1)
    private static ConsumerRecord<String, String> recordOf(String value) {
        return new ConsumerRecord<>("catedra.catalog.test", 0, 5L, "7", value);
    }
}
