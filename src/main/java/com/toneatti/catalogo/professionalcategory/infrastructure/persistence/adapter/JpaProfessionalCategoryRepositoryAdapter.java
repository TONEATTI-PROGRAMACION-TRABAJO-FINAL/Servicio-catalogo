package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.adapter;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.mapper.ProfessionalCategoryMapper;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.repository.JpaProfessionalCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaProfessionalCategoryRepositoryAdapter implements ProfessionalCategoryRepository {

    private final JpaProfessionalCategoryRepository jpaProfessionalCategoryRepository;
    private final ProfessionalCategoryMapper professionalCategoryMapper;

    @Override
    public ProfessionalCategory save(ProfessionalCategory professionalCategory) {
        // The id comes from the cátedra, so save inserts or replaces the existing row
        ProfessionalCategoryEntity professionalCategoryEntity = professionalCategoryMapper.toEntity(professionalCategory);
        ProfessionalCategoryEntity savedEntity = jpaProfessionalCategoryRepository.save(professionalCategoryEntity);
        return professionalCategoryMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<ProfessionalCategory> findById(Long id) {
        return jpaProfessionalCategoryRepository.findById(id)
                .map(professionalCategoryMapper::toDomainModel);
    }

    @Override
    public List<ProfessionalCategory> findAll() {
        return jpaProfessionalCategoryRepository.findAll().stream()
                .map(professionalCategoryMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
