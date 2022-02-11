package com.meitalk.auth.model.user;

public enum Role {

    USER("USER"),
    STREAMER("STREAMER");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

}
