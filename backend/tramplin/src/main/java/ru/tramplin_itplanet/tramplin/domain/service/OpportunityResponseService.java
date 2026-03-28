package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;

public interface OpportunityResponseService {
    OpportunityResponse apply(Long opportunityId, String userEmail);
}
