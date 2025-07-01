package com.tmv.core.dto;

public class AuthenticationRequestDTO {
    private String username;
    private String password;

    // Standard-Konstruktor (wichtig für Deserialisierung bei JSON in REST-APIs)
    public AuthenticationRequestDTO() {
    }

    public AuthenticationRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
