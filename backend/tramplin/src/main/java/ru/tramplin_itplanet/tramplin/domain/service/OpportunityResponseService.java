package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityApplication;

import java.util.List;

public interface OpportunityResponseService {
    OpportunityResponse apply(Long opportunityId, String userEmail);
    List<ApplicantOpportunityResponseCard> getMyResponses(String userEmail);
    List<ApplicantOpportunityResponseCard> getResponsesByApplicantIdForViewer(String viewerEmail, Long applicantId);
    List<EmployerOpportunityApplication> getApplicationsForOpportunity(Long opportunityId, String userEmail);
    List<EmployerOpportunityApplication> getApplicationsForMyOpportunities(String userEmail);
}
