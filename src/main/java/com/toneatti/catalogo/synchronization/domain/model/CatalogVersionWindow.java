package com.toneatti.catalogo.synchronization.domain.model;

/**
 * Versions the cátedra keeps in Redis (section 14.2): the current one and the oldest whose changes are still
 * available.
 */
public record CatalogVersionWindow(
        long currentVersion,
        long oldestAvailableVersion) {

    /**
     * Section 18.2, literally: below the oldest available version the changes that connect the local state are
     * missing, so not even changes:{oldest} is tried. Above the current version no change can go back, so only the
     * snapshot can reconcile the local copy.
     */
    public CatalogSyncAction decide(long localVersion) {
        if (localVersion < oldestAvailableVersion) {
            return CatalogSyncAction.SNAPSHOT_CHANGES_NOT_AVAILABLE;
        }
        if (localVersion > currentVersion) {
            return CatalogSyncAction.SNAPSHOT_AHEAD_OF_SOURCE;
        }
        if (localVersion == currentVersion) {
            return CatalogSyncAction.UP_TO_DATE;
        }
        return CatalogSyncAction.INCREMENTAL;
    }
}
