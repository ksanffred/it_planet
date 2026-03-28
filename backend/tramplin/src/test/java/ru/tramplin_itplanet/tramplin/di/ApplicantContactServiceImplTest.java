package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantContactEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantContactRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantContactAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidApplicantContactOperationException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContact;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactPreview;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantContactServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaApplicantRepository jpaApplicantRepository;

    @Mock
    private JpaApplicantContactRepository jpaApplicantContactRepository;

    @InjectMocks
    private ApplicantContactServiceImpl applicantContactService;

    @Test
    void create_validApplicants_returnsPendingContact() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity requester = buildApplicant(3L, 11L);
        ApplicantEntity recipient = buildApplicant(7L, 12L);

        ApplicantContactEntity saved = new ApplicantContactEntity();
        saved.setId(1L);
        saved.setRequester(requester);
        saved.setRecipient(recipient);
        saved.setStatus(ApplicantContactStatus.PENDING);
        saved.setCreatedAt(LocalDateTime.of(2026, 3, 29, 10, 0));
        saved.setUpdatedAt(LocalDateTime.of(2026, 3, 29, 10, 0));

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(requester));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(recipient));
        when(jpaApplicantContactRepository.existsBetweenApplicants(3L, 7L)).thenReturn(false);
        when(jpaApplicantContactRepository.save(any(ApplicantContactEntity.class))).thenReturn(saved);

        ApplicantContact result = applicantContactService.create("applicant@example.com", 7L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(ApplicantContactStatus.PENDING);
        assertThat(result.requesterApplicantId()).isEqualTo(3L);
        assertThat(result.recipientApplicantId()).isEqualTo(7L);
    }

    @Test
    void create_duplicate_throwsConflict() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity requester = buildApplicant(3L, 11L);
        ApplicantEntity recipient = buildApplicant(7L, 12L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(requester));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(recipient));
        when(jpaApplicantContactRepository.existsBetweenApplicants(3L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> applicantContactService.create("applicant@example.com", 7L))
                .isInstanceOf(ApplicantContactAlreadyExistsException.class);
    }

    @Test
    void updateStatus_nonRecipient_throwsForbidden() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity currentApplicant = buildApplicant(3L, 11L);
        ApplicantEntity requester = buildApplicant(4L, 13L);
        ApplicantEntity recipient = buildApplicant(7L, 12L);

        ApplicantContactEntity existing = new ApplicantContactEntity();
        existing.setId(1L);
        existing.setRequester(requester);
        existing.setRecipient(recipient);
        existing.setStatus(ApplicantContactStatus.PENDING);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(currentApplicant));
        when(jpaApplicantContactRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> applicantContactService.updateStatus(
                "applicant@example.com",
                1L,
                ApplicantContactStatus.ACCEPTED
        ))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateStatus_pendingStatusInRequest_throwsBadRequest() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity currentApplicant = buildApplicant(3L, 11L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(currentApplicant));

        assertThatThrownBy(() -> applicantContactService.updateStatus(
                "applicant@example.com",
                1L,
                ApplicantContactStatus.PENDING
        ))
                .isInstanceOf(InvalidApplicantContactOperationException.class)
                .hasMessageContaining("ACCEPTED or REJECTED");
    }

    @Test
    void create_unknownUser_throwsUnauthorized() {
        when(jpaUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicantContactService.create("missing@example.com", 7L))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid authentication token");
    }

    @Test
    void getMyContacts_returnsAcceptedContactsOnly() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity currentApplicant = buildApplicant(3L, 11L);

        ApplicantEntity requester = buildApplicant(3L, 11L);
        requester.setName("Current User");

        ApplicantEntity recipient = buildApplicant(7L, 12L);
        recipient.setName("Ivan Ivanov");
        recipient.setDesiredPosition("Backend Developer Intern");

        ApplicantContactEntity accepted = new ApplicantContactEntity();
        accepted.setId(1L);
        accepted.setRequester(requester);
        accepted.setRecipient(recipient);
        accepted.setStatus(ApplicantContactStatus.ACCEPTED);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(currentApplicant));
        when(jpaApplicantContactRepository.findByApplicantIdAndStatusWithApplicants(3L, ApplicantContactStatus.ACCEPTED))
                .thenReturn(java.util.List.of(accepted));

        java.util.List<ApplicantContactPreview> result = applicantContactService.getMyContacts("applicant@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Ivan Ivanov");
        assertThat(result.getFirst().desiredProfession()).isEqualTo("Backend Developer Intern");
    }

    private static UserEntity buildUser(Long id, String email, UserRole role) {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private static ApplicantEntity buildApplicant(Long id, Long userId) {
        ApplicantEntity applicant = new ApplicantEntity();
        applicant.setId(id);
        applicant.setUserId(userId);
        return applicant;
    }
}
