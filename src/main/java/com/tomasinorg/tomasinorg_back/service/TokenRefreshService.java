package com.tomasinorg.tomasinorg_back.service;

import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public boolean refreshGoogleToken(String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getGoogleRefreshToken() == null) {
                log.error("No refresh token available for user: {}", userEmail);
                return false;
            }

            String refreshTokenUrl = "https://oauth2.googleapis.com/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("refresh_token", user.getGoogleRefreshToken());
            params.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    refreshTokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenResponse = response.getBody();
                String newAccessToken = (String) tokenResponse.get("access_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");

                // Update user tokens
                user.setAccessToken(newAccessToken);
                user.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                log.info("Successfully refreshed token for user: {}", userEmail);
                return true;
            }

        } catch (Exception e) {
            log.error("Failed to refresh token for user: {}", userEmail, e);
        }

        return false;
    }

    public boolean isTokenExpired(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(user -> user.getTokenExpiresAt() == null || 
                           user.getTokenExpiresAt().isBefore(LocalDateTime.now()))
                .orElse(true);
    }

    public boolean hasValidToken(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(user -> user.getAccessToken() != null && 
                           user.getTokenExpiresAt() != null &&
                           user.getTokenExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }
}
