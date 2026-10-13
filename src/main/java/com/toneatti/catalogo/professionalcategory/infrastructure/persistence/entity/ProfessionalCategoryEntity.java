package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "professional_categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalCategoryEntity {
    @Id
    private Long id; // Assigned by the cátedra, not generated locally
    private String name;
    private String description;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
