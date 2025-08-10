package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.service.TokenMonitoringService;
import com.tomasinorg.tomasinorg_back.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/tokens")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class TokenAdminController {

    private final TokenMonitoringService tokenMonitoringService;
    private final TokenRefreshService tokenRefreshService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getTokenHealth() {
        try {
            Map<String, Object> stats = tokenMonitoringService.getTokenHealthStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting token health stats", e);
            return ResponseEntity.ok(Map.of(
                "error", "Failed to get token health stats",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/validate/{email}")
    public ResponseEntity<Map<String, Object>> validateUserTokens(@PathVariable String email) {
        try {
            Map<String, Object> validation = tokenMonitoringService.validateUserTokens(email);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            log.error("Error validating tokens for user: {}", email, e);
            return ResponseEntity.ok(Map.of(
                "error", "Failed to validate user tokens",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh/{email}")
    public ResponseEntity<Map<String, Object>> refreshUserToken(@PathVariable String email) {
        try {
            boolean success = tokenRefreshService.refreshGoogleToken(email);
            return ResponseEntity.ok(Map.of(
                "success", success,
                "email", email,
                "message", success ? "Token refreshed successfully" : "Token refresh failed"
            ));
        } catch (Exception e) {
            log.error("Error refreshing token for user: {}", email, e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "email", email,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/refresh-all-expired")
    public ResponseEntity<Map<String, Object>> refreshAllExpiredTokens() {
        try {
            Map<String, Object> result = tokenMonitoringService.forceRefreshAllExpiredTokens();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error refreshing all expired tokens", e);
            return ResponseEntity.ok(Map.of(
                "error", "Failed to refresh expired tokens",
                "message", e.getMessage()
            ));
        }
    }
}
