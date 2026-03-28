package ru.tramplin_itplanet.tramplin.domain.exception;

public class ApplicantAlreadyExistsException extends RuntimeException {

    public ApplicantAlreadyExistsException(Long userId) {
        super("Applicant profile already exists for user id: " + userId);
    }
}
