package com.toneatti.catalogo.synchronization.infrastructure.redis.dto;

import java.util.List;

/**
 * Value of catedra:sync:changes:{version} (section 14.5): the ids affected in each collection, not the entities.
 */
public record CatedraCatalogChangesResponse(
        Long version,
        Changes changes
) {
    public record Changes(
            List<Long> professionalCategories,
            List<Long> professionals,
            List<Long> weeklySchedules
    ) {
    }
}
