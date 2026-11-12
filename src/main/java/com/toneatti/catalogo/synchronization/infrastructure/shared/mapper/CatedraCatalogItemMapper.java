package com.toneatti.catalogo.synchronization.infrastructure.shared.mapper;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalCategoryResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraProfessionalResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.dto.CatedraWeeklyScheduleResponse;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import org.springframework.stereotype.Component;

/**
 * Catalog items of the cátedra as the domain models of each slice. The REST snapshot (section 7) and the Redis
 * Hashes (section 14.4) share the same JSON shape for each item.
 */
@Component
public class CatedraCatalogItemMapper {

    public ProfessionalCategory toDomainModel(CatedraProfessionalCategoryResponse response) {
        if (response == null) {
            return null;
        }
        return new ProfessionalCategory(response.id(), response.name(), response.description(),
                response.enabled(), response.createdAt(), response.updatedAt());
    }

    public Professional toDomainModel(CatedraProfessionalResponse response) {
        if (response == null) {
            return null;
        }
        return new Professional(response.id(), response.categoryId(), response.firstName(), response.lastName(),
                response.enabled(), response.createdAt(), response.updatedAt());
    }

    public WeeklySchedule toDomainModel(CatedraWeeklyScheduleResponse response) {
        if (response == null) {
            return null;
        }
        return new WeeklySchedule(response.id(), response.professionalId(), response.dayOfWeek(),
                response.startTime(), response.endTime(), response.slotDurationMinutes(),
                response.enabled(), response.createdAt(), response.updatedAt());
    }
}
