package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityFormat;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Request body for updating an existing opportunity card")
public record UpdateOpportunityRequest(

        @NotNull
        @Schema(description = "ID of the employer or event organiser", example = "1")
        Long employerId,

        @NotBlank
        @Schema(description = "Title of the position or event", example = "Junior Java Developer")
        String title,

        @Schema(description = "Full description including requirements", example = "Looking for a motivated developer...")
        String description,

        @NotNull
        @Schema(description = "Opportunity type", example = "VACANCY")
        OpportunityType type,

        @NotNull
        @Schema(description = "Work format", example = "REMOTE")
        OpportunityFormat format,

        @Schema(description = "Office address (required for OFFICE and HYBRID formats)", example = "Tverskaya St, 1")
        String address,

        @Schema(description = "City of the opportunity or event", example = "Moscow")
        String city,

        @Schema(description = "Latitude for map display", example = "55.7558")
        Double lat,

        @Schema(description = "Longitude for map display", example = "37.6173")
        Double lng,

        @Schema(description = "Minimum salary in RUB", example = "100000")
        BigDecimal salaryFrom,

        @Schema(description = "Maximum salary in RUB", example = "150000")
        BigDecimal salaryTo,

        @Schema(description = "Publication date")
        LocalDateTime publishedAt,

        @Schema(description = "Closing date for the vacancy or date of the event")
        LocalDateTime expiresAt,

        @NotNull
        @Schema(description = "Opportunity status", example = "ACTIVE")
        OpportunityStatus status,

        @Schema(description = "List of pre-uploaded media object paths or public URLs")
        List<String> media,

        @Schema(description = "IDs of tags to assign (technologies, level, employment type)")
        List<Long> tagIds
) {}
