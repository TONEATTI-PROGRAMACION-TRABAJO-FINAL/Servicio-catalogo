package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.mapper;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import org.junit.jupiter.api.Test;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.categoryEntity;
import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalCategoryMapperTest {

    private final ProfessionalCategoryMapper mapper = new ProfessionalCategoryMapper();

    @Test
    void toEntityCopiesAllFields() {
        ProfessionalCategory domain = category(1L, "Clinica medica", true);

        assertThat(mapper.toEntity(domain)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomainModelCopiesAllFields() {
        ProfessionalCategoryEntity entity = categoryEntity(2L, "Pediatria", false);

        assertThat(mapper.toDomainModel(entity)).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
