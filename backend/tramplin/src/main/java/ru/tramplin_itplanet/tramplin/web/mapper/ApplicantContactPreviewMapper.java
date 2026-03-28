package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactPreview;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantContactPreviewResponse;

public final class ApplicantContactPreviewMapper {

    private ApplicantContactPreviewMapper() {
    }

    public static ApplicantContactPreviewResponse toResponse(ApplicantContactPreview preview) {
        return new ApplicantContactPreviewResponse(
                preview.photo(),
                preview.name(),
                preview.desiredProfession(),
                preview.status()
        );
    }
}
