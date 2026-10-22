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
class GetAllProfessionalsUseCaseImplTest {

    @Mock
    private ProfessionalRepository repository;

    @InjectMocks
    private GetAllProfessionalsUseCaseImpl useCase;

    @Test
    void returnsOnlyTheEnabledProfessionals() {
        when(repository.findAll()).thenReturn(List.of(
                professional(15L, "Ana", false), professional(16L, "Juan", true)));

        assertThat(useCase.getAllProfessionals())
                .extracting("id")
                .containsExactly(16L);
    }
}
