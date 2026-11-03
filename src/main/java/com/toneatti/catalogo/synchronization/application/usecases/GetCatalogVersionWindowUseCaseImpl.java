package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionWindowUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogChangesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCatalogVersionWindowUseCaseImpl implements GetCatalogVersionWindowUseCase {

    private final CatalogChangesRepository catalogChangesRepository;

    @Override
    public CatalogVersionWindow getCatalogVersionWindow() {
        return catalogChangesRepository.getVersionWindow();
    }
}
