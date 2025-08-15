package com.tomasinorg.tomasinorg_back.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomasinorg.tomasinorg_back.dto.calendar.CalendarDto;
import com.tomasinorg.tomasinorg_back.dto.calendar.CalendarListRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.CreateEventRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.EventListRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.MoveEventRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.QuickAddEventRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.UpdateEventRequest;
import com.tomasinorg.tomasinorg_back.service.GoogleCalendarRestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarRestService googleCalendarService;

    // Calendar List Operations
    @GetMapping("/calendars")
    public ResponseEntity<Object> getCalendars(
            Authentication authentication,
            @ModelAttribute CalendarListRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            // Extract email from either OAuth2User or JWT token
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            Object calendars = googleCalendarService.getCalendarList(userEmail, request);
            return ResponseEntity.ok(calendars);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{calendarId}")
    public ResponseEntity<Object> getCalendar(
            Authentication authentication,
            @PathVariable String calendarId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            Object calendar = googleCalendarService.getCalendar(userEmail, calendarId);
            return ResponseEntity.ok(calendar);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/calendars")
    public ResponseEntity<Object> createCalendar(
            Authentication authentication,
            @RequestBody CalendarDto calendarDto) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            Object createdCalendar = googleCalendarService.createCalendar(userEmail, calendarDto);
            return ResponseEntity.ok(createdCalendar);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{calendarId}")
    public ResponseEntity<Object> updateCalendar(
            Authentication authentication,
            @PathVariable String calendarId,
            @RequestBody CalendarDto calendarDto) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            Object updatedCalendar = googleCalendarService.updateCalendar(userEmail, calendarId, calendarDto);
            return ResponseEntity.ok(updatedCalendar);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{calendarId}")
    public ResponseEntity<Object> deleteCalendar(
            Authentication authentication,
            @PathVariable String calendarId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            googleCalendarService.deleteCalendar(userEmail, calendarId);
            return ResponseEntity.ok(Map.of(
                "message", "Calendar deleted successfully",
                "calendarId", calendarId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    // Event Operations
    @GetMapping("/{calendarId}/events")
    public ResponseEntity<Object> getEvents(
            Authentication authentication,
            @PathVariable String calendarId,
            @ModelAttribute EventListRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            // Set the calendarId from path variable into the request
            request.setCalendarId(calendarId);
            Object events = googleCalendarService.getEvents(userEmail, request);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{calendarId}/events")
    public ResponseEntity<Object> createEvent(
            Authentication authentication,
            @PathVariable String calendarId,
            @RequestBody CreateEventRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            // Log the request for debugging
            System.out.println("Creating event for user: " + userEmail + " in calendar: " + calendarId);
            System.out.println("Event request: " + request);
            
            Object createdEvent = googleCalendarService.createEvent(userEmail, calendarId, request);
            return ResponseEntity.ok(createdEvent);
        } catch (Exception e) {
            // Enhanced error logging
            System.err.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }

    @PutMapping("/{calendarId}/events/{eventId}")
    public ResponseEntity<Object> updateEvent(
            Authentication authentication,
            @PathVariable String calendarId,
            @PathVariable String eventId,
            @RequestBody UpdateEventRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            Object updatedEvent = googleCalendarService.updateEvent(userEmail, calendarId, eventId, request);
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{calendarId}/events/{eventId}")
    public ResponseEntity<Object> deleteEvent(
            Authentication authentication,
            @PathVariable String calendarId,
            @PathVariable String eventId) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            googleCalendarService.deleteEvent(userEmail, calendarId, eventId);
            return ResponseEntity.ok(Map.of(
                "message", "Event deleted successfully",
                "calendarId", calendarId,
                "eventId", eventId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{calendarId}/events/{eventId}/move")
    public ResponseEntity<Object> moveEvent(
            Authentication authentication,
            @PathVariable String calendarId,
            @PathVariable String eventId,
            @RequestBody MoveEventRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            Object movedEvent = googleCalendarService.moveEvent(userEmail, calendarId, eventId, request.getDestinationCalendarId());
            return ResponseEntity.ok(movedEvent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{calendarId}/events/quickAdd")
    public ResponseEntity<Object> quickAddEvent(
            Authentication authentication,
            @PathVariable String calendarId,
            @RequestBody QuickAddEventRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = extractUserEmail(authentication);
            if (userEmail == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Could not extract user email from authentication",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            Object quickEvent = googleCalendarService.quickAddEvent(userEmail, calendarId, request.getText());
            return ResponseEntity.ok(quickEvent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }
    
    // Helper method to extract email from different authentication types
    private String extractUserEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            // OAuth2 flow
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            return oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof String) {
            // JWT flow - principal is the email
            return (String) authentication.getPrincipal();
        }
        return null;
    }
}
