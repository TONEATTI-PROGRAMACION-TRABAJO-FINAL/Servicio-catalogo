package com.toneatti.catalogo.synchronization.domain.ports.out;

import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;

/**
 * Incremental changes of the catalog published by the cátedra (section 14).
 * Both methods throw {@link com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException}
 * when the source cannot be reached and
 * {@link com.toneatti.catalogo.synchronization.domain.exception.CatalogIncrementUnusableException} when what it
 * holds cannot be applied.
 */
public interface CatalogChangesRepository {

    CatalogVersionWindow getVersionWindow();

    CatalogIncrement getIncrement(long version);

}
