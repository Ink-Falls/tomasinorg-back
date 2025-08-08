package com.tomasinorg.tomasinorg_back.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public class AdminUserDto {
    private Long id;
    private String email;
    private String name;
    private String picture;
    private String role;
    private String googleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserOrganizationSummaryDto> organizations;

    // Default constructor
    public AdminUserDto() {}

    // Constructor with parameters
    public AdminUserDto(Long id, String email, String name, String picture, String role, 
                       String googleId, LocalDateTime createdAt, LocalDateTime updatedAt,
                       List<UserOrganizationSummaryDto> organizations) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.role = role;
        this.googleId = googleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.organizations = organizations;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<UserOrganizationSummaryDto> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<UserOrganizationSummaryDto> organizations) {
        this.organizations = organizations;
    }
}
