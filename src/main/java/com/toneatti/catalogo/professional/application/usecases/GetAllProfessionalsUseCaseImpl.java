package com.toneatti.catalogo.professional.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.in.GetAllProfessionalsUseCase;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllProfessionalsUseCaseImpl implements GetAllProfessionalsUseCase {

    private final ProfessionalRepository professionalRepository;

    @Override
    public List<Professional> getAllProfessionals() {
        // Disabled professionals stay in the local copy but are not offered for booking
        return professionalRepository.findAll().stream()
                .filter(professional -> Boolean.TRUE.equals(professional.getEnabled()))
                .toList();
    }
}
