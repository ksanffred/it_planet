package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityApplication;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerOpportunityApplicationItem;

public final class EmployerOpportunityApplicationMapper {

    private EmployerOpportunityApplicationMapper() {
    }

    public static EmployerOpportunityApplicationItem toResponse(EmployerOpportunityApplication application) {
        return new EmployerOpportunityApplicationItem(
                application.responseId(),
                application.opportunityId(),
                application.title(),
                application.companyName(),
                application.responseStatus().name(),
                application.opportunityType().name(),
                application.opportunityStatus().name(),
                application.applicantId(),
                application.applicantName(),
                application.appliedAt()
        );
    }
}
