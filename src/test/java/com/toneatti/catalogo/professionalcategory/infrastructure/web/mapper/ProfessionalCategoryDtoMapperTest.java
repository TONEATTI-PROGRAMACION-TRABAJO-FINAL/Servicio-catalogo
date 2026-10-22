package com.toneatti.catalogo.professionalcategory.infrastructure.web.mapper;

import org.junit.jupiter.api.Test;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalCategoryDtoMapperTest {

    private final ProfessionalCategoryDtoMapper mapper = new ProfessionalCategoryDtoMapper();

    @Test
    void mapsTheFieldsServicioTurnosNeeds() {
        assertThat(mapper.toResponse(category(1L, "Clinica medica", true)))
                .extracting("id", "name", "description")
                .containsExactly(1L, "Clinica medica", "Atencion de Clinica medica");
    }

    @Test
    void returnsNullForNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
