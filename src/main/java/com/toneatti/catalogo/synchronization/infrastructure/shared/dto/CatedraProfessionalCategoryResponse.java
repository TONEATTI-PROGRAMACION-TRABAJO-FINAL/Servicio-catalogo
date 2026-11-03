package com.toneatti.catalogo.synchronization.infrastructure.shared.dto;

import java.time.Instant;

public record CatedraProfessionalCategoryResponse(
        Long id,
        String name,
        String description,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
