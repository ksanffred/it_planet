package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateEmployerByCuratorCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateEmployerCommand;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.RegisterEmployerRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateEmployerByCuratorRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateEmployerRequest;

public final class EmployerMapper {

    private EmployerMapper() {}

    public static CreateEmployerCommand toCommand(RegisterEmployerRequest request) {
        return new CreateEmployerCommand(
                request.userId(),
                request.companyName(),
                request.description(),
                request.inn(),
                request.website(),
                request.socials(),
                request.logoUrl()
        );
    }

    public static EmployerProfileResponse toResponse(EmployerProfile profile) {
        return new EmployerProfileResponse(
                profile.id(),
                profile.userId(),
                profile.companyName(),
                profile.description(),
                profile.inn(),
                profile.website(),
                profile.socials(),
                profile.logoUrl(),
                profile.verifiedOrgName(),
                profile.status()
        );
    }

    public static UpdateEmployerCommand toCommand(UpdateEmployerRequest request) {
        return new UpdateEmployerCommand(
                request.description(),
                request.website(),
                request.socials(),
                request.logoUrl()
        );
    }

    public static UpdateEmployerByCuratorCommand toCommand(UpdateEmployerByCuratorRequest request) {
        return new UpdateEmployerByCuratorCommand(
                request.userId(),
                request.companyName(),
                request.description(),
                request.inn(),
                request.website(),
                request.socials(),
                request.logoUrl(),
                request.verifiedOrgName(),
                request.status()
        );
    }
}
