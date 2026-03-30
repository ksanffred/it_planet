package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;
import java.util.List;

public record OpportunityMiniCard(
        Long id,
        String media,
        String title,
        String description,
        String employerName,
        String type,
        String format,
        String city,
        String address,
        Double lat,
        Double lng,
        List<String> tags
) implements Serializable {}
