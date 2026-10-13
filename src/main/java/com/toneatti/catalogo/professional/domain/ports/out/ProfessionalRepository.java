package com.toneatti.catalogo.professional.domain.ports.out;

import com.toneatti.catalogo.professional.domain.model.Professional;

import java.util.List;
import java.util.Optional;

public interface ProfessionalRepository {

    Professional save(Professional professional);
    Optional<Professional> findById(Long id);
    List<Professional> findAll();

}
