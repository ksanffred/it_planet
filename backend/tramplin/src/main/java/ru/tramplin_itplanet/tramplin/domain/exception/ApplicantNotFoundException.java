package ru.tramplin_itplanet.tramplin.domain.exception;

public class ApplicantNotFoundException extends ResourceNotFoundException {

    public ApplicantNotFoundException(Long id) {
        super("Applicant not found with id: " + id);
    }
}
