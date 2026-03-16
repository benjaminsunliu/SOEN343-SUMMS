package com.thehorselegend.summs.domain.user;

public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;

    public User(Long id, String name, String email, String passwordHash, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }
}