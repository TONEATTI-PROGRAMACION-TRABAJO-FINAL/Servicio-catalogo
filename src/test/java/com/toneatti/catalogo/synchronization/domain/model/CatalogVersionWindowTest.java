package com.toneatti.catalogo.synchronization.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogVersionWindowTest {

    // Redis keeps current 7 and oldest available 4
    @ParameterizedTest
    @CsvSource({
            "3, SNAPSHOT_CHANGES_NOT_AVAILABLE",
            "0, SNAPSHOT_CHANGES_NOT_AVAILABLE",
            "4, INCREMENTAL",
            "6, INCREMENTAL",
            "7, UP_TO_DATE",
            "8, SNAPSHOT_AHEAD_OF_SOURCE"
    })
    void decidesBySection182(long localVersion, CatalogSyncAction expected) {
        assertThat(new CatalogVersionWindow(7, 4).decide(localVersion)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "SNAPSHOT_WITHOUT_LOCAL_VERSION, true",
            "SNAPSHOT_CHANGES_NOT_AVAILABLE, true",
            "SNAPSHOT_AHEAD_OF_SOURCE, true",
            "UP_TO_DATE, false",
            "INCREMENTAL, false"
    })
    void onlyTheSnapshotActionsNeedTheSnapshot(CatalogSyncAction action, boolean needsSnapshot) {
        assertThat(action.needsSnapshot()).isEqualTo(needsSnapshot);
    }
}
