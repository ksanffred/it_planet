package ru.tramplin_itplanet.tramplin.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tramplin_itplanet.tramplin.domain.exception.TagAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateTagCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.repository.TagRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
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
}
