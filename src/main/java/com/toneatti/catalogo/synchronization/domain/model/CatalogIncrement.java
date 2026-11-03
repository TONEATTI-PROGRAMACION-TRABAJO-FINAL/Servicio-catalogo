package com.toneatti.catalogo.synchronization.domain.model;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;

import java.util.List;

/**
 * The entities affected by one catalog version (section 14.5), each one with its current state from the Redis
 * Hashes, disabled ones included.
 */
public record CatalogIncrement(
        long version,
        List<ProfessionalCategory> professionalCategories,
        List<Professional> professionals,
        List<WeeklySchedule> weeklySchedules) {
}
