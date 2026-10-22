package com.toneatti.catalogo.professionalcategory.infrastructure.web.mapper;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.infrastructure.web.dto.ProfessionalCategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalCategoryDtoMapper {

    public ProfessionalCategoryResponse toResponse(ProfessionalCategory domain) {
        if (domain == null) {
            return null;
        }
        return ProfessionalCategoryResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }
}
