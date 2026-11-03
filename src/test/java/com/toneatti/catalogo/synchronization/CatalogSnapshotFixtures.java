package com.toneatti.catalogo.synchronization;

/**
 * Snapshot answered by the fake cátedra, shared by the synchronization tests: version 7 with one enabled and one
 * disabled entity in each collection, start times with and without seconds and an unknown field.
 */
public final class CatalogSnapshotFixtures {

    public static final String SNAPSHOT_PATH = "/api/synchronization/snapshot";
    public static final long SNAPSHOT_VERSION = 7L;

    public static final String SNAPSHOT = """
            {"snapshotVersion":7,"generatedAt":"2026-09-10T13:44:59Z","source":"v1-compatible",
             "professionalCategories":[
              {"id":1,"name":"Clinica medica","description":"Atencion clinica general","enabled":true,
               "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"},
              {"id":2,"name":"Pediatria","description":"Atencion de ninos","enabled":false,
               "createdAt":"2026-07-02T12:00:00Z","updatedAt":"2026-09-09T10:00:00Z"}],
             "professionals":[
              {"id":15,"categoryId":1,"firstName":"Ana","lastName":"Gomez","enabled":true,
               "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"},
              {"id":16,"categoryId":1,"firstName":"Luis","lastName":"Perez","enabled":false,
               "createdAt":"2026-07-03T12:00:00Z","updatedAt":"2026-09-08T09:00:00Z"}],
             "weeklySchedules":[
              {"id":21,"professionalId":15,"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"12:00:00",
               "slotDurationMinutes":30,"enabled":true,
               "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"},
              {"id":22,"professionalId":15,"dayOfWeek":"FRIDAY","startTime":"14:00","endTime":"18:30",
               "slotDurationMinutes":20,"enabled":false,
               "createdAt":"2026-07-04T12:00:00Z","updatedAt":"2026-09-07T08:00:00Z"}]}
            """;

    public static final String EMPTY_SNAPSHOT = """
            {"snapshotVersion":8,"generatedAt":"2026-09-11T10:00:00Z",
             "professionalCategories":[],"professionals":[],"weeklySchedules":[]}
            """;

    private CatalogSnapshotFixtures() {
    }
}
