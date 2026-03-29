package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantOpportunityRecommendationEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantContactRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantOpportunityRecommendationRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantOpportunityRecommendationAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidApplicantOpportunityRecommendationOperationException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityRecommendation;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantOpportunityRecommendationServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaApplicantRepository jpaApplicantRepository;

    @Mock
    private JpaOpportunityRepository jpaOpportunityRepository;

    @Mock
    private JpaApplicantContactRepository jpaApplicantContactRepository;

    @Mock
    private JpaApplicantOpportunityRecommendationRepository jpaRecommendationRepository;

    @InjectMocks
    private ApplicantOpportunityRecommendationServiceImpl recommendationService;

    @Test
    void create_validVerifiedContact_returnsCreatedRecommendation() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity recommender = buildApplicant(3L, 11L);
        ApplicantEntity recommended = buildApplicant(7L, 12L);
        OpportunityEntity opportunity = buildOpportunity(10L);

        ApplicantOpportunityRecommendationEntity saved = new ApplicantOpportunityRecommendationEntity();
        saved.setId(15L);
        saved.setRecommender(recommender);
        saved.setRecommendedApplicant(recommended);
        saved.setOpportunity(opportunity);
        saved.setCreatedAt(LocalDateTime.of(2026, 3, 29, 11, 0));

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(recommender));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(recommended));
        when(jpaApplicantContactRepository.existsBetweenApplicantsWithStatus(3L, 7L, ApplicantContactStatus.ACCEPTED))
                .thenReturn(true);
        when(jpaOpportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jpaRecommendationRepository.existsRecommendation(3L, 7L, 10L)).thenReturn(false);
        when(jpaRecommendationRepository.save(any(ApplicantOpportunityRecommendationEntity.class))).thenReturn(saved);

        ApplicantOpportunityRecommendation result = recommendationService.create("applicant@example.com", 10L, 7L);

        assertThat(result.id()).isEqualTo(15L);
        assertThat(result.recommenderApplicantId()).isEqualTo(3L);
        assertThat(result.recommendedApplicantId()).isEqualTo(7L);
        assertThat(result.opportunityId()).isEqualTo(10L);
    }

    @Test
    void create_notVerifiedContact_throwsBadRequest() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity recommender = buildApplicant(3L, 11L);
        ApplicantEntity recommended = buildApplicant(7L, 12L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(recommender));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(recommended));
        when(jpaApplicantContactRepository.existsBetweenApplicantsWithStatus(3L, 7L, ApplicantContactStatus.ACCEPTED))
                .thenReturn(false);

        assertThatThrownBy(() -> recommendationService.create("applicant@example.com", 10L, 7L))
                .isInstanceOf(InvalidApplicantOpportunityRecommendationOperationException.class)
                .hasMessageContaining("verified contacts");
    }

    @Test
    void create_duplicate_throwsConflict() {
        UserEntity user = buildUser(11L, "applicant@example.com", UserRole.APPLICANT);
        ApplicantEntity recommender = buildApplicant(3L, 11L);
        ApplicantEntity recommended = buildApplicant(7L, 12L);
        OpportunityEntity opportunity = buildOpportunity(10L);

        when(jpaUserRepository.findByEmail("applicant@example.com")).thenReturn(Optional.of(user));
        when(jpaApplicantRepository.findByUserIdWithSkills(11L)).thenReturn(Optional.of(recommender));
        when(jpaApplicantRepository.findById(7L)).thenReturn(Optional.of(recommended));
        when(jpaApplicantContactRepository.existsBetweenApplicantsWithStatus(3L, 7L, ApplicantContactStatus.ACCEPTED))
                .thenReturn(true);
        when(jpaOpportunityRepository.findById(10L)).thenReturn(Optional.of(opportunity));
        when(jpaRecommendationRepository.existsRecommendation(3L, 7L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> recommendationService.create("applicant@example.com", 10L, 7L))
                .isInstanceOf(ApplicantOpportunityRecommendationAlreadyExistsException.class);
    }

    @Test
    void create_unknownUser_throwsUnauthorized() {
        when(jpaUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendationService.create("missing@example.com", 10L, 7L))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid authentication token");
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
