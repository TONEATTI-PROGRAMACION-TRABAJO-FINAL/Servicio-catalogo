package com.toneatti.catalogo.synchronization.domain.model;

/**
 * What processing a CatalogUpdated did.
 */
public enum CatalogEventOutcome {
    // The catalog was synchronized up to the announced version, or was already there, and the eventId recorded
    SYNCHRONIZED,
    // The eventId was already recorded: nothing was done (section 18.1)
    DUPLICATE
}
