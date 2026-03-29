package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateEmployerByCuratorCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateEmployerCommand;

public interface EmployerService {
    EmployerProfile register(CreateEmployerCommand command);
    EmployerProfile getById(Long id);
    EmployerProfile getCurrentByUserEmail(String email);
    EmployerProfile updateCurrentByUserEmail(String email, UpdateEmployerCommand command);
    EmployerProfile updateByIdAsCurator(String email, Long id, UpdateEmployerByCuratorCommand command);
    void deleteByIdAsCurator(String email, Long id);
    void assertCanManageOpportunities(String email, Long employerId);
}
