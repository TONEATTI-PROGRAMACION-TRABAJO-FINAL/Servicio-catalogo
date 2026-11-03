package com.toneatti.catalogo.synchronization.domain.model;

/**
 * What a synchronization has to do, compared with the versions in Redis (sections 14.2 and 18.2).
 */
public enum CatalogSyncAction {
    // No local version yet
    SNAPSHOT_WITHOUT_LOCAL_VERSION,
    // The local version is older than the oldest one whose changes are kept
    SNAPSHOT_CHANGES_NOT_AVAILABLE,
    // The local version is newer than the current one: the cátedra rebuilt its history
    SNAPSHOT_AHEAD_OF_SOURCE,
    UP_TO_DATE,
    INCREMENTAL;

    public boolean needsSnapshot() {
        return this == SNAPSHOT_WITHOUT_LOCAL_VERSION || this == SNAPSHOT_CHANGES_NOT_AVAILABLE
                || this == SNAPSHOT_AHEAD_OF_SOURCE;
    }
}
