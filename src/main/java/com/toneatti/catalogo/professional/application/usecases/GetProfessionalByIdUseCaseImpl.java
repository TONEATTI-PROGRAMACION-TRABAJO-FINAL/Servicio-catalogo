package com.toneatti.catalogo.professional.application.usecases;

import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professional.domain.ports.in.GetProfessionalByIdUseCase;
import com.toneatti.catalogo.professional.domain.ports.out.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetProfessionalByIdUseCaseImpl implements GetProfessionalByIdUseCase {

    private final ProfessionalRepository professionalRepository;

    @Override
    public Optional<Professional> getProfessionalById(Long id) {
        // Disabled professionals are returned too: Servicio-turnos may hold past appointments with them
        return professionalRepository.findById(id);
    }
}
