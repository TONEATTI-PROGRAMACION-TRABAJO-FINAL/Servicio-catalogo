package com.toneatti.catalogo.synchronization.domain.ports.in;

import java.util.Optional;

public interface GetCatalogVersionUseCase {
    Optional<Long> getCatalogVersion();
}
