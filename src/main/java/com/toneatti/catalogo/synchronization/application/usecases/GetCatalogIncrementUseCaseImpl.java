package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogIncrementUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogChangesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Not transactional: reading Redis must not hold a database transaction open
@Component
@RequiredArgsConstructor
public class GetCatalogIncrementUseCaseImpl implements GetCatalogIncrementUseCase {

    private final CatalogChangesRepository catalogChangesRepository;

    @Override
    public CatalogIncrement getCatalogIncrement(long version) {
        return catalogChangesRepository.getIncrement(version);
    }
}
