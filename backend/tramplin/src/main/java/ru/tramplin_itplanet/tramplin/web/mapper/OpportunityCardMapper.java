package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.Employer;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerResponse;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityCardResponse;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;

public final class OpportunityCardMapper {

    private OpportunityCardMapper() {}

    public static OpportunityCardResponse toResponse(Opportunity opportunity) {
        return new OpportunityCardResponse(
                opportunity.id(),
                opportunity.title(),
                opportunity.description(),
                opportunity.media(),
                toEmployerResponse(opportunity.employer()),
                opportunity.type().name(),
                opportunity.format().name(),
                opportunity.address(),
                opportunity.city(),
                opportunity.salaryFrom(),
                opportunity.salaryTo(),
                opportunity.publishedAt(),
                opportunity.expiresAt(),
                opportunity.status().name(),
                opportunity.tags().stream().map(OpportunityCardMapper::toTagResponse).toList()
        );
    }

    private static EmployerResponse toEmployerResponse(Employer employer) {
        return new EmployerResponse(
                employer.id(),
                employer.name(),
                employer.logoUrl(),
                employer.website(),
                employer.contacts()
        );
    }

    private static TagResponse toTagResponse(Tag tag) {
        return new TagResponse(tag.id(), tag.name(), tag.category().name());
    }
}
