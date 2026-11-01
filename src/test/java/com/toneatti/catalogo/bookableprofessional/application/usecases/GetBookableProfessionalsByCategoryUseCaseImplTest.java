package com.toneatti.catalogo.bookableprofessional.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookableProfessionalsByCategoryUseCaseImplTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ProfessionalCategoryRepository professionalCategoryRepository;

    @InjectMocks
    private GetBookableProfessionalsByCategoryUseCaseImpl useCase;

    @Test
    void returnsOnlyTheEnabledProfessionalsOfAnEnabledCategory() {
        when(professionalCategoryRepository.findById(7L))
                .thenReturn(Optional.of(ProfessionalCategory.builder().id(7L).enabled(true).build()));
        when(professionalRepository.findByCategoryId(7L)).thenReturn(List.of(
                Professional.builder().id(15L).categoryId(7L).enabled(true).build(),
                Professional.builder().id(16L).categoryId(7L).enabled(false).build()));

        assertThat(useCase.getBookableProfessionalsByCategory(7L))
                .extracting("id")
                .containsExactly(15L);
    }

    @Test
    void returnsAnEmptyListForADisabledCategoryWithoutReadingItsProfessionals() {
        when(professionalCategoryRepository.findById(8L))
                .thenReturn(Optional.of(ProfessionalCategory.builder().id(8L).enabled(false).build()));

        assertThat(useCase.getBookableProfessionalsByCategory(8L)).isEmpty();
        verifyNoInteractions(professionalRepository);
    }

    @Test
    void returnsAnEmptyListForAnUnknownCategoryWithoutReadingProfessionals() {
        when(professionalCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(useCase.getBookableProfessionalsByCategory(99L)).isEmpty();
        verifyNoInteractions(professionalRepository);
    }
}
