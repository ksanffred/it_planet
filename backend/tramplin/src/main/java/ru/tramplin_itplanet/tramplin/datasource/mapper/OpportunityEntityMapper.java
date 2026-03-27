package ru.tramplin_itplanet.tramplin.datasource.mapper;

import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.domain.model.Employer;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;

import java.util.List;

public final class OpportunityEntityMapper {

    private OpportunityEntityMapper() {}

    public static Opportunity toDomain(OpportunityEntity entity) {
        return new Opportunity(
                entity.getId(),
                toEmployerDomain(entity.getEmployer()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getType(),
                entity.getFormat(),
                entity.getAddress(),
                entity.getCity(),
                entity.getLat(),
                entity.getLng(),
                entity.getSalaryFrom(),
                entity.getSalaryTo(),
                entity.getPublishedAt(),
                entity.getExpiresAt(),
                entity.getStatus(),
                List.copyOf(entity.getMedia()),
                entity.getTags().stream().map(OpportunityEntityMapper::toTagDomain).toList()
        );
    }

    private static Employer toEmployerDomain(EmployerEntity entity) {
        return new Employer(
                entity.getId(),
                entity.getName(),
                entity.getLogoUrl(),
                entity.getWebsite(),
                entity.getSocials()
        );
    }

    private static Tag toTagDomain(TagEntity entity) {
        return new Tag(entity.getId(), entity.getName(), entity.getCategory());
    }
}
