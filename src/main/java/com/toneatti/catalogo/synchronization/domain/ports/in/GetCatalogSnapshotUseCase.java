package com.toneatti.catalogo.synchronization.domain.ports.in;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;

// Fetches the full snapshot from the cátedra; applying it is ApplyCatalogSnapshotUseCase
public interface GetCatalogSnapshotUseCase {
    CatalogSnapshot getCatalogSnapshot();
}
