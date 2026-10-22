package com.toneatti.catalogo.professional.infrastructure.web.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalResponse {
    private Long id;
    private Long categoryId;
    private String firstName;
    private String lastName;
    private Boolean enabled;
}
