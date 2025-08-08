package com.tomasinorg.tomasinorg_back.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomasinorg.tomasinorg_back.dto.AdminUserDto;
import com.tomasinorg.tomasinorg_back.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminData() {
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("message", "This is admin-only data");
        adminData.put("timestamp", System.currentTimeMillis());
        adminData.put("users", "Admin users list would go here");
        
        return ResponseEntity.ok(adminData);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 100);
        stats.put("activeUsers", 75);
        stats.put("totalRequests", 1000);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        List<AdminUserDto> users = userService.getAllUsersForAdmin();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDto> getUserById(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(userService.convertToAdminDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
