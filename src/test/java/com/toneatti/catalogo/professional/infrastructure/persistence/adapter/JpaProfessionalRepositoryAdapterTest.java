package com.toneatti.catalogo.professional.infrastructure.persistence.adapter;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.infrastructure.persistence.entity.ProfessionalEntity;
import com.toneatti.catalogo.professional.infrastructure.persistence.mapper.ProfessionalMapper;
import com.toneatti.catalogo.professional.infrastructure.persistence.repository.JpaProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professional;
import static com.toneatti.catalogo.professional.infrastructure.persistence.ProfessionalFixtures.professionalEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProfessionalRepositoryAdapterTest {

    @Mock
    private JpaProfessionalRepository jpaRepository;

    private JpaProfessionalRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProfessionalRepositoryAdapter(jpaRepository, new ProfessionalMapper());
    }

    @Test
    void saveKeepsTheAssignedIdAndReturnsTheSavedProfessional() {
        Professional professional = professional(15L, "Ana", true);
        when(jpaRepository.save(any(ProfessionalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Professional saved = adapter.save(professional);

        ArgumentCaptor<ProfessionalEntity> captor = ArgumentCaptor.forClass(ProfessionalEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(professional);
        assertThat(saved).usingRecursiveComparison().isEqualTo(professional);
    }

    @Test
    void findByIdReturnsTheProfessionalWhenItExists() {
        ProfessionalEntity entity = professionalEntity(15L, "Ana", true);
        when(jpaRepository.findById(15L)).thenReturn(Optional.of(entity));

        assertThat(adapter.findById(15L)).get().usingRecursiveComparison().isEqualTo(entity);
    }

    @Test
    void findByIdReturnsEmptyWhenItDoesNotExist() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllReturnsEveryProfessional() {
        List<ProfessionalEntity> entities = List.of(
                professionalEntity(15L, "Ana", true), professionalEntity(16L, "Juan", false));
        when(jpaRepository.findAll()).thenReturn(entities);

        assertThat(adapter.findAll()).usingRecursiveComparison().isEqualTo(entities);
    }

    @Test
    void findByCategoryIdReturnsTheProfessionalsOfThatCategory() {
        List<ProfessionalEntity> entities = List.of(professionalEntity(15L, "Ana", true));
        when(jpaRepository.findByCategoryId(7L)).thenReturn(entities);

        assertThat(adapter.findByCategoryId(7L)).usingRecursiveComparison().isEqualTo(entities);
    }
}
