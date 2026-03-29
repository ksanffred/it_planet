package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavorites;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavoriteOpportunityCard;

import java.util.List;

public interface ApplicantFavoriteService {
    ApplicantFavorites addOneByUserEmail(String email, Long opportunityId);
    ApplicantFavorites addManyByUserEmail(String email, List<Long> opportunityIds);
    ApplicantFavorites removeOneByUserEmail(String email, Long opportunityId);
    List<ApplicantFavoriteOpportunityCard> getCardsByUserEmail(String email);
    List<ApplicantFavoriteOpportunityCard> getCardsByApplicantIdForViewer(String viewerEmail, Long applicantId);
}
