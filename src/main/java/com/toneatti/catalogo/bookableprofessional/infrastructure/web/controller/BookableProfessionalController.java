package com.toneatti.catalogo.bookableprofessional.infrastructure.web.controller;

import com.toneatti.catalogo.bookableprofessional.application.service.BookableProfessionalService;
import com.toneatti.catalogo.bookableprofessional.infrastructure.web.dto.BookableProfessionalResponse;
import com.toneatti.catalogo.bookableprofessional.infrastructure.web.mapper.BookableProfessionalDtoMapper;
import com.toneatti.catalogo.professional.domain.model.Professional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Shares /professionals with the professional slice, which keeps GET /professionals/{id}
@Tag(name = "Professionals")
@RestController
@RequestMapping("/professionals")
@RequiredArgsConstructor
public class BookableProfessionalController {

    private final BookableProfessionalService bookableProfessionalService;
    private final BookableProfessionalDtoMapper bookableProfessionalDtoMapper;

    @Operation(summary = "List the bookable professionals (enabled and of an enabled category), optionally filtered by category")
    @GetMapping
    public ResponseEntity<List<BookableProfessionalResponse>> getBookableProfessionals(
            @RequestParam(required = false) Long categoryId) {
        List<Professional> professionals = categoryId == null
                ? bookableProfessionalService.getBookableProfessionals()
                : bookableProfessionalService.getBookableProfessionalsByCategory(categoryId);
        return ResponseEntity.ok(professionals.stream()
                .map(bookableProfessionalDtoMapper::toResponse)
                .toList());
    }
}
