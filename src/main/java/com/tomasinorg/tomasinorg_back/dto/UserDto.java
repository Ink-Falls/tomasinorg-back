package com.tomasinorg.tomasinorg_back.dto;

import lombok.Builder;

@Builder
public class UserDto {
    private String email;
    private String name;
    private String picture;
    private String role;

    // Default constructor
    public UserDto() {}

    // Constructor with parameters
    public UserDto(String email, String name, String picture, String role) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
