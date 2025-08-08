package com.tomasinorg.tomasinorg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationMemberDto {
    
    private String email;
    private String name;
    private String picture;
    private String role; // USER or ADMIN role
    private PositionDto position; // Position details with description
    private LocalDateTime joinedAt;
}
