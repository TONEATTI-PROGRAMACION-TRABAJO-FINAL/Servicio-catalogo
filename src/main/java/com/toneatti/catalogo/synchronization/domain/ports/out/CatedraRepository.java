package com.toneatti.catalogo.synchronization.domain.ports.out;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;

public interface CatedraRepository {

    /**
     * @throws com.toneatti.catalogo.synchronization.domain.exception.CatedraAuthenticationException on 401 or 403
     * @throws com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException on any other failure
     */
    CatalogSnapshot getSnapshot();

}
