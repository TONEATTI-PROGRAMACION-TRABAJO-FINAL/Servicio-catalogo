package com.toneatti.catalogo.professional.infrastructure.persistence.mapper;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;
import org.junit.jupiter.api.Test;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professionalEntity;
import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalMapperTest {

    private final ProfessionalMapper mapper = new ProfessionalMapper();

    @Test
    void toEntityCopiesAllFields() {
        Professional domain = professional(15L, "Ana", true);

        assertThat(mapper.toEntity(domain)).usingRecursiveComparison().isEqualTo(domain);
    }

    @Test
    void toDomainModelCopiesAllFields() {
        ProfessionalEntity entity = professionalEntity(16L, "Juan", false);

        assertThat(mapper.toDomainModel(entity)).usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomainModel(null)).isNull();
    }
}
