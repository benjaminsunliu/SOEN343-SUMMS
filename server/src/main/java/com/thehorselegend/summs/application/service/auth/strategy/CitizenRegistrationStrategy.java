package com.thehorselegend.summs.application.service.auth.strategy;

import com.thehorselegend.summs.domain.user.User;
import com.thehorselegend.summs.domain.user.UserFactory;
import com.thehorselegend.summs.domain.user.UserRole;
import org.springframework.stereotype.Component;

@Component
public class CitizenRegistrationStrategy implements UserRegistrationStrategy {

    @Override
    public UserRole supportedRole() {
        return UserRole.CITIZEN;
    }

    @Override
    public User create(String name, String email, String passwordHash) {
        return UserFactory.createCitizen(name, email, passwordHash);
    }
}
