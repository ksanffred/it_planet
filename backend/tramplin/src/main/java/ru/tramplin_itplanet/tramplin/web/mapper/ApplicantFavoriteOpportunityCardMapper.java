package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavoriteOpportunityCard;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantFavoriteOpportunityCardResponse;

public final class ApplicantFavoriteOpportunityCardMapper {

    private ApplicantFavoriteOpportunityCardMapper() {
    }

    public static ApplicantFavoriteOpportunityCardResponse toResponse(ApplicantFavoriteOpportunityCard card) {
        return new ApplicantFavoriteOpportunityCardResponse(
                card.title(),
                card.companyName(),
                card.status().name(),
                card.type().name()
        );
    }
}
