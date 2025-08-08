package com.tomasinorg.tomasinorg_back.service;

import com.tomasinorg.tomasinorg_back.dto.*;
import com.tomasinorg.tomasinorg_back.model.Organization;
import com.tomasinorg.tomasinorg_back.model.OrganizationPosition;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.model.UserOrganization;
import com.tomasinorg.tomasinorg_back.repository.OrganizationPositionRepository;
import com.tomasinorg.tomasinorg_back.repository.OrganizationRepository;
import com.tomasinorg.tomasinorg_back.repository.UserOrganizationRepository;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final OrganizationPositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleService googleService;
    private final PositionService positionService;

    @Transactional
    public OrganizationDto createOrganization(CreateOrganizationRequest request) {
        // Check if organization name already exists
        if (organizationRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Organization with name '" + request.getName() + "' already exists");
        }

        // Create organization
        Organization organization = Organization.builder()
                .name(request.getName())
                .description(request.getDescription())
                .driveEmail(request.getDriveEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .driveId(request.getDriveId())
                .build();

        Organization savedOrganization = organizationRepository.save(organization);

        // Create default "Member" position
        OrganizationPosition defaultPosition = positionService.createDefaultPosition(savedOrganization);

        // Add initial members with positions
        if (request.getMemberEmails() != null && !request.getMemberEmails().isEmpty()) {
            for (String email : request.getMemberEmails()) {
                User user = findOrCreateUserByEmail(email);
                
                // Get position from memberPositions map or use default
                Long positionId = request.getMemberPositions().get(email);
                OrganizationPosition position;
                
                if (positionId != null) {
                    position = positionRepository.findById(positionId)
                            .orElse(defaultPosition); // Fallback to default if position not found
                } else {
                    position = defaultPosition;
                }
                
                UserOrganization userOrg = UserOrganization.builder()
                        .user(user)
                        .organization(savedOrganization)
                        .position(position)
                        .build();
                        
                userOrganizationRepository.save(userOrg);
            }
        }

        log.info("Created organization: {} with {} members", savedOrganization.getName(), 
                request.getMemberEmails() != null ? request.getMemberEmails().size() : 0);

        return convertToDto(savedOrganization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> getUserOrganizations(String userEmail) {
        return organizationRepository.findByMembersEmail(userEmail).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrganizationDto> getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional
    public OrganizationDto updateOrganization(Long id, UpdateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Update fields if provided
        if (request.getName() != null) {
            // Check if new name conflicts with existing organization
            organizationRepository.findByName(request.getName())
                    .filter(org -> !org.getId().equals(id))
                    .ifPresent(org -> {
                        throw new RuntimeException("Organization with name '" + request.getName() + "' already exists");
                    });
            organization.setName(request.getName());
        }

        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }

        if (request.getDriveEmail() != null) {
            organization.setDriveEmail(request.getDriveEmail());
        }

        if (request.getPassword() != null) {
            organization.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getDriveId() != null) {
            organization.setDriveId(request.getDriveId());
        }

        // Update members if provided
        if (request.getMemberEmails() != null) {
            // Remove existing memberships
            userOrganizationRepository.deleteAll(
                userOrganizationRepository.findByOrganizationId(organization.getId())
            );
            
            // Get default position for this organization
            OrganizationPosition defaultPosition = positionService.getDefaultPosition(organization.getId());
            
            // Add new memberships with positions
            for (String email : request.getMemberEmails()) {
                User user = findOrCreateUserByEmail(email);
                
                // Get position from memberPositions map or use default
                Long positionId = request.getMemberPositions() != null ? 
                    request.getMemberPositions().get(email) : null;
                    
                OrganizationPosition position;
                if (positionId != null) {
                    position = positionRepository.findById(positionId)
                            .orElse(defaultPosition); // Fallback to default if position not found
                } else {
                    position = defaultPosition;
                }
                
                UserOrganization userOrg = UserOrganization.builder()
                        .user(user)
                        .organization(organization)
                        .position(position)
                        .build();
                        
                userOrganizationRepository.save(userOrg);
            }
        }

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Updated organization: {}", savedOrganization.getName());

        return convertToDto(savedOrganization);
    }

    @Transactional
    public void assignUserToOrganization(Long orgId, String email, Long positionId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        User user = findOrCreateUserByEmail(email);
        
        // Get the position
        OrganizationPosition position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));
                
        // Verify position belongs to this organization
        if (!position.getOrganization().getId().equals(orgId)) {
            throw new RuntimeException("Position does not belong to this organization");
        }
        
        // Check if user is already a member
        Optional<UserOrganization> existingMembership = 
            userOrganizationRepository.findByUserIdAndOrganizationId(user.getId(), orgId);
            
        if (existingMembership.isPresent()) {
            // Update position if user is already a member
            UserOrganization userOrg = existingMembership.get();
            userOrg.setPosition(position);
            userOrganizationRepository.save(userOrg);
        } else {
            // Create new membership
            UserOrganization userOrg = UserOrganization.builder()
                    .user(user)
                    .organization(organization)
                    .position(position)
                    .build();
            userOrganizationRepository.save(userOrg);
        }

        log.info("Assigned user {} to organization {} with position {}", email, organization.getName(), position.getName());
    }

    @Transactional
    public void removeUserFromOrganization(Long orgId, String email) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userOrganizationRepository.deleteByUserIdAndOrganizationId(user.getId(), orgId);

        log.info("Removed user {} from organization {}", email, organization.getName());
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(Long orgId, String password) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        return passwordEncoder.matches(password, organization.getPassword());
    }

    @Transactional(readOnly = true)
    public boolean isUserMemberOfOrganization(Long orgId, Long userId) {
        return userOrganizationRepository.existsByOrganizationIdAndUserId(orgId, userId);
    }

    public List<DriveItem> getDriveContents(Long orgId, String folderId, String userEmail) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Check if user is a member of this organization
        boolean isMember = userOrganizationRepository.findByOrganizationIdAndUserEmail(orgId, userEmail)
                .isPresent();

        if (!isMember) {
            throw new RuntimeException("You don't have access to this organization");
        }

        try {
            // Use the organization's drive ID if no specific folder is requested
            String targetFolderId = folderId != null ? folderId : organization.getDriveId();
            return googleService.listDriveContents(organization.getDriveEmail(), targetFolderId);
        } catch (Exception e) {
            log.error("Error accessing Google Drive for organization {}: {}", organization.getName(), e.getMessage());
            throw new RuntimeException("❌ You don't have access to this Google Drive folder. Contact the admin to share it with your Google account.");
        }
    }

    private User findOrCreateUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Create a placeholder user for users who haven't signed in yet
                    User newUser = User.builder()
                            .email(email)
                            .name(email.split("@")[0]) // Use email prefix as temporary name
                            .googleId("pending-" + email) // Temporary google ID
                            .role(com.tomasinorg.tomasinorg_back.model.Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    private OrganizationDto convertToDto(Organization organization) {
        List<OrganizationMemberDto> memberDtos = organization.getUserOrganizations().stream()
                .map(this::convertUserOrganizationToMemberDto)
                .collect(Collectors.toList());

        List<PositionDto> positionDtos = organization.getPositions().stream()
                .map(position -> PositionDto.builder()
                        .id(position.getId())
                        .name(position.getName())
                        .description(position.getDescription())
                        .isDefault(position.getIsDefault())
                        .createdAt(position.getCreatedAt())
                        .updatedAt(position.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return OrganizationDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .driveEmail(organization.getDriveEmail())
                .driveId(organization.getDriveId())
                .members(memberDtos)
                .positions(positionDtos)
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }

    private OrganizationMemberDto convertUserOrganizationToMemberDto(UserOrganization userOrg) {
        User user = userOrg.getUser();
        return OrganizationMemberDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .role(user.getRole().toString())
                .position(PositionDto.builder()
                        .id(userOrg.getPosition().getId())
                        .name(userOrg.getPosition().getName())
                        .description(userOrg.getPosition().getDescription())
                        .isDefault(userOrg.getPosition().getIsDefault())
                        .createdAt(userOrg.getPosition().getCreatedAt())
                        .updatedAt(userOrg.getPosition().getUpdatedAt())
                        .build())
                .joinedAt(userOrg.getJoinedAt())
                .build();
    }
}
