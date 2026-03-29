package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.CuratorEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaCuratorRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.CuratorAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.Curator;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuratorServiceImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @Mock
    private JpaCuratorRepository jpaCuratorRepository;

    @InjectMocks
    private CuratorServiceImpl curatorService;

    @Test
    void create_rootCuratorRequest_createsCuratorAndPromotesUser() {
        UserEntity rootUser = buildUser(11L, "root@example.com", UserRole.CURATOR);
        UserEntity targetUser = buildUser(42L, "target@example.com", UserRole.USER);

        CuratorEntity rootCurator = new CuratorEntity();
        rootCurator.setId(1L);
        rootCurator.setUserId(11L);

        CuratorEntity savedCurator = new CuratorEntity();
        savedCurator.setId(2L);
        savedCurator.setUserId(42L);

        when(jpaUserRepository.findByEmail("root@example.com")).thenReturn(Optional.of(rootUser));
        when(jpaCuratorRepository.findById(1L)).thenReturn(Optional.of(rootCurator));
        when(jpaUserRepository.findById(42L)).thenReturn(Optional.of(targetUser));
        when(jpaCuratorRepository.existsByUserId(42L)).thenReturn(false);
        when(jpaCuratorRepository.save(org.mockito.ArgumentMatchers.any(CuratorEntity.class))).thenReturn(savedCurator);

        Curator result = curatorService.create("root@example.com", 42L);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.userId()).isEqualTo(42L);
        assertThat(targetUser.getRole()).isEqualTo(UserRole.CURATOR);
    }

    @Test
    void create_duplicateCurator_throwsConflict() {
        UserEntity rootUser = buildUser(11L, "root@example.com", UserRole.CURATOR);
        UserEntity targetUser = buildUser(42L, "target@example.com", UserRole.USER);

        CuratorEntity rootCurator = new CuratorEntity();
        rootCurator.setId(1L);
        rootCurator.setUserId(11L);

        when(jpaUserRepository.findByEmail("root@example.com")).thenReturn(Optional.of(rootUser));
        when(jpaCuratorRepository.findById(1L)).thenReturn(Optional.of(rootCurator));
        when(jpaUserRepository.findById(42L)).thenReturn(Optional.of(targetUser));
        when(jpaCuratorRepository.existsByUserId(42L)).thenReturn(true);

        assertThatThrownBy(() -> curatorService.create("root@example.com", 42L))
                .isInstanceOf(CuratorAlreadyExistsException.class);
    }

    @Test
    void delete_nonRootCurator_throwsForbidden() {
        UserEntity requester = buildUser(12L, "curator@example.com", UserRole.CURATOR);

        CuratorEntity rootCurator = new CuratorEntity();
        rootCurator.setId(1L);
        rootCurator.setUserId(11L);

        when(jpaUserRepository.findByEmail("curator@example.com")).thenReturn(Optional.of(requester));
        when(jpaCuratorRepository.findById(1L)).thenReturn(Optional.of(rootCurator));

        assertThatThrownBy(() -> curatorService.delete("curator@example.com", 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("id 1");
    }

    private static UserEntity buildUser(Long id, String email, UserRole role) {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
