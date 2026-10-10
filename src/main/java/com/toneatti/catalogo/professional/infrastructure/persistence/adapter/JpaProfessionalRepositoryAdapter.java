package com.toneatti.catalogo.professional.infrastructure.persistence.adapter;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;
import com.toneatti.catalogo.professional.infrastructure.persistence.mapper.ProfessionalMapper;
import com.toneatti.catalogo.professional.infrastructure.persistence.repository.JpaProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaProfessionalRepositoryAdapter implements ProfessionalRepository {

    private final JpaProfessionalRepository jpaProfessionalRepository;
    private final ProfessionalMapper professionalMapper;

    @Override
    public Professional save(Professional professional) {
        // The id comes from the cátedra, so save inserts or replaces the existing row
        ProfessionalEntity professionalEntity = professionalMapper.toEntity(professional);
        ProfessionalEntity savedEntity = jpaProfessionalRepository.save(professionalEntity);
        return professionalMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Professional> findById(Long id) {
        return jpaProfessionalRepository.findById(id)
                .map(professionalMapper::toDomainModel);
    }

    @Override
    public List<Professional> findAll() {
        return jpaProfessionalRepository.findAll().stream()
                .map(professionalMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
