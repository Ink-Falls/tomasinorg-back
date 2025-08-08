package com.tomasinorg.tomasinorg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {
    
    private Long id;
    private String name;
    private String description;
    private String driveEmail;
    private String driveId;
    private List<OrganizationMemberDto> members;
    private List<PositionDto> positions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
