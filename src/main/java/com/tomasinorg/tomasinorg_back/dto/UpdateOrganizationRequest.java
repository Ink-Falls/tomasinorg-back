package com.tomasinorg.tomasinorg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrganizationRequest {
    
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Email(message = "Drive email must be a valid email address")
    private String driveEmail;
    
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;
    
    private String driveId; // Optional Google Drive folder ID
    
    private List<String> memberEmails; // Updated member emails
    
    private Map<String, Long> memberPositions; // Email -> Position ID mapping
}
