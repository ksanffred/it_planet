package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContact;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactPreview;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;

import java.util.List;

public interface ApplicantContactService {
    ApplicantContact create(String userEmail, Long recipientApplicantId);
    ApplicantContact updateStatus(String userEmail, Long contactId, ApplicantContactStatus status);
    List<ApplicantContactPreview> getMyContacts(String userEmail);
}
