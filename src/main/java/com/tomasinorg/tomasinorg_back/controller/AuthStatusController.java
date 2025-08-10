package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import com.tomasinorg.tomasinorg_back.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthStatusController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Enhanced debugging
            log.info("=== AUTH STATUS DEBUG ===");
            log.info("Request URI: {}", request.getRequestURI());
            log.info("Request Method: {}", request.getMethod());
            log.info("Remote Address: {}", request.getRemoteAddr());
            log.info("OAuth2User principal: {}", principal != null ? "Present" : "Null");
            
            // Debug cookies
            if (request.getCookies() != null) {
                log.info("Found {} cookies", request.getCookies().length);
                for (Cookie cookie : request.getCookies()) {
                    String value = cookie.getValue();
                    String truncated = value != null && value.length() > 20 ? 
                        value.substring(0, 20) + "..." : value;
                    log.info("Cookie: {} = {}", cookie.getName(), truncated);
                }
            } else {
                log.info("No cookies found in request");
            }
            
            // Method 1: Check OAuth2User principal (for direct OAuth2 flow)
            if (principal != null) {
                String email = principal.getAttribute("email");
                log.info("OAuth2 Principal email: {}", email);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    log.info("User found in database: {}", user.getEmail());
                    return ResponseEntity.ok(buildAuthResponse(user, true));
                } else {
                    log.warn("OAuth2 user {} not found in database", email);
                    response.put("authenticated", false);
                    response.put("error", "User not found in database");
                    response.put("loginUrl", "/oauth2/authorization/google");
                    return ResponseEntity.ok(response);
                }
            }
            
            // Method 2: Check JWT token from cookies (for API calls)
            String jwtToken = extractJwtFromCookies(request);
            log.info("JWT token from cookies: {}", jwtToken != null ? "Found" : "Not found");
            
            if (jwtToken != null) {
                try {
                    boolean isValid = jwtTokenProvider.validateToken(jwtToken);
                    log.info("JWT token validation result: {}", isValid);
                    
                    if (isValid) {
                        String email = jwtTokenProvider.getEmailFromToken(jwtToken);
                        log.info("JWT token email: {}", email);
                        User user = userRepository.findByEmail(email).orElse(null);
                        if (user != null) {
                            log.info("JWT user found in database: {}", user.getEmail());
                            return ResponseEntity.ok(buildAuthResponse(user, true));
                        } else {
                            log.warn("JWT user {} not found in database", email);
                        }
                    }
                } catch (Exception tokenEx) {
                    log.error("JWT token validation error: {}", tokenEx.getMessage());
                }
            }
            
            // Method 3: Try to use refresh token if access token is missing
            String refreshToken = extractRefreshTokenFromCookies(request);
            log.info("Refresh token from cookies: {}", refreshToken != null ? "Found" : "Not found");
            
            if (refreshToken != null) {
                try {
                    boolean isRefreshValid = jwtTokenProvider.validateToken(refreshToken);
                    log.info("Refresh token validation result: {}", isRefreshValid);
                    
                    if (isRefreshValid) {
                        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                        log.info("Refresh token email: {}", email);
                        User user = userRepository.findByEmail(email).orElse(null);
                        if (user != null) {
                            log.info("User found via refresh token: {}", user.getEmail());
                            // Could generate new access token here if needed
                            return ResponseEntity.ok(buildAuthResponse(user, true));
                        } else {
                            log.warn("Refresh token user {} not found in database", email);
                        }
                    }
                } catch (Exception refreshEx) {
                    log.error("Refresh token validation error: {}", refreshEx.getMessage());
                }
            }
            
            // Not authenticated
            log.info("No valid authentication found - neither OAuth2 nor JWT");
            response.put("authenticated", false);
            response.put("loginUrl", "/oauth2/authorization/google");
            response.put("message", "Please login to access your account");
            response.put("debug", Map.of(
                "hasOAuth2Principal", principal != null,
                "hasJwtCookie", jwtToken != null,
                "hasRefreshCookie", refreshToken != null,
                "cookieCount", request.getCookies() != null ? request.getCookies().length : 0
            ));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking auth status", e);
            response.put("authenticated", false);
            response.put("error", "Authentication check failed: " + e.getMessage());
            response.put("loginUrl", "/oauth2/authorization/google");
            return ResponseEntity.ok(response);
        }
    }
    
    private Map<String, Object> buildAuthResponse(User user, boolean authenticated) {
        Map<String, Object> response = new HashMap<>();
        
        if (user != null && authenticated) {
            response.put("authenticated", true);
            response.put("user", Map.of(
                "email", user.getEmail(),
                "name", user.getName(),
                "picture", user.getPicture()
            ));
            response.put("hasGoogleTokens", user.getAccessToken() != null);
            response.put("hasRefreshToken", user.getGoogleRefreshToken() != null);
            response.put("tokenExpiration", user.getTokenExpiresAt() != null ? 
                user.getTokenExpiresAt().toString() : null);
            response.put("tokenValid", isTokenValid(user));
            response.put("roles", Arrays.asList(user.getRole().toString()));
            
            // Add token health status
            if (user.getTokenExpiresAt() != null) {
                boolean expiringSoon = user.getTokenExpiresAt().isBefore(LocalDateTime.now().plusHours(1));
                response.put("tokenExpiringSoon", expiringSoon);
            }
        } else {
            response.put("authenticated", false);
            response.put("loginUrl", "/oauth2/authorization/google");
        }
        
        return response;
    }
    
    private boolean isTokenValid(User user) {
        return user.getAccessToken() != null && 
               user.getTokenExpiresAt() != null && 
               user.getTokenExpiresAt().isAfter(LocalDateTime.now());
    }
    
    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", "/oauth2/authorization/google");
        response.put("message", "Redirect to this URL to start Google OAuth2 login");
        return ResponseEntity.ok(response);
    }
}
