package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;

public final class TagMapper {

    private TagMapper() {}

    public static TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.id(), tag.name(), tag.category().name());
    }
}
