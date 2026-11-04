package com.toneatti.catalogo.synchronization.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.Instant;

// The unique eventId is what guarantees that a CatalogUpdated is processed once; the index serves the purge
@Getter
@Setter
@Entity
@Table(name = "processed_catalog_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_processed_catalog_events_event_id",
                columnNames = "event_id"),
        indexes = @Index(name = "ix_processed_catalog_events_processed_at", columnList = "processed_at"))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedCatalogEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private String eventId;
    @Column(nullable = false, updatable = false)
    private Long newVersion;
    @Column(nullable = false, updatable = false)
    private Instant processedAt;
}
