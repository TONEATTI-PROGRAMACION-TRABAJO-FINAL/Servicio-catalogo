package com.toneatti.catalogo.professionalcategory.infrastructure.web.controller;

import com.toneatti.catalogo.professionalcategory.application.service.ProfessionalCategoryService;
import com.toneatti.catalogo.professionalcategory.infrastructure.web.dto.ProfessionalCategoryResponse;
import com.toneatti.catalogo.professionalcategory.infrastructure.web.mapper.ProfessionalCategoryDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Professional categories")
@RestController
@RequestMapping("/professional-categories")
@RequiredArgsConstructor
public class ProfessionalCategoryController {

    private final ProfessionalCategoryService professionalCategoryService;
    private final ProfessionalCategoryDtoMapper professionalCategoryDtoMapper;

    @Operation(summary = "List the enabled professional categories")
    @GetMapping
    public ResponseEntity<List<ProfessionalCategoryResponse>> getAllProfessionalCategories() {
        List<ProfessionalCategoryResponse> responses = professionalCategoryService.getAllProfessionalCategories().stream()
                .map(professionalCategoryDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
