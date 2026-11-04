package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.domain.ports.in.RecordProcessedCatalogEventUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.ProcessedCatalogEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Records a CatalogUpdated as processed, so its redelivery is recognized (section 18.1), and in the same transaction
 * deletes the ones older than the retention. Kafka only redelivers records whose offset was not committed (a crash or
 * a rebalance), so an eventId is not needed for long; the default is P7D.
 * An eventId recorded meanwhile by another consumer fails on its unique constraint and rolls back.
 */
@Slf4j
@Component
public class RecordProcessedCatalogEventUseCaseImpl implements RecordProcessedCatalogEventUseCase {

    private final ProcessedCatalogEventRepository processedCatalogEventRepository;
    private final Duration retention;

    public RecordProcessedCatalogEventUseCaseImpl(
            ProcessedCatalogEventRepository processedCatalogEventRepository,
            @Value("${catedra.kafka.processed-events-retention:P7D}") Duration retention) {
        if (retention.isNegative() || retention.isZero()) {
            throw new IllegalArgumentException("catedra.kafka.processed-events-retention must be positive, was "
                    + retention);
        }
        this.processedCatalogEventRepository = processedCatalogEventRepository;
        this.retention = retention;
    }

    @Override
    @Transactional
    public ProcessedCatalogEvent recordProcessedCatalogEvent(ProcessedCatalogEvent event) {
        ProcessedCatalogEvent recorded = processedCatalogEventRepository.create(event);
        Instant cutoff = event.processedAt().minus(retention);
        int deleted = processedCatalogEventRepository.deleteProcessedBefore(cutoff);
        if (deleted > 0) {
            log.debug("Deleted {} catalog events processed before {}", deleted, cutoff);
        }
        return recorded;
    }
}
