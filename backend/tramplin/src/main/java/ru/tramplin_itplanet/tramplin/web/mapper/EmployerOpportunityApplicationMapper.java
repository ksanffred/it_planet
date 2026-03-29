package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityApplication;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerOpportunityApplicationItem;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;

public final class EmployerOpportunityApplicationMapper {

    private EmployerOpportunityApplicationMapper() {
    }

    public static EmployerOpportunityApplicationItem toResponse(EmployerOpportunityApplication application) {
        return new EmployerOpportunityApplicationItem(
                application.applicantId(),
                application.applicantName(),
                application.university(),
                application.desiredPosition(),
                application.recommendation(),
                application.matchingTags().stream()
                        .map(tag -> new TagResponse(tag.id(), tag.name(), tag.category().name()))
                        .toList()
        );
    }
}
