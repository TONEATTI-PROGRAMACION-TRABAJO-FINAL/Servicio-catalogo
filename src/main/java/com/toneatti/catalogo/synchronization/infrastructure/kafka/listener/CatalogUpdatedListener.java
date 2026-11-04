package com.toneatti.catalogo.synchronization.infrastructure.kafka.listener;

import com.toneatti.catalogo.synchronization.application.service.CatalogSynchronizationService;
import com.toneatti.catalogo.synchronization.domain.model.CatalogUpdated;
import com.toneatti.catalogo.synchronization.infrastructure.kafka.dto.CatalogUpdatedMessage;
import com.toneatti.catalogo.synchronization.infrastructure.kafka.mapper.CatalogUpdatedMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Consumes the CatalogUpdated of the cátedra and synchronizes the catalog (section 16). A message that cannot be
 * read is logged and skipped: reading it again would fail the same way, and retrying it would block the events behind
 * it in its partition. A failure while processing a readable event is thrown, so the error handler of
 * {@code CatalogUpdatedKafkaConfig} retries it; the offset is committed only after the listener returns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogUpdatedListener {

    private final CatalogSynchronizationService catalogSynchronizationService;
    private final CatalogUpdatedMessageMapper catalogUpdatedMessageMapper;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${catedra.kafka.catalog-topic}")
    public void onCatalogUpdated(ConsumerRecord<String, String> consumerRecord) {
        CatalogUpdated event;
        try {
            event = catalogUpdatedMessageMapper.toDomain(
                    jsonMapper.readValue(consumerRecord.value(), CatalogUpdatedMessage.class));
            // The JSON literal null
            if (event == null) {
                throw new IllegalArgumentException("Empty event");
            }
        } catch (JacksonException | IllegalArgumentException e) {
            log.warn("Skipped an unreadable CatalogUpdated at {}-{}@{}: {}", consumerRecord.topic(),
                    consumerRecord.partition(), consumerRecord.offset(), e.getMessage());
            return;
        }
        catalogSynchronizationService.processCatalogUpdated(event);
    }
}
