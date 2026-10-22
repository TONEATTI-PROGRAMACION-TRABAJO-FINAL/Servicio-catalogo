package com.toneatti.catalogo.professional.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.in.GetProfessionalsByCategoryUseCase;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetProfessionalsByCategoryUseCaseImpl implements GetProfessionalsByCategoryUseCase {

    private final ProfessionalRepository professionalRepository;

    @Override
    public List<Professional> getProfessionalsByCategory(Long categoryId) {
        // An unknown category or one without enabled professionals gives an empty list, not an error:
        // checking that the category exists would need the professional category slice
        return professionalRepository.findByCategoryId(categoryId).stream()
                .filter(professional -> Boolean.TRUE.equals(professional.getEnabled()))
                .toList();
    }
}
