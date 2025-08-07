package com.tomasinorg.tomasinorg_back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

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
}
