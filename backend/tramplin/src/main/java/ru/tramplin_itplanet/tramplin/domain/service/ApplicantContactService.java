package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContact;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;

public interface ApplicantContactService {
    ApplicantContact create(String userEmail, Long recipientApplicantId);
    ApplicantContact updateStatus(String userEmail, Long contactId, ApplicantContactStatus status);
}
