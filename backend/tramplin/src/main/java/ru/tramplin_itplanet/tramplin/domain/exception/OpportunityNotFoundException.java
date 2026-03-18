package ru.tramplin_itplanet.tramplin.domain.exception;

public class OpportunityNotFoundException extends ResourceNotFoundException {

    public OpportunityNotFoundException(Long id) {
        super("Opportunity not found with id: " + id);
    }
}
