package com.toneatti.catalogo.professionalcategory.domain.ports.in;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;

import java.util.List;

public interface GetAllProfessionalCategoriesUseCase {
    List<ProfessionalCategory> getAllProfessionalCategories();
}
