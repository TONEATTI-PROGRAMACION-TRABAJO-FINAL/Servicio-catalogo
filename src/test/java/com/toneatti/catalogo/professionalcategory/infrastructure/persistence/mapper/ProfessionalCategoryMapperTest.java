package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.mapper;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalCategoryMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    private final ProfessionalCategoryMapper mapper = new ProfessionalCategoryMapper();

    @Test
    void toEntityCopiesAllFields() {
        ProfessionalCategory domain = ProfessionalCategory.builder()
                .id(1L)
                .name("Clinica medica")
                .description("Atencion clinica general")
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        ProfessionalCategoryEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("Clinica medica");
        assertThat(entity.getDescription()).isEqualTo("Atencion clinica general");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void toDomainModelCopiesAllFields() {
        ProfessionalCategoryEntity entity = ProfessionalCategoryEntity.builder()
                .id(2L)
                .name("Pediatria")
                .description("Atencion pediatrica")
                .enabled(false)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();

        ProfessionalCategory domain = mapper.toDomainModel(entity);

        assertThat(domain.getId()).isEqualTo(2L);
        assertThat(domain.getName()).isEqualTo("Pediatria");
        assertThat(domain.getDescription()).isEqualTo("Atencion pediatrica");
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
