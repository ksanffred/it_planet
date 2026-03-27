package ru.tramplin_itplanet.tramplin.domain.repository;

import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;

import java.util.List;

public interface TagRepository {

    List<Tag> findAll();

    boolean existsByNameIgnoreCase(String name);

    Tag save(String name, TagCategory category);
}
