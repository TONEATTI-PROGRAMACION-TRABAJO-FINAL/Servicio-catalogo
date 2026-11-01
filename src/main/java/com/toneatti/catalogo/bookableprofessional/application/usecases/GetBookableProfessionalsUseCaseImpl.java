package com.toneatti.catalogo.bookableprofessional.application.usecases;

import com.toneatti.catalogo.bookableprofessional.domain.ports.in.GetBookableProfessionalsUseCase;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetBookableProfessionalsUseCaseImpl implements GetBookableProfessionalsUseCase {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalCategoryRepository professionalCategoryRepository;

    @Override
    public List<Professional> getBookableProfessionals() {
        // Bookable means the professional and its category are both enabled.
        // A category missing from the local copy counts as disabled.
        Set<Long> enabledCategoryIds = professionalCategoryRepository.findAll().stream()
                .filter(category -> Boolean.TRUE.equals(category.getEnabled()))
                .map(ProfessionalCategory::getId)
                .collect(Collectors.toSet());
        return professionalRepository.findAll().stream()
                .filter(professional -> Boolean.TRUE.equals(professional.getEnabled()))
                .filter(professional -> enabledCategoryIds.contains(professional.getCategoryId()))
                .toList();
    }
}
