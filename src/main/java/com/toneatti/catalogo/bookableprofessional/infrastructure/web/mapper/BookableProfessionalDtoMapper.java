package com.toneatti.catalogo.bookableprofessional.infrastructure.web.mapper;

import com.toneatti.catalogo.bookableprofessional.infrastructure.web.dto.BookableProfessionalResponse;
import com.toneatti.catalogo.professional.domain.model.Professional;
import org.springframework.stereotype.Component;

@Component
public class BookableProfessionalDtoMapper {

    public BookableProfessionalResponse toResponse(Professional domain) {
        if (domain == null) {
            return null;
        }
        return BookableProfessionalResponse.builder()
                .id(domain.getId())
                .categoryId(domain.getCategoryId())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .enabled(domain.getEnabled())
                .build();
    }
}
