package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.UserNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;

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
}
