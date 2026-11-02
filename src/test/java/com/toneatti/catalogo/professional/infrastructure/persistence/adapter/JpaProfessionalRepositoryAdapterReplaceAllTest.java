package com.toneatti.catalogo.professional.infrastructure.persistence.adapter;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.professional.domain.model.Professional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static org.assertj.core.api.Assertions.assertThat;

// Real H2 and Hibernate, rolled back after each test, to check deleteByIdNotIn and not a mock of it
@CatalogIntegrationTest
@Transactional
class JpaProfessionalRepositoryAdapterReplaceAllTest {

    @Autowired
    private JpaProfessionalRepositoryAdapter adapter;

    @BeforeEach
    void saveTwoProfessionals() {
        adapter.save(professional(15L, "Ana", true));
        adapter.save(professional(16L, "Juan", true));
    }

    @Test
    void leavesExactlyTheGivenProfessionals() {
        List<Professional> given = List.of(professional(16L, "Juan", false), professional(17L, "Lucia", true));

        adapter.replaceAll(given);

        assertThat(adapter.findAll()).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(given);
    }

    @Test
    void deletesEveryProfessionalWhenGivenNone() {
        adapter.replaceAll(List.of());

        assertThat(adapter.findAll()).isEmpty();
    }
}
