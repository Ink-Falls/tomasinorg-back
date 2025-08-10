package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.dto.UserDto;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDto userDto = userService.convertToDto(userOpt.get());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getUserProfile() {
        return getCurrentUser();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            // Clear all tokens from database
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Clear all tokens in database
                user.setRefreshToken(null);           // Clear JWT refresh token
                user.setAccessToken(null);            // Clear Google access token
                user.setGoogleRefreshToken(null);     // Clear Google refresh token
                user.setTokenExpiresAt(null);         // Clear token expiration
                user.setUpdatedAt(LocalDateTime.now());
                
                userService.save(user);
                
                log.info("Successfully cleared all tokens for user: {}", email);
            } else {
                log.warn("User not found during logout: {}", email);
            }

            // Clear cookies
            clearTokenCookie(response, "accessToken");
            clearTokenCookie(response, "refreshToken");
            
            return ResponseEntity.ok().body("Logged out successfully");
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            
            // Still clear cookies even if database update fails
            clearTokenCookie(response, "accessToken");
            clearTokenCookie(response, "refreshToken");
            
            return ResponseEntity.ok().body("Logged out successfully");
        }
    }

    private void clearTokenCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
