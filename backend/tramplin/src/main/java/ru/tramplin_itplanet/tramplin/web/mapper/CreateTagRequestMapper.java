package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.CreateTagCommand;
import ru.tramplin_itplanet.tramplin.web.dto.CreateTagRequest;

public final class CreateTagRequestMapper {

    private CreateTagRequestMapper() {}

    public static CreateTagCommand toCommand(CreateTagRequest request) {
        return new CreateTagCommand(request.name(), request.category());
    }
}
