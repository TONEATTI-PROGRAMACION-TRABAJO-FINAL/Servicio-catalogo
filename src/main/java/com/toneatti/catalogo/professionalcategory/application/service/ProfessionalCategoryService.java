package com.toneatti.catalogo.professionalcategory.application.service;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.in.GetAllProfessionalCategoriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalCategoryService {

    private final GetAllProfessionalCategoriesUseCase getAllProfessionalCategoriesUseCase;

    public List<ProfessionalCategory> getAllProfessionalCategories() {
        return getAllProfessionalCategoriesUseCase.getAllProfessionalCategories();
    }
}
