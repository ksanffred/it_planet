package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityResponseCreatedResponse;

public final class OpportunityResponseMapper {

    private OpportunityResponseMapper() {
    }

    public static OpportunityResponseCreatedResponse toResponse(OpportunityResponse opportunityResponse) {
        return new OpportunityResponseCreatedResponse(
                opportunityResponse.id(),
                opportunityResponse.opportunityId(),
                opportunityResponse.applicantId(),
                opportunityResponse.status().name(),
                opportunityResponse.createdAt(),
                opportunityResponse.updatedAt()
        );
    }
}
