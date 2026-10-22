package com.toneatti.catalogo.professional.domain.ports.in;

import com.toneatti.catalogo.professional.domain.model.Professional;

import java.util.Optional;

public interface GetProfessionalByIdUseCase {
    Optional<Professional> getProfessionalById(Long id);
}
