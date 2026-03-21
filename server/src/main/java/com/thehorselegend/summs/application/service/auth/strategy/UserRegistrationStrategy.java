package com.thehorselegend.summs.application.service.auth.strategy;

import com.thehorselegend.summs.domain.user.User;
import com.thehorselegend.summs.domain.user.UserRole;

public interface UserRegistrationStrategy {

    UserRole supportedRole();

    User create(String name, String email, String passwordHash);
}
