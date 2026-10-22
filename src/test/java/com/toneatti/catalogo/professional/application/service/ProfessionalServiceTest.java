package com.toneatti.catalogo.professional.application.service;

import com.toneatti.catalogo.professional.application.exception.ProfessionalException;
import com.toneatti.catalogo.professional.domain.ports.in.GetProfessionalByIdUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private GetProfessionalByIdUseCase getProfessionalByIdUseCase;

    @InjectMocks
    private ProfessionalService service;

    @Test
    void throwsProfessionalExceptionWhenTheProfessionalDoesNotExist() {
        when(getProfessionalByIdUseCase.getProfessionalById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfessionalById(99L))
                .isInstanceOf(ProfessionalException.class)
                .hasMessage("Could not find professional with id: 99");
    }
}
