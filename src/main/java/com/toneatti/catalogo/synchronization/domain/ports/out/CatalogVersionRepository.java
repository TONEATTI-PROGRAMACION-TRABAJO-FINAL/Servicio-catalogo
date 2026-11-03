package com.toneatti.catalogo.synchronization.domain.ports.out;

import java.time.Instant;
import java.util.Optional;

/**
 * Local version of the catalog: the {@code snapshotVersion} of the last snapshot applied completely (section 7).
 */
public interface CatalogVersionRepository {

    // Empty until the first snapshot is applied
    Optional<Long> findCurrentVersion();

    // Replaces the previous version; joins the transaction that applies the snapshot
    void save(Long snapshotVersion, Instant generatedAt, Instant syncedAt);

}
