package com.toneatti.catalogo.synchronization.domain.ports.in;

import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;

public interface GetCatalogVersionWindowUseCase {
    CatalogVersionWindow getCatalogVersionWindow();
}
