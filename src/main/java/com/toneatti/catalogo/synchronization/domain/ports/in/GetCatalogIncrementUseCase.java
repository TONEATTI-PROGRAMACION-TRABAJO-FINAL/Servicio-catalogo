package com.toneatti.catalogo.synchronization.domain.ports.in;

import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;

public interface GetCatalogIncrementUseCase {
    CatalogIncrement getCatalogIncrement(long version);
}
