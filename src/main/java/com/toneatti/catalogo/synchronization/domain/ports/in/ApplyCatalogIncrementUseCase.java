package com.toneatti.catalogo.synchronization.domain.ports.in;

import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;

public interface ApplyCatalogIncrementUseCase {
    void applyCatalogIncrement(CatalogIncrement increment);
}
