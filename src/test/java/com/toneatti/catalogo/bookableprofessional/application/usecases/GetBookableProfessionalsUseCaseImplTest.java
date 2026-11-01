package com.toneatti.catalogo.bookableprofessional.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookableProfessionalsUseCaseImplTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ProfessionalCategoryRepository professionalCategoryRepository;

    @InjectMocks
    private GetBookableProfessionalsUseCaseImpl useCase;

    // Category 7 is enabled, 8 is disabled and 9 is missing from the local copy
    @BeforeEach
    void stubCatalog() {
        when(professionalCategoryRepository.findAll()).thenReturn(List.of(category(7L, true), category(8L, false)));
        when(professionalRepository.findAll()).thenReturn(List.of(
                professional(15L, 7L, true), professional(16L, 7L, false),
                professional(17L, 8L, true), professional(18L, 9L, true)));
    }

    @Test
    void returnsOnlyTheEnabledProfessionalsOfEnabledCategories() {
        assertThat(useCase.getBookableProfessionals())
                .extracting("id")
                .containsExactly(15L);
    }

    @Test
    void excludesTheEnabledProfessionalsOfADisabledCategory() {
        assertThat(useCase.getBookableProfessionals())
                .extracting("id")
                .doesNotContain(17L);
    }

    @Test
    void excludesTheProfessionalsOfACategoryMissingFromTheLocalCopy() {
        assertThat(useCase.getBookableProfessionals())
                .extracting("id")
                .doesNotContain(18L);
    }

    private static ProfessionalCategory category(Long id, boolean enabled) {
        return ProfessionalCategory.builder().id(id).enabled(enabled).build();
    }

    private static Professional professional(Long id, Long categoryId, boolean enabled) {
        return Professional.builder().id(id).categoryId(categoryId).enabled(enabled).build();
    }
}
