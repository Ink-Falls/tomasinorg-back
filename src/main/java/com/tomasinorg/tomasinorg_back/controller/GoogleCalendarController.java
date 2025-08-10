package com.tomasinorg.tomasinorg_back.controller;

import com.tomasinorg.tomasinorg_back.dto.calendar.*;
import com.tomasinorg.tomasinorg_back.service.GoogleCalendarRestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarRestService googleCalendarService;

    // Calendar List Operations
    @GetMapping("/calendars")
    public ResponseEntity<Object> getCalendars(
            @AuthenticationPrincipal OAuth2User principal,
            @ModelAttribute CalendarListRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody CalendarDto calendarDto) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @RequestBody CalendarDto calendarDto) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @ModelAttribute EventListRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @RequestBody CreateEventRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
            Object createdEvent = googleCalendarService.createEvent(userEmail, calendarId, request);
            return ResponseEntity.ok(createdEvent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{calendarId}/events/{eventId}")
    public ResponseEntity<Object> updateEvent(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @PathVariable String eventId,
            @RequestBody UpdateEventRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @PathVariable String eventId) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @PathVariable String eventId,
            @RequestBody MoveEventRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
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
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable String calendarId,
            @RequestBody QuickAddEventRequest request) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated",
                    "loginUrl", "/oauth2/authorization/google"
                ));
            }
            
            String userEmail = principal.getAttribute("email");
            Object quickEvent = googleCalendarService.quickAddEvent(userEmail, calendarId, request.getText());
            return ResponseEntity.ok(quickEvent);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", e.getMessage()
            ));
        }
    }
}
