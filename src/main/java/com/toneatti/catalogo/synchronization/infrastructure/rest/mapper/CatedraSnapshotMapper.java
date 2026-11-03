package com.toneatti.catalogo.synchronization.infrastructure.rest.mapper;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraProfessionalCategoryResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraProfessionalResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraSnapshotResponse;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraWeeklyScheduleResponse;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class CatedraSnapshotMapper {

    public CatalogSnapshot toDomainModel(CatedraSnapshotResponse response) {
        if (response == null) {
            return null;
        }
        return new CatalogSnapshot(
                response.snapshotVersion(),
                response.generatedAt(),
                mapAll(response.professionalCategories(), this::toDomainModel),
                mapAll(response.professionals(), this::toDomainModel),
                mapAll(response.weeklySchedules(), this::toDomainModel));
    }

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

    // Null entries carry nothing to apply, so they are left out.
    private static <T, R> List<R> mapAll(List<T> responses, Function<T, R> mapper) {
        if (responses == null) {
            return null;
        }
        return responses.stream().filter(Objects::nonNull).map(mapper).toList();
    }
}
