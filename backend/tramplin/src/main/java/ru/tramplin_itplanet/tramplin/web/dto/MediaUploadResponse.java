package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Uploaded media path information")
public record MediaUploadResponse(
        @Schema(description = "Path of the object stored in S3", example = "opportunities/1/c6ac601f-3ee2-470c-a448-4ff45f6b1d9d.png") String path,
        @Schema(description = "Public URL if configured", example = "https://cdn.tramplin.ru/opportunities/1/c6ac601f-3ee2-470c-a448-4ff45f6b1d9d.png") String url
) {}
