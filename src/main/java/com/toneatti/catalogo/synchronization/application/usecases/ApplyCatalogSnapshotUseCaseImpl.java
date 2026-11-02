package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Replaces the local copy with the snapshot (section 7): the three collections and the version in one transaction,
 * so a failure rolls everything back and the version only advances when all three were applied.
 */
@Component
@RequiredArgsConstructor
public class ApplyCatalogSnapshotUseCaseImpl implements ApplyCatalogSnapshotUseCase {

    private final ProfessionalCategoryRepository professionalCategoryRepository;
    private final ProfessionalRepository professionalRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final CatalogVersionRepository catalogVersionRepository;

    @Override
    @Transactional
    public void applyCatalogSnapshot(CatalogSnapshot snapshot) {
        // Parent to child for readability; the tables have no foreign keys between them
        professionalCategoryRepository.replaceAll(snapshot.professionalCategories());
        professionalRepository.replaceAll(snapshot.professionals());
        weeklyScheduleRepository.replaceAll(snapshot.weeklySchedules());
        catalogVersionRepository.save(snapshot.snapshotVersion(), snapshot.generatedAt(), Instant.now());
    }
}
