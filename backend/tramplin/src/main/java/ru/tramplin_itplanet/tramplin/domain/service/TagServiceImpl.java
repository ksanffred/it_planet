package ru.tramplin_itplanet.tramplin.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.domain.exception.TagAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateTagCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateTagCommand;
import ru.tramplin_itplanet.tramplin.domain.repository.TagRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private static final Logger log = LoggerFactory.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<Tag> findAll() {
        log.info("Fetching all tags");
        return tagRepository.findAll();
    }

    @Override
    @Transactional
    public Tag create(CreateTagCommand command) {
        String normalizedName = command.name().trim();
        log.info("Creating tag: name={}, category={}", normalizedName, command.category());

        if (tagRepository.existsByNameIgnoreCase(normalizedName)) {
            log.warn("Tag creation failed: duplicate name={}", normalizedName);
            throw new TagAlreadyExistsException(normalizedName);
        }

        Tag saved = tagRepository.save(normalizedName, command.category());
        log.info("Tag created with id={}, name={}", saved.id(), saved.name());
        return saved;
    }

    @Override
    @Transactional
    public Tag update(Long id, UpdateTagCommand command) {
        String normalizedName = command.name().trim();
        log.info("Updating tag: id={}, name={}, category={}", id, normalizedName, command.category());
        if (tagRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            log.warn("Tag update failed: duplicate name={} for id={}", normalizedName, id);
            throw new TagAlreadyExistsException(normalizedName);
        }
        return tagRepository.update(id, new UpdateTagCommand(normalizedName, command.category()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting tag: id={}", id);
        tagRepository.deleteById(id);
    }
}
