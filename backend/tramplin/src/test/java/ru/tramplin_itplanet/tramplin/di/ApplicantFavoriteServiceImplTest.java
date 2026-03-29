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
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantFavoriteOpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantFavoriteOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavorites;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavoriteOpportunityCard;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityType;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantFavoriteServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaApplicantRepository jpaApplicantRepository;

    @Mock
    private JpaOpportunityRepository jpaOpportunityRepository;

    @Mock
    private JpaApplicantFavoriteOpportunityRepository jpaApplicantFavoriteOpportunityRepository;

    @InjectMocks
    private ApplicantFavoriteServiceImpl applicantFavoriteService;

    @Test
    void addOneByUserEmail_newFavorite_persistsAndReturnsSnapshot() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(1L, 12L);
        OpportunityEntity opportunity = buildOpportunity(10L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaOpportunityRepository.findAllById(List.of(10L))).thenReturn(List.of(opportunity));
        when(jpaApplicantFavoriteOpportunityRepository.findOpportunityIdsByApplicantId(1L))
                .thenReturn(List.of(), List.of(10L));

        ApplicantFavorites result = applicantFavoriteService.addOneByUserEmail("applicant@example.com", 10L);

        assertThat(result.applicantId()).isEqualTo(1L);
        assertThat(result.opportunityIds()).containsExactly(10L);
        verify(jpaApplicantFavoriteOpportunityRepository)
                .saveAll(org.mockito.ArgumentMatchers.<ApplicantFavoriteOpportunityEntity>anyList());
    }

    @Test
    void addManyByUserEmail_duplicatesAndExisting_skipsDuplicateInsert() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(1L, 12L);
        OpportunityEntity opportunity10 = buildOpportunity(10L);
        OpportunityEntity opportunity12 = buildOpportunity(12L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaOpportunityRepository.findAllById(List.of(12L, 10L))).thenReturn(List.of(opportunity12, opportunity10));
        when(jpaApplicantFavoriteOpportunityRepository.findOpportunityIdsByApplicantId(1L))
                .thenReturn(List.of(10L), List.of(12L, 10L));

        ApplicantFavorites result = applicantFavoriteService.addManyByUserEmail(
                "applicant@example.com",
                List.of(12L, 10L, 12L)
        );

        assertThat(result.opportunityIds()).containsExactly(12L, 10L);
        verify(jpaApplicantFavoriteOpportunityRepository)
                .saveAll(org.mockito.ArgumentMatchers.<ApplicantFavoriteOpportunityEntity>anyList());
    }

    @Test
    void addManyByUserEmail_missingOpportunity_throwsNotFound() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(1L, 12L);
        OpportunityEntity existing = buildOpportunity(10L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaOpportunityRepository.findAllById(List.of(10L, 99L))).thenReturn(List.of(existing));

        assertThatThrownBy(() -> applicantFavoriteService.addManyByUserEmail(
                "applicant@example.com",
                List.of(10L, 99L)
        ))
                .isInstanceOf(OpportunityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addManyByUserEmail_nonApplicantRole_throwsForbidden() {
        UserEntity user = buildUser(12L, "employer@example.com", UserRole.EMPLOYER);
        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> applicantFavoriteService.addManyByUserEmail(
                "employer@example.com",
                List.of(1L)
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("APPLICANT");
    }

    @Test
    void addManyByUserEmail_unknownUser_throwsUnauthorized() {
        when(jpaUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicantFavoriteService.addManyByUserEmail(
                "missing@example.com",
                List.of(1L)
        ))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid authentication token");

        verify(jpaApplicantFavoriteOpportunityRepository, never())
                .saveAll(org.mockito.ArgumentMatchers.<ApplicantFavoriteOpportunityEntity>anyList());
    }

    @Test
    void addManyByUserEmail_emptyInput_returnsExistingFavorites() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(1L, 12L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaApplicantFavoriteOpportunityRepository.findOpportunityIdsByApplicantId(1L))
                .thenReturn(List.of(7L, 3L));

        ApplicantFavorites result = applicantFavoriteService.addManyByUserEmail(
                "applicant@example.com",
                List.of()
        );

        assertThat(result.opportunityIds()).containsExactly(7L, 3L);
        verify(jpaApplicantFavoriteOpportunityRepository, never())
                .saveAll(org.mockito.ArgumentMatchers.<ApplicantFavoriteOpportunityEntity>anyList());
    }

    @Test
    void removeOneByUserEmail_existingFavorite_returnsUpdatedSnapshot() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(1L, 12L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaApplicantFavoriteOpportunityRepository.findOpportunityIdsByApplicantId(1L))
                .thenReturn(List.of(5L, 3L));

        ApplicantFavorites result = applicantFavoriteService.removeOneByUserEmail("applicant@example.com", 10L);

        assertThat(result.opportunityIds()).containsExactly(5L, 3L);
        verify(jpaApplicantFavoriteOpportunityRepository)
                .deleteById(org.mockito.ArgumentMatchers.argThat(id ->
                        id.getApplicantId().equals(1L) && id.getOpportunityId().equals(10L)
                ));
    }

    @Test
    void getCardsByUserEmail_existingFavorites_returnsCardSummaries() {
        UserEntity user = buildUser(12L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity applicant = buildApplicant(1L, 12L);

        EmployerEntity employer = new EmployerEntity();
        employer.setName("Acme Corp");

        OpportunityEntity opportunity = buildOpportunity(10L);
        opportunity.setTitle("Java Developer");
        opportunity.setStatus(OpportunityStatus.ACTIVE);
        opportunity.setType(OpportunityType.VACANCY);
        opportunity.setEmployer(employer);

        ApplicantFavoriteOpportunityEntity favorite = new ApplicantFavoriteOpportunityEntity();
        favorite.setOpportunity(opportunity);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(12L)).thenReturn(Optional.of(applicant));
        when(jpaApplicantFavoriteOpportunityRepository.findAllByApplicantIdWithOpportunity(1L))
                .thenReturn(List.of(favorite));

        List<ApplicantFavoriteOpportunityCard> result = applicantFavoriteService.getCardsByUserEmail("applicant@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Java Developer");
        assertThat(result.getFirst().companyName()).isEqualTo("Acme Corp");
        assertThat(result.getFirst().status()).isEqualTo(OpportunityStatus.ACTIVE);
        assertThat(result.getFirst().type()).isEqualTo(OpportunityType.VACANCY);
    }

    @Test
    void getCardsByApplicantIdForViewer_publicProfile_allowsApplicantViewer() {
        UserEntity viewer = buildUser(21L, "viewer@example.com", UserRole.APPLICANT);
        ApplicantEntity target = buildApplicant(7L, 30L);
        target.setVisibility(ApplicantVisibility.PUBLIC);

        EmployerEntity employer = new EmployerEntity();
        employer.setName("Acme Corp");

        OpportunityEntity opportunity = buildOpportunity(10L);
        opportunity.setTitle("Java Developer");
        opportunity.setStatus(OpportunityStatus.ACTIVE);
        opportunity.setType(OpportunityType.VACANCY);
        opportunity.setEmployer(employer);

        ApplicantFavoriteOpportunityEntity favorite = new ApplicantFavoriteOpportunityEntity();
        favorite.setOpportunity(opportunity);

        when(jpaUserRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(target));
        when(jpaApplicantFavoriteOpportunityRepository.findAllByApplicantIdWithOpportunity(7L))
                .thenReturn(List.of(favorite));

        List<ApplicantFavoriteOpportunityCard> result =
                applicantFavoriteService.getCardsByApplicantIdForViewer("viewer@example.com", 7L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Java Developer");
    }

    @Test
    void getCardsByApplicantIdForViewer_privateProfile_otherApplicantDenied() {
        UserEntity viewer = buildUser(21L, "viewer@example.com", UserRole.APPLICANT);
        ApplicantEntity target = buildApplicant(7L, 30L);
        target.setVisibility(ApplicantVisibility.PRIVATE);

        when(jpaUserRepository.findByEmail("viewer@example.com")).thenReturn(Optional.of(viewer));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> applicantFavoriteService.getCardsByApplicantIdForViewer("viewer@example.com", 7L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("private");
    }

    @Test
    void getCardsByApplicantIdForViewer_privateProfile_employerAllowed() {
        UserEntity viewer = buildUser(22L, "employer@example.com", UserRole.EMPLOYER);
        ApplicantEntity target = buildApplicant(7L, 30L);
        target.setVisibility(ApplicantVisibility.PRIVATE);

        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(viewer));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(target));
        when(jpaApplicantFavoriteOpportunityRepository.findAllByApplicantIdWithOpportunity(7L))
                .thenReturn(List.of());

        List<ApplicantFavoriteOpportunityCard> result =
                applicantFavoriteService.getCardsByApplicantIdForViewer("employer@example.com", 7L);

        assertThat(result).isEmpty();
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
