package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.repository;

import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface JpaProfessionalCategoryRepository extends JpaRepository<ProfessionalCategoryEntity, Long> {

    void deleteByIdNotIn(Collection<Long> ids);
}
