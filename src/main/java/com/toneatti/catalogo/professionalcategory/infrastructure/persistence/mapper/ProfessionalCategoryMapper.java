package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.mapper;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalCategoryMapper {

    public ProfessionalCategoryEntity toEntity(ProfessionalCategory professionalCategory) {
        if (professionalCategory == null) {
            return null;
        }
        return ProfessionalCategoryEntity.builder()
                .id(professionalCategory.getId())
                .name(professionalCategory.getName())
                .description(professionalCategory.getDescription())
                .enabled(professionalCategory.getEnabled())
                .createdAt(professionalCategory.getCreatedAt())
                .updatedAt(professionalCategory.getUpdatedAt())
                .build();
    }

    public ProfessionalCategory toDomainModel(ProfessionalCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProfessionalCategory(entity.getId(), entity.getName(), entity.getDescription(),
                entity.getEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
