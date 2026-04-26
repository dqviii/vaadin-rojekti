package com.tonip.security.domain;

public enum Role {
    ADMIN,
    SUPER,
    USER;

    public String authority() {
        return "ROLE_" + name();
    }
}