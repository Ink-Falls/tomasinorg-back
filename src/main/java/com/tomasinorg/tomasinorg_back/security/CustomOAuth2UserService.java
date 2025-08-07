package com.tomasinorg.tomasinorg_back.security;

import com.tomasinorg.tomasinorg_back.model.Role;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("OAuth2 processing error");
        }
        
        return oauth2User;
    }

    private void processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String googleId = (String) attributes.get("sub");

        // Get tokens from the OAuth2UserRequest
        String accessToken = userRequest.getAccessToken().getTokenValue();
        String refreshToken = userRequest.getAdditionalParameters().get("refresh_token") != null 
            ? userRequest.getAdditionalParameters().get("refresh_token").toString() 
            : null;

        User user = userRepository.findByGoogleId(googleId)
                .map(existingUser -> updateExistingUser(existingUser, name, picture, accessToken, refreshToken))
                .orElseGet(() -> createNewUser(email, name, picture, googleId, accessToken, refreshToken));

        userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, String name, String picture, 
                                  String accessToken, String refreshToken) {
        existingUser.setName(name);
        existingUser.setPicture(picture);
        existingUser.setAccessToken(accessToken);  // Google access token
        if (refreshToken != null) {
            existingUser.setGoogleRefreshToken(refreshToken);  // Google refresh token
        }
        existingUser.setTokenExpiresAt(LocalDateTime.now().plusSeconds(3600));
        existingUser.setUpdatedAt(LocalDateTime.now());
        return existingUser;
    }

    private User createNewUser(String email, String name, String picture, String googleId,
                              String accessToken, String refreshToken) {
        Role role = adminEmail.equals(email) ? Role.ADMIN : Role.USER;
        
        return User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .googleId(googleId)
                .role(role)
                .accessToken(accessToken)  // Google access token
                .googleRefreshToken(refreshToken)  // Google refresh token
                .tokenExpiresAt(LocalDateTime.now().plusSeconds(3600))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
