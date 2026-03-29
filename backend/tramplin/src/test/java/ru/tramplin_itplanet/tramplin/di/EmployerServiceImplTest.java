package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.UserNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateEmployerByCuratorCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerServiceImplTest {

    @Mock
    private JpaEmployerRepository jpaEmployerRepository;

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private WhoisClient whoisClient;

    @InjectMocks
    private EmployerServiceImpl employerService;

    @Test
    void register_setsPendingStatusAndReturnsProfile() {
        EmployerEntity saved = new EmployerEntity();
        saved.setId(10L);
        saved.setName("Acme");
        saved.setInn("7701234567");
        saved.setStatus("pending");

        when(jpaEmployerRepository.save(any(EmployerEntity.class))).thenReturn(saved);

        EmployerProfile result = employerService.register(new CreateEmployerCommand(
                null,
                "Acme",
                null,
                "7701234567",
                null,
                null,
                null
        ));

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.inn()).isEqualTo("7701234567");
        assertThat(result.status()).isEqualTo("pending");
        verify(jpaEmployerRepository).save(any(EmployerEntity.class));
    }

    @Test
    void register_withUnknownUserId_throwsNotFound() {
        when(jpaUserRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerService.register(new CreateEmployerCommand(
                999L,
                "Acme",
                null,
                "7701234567",
                null,
                null,
                null
        )))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void register_withVerifiedUserAndMatchingTaxpayerId_setsAutoVerified() {
        UserEntity user = new UserEntity();
        user.setEmail("zakharov@ranepa.ru");
        user.setVerified(true);

        EmployerEntity saved = new EmployerEntity();
        saved.setId(11L);
        saved.setInn("7729050901");
        saved.setVerifiedOrgName("RANEPA");
        saved.setStatus("auto_verified");

        when(jpaUserRepository.findById(11L)).thenReturn(Optional.of(user));
        when(whoisClient.findVerificationDataByDomain("ranepa.ru"))
                .thenReturn(Optional.of(new WhoisVerificationData("7729050901", "RANEPA")));
        when(jpaEmployerRepository.save(any(EmployerEntity.class))).thenReturn(saved);

        EmployerProfile result = employerService.register(new CreateEmployerCommand(
                11L,
                "RANEPA",
                null,
                "7729050901",
                null,
                null,
                null
        ));

        assertThat(result.status()).isEqualTo("auto_verified");
        assertThat(result.verifiedOrgName()).isEqualTo("RANEPA");
    }

    @Test
    void register_withVerifiedUserAndMismatchedTaxpayerId_setsAutoRejected() {
        UserEntity user = new UserEntity();
        user.setEmail("zakharov@ranepa.ru");
        user.setVerified(true);

        EmployerEntity saved = new EmployerEntity();
        saved.setId(12L);
        saved.setInn("7701234567");
        saved.setVerifiedOrgName("RANEPA");
        saved.setStatus("auto_rejected");

        when(jpaUserRepository.findById(12L)).thenReturn(Optional.of(user));
        when(whoisClient.findVerificationDataByDomain("ranepa.ru"))
                .thenReturn(Optional.of(new WhoisVerificationData("7729050901", "RANEPA")));
        when(jpaEmployerRepository.save(any(EmployerEntity.class))).thenReturn(saved);

        EmployerProfile result = employerService.register(new CreateEmployerCommand(
                12L,
                "Acme",
                null,
                "7701234567",
                null,
                null,
                null
        ));

        assertThat(result.status()).isEqualTo("auto_rejected");
        assertThat(result.verifiedOrgName()).isEqualTo("RANEPA");
    }

    @Test
    void register_withVerifiedUserAndMissingWhoisFields_setsAutoRejected() {
        UserEntity user = new UserEntity();
        user.setEmail("zakharov@ranepa.ru");
        user.setVerified(true);

        EmployerEntity saved = new EmployerEntity();
        saved.setId(13L);
        saved.setInn("7701234567");
        saved.setStatus("auto_rejected");

        when(jpaUserRepository.findById(13L)).thenReturn(Optional.of(user));
        when(whoisClient.findVerificationDataByDomain("ranepa.ru")).thenReturn(Optional.empty());
        when(jpaEmployerRepository.save(any(EmployerEntity.class))).thenReturn(saved);

        EmployerProfile result = employerService.register(new CreateEmployerCommand(
                13L,
                "Acme",
                null,
                "7701234567",
                null,
                null,
                null
        ));

        assertThat(result.status()).isEqualTo("auto_rejected");
        assertThat(result.verifiedOrgName()).isNull();
    }

    @Test
    void getById_missingEmployer_throwsNotFound() {
        when(jpaEmployerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerService.getById(99L))
                .isInstanceOf(EmployerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getCurrentByUserEmail_existingEmployer_returnsProfile() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 20L);
        user.setEmail("employer@example.com");
        user.setVerified(true);

        EmployerEntity employer = new EmployerEntity();
        employer.setId(55L);
        employer.setUserId(20L);
        employer.setName("Acme Corp");
        employer.setInn("7701234567");
        employer.setStatus("pending");

        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(user));
        when(jpaEmployerRepository.findByUserId(20L)).thenReturn(Optional.of(employer));

        EmployerProfile result = employerService.getCurrentByUserEmail("employer@example.com");

        assertThat(result.id()).isEqualTo(55L);
        assertThat(result.companyName()).isEqualTo("Acme Corp");
        assertThat(result.inn()).isEqualTo("7701234567");
    }

    @Test
    void getCurrentByUserEmail_unknownUser_throwsUnauthorized() {
        when(jpaUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employerService.getCurrentByUserEmail("missing@example.com"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid authentication token");
    }

    @Test
    void updateCurrentByUserEmail_existingEmployer_updatesFields() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 21L);
        user.setEmail("employer@example.com");
        user.setVerified(true);

        EmployerEntity employer = new EmployerEntity();
        employer.setId(56L);
        employer.setUserId(21L);
        employer.setName("Old Name");
        employer.setInn("1111111111");
        employer.setStatus("pending");

        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(user));
        when(jpaEmployerRepository.findByUserId(21L)).thenReturn(Optional.of(employer));
        when(jpaEmployerRepository.save(employer)).thenReturn(employer);

        EmployerProfile result = employerService.updateCurrentByUserEmail(
                "employer@example.com",
                new UpdateEmployerCommand(
                        "Global software company",
                        "https://acme.com",
                        "@acme_hr",
                        "https://acme.com/logo.png"
                )
        );

        assertThat(result.companyName()).isEqualTo("Old Name");
        assertThat(result.description()).isEqualTo("Global software company");
        assertThat(result.inn()).isEqualTo("1111111111");
        assertThat(result.website()).isEqualTo("https://acme.com");
        assertThat(result.socials()).isEqualTo("@acme_hr");
        assertThat(result.logoUrl()).isEqualTo("https://acme.com/logo.png");
    }

    @Test
    void assertCanManageOpportunities_fullVerifiedEmployerOwner_allowsAccess() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 30L);
        user.setEmail("employer@example.com");
        user.setRole(ru.tramplin_itplanet.tramplin.domain.model.UserRole.EMPLOYER);

        EmployerEntity employer = new EmployerEntity();
        employer.setId(77L);
        employer.setUserId(30L);
        employer.setStatus("full_verified");

        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(user));
        when(jpaEmployerRepository.findByUserId(30L)).thenReturn(Optional.of(employer));

        employerService.assertCanManageOpportunities("employer@example.com", 77L);
    }

    @Test
    void assertCanManageOpportunities_notEmployerRole_throwsForbidden() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 31L);
        user.setEmail("user@example.com");
        user.setRole(ru.tramplin_itplanet.tramplin.domain.model.UserRole.USER);

        when(jpaUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> employerService.assertCanManageOpportunities("user@example.com", 77L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only EMPLOYER users can manage opportunities");
    }

    @Test
    void assertCanManageOpportunities_notFullVerified_throwsForbidden() {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", 32L);
        user.setEmail("employer@example.com");
        user.setRole(ru.tramplin_itplanet.tramplin.domain.model.UserRole.EMPLOYER);

        EmployerEntity employer = new EmployerEntity();
        employer.setId(78L);
        employer.setUserId(32L);
        employer.setStatus("pending");

        when(jpaUserRepository.findByEmail("employer@example.com")).thenReturn(Optional.of(user));
        when(jpaEmployerRepository.findByUserId(32L)).thenReturn(Optional.of(employer));

        assertThatThrownBy(() -> employerService.assertCanManageOpportunities("employer@example.com", 78L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("full_verified");
    }

    @Test
    void updateByIdAsCurator_curator_updatesAllFields() {
        UserEntity curator = new UserEntity();
        ReflectionTestUtils.setField(curator, "id", 40L);
        curator.setEmail("curator@example.com");
        curator.setRole(UserRole.CURATOR);

        EmployerEntity employer = new EmployerEntity();
        employer.setId(10L);
        employer.setUserId(11L);
        employer.setName("Old Name");
        employer.setInn("1111111111");
        employer.setStatus("pending");

        when(jpaUserRepository.findByEmail("curator@example.com")).thenReturn(Optional.of(curator));
        when(jpaEmployerRepository.findById(10L)).thenReturn(Optional.of(employer));
        when(jpaEmployerRepository.save(employer)).thenReturn(employer);

        EmployerProfile result = employerService.updateByIdAsCurator(
                "curator@example.com",
                10L,
                new UpdateEmployerByCuratorCommand(
                        50L,
                        "Acme Corp",
                        "Global software company",
                        "7701234567",
                        "https://acme.com",
                        "@acme_hr",
                        "https://acme.com/logo.png",
                        "Acme Corporation",
                        "full_verified"
                )
        );

        assertThat(result.companyName()).isEqualTo("Acme Corp");
        assertThat(result.inn()).isEqualTo("7701234567");
        assertThat(result.status()).isEqualTo("full_verified");
        assertThat(result.userId()).isEqualTo(50L);
    }

    @Test
    void deleteByIdAsCurator_curator_deletesEmployer() {
        UserEntity curator = new UserEntity();
        ReflectionTestUtils.setField(curator, "id", 40L);
        curator.setEmail("curator@example.com");
        curator.setRole(UserRole.CURATOR);

        EmployerEntity employer = new EmployerEntity();
        employer.setId(10L);

        when(jpaUserRepository.findByEmail("curator@example.com")).thenReturn(Optional.of(curator));
        when(jpaEmployerRepository.findById(10L)).thenReturn(Optional.of(employer));

        employerService.deleteByIdAsCurator("curator@example.com", 10L);

        verify(jpaEmployerRepository).delete(employer);
    }
}
