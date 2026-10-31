package com.toneatti.catalogo.bookableprofessional.infrastructure.web.controller;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;
import com.toneatti.catalogo.professional.infrastructure.persistence.repository.JpaProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.repository.JpaProfessionalCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.toneatti.catalogo.ProblemDetailsMatchers.problem;
import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professionalEntity;
import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.categoryEntity;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CatalogIntegrationTest
class BookableProfessionalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaProfessionalRepository jpaProfessionalRepository;

    @Autowired
    private JpaProfessionalCategoryRepository jpaProfessionalCategoryRepository;

    // Category 7 is enabled and 8 is disabled. Only Juan (16) is bookable: Ana (15) is disabled
    // and Luis (17) is enabled but belongs to the disabled category.
    @BeforeEach
    void seedDatabase() {
        jpaProfessionalRepository.deleteAll();
        jpaProfessionalCategoryRepository.deleteAll();
        jpaProfessionalCategoryRepository.saveAll(List.of(
                categoryEntity(7L, "Clinica medica", true), categoryEntity(8L, "Pediatria", false)));
        ProfessionalEntity ofDisabledCategory = professionalEntity(17L, "Luis", true);
        ofDisabledCategory.setCategoryId(8L);
        jpaProfessionalRepository.saveAll(List.of(
                professionalEntity(15L, "Ana", false), professionalEntity(16L, "Juan", true), ofDisabledCategory));
    }

    @Test
    void listsOnlyTheEnabledProfessionalsOfEnabledCategories() throws Exception {
        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(16))
                .andExpect(jsonPath("$[0].firstName").value("Juan"))
                .andExpect(jsonPath("$[0].createdAt").doesNotExist());
    }

    @Test
    void filtersTheBookableProfessionalsByCategory() throws Exception {
        mockMvc.perform(get("/professionals").param("categoryId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(16)));
    }

    @Test
    void returnsAnEmptyListForADisabledCategory() throws Exception {
        mockMvc.perform(get("/professionals").param("categoryId", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void returnsAnEmptyListForAnUnknownCategory() throws Exception {
        mockMvc.perform(get("/professionals").param("categoryId", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void returnsBadRequestForANonNumericCategoryId() throws Exception {
        mockMvc.perform(get("/professionals").param("categoryId", "abc"))
                .andExpect(problem(HttpStatus.BAD_REQUEST));
    }

    @Test
    void stillReturnsByIdAProfessionalOfADisabledCategory() throws Exception {
        mockMvc.perform(get("/professionals/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(8))
                .andExpect(jsonPath("$.enabled").value(true));
    }
}
