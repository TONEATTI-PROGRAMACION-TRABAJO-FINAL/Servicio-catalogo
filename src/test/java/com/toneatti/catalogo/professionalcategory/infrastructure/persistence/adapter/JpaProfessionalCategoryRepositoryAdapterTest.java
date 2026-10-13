package com.toneatti.catalogo.professionalcategory.infrastructure.persistence.adapter;

import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.mapper.ProfessionalCategoryMapper;
import com.toneatti.catalogo.professionalcategory.infrastructure.persistence.repository.JpaProfessionalCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProfessionalCategoryRepositoryAdapterTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    @Mock
    private JpaProfessionalCategoryRepository jpaRepository;

    private JpaProfessionalCategoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProfessionalCategoryRepositoryAdapter(jpaRepository, new ProfessionalCategoryMapper());
    }

    @Test
    void saveKeepsTheAssignedIdAndReturnsTheSavedCategory() {
        ProfessionalCategory category = ProfessionalCategory.builder()
                .id(1L)
                .name("Clinica medica")
                .description("Atencion clinica general")
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
        when(jpaRepository.save(any(ProfessionalCategoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfessionalCategory saved = adapter.save(category);

        ArgumentCaptor<ProfessionalCategoryEntity> captor = ArgumentCaptor.forClass(ProfessionalCategoryEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("Clinica medica");
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void findByIdReturnsTheCategoryWhenItExists() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity(1L, "Clinica medica")));

        Optional<ProfessionalCategory> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Clinica medica");
    }

    @Test
    void findByIdReturnsEmptyWhenItDoesNotExist() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllReturnsEveryCategory() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity(1L, "Clinica medica"), entity(2L, "Pediatria")));

        List<ProfessionalCategory> result = adapter.findAll();

        assertThat(result).extracting(ProfessionalCategory::getId).containsExactly(1L, 2L);
        assertThat(result).extracting(ProfessionalCategory::getName).containsExactly("Clinica medica", "Pediatria");
    }

    private static ProfessionalCategoryEntity entity(Long id, String name) {
        return ProfessionalCategoryEntity.builder()
                .id(id)
                .name(name)
                .description("desc")
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }
}
