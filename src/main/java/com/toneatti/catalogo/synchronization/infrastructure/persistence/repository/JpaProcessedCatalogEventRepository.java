package com.toneatti.catalogo.synchronization.infrastructure.persistence.repository;

import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.ProcessedCatalogEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface JpaProcessedCatalogEventRepository extends JpaRepository<ProcessedCatalogEventEntity, Long> {

    boolean existsByEventId(String eventId);

    // One bulk statement, instead of loading every row as a derived delete does
    @Modifying
    @Query("delete from ProcessedCatalogEventEntity e where e.processedAt < :cutoff")
    int deleteProcessedBefore(@Param("cutoff") Instant cutoff);
}
