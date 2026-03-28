package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavorites;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantFavoritesResponse;

public final class ApplicantFavoritesMapper {

    private ApplicantFavoritesMapper() {
    }

    public static ApplicantFavoritesResponse toResponse(ApplicantFavorites favorites) {
        return new ApplicantFavoritesResponse(
                favorites.applicantId(),
                favorites.opportunityIds()
        );
    }
}
