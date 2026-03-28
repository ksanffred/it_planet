package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;

import java.util.List;

public interface OpportunityResponseService {
    OpportunityResponse apply(Long opportunityId, String userEmail);
    List<ApplicantOpportunityResponseCard> getMyResponses(String userEmail);
}
