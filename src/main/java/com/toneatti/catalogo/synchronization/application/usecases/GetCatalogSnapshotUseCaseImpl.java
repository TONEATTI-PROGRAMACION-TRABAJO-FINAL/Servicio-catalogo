package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.model.CatalogSnapshot;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogSnapshotUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatedraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Not transactional: the HTTP call must not hold a database transaction open
@Component
@RequiredArgsConstructor
public class GetCatalogSnapshotUseCaseImpl implements GetCatalogSnapshotUseCase {

    private final CatedraRepository catedraRepository;

    @Override
    public CatalogSnapshot getCatalogSnapshot() {
        return catedraRepository.getSnapshot();
    }
}
