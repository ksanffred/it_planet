package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.OpportunityMiniCard;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityMiniCardResponse;

public final class OpportunityMiniCardMapper {

    private OpportunityMiniCardMapper() {}

    public static OpportunityMiniCardResponse toResponse(OpportunityMiniCard opportunity) {
        return new OpportunityMiniCardResponse(
                opportunity.id(),
                opportunity.media(),
                opportunity.title(),
                opportunity.description(),
                opportunity.employerName(),
                opportunity.format(),
                opportunity.tags()
        );
    }
}
