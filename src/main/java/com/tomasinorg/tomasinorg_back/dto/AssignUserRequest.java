package com.tomasinorg.tomasinorg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignUserRequest {
    
    @NotNull(message = "Position ID is required")
    private Long positionId; // ID of the OrganizationPosition
}
