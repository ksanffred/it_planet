package ru.tramplin_itplanet.tramplin.datasource.mapper;

import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.domain.model.User;

public class UserEntityMapper {

    private UserEntityMapper() {}

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isVerified(),
                entity.getCreatedAt()
        );
    }
}
