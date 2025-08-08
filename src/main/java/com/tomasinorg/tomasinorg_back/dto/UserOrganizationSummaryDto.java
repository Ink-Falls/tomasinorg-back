package com.tomasinorg.tomasinorg_back.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public class UserOrganizationSummaryDto {
    private Long organizationId;
    private String organizationName;
    private String organizationDescription;
    private Long positionId;
    private String positionName;
    private String positionDescription;
    private boolean isDefaultPosition;
    private LocalDateTime joinedAt;

    // Default constructor
    public UserOrganizationSummaryDto() {}

    // Constructor with parameters
    public UserOrganizationSummaryDto(Long organizationId, String organizationName, String organizationDescription,
                                     Long positionId, String positionName, String positionDescription,
                                     boolean isDefaultPosition, LocalDateTime joinedAt) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationDescription = organizationDescription;
        this.positionId = positionId;
        this.positionName = positionName;
        this.positionDescription = positionDescription;
        this.isDefaultPosition = isDefaultPosition;
        this.joinedAt = joinedAt;
    }

    // Getters and Setters
    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getPositionDescription() {
        return positionDescription;
    }

    public void setPositionDescription(String positionDescription) {
        this.positionDescription = positionDescription;
    }

    public boolean isDefaultPosition() {
        return isDefaultPosition;
    }

    public void setDefaultPosition(boolean defaultPosition) {
        isDefaultPosition = defaultPosition;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
