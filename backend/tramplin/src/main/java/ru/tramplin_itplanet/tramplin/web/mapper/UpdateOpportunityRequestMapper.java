package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.UpdateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateOpportunityRequest;

import java.util.List;

public final class UpdateOpportunityRequestMapper {

    private UpdateOpportunityRequestMapper() {}

    public static UpdateOpportunityCommand toCommand(UpdateOpportunityRequest request) {
        return new UpdateOpportunityCommand(
                request.employerId(),
                request.title(),
                request.description(),
                request.type(),
                request.format(),
                request.address(),
                request.city(),
                request.lat(),
                request.lng(),
                request.salaryFrom(),
                request.salaryTo(),
                request.publishedAt(),
                request.expiresAt(),
                request.status(),
                request.media() != null ? request.media() : List.of(),
                request.tagIds() != null ? request.tagIds() : List.of()
        );
    }
}
