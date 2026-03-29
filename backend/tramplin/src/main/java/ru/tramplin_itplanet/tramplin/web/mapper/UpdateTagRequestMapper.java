package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateTagCommand;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateTagRequest;

public final class UpdateTagRequestMapper {

    private UpdateTagRequestMapper() {
    }

    public static UpdateTagCommand toCommand(UpdateTagRequest request) {
        return new UpdateTagCommand(
                request.name(),
                TagCategory.valueOf(request.category().toUpperCase())
        );
    }
}
