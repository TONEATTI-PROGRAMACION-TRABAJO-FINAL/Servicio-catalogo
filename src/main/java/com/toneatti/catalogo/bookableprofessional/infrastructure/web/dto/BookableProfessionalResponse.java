package com.toneatti.catalogo.bookableprofessional.infrastructure.web.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookableProfessionalResponse {
    private Long id;
    private Long categoryId;
    private String firstName;
    private String lastName;
    private Boolean enabled;
}
