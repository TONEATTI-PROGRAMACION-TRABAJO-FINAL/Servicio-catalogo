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

import java.util.List;
import java.util.Optional;

import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.category;
import static com.toneatti.catalogo.professionalcategory.infrastructure.persistence.ProfessionalCategoryFixtures.categoryEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProfessionalCategoryRepositoryAdapterTest {

    @Mock
    private JpaProfessionalCategoryRepository jpaRepository;

    private JpaProfessionalCategoryRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProfessionalCategoryRepositoryAdapter(jpaRepository, new ProfessionalCategoryMapper());
    }

    @Test
    void saveKeepsTheAssignedIdAndReturnsTheSavedCategory() {
        ProfessionalCategory category = category(1L, "Clinica medica", true);
        when(jpaRepository.save(any(ProfessionalCategoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfessionalCategory saved = adapter.save(category);

        ArgumentCaptor<ProfessionalCategoryEntity> captor = ArgumentCaptor.forClass(ProfessionalCategoryEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(category);
        assertThat(saved).usingRecursiveComparison().isEqualTo(category);
    }

    @Test
    void findByIdReturnsTheCategoryWhenItExists() {
        ProfessionalCategoryEntity entity = categoryEntity(1L, "Clinica medica", true);
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(adapter.findById(1L)).get().usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void findByIdReturnsEmptyWhenItDoesNotExist() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllReturnsEveryCategory() {
        List<ProfessionalCategoryEntity> entities = List.of(
                categoryEntity(1L, "Clinica medica", true), categoryEntity(2L, "Pediatria", false));
        when(jpaRepository.findAll()).thenReturn(entities);

        assertThat(adapter.findAll()).usingRecursiveComparison().isEqualTo(entities);
    }
}
