package com.toneatti.catalogo.professional.infrastructure.web.controller;

import com.toneatti.catalogo.CatalogIntegrationTest;
import com.toneatti.catalogo.professional.infrastructure.persistence.repository.JpaProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professionalEntity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CatalogIntegrationTest
class ProfessionalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaProfessionalRepository jpaProfessionalRepository;

    @BeforeEach
    void cleanDatabase() {
        jpaProfessionalRepository.deleteAll();
    }

    @Test
    void returnsADisabledProfessionalByIdWithoutSyncMetadata() throws Exception {
        jpaProfessionalRepository.save(professionalEntity(15L, "Ana", false));

        mockMvc.perform(get("/professionals/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.categoryId").value(7))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Gomez"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.createdAt").doesNotExist())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void listsOnlyTheEnabledProfessionals() throws Exception {
        jpaProfessionalRepository.saveAll(List.of(
                professionalEntity(15L, "Ana", false), professionalEntity(16L, "Juan", true)));

        mockMvc.perform(get("/professionals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(16))
                .andExpect(jsonPath("$[0].firstName").value("Juan"));
    }

    @Test
    void returnsNotFoundForAnUnknownId() throws Exception {
        mockMvc.perform(get("/professionals/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsBadRequestForANonNumericId() throws Exception {
        mockMvc.perform(get("/professionals/abc"))
                .andExpect(status().isBadRequest());
    }
}
