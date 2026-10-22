package com.toneatti.catalogo.professional.infrastructure.persistence.repository;

import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {

    List<ProfessionalEntity> findByCategoryId(Long categoryId);
}
