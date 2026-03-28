package ru.tramplin_itplanet.tramplin.domain.service;

import org.springframework.web.multipart.MultipartFile;
import ru.tramplin_itplanet.tramplin.web.dto.MediaUploadResponse;

public interface MediaService {
    MediaUploadResponse uploadEmployerLogo(Long employerId, MultipartFile file);
    MediaUploadResponse uploadApplicantAvatar(Long applicantId, MultipartFile file);
    MediaUploadResponse uploadOpportunityDraftMedia(MultipartFile file);
    MediaUploadResponse uploadOpportunityMedia(Long opportunityId, MultipartFile file);
    MediaUploadResponse uploadApplicantResume(Long applicantId, MultipartFile file);
}
