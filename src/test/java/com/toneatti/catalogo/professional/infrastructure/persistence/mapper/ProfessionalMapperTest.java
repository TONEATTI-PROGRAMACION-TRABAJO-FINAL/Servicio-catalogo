package com.toneatti.catalogo.professional.infrastructure.persistence.mapper;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    private final ProfessionalMapper mapper = new ProfessionalMapper();

    @Test
    void toEntityCopiesAllFields() {
        Professional domain = Professional.builder()
                .id(15L)
                .categoryId(1L)
                .firstName("Ana")
                .lastName("Gomez")
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        ProfessionalEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(15L);
        assertThat(entity.getCategoryId()).isEqualTo(1L);
        assertThat(entity.getFirstName()).isEqualTo("Ana");
        assertThat(entity.getLastName()).isEqualTo("Gomez");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void toDomainModelCopiesAllFields() {
        ProfessionalEntity entity = ProfessionalEntity.builder()
                .id(16L)
                .categoryId(2L)
                .firstName("Juan")
                .lastName("Perez")
                .enabled(false)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        Professional domain = mapper.toDomainModel(entity);

        assertThat(domain.getId()).isEqualTo(16L);
        assertThat(domain.getCategoryId()).isEqualTo(2L);
        assertThat(domain.getFirstName()).isEqualTo("Juan");
        assertThat(domain.getLastName()).isEqualTo("Perez");
        assertThat(domain.getEnabled()).isFalse();
        assertThat(domain.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(domain.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void toEntityReturnsNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomainModelReturnsNullForNullInput() {
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
