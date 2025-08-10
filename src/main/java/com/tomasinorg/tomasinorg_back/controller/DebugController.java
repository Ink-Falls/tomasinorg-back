package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserRepository userRepository;

    @GetMapping("/user-tokens")
    public ResponseEntity<Map<String, Object>> getUserTokens(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("error", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            response.put("error", "User not found in database");
            return ResponseEntity.status(404).body(response);
        }

        response.put("email", user.getEmail());
        response.put("hasGoogleAccessToken", user.getAccessToken() != null);
        response.put("hasGoogleRefreshToken", user.getGoogleRefreshToken() != null);
        response.put("hasJwtRefreshToken", user.getRefreshToken() != null);
        response.put("tokenExpiresAt", user.getTokenExpiresAt());
        response.put("googleAccessTokenLength", user.getAccessToken() != null ? user.getAccessToken().length() : 0);
        response.put("googleRefreshTokenLength", user.getGoogleRefreshToken() != null ? user.getGoogleRefreshToken().length() : 0);
        
        return ResponseEntity.ok(response);
    }
}
