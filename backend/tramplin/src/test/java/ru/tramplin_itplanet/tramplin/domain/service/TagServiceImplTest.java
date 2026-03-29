package ru.tramplin_itplanet.tramplin.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tramplin_itplanet.tramplin.domain.exception.TagAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.TagNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateTagCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateTagCommand;
import ru.tramplin_itplanet.tramplin.domain.repository.TagRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void findAll_returnsAllTags() {
        List<Tag> expected = List.of(
                new Tag(1L, "Java", TagCategory.TECHNOLOGY),
                new Tag(2L, "Middle", TagCategory.LEVEL)
        );
        when(tagRepository.findAll()).thenReturn(expected);

        List<Tag> result = tagService.findAll();

        assertThat(result).isEqualTo(expected);
        verify(tagRepository).findAll();
    }

    @Test
    void create_uniqueName_returnsSavedTag() {
        CreateTagCommand command = new CreateTagCommand("  Docker  ", TagCategory.TECHNOLOGY);
        Tag expected = new Tag(1L, "Docker", TagCategory.TECHNOLOGY);

        when(tagRepository.existsByNameIgnoreCase("Docker")).thenReturn(false);
        when(tagRepository.save("Docker", TagCategory.TECHNOLOGY)).thenReturn(expected);

        Tag result = tagService.create(command);

        assertThat(result).isEqualTo(expected);
        verify(tagRepository).existsByNameIgnoreCase("Docker");
        verify(tagRepository).save("Docker", TagCategory.TECHNOLOGY);
    }

    @Test
    void create_duplicateName_throwsTagAlreadyExistsException() {
        CreateTagCommand command = new CreateTagCommand("Java", TagCategory.TECHNOLOGY);

        when(tagRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(command))
                .isInstanceOf(TagAlreadyExistsException.class)
                .hasMessage("Tag already exists with name: Java");

        verify(tagRepository).existsByNameIgnoreCase("Java");
        verify(tagRepository, never()).save("Java", TagCategory.TECHNOLOGY);
    }

    @Test
    void update_uniqueName_returnsUpdatedTag() {
        UpdateTagCommand command = new UpdateTagCommand("  Java Core  ", TagCategory.TECHNOLOGY);
        Tag expected = new Tag(1L, "Java Core", TagCategory.TECHNOLOGY);

        when(tagRepository.existsByNameIgnoreCaseAndIdNot("Java Core", 1L)).thenReturn(false);
        when(tagRepository.update(1L, new UpdateTagCommand("Java Core", TagCategory.TECHNOLOGY)))
                .thenReturn(expected);

        Tag result = tagService.update(1L, command);

        assertThat(result).isEqualTo(expected);
        verify(tagRepository).existsByNameIgnoreCaseAndIdNot("Java Core", 1L);
        verify(tagRepository).update(1L, new UpdateTagCommand("Java Core", TagCategory.TECHNOLOGY));
    }

    @Test
    void update_duplicateName_throwsTagAlreadyExistsException() {
        UpdateTagCommand command = new UpdateTagCommand("Docker", TagCategory.TECHNOLOGY);
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("Docker", 1L)).thenReturn(true);

        assertThatThrownBy(() -> tagService.update(1L, command))
                .isInstanceOf(TagAlreadyExistsException.class)
                .hasMessage("Tag already exists with name: Docker");

        verify(tagRepository).existsByNameIgnoreCaseAndIdNot("Docker", 1L);
        verify(tagRepository, never()).update(1L, command);
    }

    @Test
    void delete_existingTag_deletesTag() {
        tagService.delete(1L);

        verify(tagRepository).deleteById(1L);
    }

    @Test
    void delete_missingTag_throwsTagNotFoundException() {
        doThrow(new TagNotFoundException(999L)).when(tagRepository).deleteById(999L);

        assertThatThrownBy(() -> tagService.delete(999L))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tag not found with id: 999");

        verify(tagRepository).deleteById(999L);
    }
}
