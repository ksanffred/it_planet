package ru.tramplin_itplanet.tramplin.domain.exception;

public class OpportunityResponseAlreadyExistsException extends RuntimeException {

    public OpportunityResponseAlreadyExistsException(Long opportunityId, Long applicantId) {
        super("Response already exists for opportunity id: " + opportunityId + " and applicant id: " + applicantId);
    }
}
