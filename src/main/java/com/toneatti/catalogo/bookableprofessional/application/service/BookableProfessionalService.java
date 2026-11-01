package com.toneatti.catalogo.bookableprofessional.application.service;

import com.toneatti.catalogo.bookableprofessional.domain.ports.in.GetBookableProfessionalsByCategoryUseCase;
import com.toneatti.catalogo.bookableprofessional.domain.ports.in.GetBookableProfessionalsUseCase;
import com.toneatti.catalogo.professional.domain.model.Professional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookableProfessionalService {

    private final GetBookableProfessionalsUseCase getBookableProfessionalsUseCase;
    private final GetBookableProfessionalsByCategoryUseCase getBookableProfessionalsByCategoryUseCase;

    public List<Professional> getBookableProfessionals() {
        return getBookableProfessionalsUseCase.getBookableProfessionals();
    }

    public List<Professional> getBookableProfessionalsByCategory(Long categoryId) {
        return getBookableProfessionalsByCategoryUseCase.getBookableProfessionalsByCategory(categoryId);
    }
}
