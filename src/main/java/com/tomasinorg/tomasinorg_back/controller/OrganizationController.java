package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.dto.*;
import com.tomasinorg.tomasinorg_back.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class OrganizationController {

    private final OrganizationService organizationService;

    // Admin endpoints
    @PostMapping("/admin/orgs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationDto> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        try {
            OrganizationDto organization = organizationService.createOrganization(request);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            log.error("Error creating organization: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/admin/orgs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        List<OrganizationDto> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @PutMapping("/admin/orgs/{orgId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationDto> updateOrganization(
            @PathVariable Long orgId,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        try {
            OrganizationDto organization = organizationService.updateOrganization(orgId, request);
            return ResponseEntity.ok(organization);
        } catch (RuntimeException e) {
            log.error("Error updating organization {}: {}", orgId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/admin/orgs/{orgId}/members/{email}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> assignUserToOrganization(
            @PathVariable Long orgId,
            @PathVariable String email,
            @Valid @RequestBody AssignUserRequest request) {
        try {
            organizationService.assignUserToOrganization(orgId, email, request.getPositionId());
            return ResponseEntity.ok(Map.of("message", "User assigned successfully with position ID: " + request.getPositionId()));
        } catch (RuntimeException e) {
            log.error("Error assigning user {} to organization {}: {}", email, orgId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/orgs/{orgId}/members/{email}/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> removeUserFromOrganization(
            @PathVariable Long orgId,
            @PathVariable String email) {
        try {
            organizationService.removeUserFromOrganization(orgId, email);
            return ResponseEntity.ok(Map.of("message", "User removed successfully"));
        } catch (RuntimeException e) {
            log.error("Error removing user {} from organization {}: {}", email, orgId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // User endpoints
    @GetMapping("/user/orgs")
    public ResponseEntity<List<OrganizationDto>> getUserOrganizations(Authentication authentication) {
        String userEmail = authentication.getName();
        List<OrganizationDto> organizations = organizationService.getUserOrganizations(userEmail);
        return ResponseEntity.ok(organizations);
    }

    // Common endpoints
    @GetMapping("/orgs/{orgId}/drive")
    public ResponseEntity<?> getOrganizationDriveContents(
            @PathVariable Long orgId,
            @RequestParam(required = false) String folderId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<DriveItem> contents = organizationService.getDriveContents(orgId, folderId, userEmail);
            return ResponseEntity.ok(contents);
        } catch (RuntimeException e) {
            log.error("Error accessing drive contents for organization {}: {}", orgId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orgs/{orgId}/verify-password")
    public ResponseEntity<Map<String, Object>> verifyOrganizationPassword(
            @PathVariable Long orgId,
            @Valid @RequestBody VerifyPasswordRequest request,
            Authentication authentication) {
        try {
            boolean isValid = organizationService.verifyPassword(orgId, request.getPassword());
            if (isValid) {
                // Check if user is a member of this organization
                String userEmail = authentication.getName();
                List<OrganizationDto> userOrgs = organizationService.getUserOrganizations(userEmail);
                boolean isMember = userOrgs.stream()
                        .anyMatch(org -> org.getId().equals(orgId));
                
                if (!isMember) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "You are not a member of this organization"));
                }
                
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Password verified successfully"
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                            "valid", false,
                            "error", "Invalid password"
                        ));
            }
        } catch (RuntimeException e) {
            log.error("Error verifying password for organization {}: {}", orgId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orgs/{orgId}/drive-contents")
    public ResponseEntity<?> getDriveContentsAfterAuth(
            @PathVariable Long orgId,
            @RequestParam(required = false) String folderId,
            Authentication authentication) {
        // This endpoint assumes password has already been verified
        // You might want to implement session-based authentication for this
        return getOrganizationDriveContents(orgId, folderId, authentication);
    }
}
