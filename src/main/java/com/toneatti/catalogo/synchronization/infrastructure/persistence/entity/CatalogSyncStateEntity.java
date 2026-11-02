package com.toneatti.catalogo.synchronization.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

// A single row: the local catalog has one current version
@Getter
@Setter
@Entity
@Table(name = "catalog_sync_state")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogSyncStateEntity {

    public static final Long SINGLE_ROW_ID = 1L;

    @Id
    private Long id; // Always SINGLE_ROW_ID, not generated
    private Long snapshotVersion;
    private Instant generatedAt; // When the cátedra generated the snapshot
    private Instant syncedAt; // When this service applied it
}
