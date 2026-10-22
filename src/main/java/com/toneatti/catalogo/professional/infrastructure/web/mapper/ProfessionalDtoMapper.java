package com.toneatti.catalogo.professional.infrastructure.web.mapper;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.infrastructure.web.dto.ProfessionalResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalDtoMapper {

    public ProfessionalResponse toResponse(Professional domain) {
        if (domain == null) {
            return null;
        }
        return ProfessionalResponse.builder()
                .id(domain.getId())
                .categoryId(domain.getCategoryId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .enabled(domain.getEnabled())
                .build();
    }
}
