package com.toneatti.catalogo.professional.domain.model;

import java.time.Instant;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class Professional {
    private Long id;
    private Long categoryId;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
