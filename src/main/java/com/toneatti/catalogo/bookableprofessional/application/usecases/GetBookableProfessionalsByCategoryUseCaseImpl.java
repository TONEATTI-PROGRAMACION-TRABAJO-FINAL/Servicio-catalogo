package com.toneatti.catalogo.bookableprofessional.application.usecases;

import com.toneatti.catalogo.bookableprofessional.domain.ports.in.GetBookableProfessionalsByCategoryUseCase;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetBookableProfessionalsByCategoryUseCaseImpl implements GetBookableProfessionalsByCategoryUseCase {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalCategoryRepository professionalCategoryRepository;

    @Override
    public List<Professional> getBookableProfessionalsByCategory(Long categoryId) {
        // A disabled or unknown category has no bookable professionals, so they are not read.
        // Both cases give an empty list, not an error.
        boolean categoryEnabled = professionalCategoryRepository.findById(categoryId)
                .map(category -> Boolean.TRUE.equals(category.getEnabled()))
                .orElse(false);
        if (!categoryEnabled) {
            return List.of();
        }
        return professionalRepository.findByCategoryId(categoryId).stream()
                .filter(professional -> Boolean.TRUE.equals(professional.getEnabled()))
                .toList();
    }
}
