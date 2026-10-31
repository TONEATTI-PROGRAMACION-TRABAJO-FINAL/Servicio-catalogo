package com.toneatti.catalogo.bookableprofessional.infrastructure.web.mapper;

import com.toneatti.catalogo.professional.domain.model.Professional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookableProfessionalDtoMapperTest {

    private final BookableProfessionalDtoMapper mapper = new BookableProfessionalDtoMapper();

    @Test
    void mapsTheFieldsServicioTurnosNeeds() {
        Professional professional = Professional.builder()
                .id(15L).categoryId(7L).firstName("Ana").lastName("Gomez").enabled(true).build();

        assertThat(mapper.toResponse(professional))
                .extracting("id", "categoryId", "firstName", "lastName", "enabled")
                .containsExactly(15L, 7L, "Ana", "Gomez", true);
    }

    @Test
    void returnsNullForNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
