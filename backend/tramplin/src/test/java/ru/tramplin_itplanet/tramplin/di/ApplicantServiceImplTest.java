package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaTagRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceImplTest {

    @Mock
    private JpaApplicantRepository jpaApplicantRepository;

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaTagRepository jpaTagRepository;

    @InjectMocks
    private ApplicantServiceImpl applicantService;

    @Test
    void create_validApplicantUser_returnsProfile() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 12L);
        user.setDisplayName("Ivan Ivanov");
        user.setRole(UserRole.APPLICANT);

        TagEntity javaTag = new TagEntity();
        javaTag.setId(2L);
        javaTag.setName("Java");
        javaTag.setCategory(TagCategory.TECHNOLOGY);

        ApplicantEntity saved = new ApplicantEntity();
        saved.setId(1L);
        saved.setUserId(12L);
        saved.setName("Ivan Ivanov");
        saved.setSkills(List.of(javaTag));

        when(jpaUserRepository.findById(12L)).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.empty());
        when(jpaTagRepository.findAllById(List.of(2L))).thenReturn(List.of(javaTag));
        when(jpaApplicantRepository.save(any(ApplicantEntity.class))).thenReturn(saved);

        ApplicantProfile result = applicantService.create(new CreateApplicantCommand(
                12L,
                "RANEPA",
                "IT",
                "Software Engineering",
                "Applied Informatics",
                2027,
                "ML course",
                "https://github.com/user",
                "applicants/1/resume/cv.pdf",
                List.of(2L)
        ));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Ivan Ivanov");
        assertThat(result.skills()).hasSize(1);
        assertThat(result.skills().getFirst().name()).isEqualTo("Java");
    }

    @Test
    void create_nonApplicantRole_throwsForbidden() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 12L);
        user.setRole(UserRole.USER);

        when(jpaUserRepository.findById(12L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> applicantService.create(new CreateApplicantCommand(
                12L, null, null, null, null, null, null, null, null, List.of()
        )))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("APPLICANT");
    }

    @Test
    void create_existingApplicant_throwsConflict() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 12L);
        user.setRole(UserRole.APPLICANT);

        when(jpaUserRepository.findById(12L)).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(new ApplicantEntity()));

        assertThatThrownBy(() -> applicantService.create(new CreateApplicantCommand(
                12L, null, null, null, null, null, null, null, null, List.of()
        )))
                .isInstanceOf(ApplicantAlreadyExistsException.class);
    }

    @Test
    void getById_missingApplicant_throwsNotFound() {
        when(jpaApplicantRepository.findByIdWithSkills(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicantService.getById(999L))
                .isInstanceOf(ApplicantNotFoundException.class)
                .hasMessageContaining("999");
    }
}
