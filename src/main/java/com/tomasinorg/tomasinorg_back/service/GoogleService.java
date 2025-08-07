package com.tomasinorg.tomasinorg_back.service;

import com.tomasinorg.tomasinorg_back.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private final UserService userService;
    private final RestTemplate restTemplate;

    public Object getCalendarEvents(String userEmail) {
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = user.getAccessToken();
        
        // Check if token is expired
        if (isTokenExpired(user)) {
            accessToken = refreshAccessToken(user);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                "https://www.googleapis.com/calendar/v3/calendars/primary/events",
                HttpMethod.GET,
                entity,
                Object.class
            );
            return response.getBody();
        } catch (RuntimeException e) {
            log.error("Error fetching calendar events: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch calendar events");
        }
    }

    private boolean isTokenExpired(User user) {
        return user.getTokenExpiresAt() != null && 
               user.getTokenExpiresAt().isBefore(LocalDateTime.now());
    }

    private String refreshAccessToken(User user) {
        // Implementation for refreshing Google access token using refresh token
        // This is a simplified version - in production you'd need proper OAuth2 flow
        
        if (user.getGoogleRefreshToken() == null) {
            throw new RuntimeException("No Google refresh token available");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            String body = "client_id=727162544518-11ckdobgf43uipugmrvho4msi70admg5.apps.googleusercontent.com" +
                         "&client_secret=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" +
                         "&refresh_token=" + user.getGoogleRefreshToken() +
                         "&grant_type=refresh_token";

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<?> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                entity,
                Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from Google token endpoint");
            }
            
            String newAccessToken = (String) responseBody.get("access_token");
            Integer expiresIn = (Integer) responseBody.get("expires_in");

            // Update user's access token
            user.setAccessToken(newAccessToken);
            user.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            userService.save(user);

            return newAccessToken;
        } catch (RuntimeException e) {
            log.error("Error refreshing access token: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh access token");
        }
    }
}
