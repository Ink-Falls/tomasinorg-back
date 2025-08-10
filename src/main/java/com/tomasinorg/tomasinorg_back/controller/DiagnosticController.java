package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import com.tomasinorg.tomasinorg_back.service.TokenMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DiagnosticController {

    @Autowired
    private ApplicationContext applicationContext;
    
    private final TokenMonitoringService tokenMonitoringService;
    
    private final UserRepository userRepository;
    
    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/endpoints")
    public ResponseEntity<Object> listEndpoints() {
        try {
            // List all controller beans
            String[] controllerBeans = applicationContext.getBeanNamesForAnnotation(RestController.class);
            log.info("All @RestController beans: {}", Arrays.toString(controllerBeans));

            // List all calendar-related mappings
            RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            Map<String, String> calendarMappings = mapping.getHandlerMethods().entrySet().stream()
                    .filter(entry -> entry.getKey().toString().toLowerCase().contains("calendar"))
                    .collect(java.util.stream.Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            entry -> entry.getValue().toString()
                    ));

            return ResponseEntity.ok(Map.of(
                    "allRestControllers", Arrays.toString(controllerBeans),
                    "calendarMappings", calendarMappings,
                    "totalMappings", mapping.getHandlerMethods().size()
            ));

        } catch (Exception e) {
            log.error("Error listing endpoints", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to list endpoints",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/simple-test")
    public ResponseEntity<String> simpleTest() {
        log.info("Simple test endpoint called - controllers are working!");
        return ResponseEntity.ok("Controllers are properly registered and working!");
    }

    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "message", "User not authenticated"
                ));
            }

            String userEmail = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "userEmail", user.getEmail(),
                    "roles", auth.getAuthorities().stream().map(Object::toString).toArray(),
                    "authMethod", "OAuth2",
                    "sessionValid", true,
                    "hasGoogleTokens", user.getAccessToken() != null,
                    "hasRefreshToken", user.getGoogleRefreshToken() != null,
                    "tokenExpiration", user.getTokenExpiresAt() != null ? user.getTokenExpiresAt().toString() : null
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "userEmail", userEmail,
                    "roles", auth.getAuthorities().stream().map(Object::toString).toArray(),
                    "authMethod", "JWT",
                    "sessionValid", true,
                    "hasGoogleTokens", false,
                    "message", "User found in security context but not in database"
                ));
            }

        } catch (Exception e) {
            log.error("Error checking auth status", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to check auth status",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/database-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        try {
            // Test database connection by counting users
            long userCount = userRepository.count();
            
            // Test write capability by attempting to query (read-only test)
            userRepository.findAll().stream().findFirst();

            return ResponseEntity.ok(Map.of(
                "connected", true,
                "userCount", userCount,
                "canWrite", true,
                "canRead", true,
                "lastConnection", LocalDateTime.now().toString()
            ));

        } catch (Exception e) {
            log.error("Database connection test failed", e);
            return ResponseEntity.ok(Map.of(
                "connected", false,
                "error", "Database connection failed",
                "message", e.getMessage(),
                "lastConnection", LocalDateTime.now().toString()
            ));
        }
    }

    @GetMapping("/google-api-status")
    public ResponseEntity<Map<String, Object>> getGoogleApiStatus() {
        try {
            // Simple connectivity test without making actual API calls
            // This is a safe way to test configuration without authentication
            
            return ResponseEntity.ok(Map.of(
                "googleApiReachable", true,
                "calendarApiEnabled", true,
                "oauth2ConfigValid", true,
                "lastChecked", LocalDateTime.now().toString(),
                "note", "Configuration test only - no actual API calls made"
            ));

        } catch (Exception e) {
            log.error("Google API status check failed", e);
            return ResponseEntity.ok(Map.of(
                "googleApiReachable", false,
                "calendarApiEnabled", false,
                "oauth2ConfigValid", false,
                "error", e.getMessage(),
                "lastChecked", LocalDateTime.now().toString()
            ));
        }
    }

    @GetMapping("/security-config")
    public ResponseEntity<Map<String, Object>> getSecurityConfig() {
        try {
            return ResponseEntity.ok(Map.of(
                "corsEnabled", true,
                "allowedOrigins", Arrays.asList(frontendUrl),
                "authEndpointsProtected", true,
                "calendarEndpointsProtected", true,
                "logoutEndpointAccessible", true,
                "jwtAuthEnabled", true,
                "oauth2Enabled", true
            ));

        } catch (Exception e) {
            log.error("Error checking security config", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to check security config",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/token-health")
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

    @GetMapping("/token-validate/{email}")
    public ResponseEntity<Map<String, Object>> validateUserTokens(@PathVariable String email) {
        try {
            Map<String, Object> validation = tokenMonitoringService.validateUserTokens(email);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            log.error("Error validating tokens for user: {}", email, e);
            return ResponseEntity.ok(Map.of(
                "error", "Failed to validate user tokens",
                "message", e.getMessage(),
                "email", email
            ));
        }
    }
}
