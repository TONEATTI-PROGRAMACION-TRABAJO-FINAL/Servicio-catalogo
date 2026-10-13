package com.toneatti.catalogo.professionalcategory.domain.ports.out;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;

import java.util.List;
import java.util.Optional;

public interface ProfessionalCategoryRepository {

    ProfessionalCategory save(ProfessionalCategory professionalCategory);
    Optional<ProfessionalCategory> findById(Long id);
    List<ProfessionalCategory> findAll();

}
