package com.toneatti.catalogo.professionalcategory.infrastructure.web.controller;

import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.repository.JpaProfessionalCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.categoryEntity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfessionalCategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaProfessionalCategoryRepository jpaProfessionalCategoryRepository;

    @BeforeEach
    void cleanDatabase() {
        jpaProfessionalCategoryRepository.deleteAll();
    }

    @Test
    void listsOnlyTheEnabledCategoriesWithoutSyncMetadata() throws Exception {
        jpaProfessionalCategoryRepository.saveAll(List.of(
                categoryEntity(1L, "Clinica medica", true), categoryEntity(2L, "Pediatria", false)));

        mockMvc.perform(get("/professional-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Clinica medica"))
                .andExpect(jsonPath("$[0].description").value("Atencion de Clinica medica"))
                .andExpect(jsonPath("$[0].enabled").doesNotExist())
                .andExpect(jsonPath("$[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$[0].updatedAt").doesNotExist());
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoCategories() throws Exception {
        mockMvc.perform(get("/professional-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
