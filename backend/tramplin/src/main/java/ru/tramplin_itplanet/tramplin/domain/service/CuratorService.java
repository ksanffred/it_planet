package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.Curator;

public interface CuratorService {
    Curator create(String requesterEmail, Long userId);
    void delete(String requesterEmail, Long curatorId);
}
