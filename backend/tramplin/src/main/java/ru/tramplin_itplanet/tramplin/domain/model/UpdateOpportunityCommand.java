package ru.tramplin_itplanet.tramplin.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateOpportunityCommand(
        Long employerId,
        String title,
        String description,
        OpportunityType type,
        OpportunityFormat format,
        String address,
        String city,
        Double lat,
        Double lng,
        BigDecimal salaryFrom,
        BigDecimal salaryTo,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt,
        OpportunityStatus status,
        List<String> media,
        List<Long> tagIds
) {}
