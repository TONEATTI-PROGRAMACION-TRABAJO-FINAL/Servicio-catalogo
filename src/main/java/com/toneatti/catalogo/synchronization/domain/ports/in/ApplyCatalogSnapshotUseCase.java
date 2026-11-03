package com.toneatti.catalogo.synchronization.domain.ports.in;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;

public interface ApplyCatalogSnapshotUseCase {
    void applyCatalogSnapshot(CatalogSnapshot snapshot);
}
