package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;

public interface EmployerService {
    EmployerProfile register(CreateEmployerCommand command);
    EmployerProfile getById(Long id);
}
