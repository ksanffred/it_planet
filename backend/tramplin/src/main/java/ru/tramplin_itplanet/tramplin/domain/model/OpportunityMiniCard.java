package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;
import java.util.List;

public record OpportunityMiniCard(
        Long id,
        String media,
        String title,
        String description,
        String employerName,
        String format,
        List<String> tags
) implements Serializable {}
