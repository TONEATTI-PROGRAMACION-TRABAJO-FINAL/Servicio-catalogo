package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.adapter;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static org.assertj.core.api.Assertions.assertThat;

// Against the real H2 database: a mock cannot show what Hibernate does with deleteByIdNotIn
@CatalogIntegrationTest
@Transactional
class JpaProfessionalCategoryRepositoryAdapterReplaceAllTest {

    @Autowired
    private JpaProfessionalCategoryRepositoryAdapter adapter;

    @Test
    void replaceAllUpdatesAddsAndDeletesTheCategoriesNotInTheList() {
        adapter.save(category(1L, "Clinica medica", true));
        adapter.save(category(2L, "Pediatria", true));
        List<ProfessionalCategory> snapshot = List.of(category(2L, "Pediatria", false), category(3L, "Nutricion", true));

        adapter.replaceAll(snapshot);

        assertThat(adapter.findAll()).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(snapshot);
    }

    @Test
    void replaceAllWithAnEmptyListDeletesEveryCategory() {
        adapter.save(category(1L, "Clinica medica", true));
        adapter.save(category(2L, "Pediatria", false));

        adapter.replaceAll(List.of());

        assertThat(adapter.findAll()).isEmpty();
    }
}
