package com.thehorselegend.summs.infrastructure.persistence;

import com.thehorselegend.summs.domain.user.User;

public class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole()
        );
    }

    public static UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole()
        );
    }
}