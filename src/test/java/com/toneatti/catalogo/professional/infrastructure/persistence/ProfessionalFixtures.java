package com.toneatti.catalogo.professional.infrastructure.persistence;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;

import java.time.Instant;

// Fields of the same type get different values, so a swapped constructor argument fails the tests
public final class ProfessionalFixtures {

    private static final Long CATEGORY_ID = 7L;
    private static final String LAST_NAME = "Gomez";
    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    private ProfessionalFixtures() {
    }

    public static Professional professional(Long id, String firstName, boolean enabled) {
        return new Professional(id, CATEGORY_ID, firstName, LAST_NAME, enabled, CREATED_AT, UPDATED_AT);
    }

    public static ProfessionalEntity professionalEntity(Long id, String firstName, boolean enabled) {
        return new ProfessionalEntity(id, CATEGORY_ID, firstName, LAST_NAME, enabled, CREATED_AT, UPDATED_AT);
    }
}
