package com.toneatti.catalogo.professionalcategory.application.usecases;

import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllProfessionalCategoriesUseCaseImplTest {

    @Mock
    private ProfessionalCategoryRepository repository;

    @InjectMocks
    private GetAllProfessionalCategoriesUseCaseImpl useCase;

    @Test
    void returnsOnlyTheEnabledCategories() {
        when(repository.findAll()).thenReturn(List.of(
                category(1L, "Clinica medica", true), category(2L, "Pediatria", false)));

        assertThat(useCase.getAllProfessionalCategories())
                .extracting("id")
                .containsExactly(1L);
    }
}
