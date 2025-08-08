package com.tomasinorg.tomasinorg_back.service;

import com.tomasinorg.tomasinorg_back.dto.CreatePositionRequest;
import com.tomasinorg.tomasinorg_back.dto.PositionDto;
import com.tomasinorg.tomasinorg_back.dto.UpdatePositionRequest;
import com.tomasinorg.tomasinorg_back.model.Organization;
import com.tomasinorg.tomasinorg_back.model.OrganizationPosition;
import com.tomasinorg.tomasinorg_back.model.UserOrganization;
import com.tomasinorg.tomasinorg_back.repository.OrganizationPositionRepository;
import com.tomasinorg.tomasinorg_back.repository.OrganizationRepository;
import com.tomasinorg.tomasinorg_back.repository.UserOrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionService {

    private final OrganizationPositionRepository positionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;

    @Transactional
    public PositionDto createPosition(Long organizationId, CreatePositionRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Check if position name already exists in this organization
        if (positionRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new RuntimeException("Position with name '" + request.getName() + "' already exists in this organization");
        }

        OrganizationPosition position = OrganizationPosition.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organization(organization)
                .isDefault(false)
                .build();

        OrganizationPosition savedPosition = positionRepository.save(position);
        log.info("Created position '{}' for organization '{}'", savedPosition.getName(), organization.getName());

        return convertToDto(savedPosition);
    }

    @Transactional(readOnly = true)
    public List<PositionDto> getOrganizationPositions(Long organizationId) {
        return positionRepository.findByOrganizationId(organizationId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PositionDto> getPositionById(Long positionId) {
        return positionRepository.findById(positionId)
                .map(this::convertToDto);
    }

    @Transactional
    public PositionDto updatePosition(Long positionId, UpdatePositionRequest request) {
        OrganizationPosition position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        // Check if it's the default position and prevent name change
        if (position.getIsDefault() && request.getName() != null && !"Member".equals(request.getName())) {
            throw new RuntimeException("Cannot change the name of the default 'Member' position");
        }

        // Check if new name conflicts with existing position in the same organization
        if (request.getName() != null && !request.getName().equals(position.getName())) {
            if (positionRepository.existsByOrganizationIdAndName(position.getOrganization().getId(), request.getName())) {
                throw new RuntimeException("Position with name '" + request.getName() + "' already exists in this organization");
            }
            position.setName(request.getName());
        }

        if (request.getDescription() != null) {
            position.setDescription(request.getDescription());
        }

        OrganizationPosition savedPosition = positionRepository.save(position);
        log.info("Updated position '{}' in organization '{}'", savedPosition.getName(), savedPosition.getOrganization().getName());

        return convertToDto(savedPosition);
    }

    @Transactional
    public void deletePosition(Long positionId) {
        OrganizationPosition position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        // Prevent deletion of default position
        if (position.getIsDefault()) {
            throw new RuntimeException("Cannot delete the default 'Member' position");
        }

        // Check if any users are assigned to this position
        Long usersWithPosition = positionRepository.countUsersWithPosition(positionId);
        if (usersWithPosition > 0) {
            // Get the default position for reassignment
            OrganizationPosition defaultPosition = positionRepository
                    .findByOrganizationIdAndIsDefaultTrue(position.getOrganization().getId())
                    .orElseThrow(() -> new RuntimeException("Default position not found"));

            // Reassign all users to the default position
            List<UserOrganization> userOrganizations = userOrganizationRepository.findByPositionId(positionId);
            userOrganizations.forEach(userOrg -> {
                userOrg.setPosition(defaultPosition);
                userOrganizationRepository.save(userOrg);
            });

            log.info("Reassigned {} users from position '{}' to default position", usersWithPosition, position.getName());
        }

        positionRepository.delete(position);
        log.info("Deleted position '{}' from organization '{}'", position.getName(), position.getOrganization().getName());
    }

    @Transactional
    public OrganizationPosition createDefaultPosition(Organization organization) {
        OrganizationPosition defaultPosition = OrganizationPosition.builder()
                .name("Member")
                .description("General organization member")
                .organization(organization)
                .isDefault(true)
                .build();

        return positionRepository.save(defaultPosition);
    }

    @Transactional(readOnly = true)
    public OrganizationPosition getDefaultPosition(Long organizationId) {
        return positionRepository.findByOrganizationIdAndIsDefaultTrue(organizationId)
                .orElseThrow(() -> new RuntimeException("Default position not found for organization"));
    }

    private PositionDto convertToDto(OrganizationPosition position) {
        return PositionDto.builder()
                .id(position.getId())
                .name(position.getName())
                .description(position.getDescription())
                .isDefault(position.getIsDefault())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }
}
