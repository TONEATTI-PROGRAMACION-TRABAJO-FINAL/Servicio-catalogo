package com.toneatti.catalogo.professionalcategory.application.usecases;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.in.GetAllProfessionalCategoriesUseCase;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllProfessionalCategoriesUseCaseImpl implements GetAllProfessionalCategoriesUseCase {

    private final ProfessionalCategoryRepository professionalCategoryRepository;

    @Override
    public List<ProfessionalCategory> getAllProfessionalCategories() {
        // Disabled categories stay in the local copy but are not offered for booking
        return professionalCategoryRepository.findAll().stream()
                .filter(category -> Boolean.TRUE.equals(category.getEnabled()))
                .toList();
    }
}
