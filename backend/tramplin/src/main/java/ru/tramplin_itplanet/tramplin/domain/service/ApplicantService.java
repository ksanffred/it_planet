package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateCurrentApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateApplicantCommand;

public interface ApplicantService {
    ApplicantProfile create(CreateApplicantCommand command);
    ApplicantProfile getById(Long id);
    ApplicantProfile update(Long id, UpdateApplicantCommand command);
    ApplicantProfile getCurrentByUserEmail(String email);
    ApplicantProfile updateCurrentByUserEmail(String email, UpdateCurrentApplicantCommand command);
    ApplicantProfile updateVisibilityByUserEmail(String email, ApplicantVisibility visibility);
}
