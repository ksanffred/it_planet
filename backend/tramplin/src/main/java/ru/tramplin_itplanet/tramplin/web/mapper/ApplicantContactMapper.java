package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContact;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantContactResponse;

public final class ApplicantContactMapper {

    private ApplicantContactMapper() {
    }

    public static ApplicantContactResponse toResponse(ApplicantContact contact) {
        return new ApplicantContactResponse(
                contact.id(),
                contact.requesterApplicantId(),
                contact.recipientApplicantId(),
                contact.status().name(),
                contact.createdAt(),
                contact.updatedAt()
        );
    }
}
