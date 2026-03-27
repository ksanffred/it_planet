package ru.tramplin_itplanet.tramplin.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaTagRepository;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.repository.TagRepository;

import java.util.List;

@Repository
public class TagRepositoryAdapter implements TagRepository {

    private static final Logger log = LoggerFactory.getLogger(TagRepositoryAdapter.class);

    private final JpaTagRepository jpaTagRepository;

    public TagRepositoryAdapter(JpaTagRepository jpaTagRepository) {
        this.jpaTagRepository = jpaTagRepository;
    }

    @Override
    public List<Tag> findAll() {
        log.debug("Loading all tags");
        return jpaTagRepository.findAll(Sort.by("category").ascending().and(Sort.by("name").ascending()))
                .stream()
                .map(entity -> new Tag(entity.getId(), entity.getName(), entity.getCategory()))
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        log.debug("Checking if tag exists by name: {}", name);
        return jpaTagRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Tag save(String name, TagCategory category) {
        log.debug("Persisting tag: name={}, category={}", name, category);

        TagEntity entity = new TagEntity();
        entity.setName(name);
        entity.setCategory(category);

        TagEntity saved = jpaTagRepository.save(entity);
        return new Tag(saved.getId(), saved.getName(), saved.getCategory());
    }
}
