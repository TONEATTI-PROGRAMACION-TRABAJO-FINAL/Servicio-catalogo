package com.toneatti.catalogo.synchronization.infrastructure.persistence.adapter;

import com.toneatti.catalogo.synchronization.domain.model.ProcessedCatalogEvent;
import com.toneatti.catalogo.synchronization.domain.ports.out.ProcessedCatalogEventRepository;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.ProcessedCatalogEventEntity;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.mapper.ProcessedCatalogEventMapper;
import com.toneatti.catalogo.synchronization.infrastructure.persistence.repository.JpaProcessedCatalogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JpaProcessedCatalogEventRepositoryAdapter implements ProcessedCatalogEventRepository {

    private final JpaProcessedCatalogEventRepository jpaProcessedCatalogEventRepository;
    private final ProcessedCatalogEventMapper processedCatalogEventMapper;

    // Flushed right away, so an eventId recorded meanwhile by another consumer fails here, inside the transaction of
    // the use case, with a DataIntegrityViolationException that rolls it back.
    @Override
    public ProcessedCatalogEvent create(ProcessedCatalogEvent event) {
        ProcessedCatalogEventEntity savedEntity = Objects.requireNonNull(
                jpaProcessedCatalogEventRepository.saveAndFlush(processedCatalogEventMapper.toEntity(event)),
                "saveAndFlush returned null");
        return processedCatalogEventMapper.toDomainModel(savedEntity);
    }

    @Override
    public boolean existsByEventId(String eventId) {
        return jpaProcessedCatalogEventRepository.existsByEventId(eventId);
    }

    // Joins the transaction of the use case; a bulk delete needs one
    @Override
    @Transactional
    public int deleteProcessedBefore(Instant cutoff) {
        return jpaProcessedCatalogEventRepository.deleteProcessedBefore(cutoff);
    }
}
