package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.dto.JwtResponse;
import com.tomasinorg.tomasinorg_back.dto.RefreshTokenRequest;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.security.JwtTokenProvider;
import com.tomasinorg.tomasinorg_back.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody(required = false) RefreshTokenRequest request, 
                                        HttpServletRequest httpRequest,
                                        HttpServletResponse response) {
        try {
            String refreshToken = null;
            
            // First try to get refresh token from request body
            if (request != null && request.getRefreshToken() != null) {
                refreshToken = request.getRefreshToken();
            } else {
                // If not in body, try to get from cookies
                if (httpRequest.getCookies() != null) {
                    for (Cookie cookie : httpRequest.getCookies()) {
                        if ("refreshToken".equals(cookie.getName())) {
                            refreshToken = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Refresh token not found");
            }
            
            if (!tokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.badRequest().body("Invalid refresh token");
            }

            String email = tokenProvider.getEmailFromToken(refreshToken);
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty() || !refreshToken.equals(userOpt.get().getRefreshToken())) {
                return ResponseEntity.badRequest().body("Invalid refresh token");
            }

            User user = userOpt.get();
            
            // Generate new access token
            String newAccessToken = tokenProvider.generateToken(email, user.getRole().toString());
            String newRefreshToken = tokenProvider.generateRefreshToken(email);
            
            // Update user's refresh token
            user.setRefreshToken(newRefreshToken);
            user.setUpdatedAt(LocalDateTime.now());
            userService.save(user);

            // Set new cookies
            setTokenCookie(response, "accessToken", newAccessToken, 
                          (int) (tokenProvider.getJwtExpirationMs() / 1000));
            setTokenCookie(response, "refreshToken", newRefreshToken, 
                          (int) (tokenProvider.getJwtRefreshExpirationMs() / 1000));

            JwtResponse jwtResponse = JwtResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(tokenProvider.getJwtExpirationMs())
                    .user(userService.convertToDto(user))
                    .build();

            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Token refresh failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear cookies
        clearTokenCookie(response, "accessToken");
        clearTokenCookie(response, "refreshToken");
        
        return ResponseEntity.ok().body("Logged out successfully");
    }

    private void setTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void clearTokenCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
