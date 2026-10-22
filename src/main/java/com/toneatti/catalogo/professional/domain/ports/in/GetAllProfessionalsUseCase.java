package com.toneatti.catalogo.professional.domain.ports.in;

import com.toneatti.catalogo.professional.domain.model.Professional;

import java.util.List;

public interface GetAllProfessionalsUseCase {
    List<Professional> getAllProfessionals();
}
