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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProfessionalRepositoryAdapterTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-01T12:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-10T13:40:00Z");

    @Mock
    private JpaProfessionalRepository jpaRepository;

    private JpaProfessionalRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaProfessionalRepositoryAdapter(jpaRepository, new ProfessionalMapper());
    }

    @Test
    void saveKeepsTheAssignedIdAndReturnsTheSavedProfessional() {
        Professional professional = Professional.builder()
                .id(15L)
                .categoryId(1L)
                .firstName("Ana")
                .lastName("Gomez")
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
        when(jpaRepository.save(any(ProfessionalEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Professional saved = adapter.save(professional);

        ArgumentCaptor<ProfessionalEntity> captor = ArgumentCaptor.forClass(ProfessionalEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(15L);
        assertThat(saved.getId()).isEqualTo(15L);
        assertThat(saved.getCategoryId()).isEqualTo(1L);
        assertThat(saved.getFirstName()).isEqualTo("Ana");
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getUpdatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void findByIdReturnsTheProfessionalWhenItExists() {
        when(jpaRepository.findById(15L)).thenReturn(Optional.of(entity(15L, "Ana")));

        Optional<Professional> result = adapter.findById(15L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(15L);
        assertThat(result.get().getFirstName()).isEqualTo("Ana");
    }

    @Test
    void findByIdReturnsEmptyWhenItDoesNotExist() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAllReturnsEveryProfessional() {
        when(jpaRepository.findAll()).thenReturn(List.of(entity(15L, "Ana"), entity(16L, "Juan")));

        List<Professional> result = adapter.findAll();

        assertThat(result).extracting(Professional::getId).containsExactly(15L, 16L);
        assertThat(result).extracting(Professional::getFirstName).containsExactly("Ana", "Juan");
    }

    private static ProfessionalEntity entity(Long id, String firstName) {
        return ProfessionalEntity.builder()
                .id(id)
                .categoryId(1L)
                .firstName(firstName)
                .lastName("Gomez")
                .enabled(true)
                .createdAt(CREATED_AT)
                .updatedAt(UPDATED_AT)
                .build();
    }
}
