package ru.tramplin_itplanet.tramplin.domain.exception;

public class ApplicantContactNotFoundException extends ResourceNotFoundException {

    public ApplicantContactNotFoundException(Long id) {
        super("Applicant contact not found with id: " + id);
    }
}
