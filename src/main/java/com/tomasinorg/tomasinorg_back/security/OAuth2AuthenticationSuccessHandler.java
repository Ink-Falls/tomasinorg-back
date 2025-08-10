package com.tomasinorg.tomasinorg_back.security;

import com.tomasinorg.tomasinorg_back.model.Role;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final Environment environment;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("=== OAuth2 Authentication Success Handler Called ===");
        log.info("Authentication type: {}", authentication.getClass().getSimpleName());
        
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String googleId = (String) attributes.get("sub");

        log.info("OAuth2 user info - Email: {}, Name: {}, GoogleId: {}", email, name, googleId);

        // Get the OAuth2AuthorizedClient to access tokens
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oauth2Token.getAuthorizedClientRegistrationId(), 
                oauth2Token.getName()
        );

        final String googleAccessToken;
        final String googleRefreshToken;
        final LocalDateTime tokenExpiresAt;

        if (authorizedClient != null) {
            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
            
            if (accessToken != null) {
                googleAccessToken = accessToken.getTokenValue();
                if (accessToken.getExpiresAt() != null) {
                    tokenExpiresAt = LocalDateTime.ofInstant(accessToken.getExpiresAt(), 
                                                           java.time.ZoneId.systemDefault());
                } else {
                    tokenExpiresAt = LocalDateTime.now().plusSeconds(3600); // Default 1 hour
                }
            } else {
                googleAccessToken = null;
                tokenExpiresAt = null;
            }
            
            if (refreshToken != null) {
                googleRefreshToken = refreshToken.getTokenValue();
            } else {
                googleRefreshToken = null;
            }
        } else {
            googleAccessToken = null;
            googleRefreshToken = null;
            tokenExpiresAt = null;
        }

        // Find or create user with Google tokens
        User user = userRepository.findByGoogleId(googleId)
                .map(existingUser -> updateExistingUser(existingUser, name, picture, 
                                                      googleAccessToken, googleRefreshToken, tokenExpiresAt))
                .orElseGet(() -> createNewUser(email, name, picture, googleId, 
                                             googleAccessToken, googleRefreshToken, tokenExpiresAt));

        // Generate JWT tokens
        String jwtAccessToken = tokenProvider.generateToken(email, user.getRole().toString());
        String jwtRefreshToken = tokenProvider.generateRefreshToken(email);

        // Update user's JWT refresh token in database
        user.setRefreshToken(jwtRefreshToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Successfully authenticated user: {} with Google tokens - Access: {}, Refresh: {}", 
                email, 
                googleAccessToken != null ? "Present" : "Missing",
                googleRefreshToken != null ? "Present" : "Missing");

        // Set cookies
        log.info("Setting JWT cookies - AccessToken length: {}, RefreshToken length: {}", 
                jwtAccessToken.length(), jwtRefreshToken.length());
        setTokenCookie(response, "accessToken", jwtAccessToken, (int) (tokenProvider.getJwtExpirationMs() / 1000));
        setTokenCookie(response, "refreshToken", jwtRefreshToken, (int) (tokenProvider.getJwtRefreshExpirationMs() / 1000));
        log.info("JWT cookies set successfully");

        // Redirect to frontend
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth2/redirect");
    }

    private User updateExistingUser(User existingUser, String name, String picture, 
                                  String googleAccessToken, String googleRefreshToken, 
                                  LocalDateTime tokenExpiresAt) {
        existingUser.setName(name);
        existingUser.setPicture(picture);
        existingUser.setAccessToken(googleAccessToken);
        if (googleRefreshToken != null) {
            existingUser.setGoogleRefreshToken(googleRefreshToken);
        }
        existingUser.setTokenExpiresAt(tokenExpiresAt);
        existingUser.setUpdatedAt(LocalDateTime.now());
        return existingUser;
    }

    private User createNewUser(String email, String name, String picture, String googleId,
                              String googleAccessToken, String googleRefreshToken, 
                              LocalDateTime tokenExpiresAt) {
        // Hardcoded admin email check
        Role role = "lennardacef@gmail.com".equals(email) ? Role.ADMIN : Role.USER;
        
        return userRepository.save(User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .googleId(googleId)
                .role(role)
                .accessToken(googleAccessToken)
                .googleRefreshToken(googleRefreshToken)
                .tokenExpiresAt(tokenExpiresAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        log.info("Setting cookie: {} with maxAge: {} seconds", name, maxAge);
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        
        // For production/HTTPS environments
        if (isSecureEnvironment()) {
            cookie.setSecure(true);
            // SameSite=None needed for cross-origin requests in production
            String cookieHeader = String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None", 
                name, value, maxAge);
            log.info("Setting secure cookie header: {}", cookieHeader.substring(0, Math.min(100, cookieHeader.length())) + "...");
            response.addHeader("Set-Cookie", cookieHeader);  // Use addHeader instead of setHeader
        } else {
            // For development/HTTP environments
            String cookieHeader = String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Lax", 
                name, value, maxAge);
            log.info("Setting development cookie header: {}", cookieHeader.substring(0, Math.min(100, cookieHeader.length())) + "...");
            response.addHeader("Set-Cookie", cookieHeader);  // Use addHeader instead of setHeader
        }
        log.info("Cookie {} set successfully", name);
    }
    
    private boolean isSecureEnvironment() {
        // Check if we're in production or using HTTPS
        String[] profiles = environment.getActiveProfiles();
        return Arrays.asList(profiles).contains("prod") || 
               Arrays.asList(profiles).contains("production") ||
               frontendUrl.startsWith("https://");
    }
}
