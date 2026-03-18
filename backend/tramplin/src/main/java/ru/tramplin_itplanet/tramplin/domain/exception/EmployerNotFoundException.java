package ru.tramplin_itplanet.tramplin.domain.exception;

public class EmployerNotFoundException extends ResourceNotFoundException {

    public EmployerNotFoundException(Long id) {
        super("Employer not found with id: " + id);
    }
}
