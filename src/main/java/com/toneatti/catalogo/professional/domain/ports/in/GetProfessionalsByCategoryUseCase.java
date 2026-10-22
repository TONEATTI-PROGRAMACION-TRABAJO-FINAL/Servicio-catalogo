package com.toneatti.catalogo.professional.domain.ports.in;

import com.toneatti.catalogo.professional.domain.model.Professional;

import java.util.List;

public interface GetProfessionalsByCategoryUseCase {
    List<Professional> getProfessionalsByCategory(Long categoryId);
}
