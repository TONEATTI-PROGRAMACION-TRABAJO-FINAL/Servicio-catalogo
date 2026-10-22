package com.toneatti.catalogo.professional.infrastructure.web.controller;

import com.toneatti.catalogo.professional.application.exception.ProfessionalException;
import com.toneatti.catalogo.professional.application.service.ProfessionalService;
import com.toneatti.catalogo.professional.infrastructure.web.dto.ProfessionalResponse;
import com.toneatti.catalogo.professional.infrastructure.web.mapper.ProfessionalDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Professionals")
@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private final ProfessionalDtoMapper professionalDtoMapper;

    @Operation(summary = "Get a professional by id, enabled or not")
    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> getProfessionalById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(professionalDtoMapper.toResponse(professionalService.getProfessionalById(id)));
        } catch (ProfessionalException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "List the enabled professionals")
    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> getAllProfessionals() {
        List<ProfessionalResponse> responses = professionalService.getAllProfessionals().stream()
                .map(professionalDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
