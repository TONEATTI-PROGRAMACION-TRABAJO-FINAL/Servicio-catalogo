package com.toneatti.catalogo.professional.application.exception;

public class ProfessionalException extends RuntimeException {
    public ProfessionalException(Long id) {
        super("Could not find professional with id: " + id);
    }
}
