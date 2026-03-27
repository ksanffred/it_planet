package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityMiniCardResponse;

import java.util.List;

public final class OpportunityMiniCardMapper {

    private OpportunityMiniCardMapper() {}

    public static OpportunityMiniCardResponse toResponse(Opportunity opportunity) {
        return new OpportunityMiniCardResponse(
                opportunity.id(),
                firstMedia(opportunity),
                opportunity.title(),
                opportunity.description(),
                opportunity.employer().name(),
                opportunity.format().name(),
                firstThreeTagNames(opportunity)
        );
    }

    private static String firstMedia(Opportunity opportunity) {
        return opportunity.media().isEmpty() ? null : opportunity.media().getFirst();
    }

    private static List<String> firstThreeTagNames(Opportunity opportunity) {
        return opportunity.tags().stream()
                .limit(3)
                .map(tag -> tag.name())
                .toList();
    }
}
