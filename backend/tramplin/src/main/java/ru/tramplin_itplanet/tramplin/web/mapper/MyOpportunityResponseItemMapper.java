package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;
import ru.tramplin_itplanet.tramplin.web.dto.MyOpportunityResponseItem;

public final class MyOpportunityResponseItemMapper {

    private MyOpportunityResponseItemMapper() {
    }

    public static MyOpportunityResponseItem toResponse(ApplicantOpportunityResponseCard card) {
        return new MyOpportunityResponseItem(
                card.title(),
                card.companyName(),
                card.responseStatus().name(),
                card.opportunityType().name(),
                card.opportunityStatus().name()
        );
    }
}
