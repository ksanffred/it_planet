package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Opportunity(
        Long id,
        Employer employer,
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
        List<Tag> tags
) implements Serializable {}
