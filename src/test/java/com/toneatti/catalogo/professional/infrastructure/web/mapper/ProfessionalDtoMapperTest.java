package com.toneatti.catalogo.professional.infrastructure.web.mapper;

import org.junit.jupiter.api.Test;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalDtoMapperTest {

    private final ProfessionalDtoMapper mapper = new ProfessionalDtoMapper();

    @Test
    void mapsTheFieldsServicioTurnosNeeds() {
        assertThat(mapper.toResponse(professional(15L, "Ana", false)))
                .extracting("id", "categoryId", "firstName", "lastName", "enabled")
                .containsExactly(15L, 7L, "Ana", "Gomez", false);
    }

    @Test
    void returnsNullForNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
