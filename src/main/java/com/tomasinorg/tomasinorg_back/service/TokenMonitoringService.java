package com.tomasinorg.tomasinorg_back.service;

import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenMonitoringService {

    private final UserRepository userRepository;
    private final TokenRefreshService tokenRefreshService;

    /**
     * Monitors token health every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void monitorTokenHealth() {
        try {
            List<User> usersWithTokens = userRepository.findByAccessTokenIsNotNull();
            
            long totalUsers = usersWithTokens.size();
            
            long expiredTokens = usersWithTokens.stream()
                .filter(user -> user.getTokenExpiresAt() != null && 
                               user.getTokenExpiresAt().isBefore(LocalDateTime.now()))
                .count();
                
            long soonToExpireTokens = usersWithTokens.stream()
                .filter(user -> user.getTokenExpiresAt() != null && 
                               user.getTokenExpiresAt().isBefore(LocalDateTime.now().plusHours(1)) &&
                               user.getTokenExpiresAt().isAfter(LocalDateTime.now()))
                .count();
                
            long validTokens = totalUsers - expiredTokens;
            
            log.info("Token Health Monitor: {} total users, {} valid tokens, {} expired, {} expiring within 1 hour", 
                     totalUsers, validTokens, expiredTokens, soonToExpireTokens);
                     
            // Alert if many tokens are expired
            if (totalUsers > 0 && (expiredTokens * 100 / totalUsers) > 50) {
                log.warn("HIGH ALERT: More than 50% of user tokens are expired! ({}/{})", 
                        expiredTokens, totalUsers);
            }
            
        } catch (Exception e) {
            log.error("Error during token health monitoring", e);
        }
    }

    /**
     * Auto-refresh tokens that are expiring soon
     */
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void autoRefreshExpiringTokens() {
        try {
            List<User> soonToExpireUsers = userRepository.findByAccessTokenIsNotNull().stream()
                .filter(user -> user.getTokenExpiresAt() != null && 
                               user.getTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(30)) &&
                               user.getTokenExpiresAt().isAfter(LocalDateTime.now()) &&
                               user.getGoogleRefreshToken() != null)
                .collect(Collectors.toList());
                
            log.info("Auto-refresh check: Found {} users with tokens expiring within 30 minutes", 
                    soonToExpireUsers.size());
                    
            for (User user : soonToExpireUsers) {
                try {
                    log.info("Auto-refreshing token for user: {}", user.getEmail());
                    boolean refreshed = tokenRefreshService.refreshGoogleToken(user.getEmail());
                    if (refreshed) {
                        log.info("Successfully auto-refreshed token for user: {}", user.getEmail());
                    } else {
                        log.warn("Failed to auto-refresh token for user: {}", user.getEmail());
                    }
                } catch (Exception e) {
                    log.error("Error auto-refreshing token for user: {}", user.getEmail(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error during auto-refresh process", e);
        }
    }

    /**
     * Get detailed token health statistics
     */
    public Map<String, Object> getTokenHealthStats() {
        List<User> allUsers = userRepository.findAll();
        List<User> usersWithTokens = userRepository.findByAccessTokenIsNotNull();
        
        LocalDateTime now = LocalDateTime.now();
        
        long totalUsers = allUsers.size();
        long usersWithGoogleTokens = usersWithTokens.size();
        
        long validTokens = usersWithTokens.stream()
            .filter(user -> user.getTokenExpiresAt() != null && 
                           user.getTokenExpiresAt().isAfter(now))
            .count();
            
        long expiredTokens = usersWithTokens.stream()
            .filter(user -> user.getTokenExpiresAt() != null && 
                           user.getTokenExpiresAt().isBefore(now))
            .count();
            
        long expiringSoonTokens = usersWithTokens.stream()
            .filter(user -> user.getTokenExpiresAt() != null && 
                           user.getTokenExpiresAt().isBefore(now.plusHours(1)) &&
                           user.getTokenExpiresAt().isAfter(now))
            .count();
            
        long usersWithRefreshTokens = usersWithTokens.stream()
            .filter(user -> user.getGoogleRefreshToken() != null)
            .count();

        return Map.of(
            "totalUsers", totalUsers,
            "usersWithGoogleTokens", usersWithGoogleTokens,
            "validTokens", validTokens,
            "expiredTokens", expiredTokens,
            "expiringSoonTokens", expiringSoonTokens,
            "usersWithRefreshTokens", usersWithRefreshTokens,
            "healthPercentage", usersWithGoogleTokens > 0 ? (validTokens * 100 / usersWithGoogleTokens) : 0,
            "lastChecked", now.toString()
        );
    }

    /**
     * Validate a specific user's token health
     */
    public Map<String, Object> validateUserTokens(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return Map.of(
                "userExists", false,
                "error", "User not found"
            );
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean hasAccessToken = user.getAccessToken() != null;
        boolean hasRefreshToken = user.getGoogleRefreshToken() != null;
        boolean tokenValid = user.getTokenExpiresAt() != null && user.getTokenExpiresAt().isAfter(now);
        boolean expiringSoon = user.getTokenExpiresAt() != null && 
                              user.getTokenExpiresAt().isBefore(now.plusHours(1)) &&
                              user.getTokenExpiresAt().isAfter(now);
        
        return Map.of(
            "userExists", true,
            "email", user.getEmail(),
            "hasAccessToken", hasAccessToken,
            "hasRefreshToken", hasRefreshToken,
            "tokenValid", tokenValid,
            "expiringSoon", expiringSoon,
            "tokenExpiration", user.getTokenExpiresAt() != null ? user.getTokenExpiresAt().toString() : null,
            "canRefresh", hasRefreshToken && !tokenValid,
            "lastUpdated", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
        );
    }

    /**
     * Force refresh all expired tokens (admin function)
     */
    public Map<String, Object> forceRefreshAllExpiredTokens() {
        List<User> expiredUsers = userRepository.findByAccessTokenIsNotNull().stream()
            .filter(user -> user.getTokenExpiresAt() != null && 
                           user.getTokenExpiresAt().isBefore(LocalDateTime.now()) &&
                           user.getGoogleRefreshToken() != null)
            .collect(Collectors.toList());
            
        int attempted = expiredUsers.size();
        int successful = 0;
        int failed = 0;
        
        for (User user : expiredUsers) {
            try {
                boolean refreshed = tokenRefreshService.refreshGoogleToken(user.getEmail());
                if (refreshed) {
                    successful++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
                log.error("Failed to refresh token for user: {}", user.getEmail(), e);
            }
        }
        
        return Map.of(
            "attempted", attempted,
            "successful", successful,
            "failed", failed,
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
