package ru.tramplin_itplanet.tramplin.domain.repository;

import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateTagCommand;

import java.util.List;

public interface TagRepository {

    List<Tag> findAll();

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Tag save(String name, TagCategory category);
    Tag update(Long id, UpdateTagCommand command);
    void deleteById(Long id);
}
