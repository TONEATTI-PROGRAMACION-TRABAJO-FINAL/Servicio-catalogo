package com.toneatti.catalogo.synchronization.infrastructure.rest.mapper;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.infrastructure.rest.dto.CatedraSnapshotResponse;
import com.toneatti.catalogo.synchronization.infrastructure.shared.mapper.CatedraCatalogItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CatedraSnapshotMapper {

    private final CatedraCatalogItemMapper catedraCatalogItemMapper;

    public CatalogSnapshot toDomainModel(CatedraSnapshotResponse response) {
        if (response == null) {
            return null;
        }
        return new CatalogSnapshot(
                response.snapshotVersion(),
                response.generatedAt(),
                mapAll(response.professionalCategories(), catedraCatalogItemMapper::toDomainModel),
                mapAll(response.professionals(), catedraCatalogItemMapper::toDomainModel),
                mapAll(response.weeklySchedules(), catedraCatalogItemMapper::toDomainModel));
    }

    // Null entries carry nothing to apply, so they are left out.
    private static <T, R> List<R> mapAll(List<T> responses, Function<T, R> mapper) {
        if (responses == null) {
            return null;
        }
        return responses.stream().filter(Objects::nonNull).map(mapper).toList();
    }
}
