package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;

public interface ApplicantService {
    ApplicantProfile create(CreateApplicantCommand command);
    ApplicantProfile getById(Long id);
}
