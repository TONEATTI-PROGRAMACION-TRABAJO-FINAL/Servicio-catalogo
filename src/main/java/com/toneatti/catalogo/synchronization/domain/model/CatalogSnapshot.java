package com.toneatti.catalogo.synchronization.domain.model;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;

import java.time.Instant;
import java.util.List;

/**
 * Full picture of the cátedra catalog at {@code snapshotVersion} (section 7 of the integration contract),
 * enabled and disabled entities included.
 */
public record CatalogSnapshot(
        Long snapshotVersion,
        Instant generatedAt,
        List<ProfessionalCategory> professionalCategories,
        List<Professional> professionals,
        List<WeeklySchedule> weeklySchedules) {
}
