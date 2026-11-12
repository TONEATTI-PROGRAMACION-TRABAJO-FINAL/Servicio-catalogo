package com.toneatti.catalogo.synchronization.infrastructure.shared.dto;

import java.time.Instant;

public record CatedraProfessionalResponse(
        Long id,
        Long categoryId,
        String firstName,
        String lastName,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
