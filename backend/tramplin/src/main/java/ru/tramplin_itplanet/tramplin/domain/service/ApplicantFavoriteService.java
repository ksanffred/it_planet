package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavorites;

import java.util.List;

public interface ApplicantFavoriteService {
    ApplicantFavorites addOneByUserEmail(String email, Long opportunityId);
    ApplicantFavorites addManyByUserEmail(String email, List<Long> opportunityIds);
}
