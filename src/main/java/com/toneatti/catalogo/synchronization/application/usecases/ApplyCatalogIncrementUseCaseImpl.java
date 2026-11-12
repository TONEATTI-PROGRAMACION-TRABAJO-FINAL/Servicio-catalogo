package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import com.toneatti.catalogo.professionalcategory.domain.ports.out.ProfessionalCategoryRepository;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.ports.in.ApplyCatalogIncrementUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import com.toneatti.catalogo.weeklyschedule.domain.ports.out.WeeklyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Applies one catalog version (section 14.5): upserts every affected entity with its current state and then advances
 * the local version, all in one transaction, so a failure leaves neither part of the version nor the new version.
 * Disabled entities are saved as they come: they are logical deletes.
 */
@Component
@RequiredArgsConstructor
public class ApplyCatalogIncrementUseCaseImpl implements ApplyCatalogIncrementUseCase {

    private final ProfessionalCategoryRepository professionalCategoryRepository;
    private final ProfessionalRepository professionalRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final CatalogVersionRepository catalogVersionRepository;

    @Override
    @Transactional
    public void applyCatalogIncrement(CatalogIncrement increment) {
        // save inserts or replaces, because the ids come from the cátedra
        increment.professionalCategories().forEach(professionalCategoryRepository::save);
        increment.professionals().forEach(professionalRepository::save);
        increment.weeklySchedules().forEach(weeklyScheduleRepository::save);
        // changes:{version} carries no generation instant; only snapshots have one
        catalogVersionRepository.save(increment.version(), null, Instant.now());
    }
}
