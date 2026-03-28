package ru.tramplin_itplanet.tramplin.domain.exception;

public class ApplicantContactAlreadyExistsException extends RuntimeException {

    public ApplicantContactAlreadyExistsException(Long requesterId, Long recipientId) {
        super("Contact request already exists between applicants: " + requesterId + " and " + recipientId);
    }
}
