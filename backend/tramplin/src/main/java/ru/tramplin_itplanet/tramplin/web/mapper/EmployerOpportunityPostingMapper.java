package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityPosting;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerOpportunityPostingResponse;

public final class EmployerOpportunityPostingMapper {

    private EmployerOpportunityPostingMapper() {
    }

    public static EmployerOpportunityPostingResponse toResponse(EmployerOpportunityPosting posting) {
        return new EmployerOpportunityPostingResponse(
                posting.id(),
                posting.title(),
                posting.status().name(),
                posting.type().name(),
                posting.publishedAt(),
                posting.expiresAt(),
                posting.applicationsCount()
        );
    }
}
