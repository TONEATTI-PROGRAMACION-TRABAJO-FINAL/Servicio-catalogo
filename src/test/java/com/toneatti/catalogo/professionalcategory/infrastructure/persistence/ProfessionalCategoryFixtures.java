package com.toneatti.catalogo.professionalcategory.infrastructure.persistence;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;

import java.time.Instant;

// Fields of the same type get different values, so a swapped constructor argument fails the tests
public final class ProfessionalCategoryFixtures {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    private ProfessionalCategoryFixtures() {
    }

    public static ProfessionalCategory category(Long id, String name, boolean enabled) {
        return new ProfessionalCategory(id, name, "Atencion de " + name, enabled, CREATED_AT, UPDATED_AT);
    }

    public static ProfessionalCategoryEntity categoryEntity(Long id, String name, boolean enabled) {
        return new ProfessionalCategoryEntity(id, name, "Atencion de " + name, enabled, CREATED_AT, UPDATED_AT);
    }
}
