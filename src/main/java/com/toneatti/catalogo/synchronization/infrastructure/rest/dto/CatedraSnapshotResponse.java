package com.toneatti.catalogo.synchronization.infrastructure.rest.dto;

import java.time.Instant;
import java.util.List;

/**
 * Body of GET /api/synchronization/snapshot (section 7 of the integration contract). Unknown fields are ignored.
 */
public record CatedraSnapshotResponse(
        Long snapshotVersion,
        Instant generatedAt,
        List<CatedraProfessionalCategoryResponse> professionalCategories,
        List<CatedraProfessionalResponse> professionals,
        List<CatedraWeeklyScheduleResponse> weeklySchedules
) {
}
