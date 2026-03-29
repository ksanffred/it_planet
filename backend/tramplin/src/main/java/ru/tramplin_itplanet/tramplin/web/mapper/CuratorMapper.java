package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.Curator;
import ru.tramplin_itplanet.tramplin.web.dto.CuratorResponse;

public final class CuratorMapper {

    private CuratorMapper() {
    }

    public static CuratorResponse toResponse(Curator curator) {
        return new CuratorResponse(curator.id(), curator.userId());
    }
}
