package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.service.GoogleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleService googleService;

    @GetMapping("/calendar")
    public ResponseEntity<Object> getCalendarEvents() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        try {
            Object calendarEvents = googleService.getCalendarEvents(email);
            return ResponseEntity.ok(calendarEvents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch calendar events: " + e.getMessage());
        }
    }
}
