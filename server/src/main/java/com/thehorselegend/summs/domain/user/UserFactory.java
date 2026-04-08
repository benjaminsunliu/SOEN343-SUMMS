package com.thehorselegend.summs.domain.user;

public class UserFactory {

    private UserFactory() {
    }

    public static User createCitizen(String name, String email, String passwordHash) {
        return new User(null, name, email, passwordHash, UserRole.CITIZEN);
    }

    public static User createProvider(String name, String email, String passwordHash) {
        return new User(null, name, email, passwordHash, UserRole.PROVIDER);
    }

    public static User createCityProvider(String name, String email, String passwordHash) {
        return new User(null, name, email, passwordHash, UserRole.CITY_PROVIDER);
    }

    public static User createAdmin(String name, String email, String passwordHash) {
        return new User(null, name, email, passwordHash, UserRole.ADMIN);
    }
}