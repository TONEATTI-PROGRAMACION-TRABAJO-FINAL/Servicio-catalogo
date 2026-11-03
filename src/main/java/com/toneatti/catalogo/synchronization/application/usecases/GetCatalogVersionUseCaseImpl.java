package com.toneatti.catalogo.synchronization.application.usecases;

import com.toneatti.catalogo.synchronization.domain.ports.in.GetCatalogVersionUseCase;
import com.toneatti.catalogo.synchronization.domain.ports.out.CatalogVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetCatalogVersionUseCaseImpl implements GetCatalogVersionUseCase {

    private final CatalogVersionRepository catalogVersionRepository;

    @Override
    public Optional<Long> getCatalogVersion() {
        return catalogVersionRepository.findCurrentVersion();
    }
}
