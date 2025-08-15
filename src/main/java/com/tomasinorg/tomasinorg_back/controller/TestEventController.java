package com.tomasinorg.tomasinorg_back.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomasinorg.tomasinorg_back.dto.calendar.CreateEventRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.EventDto;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestEventController {

    @PostMapping("/event-debug")
    public ResponseEntity<Object> debugEventCreation(
            @RequestBody CreateEventRequest request) {
        
        log.info("=== EVENT DEBUG ===");
        log.info("Authentication: Not required for debug");
        log.info("Request received: {}", request);
        log.info("Summary: {}", request.getSummary());
        log.info("Description: {}", request.getDescription());
        log.info("Start: {}", request.getStart());
        log.info("End: {}", request.getEnd());
        log.info("Location: {}", request.getLocation());
        log.info("Attendees: {}", request.getAttendees());
        
        // Check date formats
        if (request.getStart() != null) {
            log.info("Start DateTime: {}", request.getStart().getDateTime());
            log.info("Start Date: {}", request.getStart().getDate());
            log.info("Start TimeZone: {}", request.getStart().getTimeZone());
        }
        
        if (request.getEnd() != null) {
            log.info("End DateTime: {}", request.getEnd().getDateTime());
            log.info("End Date: {}", request.getEnd().getDate());
            log.info("End TimeZone: {}", request.getEnd().getTimeZone());
        }
        
        log.info("==================");
        
        return ResponseEntity.ok(Map.of(
            "message", "Event data received successfully",
            "data", request,
            "user", "debug-mode"
        ));
    }
    
    @PostMapping("/minimal-event")
    public ResponseEntity<Object> createMinimalEvent(@RequestBody Map<String, Object> rawData) {
        log.info("=== RAW EVENT DATA ===");
        log.info("Raw JSON received: {}", rawData);
        log.info("======================");
        
        return ResponseEntity.ok(Map.of(
            "message", "Raw data received",
            "receivedData", rawData
        ));
    }
    
    @GetMapping("/sample-event")
    public ResponseEntity<Object> getSampleEventFormat() {
        // Create a properly formatted sample event
        EventDto.EventDateTime start = EventDto.EventDateTime.builder()
                .dateTime("2025-08-15T14:00:00.000Z")
                .timeZone("UTC")
                .build();
                
        EventDto.EventDateTime end = EventDto.EventDateTime.builder()
                .dateTime("2025-08-15T15:00:00.000Z")
                .timeZone("UTC")
                .build();
        
        CreateEventRequest sampleEvent = CreateEventRequest.builder()
                .summary("Sample Test Event")
                .description("This is a test event with proper formatting")
                .start(start)
                .end(end)
                .build();
        
        return ResponseEntity.ok(Map.of(
            "message", "Sample event with correct format",
            "sampleEvent", sampleEvent,
            "note", "Use this format for creating events"
        ));
    }
}
