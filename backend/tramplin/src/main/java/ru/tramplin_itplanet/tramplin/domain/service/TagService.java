package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.CreateTagCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateTagCommand;

import java.util.List;

public interface TagService {

    List<Tag> findAll();

    Tag create(CreateTagCommand command);
    Tag update(Long id, UpdateTagCommand command);
    void delete(Long id);
}
