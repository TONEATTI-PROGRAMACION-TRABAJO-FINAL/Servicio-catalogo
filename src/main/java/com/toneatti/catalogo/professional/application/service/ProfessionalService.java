package com.toneatti.catalogo.professional.application.service;

import com.toneatti.catalogo.professional.application.exception.ProfessionalException;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.in.GetProfessionalByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final GetProfessionalByIdUseCase getProfessionalByIdUseCase;

    public Professional getProfessionalById(Long id) {
        return getProfessionalByIdUseCase.getProfessionalById(id)
                .orElseThrow(() -> new ProfessionalException(id));
    }
}
