package com.toneatti.catalogo.professionalcategory.domain.model;

import java.time.Instant;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ProfessionalCategory {
    private Long id;
    private String name;
    private String description;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
