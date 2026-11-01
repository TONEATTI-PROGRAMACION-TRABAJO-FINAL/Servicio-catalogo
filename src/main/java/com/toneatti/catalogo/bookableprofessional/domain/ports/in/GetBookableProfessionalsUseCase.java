package com.toneatti.catalogo.bookableprofessional.domain.ports.in;

import com.toneatti.catalogo.professional.domain.model.Professional;

import java.util.List;

public interface GetBookableProfessionalsUseCase {
    List<Professional> getBookableProfessionals();
}
