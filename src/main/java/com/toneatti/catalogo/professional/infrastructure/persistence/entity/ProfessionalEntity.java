package com.toneatti.catalogo.professional.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
// The index serves the searches by category; the migration V1 creates it
@Table(name = "professionals", indexes = @Index(name = "ix_professionals_category_id", columnList = "category_id"))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalEntity {
    @Id
    private Long id; // Assigned by the cátedra, not generated locally
    private Long categoryId; // Plain id, no JPA relation to the professional category slice
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
