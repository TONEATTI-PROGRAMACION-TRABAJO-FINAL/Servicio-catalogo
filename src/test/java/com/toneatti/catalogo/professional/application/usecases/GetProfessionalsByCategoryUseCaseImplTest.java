package com.toneatti.catalogo.professional.application.usecases;

import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProfessionalsByCategoryUseCaseImplTest {

    @Mock
    private ProfessionalRepository repository;

    @InjectMocks
    private GetProfessionalsByCategoryUseCaseImpl useCase;

    @Test
    void returnsOnlyTheEnabledProfessionalsOfTheCategory() {
        when(repository.findByCategoryId(7L)).thenReturn(List.of(
                professional(15L, "Ana", true), professional(16L, "Juan", false)));

        assertThat(useCase.getProfessionalsByCategory(7L))
                .extracting("id")
                .containsExactly(15L);
    }

    @Test
    void returnsAnEmptyListForACategoryWithoutProfessionals() {
        when(repository.findByCategoryId(99L)).thenReturn(List.of());

        assertThat(useCase.getProfessionalsByCategory(99L)).isEmpty();
    }
}
