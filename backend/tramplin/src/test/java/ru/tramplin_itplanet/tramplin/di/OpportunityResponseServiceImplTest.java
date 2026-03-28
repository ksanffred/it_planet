package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityResponseEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityResponseRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityResponseAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponseStatus;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityResponseServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaApplicantRepository jpaApplicantRepository;

    @Mock
    private JpaOpportunityRepository jpaOpportunityRepository;

    @Mock
    private JpaOpportunityResponseRepository jpaOpportunityResponseRepository;

    @InjectMocks
    private OpportunityResponseServiceImpl opportunityResponseService;

    @Test
    void apply_validApplicant_createsNotReviewedResponse() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(3L, 12L);
        OpportunityEntity opportunity = buildOpportunity(10L);

        OpportunityResponseEntity saved = new OpportunityResponseEntity();
        saved.setId(1L);
        saved.setApplicant(applicant);
        saved.setOpportunity(opportunity);
        saved.setStatus(OpportunityResponseStatus.NOT_REVIEWED);
        saved.setCreatedAt(LocalDateTime.of(2026, 3, 28, 12, 0));
        saved.setUpdatedAt(LocalDateTime.of(2026, 3, 28, 12, 0));

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaOpportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jpaOpportunityResponseRepository.existsByOpportunity_IdAndApplicant_Id(10L, 3L)).thenReturn(false);
        when(jpaOpportunityResponseRepository.save(any(OpportunityResponseEntity.class))).thenReturn(saved);

        OpportunityResponse result = opportunityResponseService.apply(10L, "applicant@example.com");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(OpportunityResponseStatus.NOT_REVIEWED);
        assertThat(result.opportunityId()).isEqualTo(10L);
        assertThat(result.applicantId()).isEqualTo(3L);
    }

    @Test
    void apply_duplicateResponse_throwsConflict() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(3L, 12L);
        OpportunityEntity opportunity = buildOpportunity(10L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaOpportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jpaOpportunityResponseRepository.existsByOpportunity_IdAndApplicant_Id(10L, 3L)).thenReturn(true);

        assertThatThrownBy(() -> opportunityResponseService.apply(10L, "applicant@example.com"))
                .isInstanceOf(OpportunityResponseAlreadyExistsException.class)
                .hasMessageContaining("opportunity id: 10");
    }

    @Test
    void apply_nonApplicantRole_throwsForbidden() {
        UserEntity user = buildUser(12L, "employer@example.com", UserRole.EMPLOYER);
        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> opportunityResponseService.apply(10L, "employer@example.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("APPLICANT");
    }

    @Test
    void apply_unknownUser_throwsUnauthorized() {
        when(jpaUserRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> opportunityResponseService.apply(10L, "unknown@example.com"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid authentication token");
    }

    @Test
    void apply_unknownOpportunity_throwsNotFound() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(3L, 12L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaOpportunityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> opportunityResponseService.apply(999L, "applicant@example.com"))
                .isInstanceOf(OpportunityNotFoundException.class)
                .hasMessageContaining("999");
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

    private static OpportunityEntity buildOpportunity(Long id) {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setId(id);
        return opportunity;
    }
}
