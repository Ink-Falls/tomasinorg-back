package com.tomasinorg.tomasinorg_back.service;

import com.tomasinorg.tomasinorg_back.dto.AdminUserDto;
import com.tomasinorg.tomasinorg_back.dto.UserDto;
import com.tomasinorg.tomasinorg_back.dto.UserOrganizationSummaryDto;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.model.UserOrganization;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public UserDto convertToDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .role(user.getRole().toString())
                .build();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<AdminUserDto> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(this::convertToAdminDto)
                .collect(Collectors.toList());
    }

    public AdminUserDto convertToAdminDto(User user) {
        List<UserOrganizationSummaryDto> organizationSummaries = user.getUserOrganizations().stream()
                .map(this::convertUserOrganizationToSummary)
                .collect(Collectors.toList());

        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .role(user.getRole().toString())
                .googleId(user.getGoogleId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .organizations(organizationSummaries)
                .build();
    }

    private UserOrganizationSummaryDto convertUserOrganizationToSummary(UserOrganization userOrg) {
        return UserOrganizationSummaryDto.builder()
                .organizationId(userOrg.getOrganization().getId())
                .organizationName(userOrg.getOrganization().getName())
                .organizationDescription(userOrg.getOrganization().getDescription())
                .positionId(userOrg.getPosition().getId())
                .positionName(userOrg.getPosition().getName())
                .positionDescription(userOrg.getPosition().getDescription())
                .isDefaultPosition(userOrg.getPosition().getIsDefault())
                .joinedAt(userOrg.getJoinedAt())
                .build();
    }
}
