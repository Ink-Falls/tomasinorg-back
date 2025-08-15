package com.tomasinorg.tomasinorg_back.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tomasinorg.tomasinorg_back.dto.calendar.CalendarDto;
import com.tomasinorg.tomasinorg_back.dto.calendar.CalendarListRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.CreateEventRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.EventListRequest;
import com.tomasinorg.tomasinorg_back.dto.calendar.UpdateEventRequest;
import com.tomasinorg.tomasinorg_back.model.User;
import com.tomasinorg.tomasinorg_back.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarRestService {

    private final UserRepository userRepository;
    private final TokenRefreshService tokenRefreshService;
    private final RestTemplate restTemplate;

    private static final String GOOGLE_CALENDAR_API_BASE_URL = "https://www.googleapis.com/calendar/v3";

    private HttpHeaders createAuthHeaders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        if (user.getAccessToken() == null) {
            throw new RuntimeException("User not authenticated with Google - no access token");
        }
        
        // Check if token is expired and try to refresh
        if (user.getTokenExpiresAt() == null || user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Token expired for user: {}, attempting refresh", userEmail);
            boolean refreshed = tokenRefreshService.refreshGoogleToken(userEmail);
            if (!refreshed) {
                throw new RuntimeException("Access token expired and refresh failed - please re-authenticate");
            }
            // Reload user after token refresh
            user = userRepository.findByEmail(userEmail).orElseThrow();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Calendar List Operations
    public Object getCalendarList(String userEmail, CalendarListRequest request) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(GOOGLE_CALENDAR_API_BASE_URL + "/users/me/calendarList");
            
            if (request.getMaxResults() != null) {
                builder.queryParam("maxResults", request.getMaxResults());
            }
            if (request.getPageToken() != null) {
                builder.queryParam("pageToken", request.getPageToken());
            }
            if (request.getShowDeleted() != null) {
                builder.queryParam("showDeleted", request.getShowDeleted());
            }
            if (request.getShowHidden() != null) {
                builder.queryParam("showHidden", request.getShowHidden());
            }
            if (request.getSyncToken() != null) {
                builder.queryParam("syncToken", request.getSyncToken());
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching calendar list for user: {}", userEmail, e);
            throw new RuntimeException("Failed to fetch calendar list", e);
        }
    }

    public Object getCalendar(String userEmail, String calendarId) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId;
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching calendar {} for user: {}", calendarId, userEmail, e);
            throw new RuntimeException("Failed to fetch calendar", e);
        }
    }

    public Object createCalendar(String userEmail, CalendarDto calendarDto) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars";
            
            HttpEntity<CalendarDto> entity = new HttpEntity<>(calendarDto, headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating calendar for user: {}", userEmail, e);
            throw new RuntimeException("Failed to create calendar", e);
        }
    }

    public Object updateCalendar(String userEmail, String calendarId, CalendarDto calendarDto) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId;
            
            HttpEntity<CalendarDto> entity = new HttpEntity<>(calendarDto, headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error updating calendar {} for user: {}", calendarId, userEmail, e);
            throw new RuntimeException("Failed to update calendar", e);
        }
    }

    public void deleteCalendar(String userEmail, String calendarId) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId;
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            log.error("Error deleting calendar {} for user: {}", calendarId, userEmail, e);
            throw new RuntimeException("Failed to delete calendar", e);
        }
    }

    // Event Operations
    public Object getEvents(String userEmail, EventListRequest request) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + request.getCalendarId() + "/events");
            
            if (request.getMaxResults() != null) {
                builder.queryParam("maxResults", request.getMaxResults());
            }
            if (request.getPageToken() != null) {
                builder.queryParam("pageToken", request.getPageToken());
            }
            if (request.getTimeMin() != null) {
                builder.queryParam("timeMin", request.getTimeMin());
            }
            if (request.getTimeMax() != null) {
                builder.queryParam("timeMax", request.getTimeMax());
            }
            if (request.getQ() != null) {
                builder.queryParam("q", request.getQ());
            }
            if (request.getOrderBy() != null) {
                builder.queryParam("orderBy", request.getOrderBy());
            }
            if (request.getSingleEvents() != null) {
                builder.queryParam("singleEvents", request.getSingleEvents());
            }
            if (request.getShowDeleted() != null) {
                builder.queryParam("showDeleted", request.getShowDeleted());
            }
            if (request.getTimeZone() != null) {
                builder.queryParam("timeZone", request.getTimeZone());
            }
            if (request.getUpdatedMin() != null) {
                builder.queryParam("updatedMin", request.getUpdatedMin());
            }
            if (request.getSyncToken() != null) {
                builder.queryParam("syncToken", request.getSyncToken());
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching events for calendar {} user: {}", request.getCalendarId(), userEmail, e);
            throw new RuntimeException("Failed to fetch events", e);
        }
    }

    public Object getEvent(String userEmail, String calendarId, String eventId) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId + "/events/" + eventId;
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching event {} from calendar {} for user: {}", eventId, calendarId, userEmail, e);
            throw new RuntimeException("Failed to fetch event", e);
        }
    }

    public Object createEvent(String userEmail, String calendarId, CreateEventRequest request) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId + "/events";
            
            // Log the request details for debugging
            log.info("Creating event for user: {} in calendar: {}", userEmail, calendarId);
            log.info("Request URL: {}", url);
            log.info("Event data: {}", request);
            
            HttpEntity<CreateEventRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class
            );

            log.info("Event created successfully, response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating event in calendar {} for user: {}", calendarId, userEmail, e);
            log.error("Error details - Type: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("Failed to create event: " + e.getMessage(), e);
        }
    }

    public Object updateEvent(String userEmail, String calendarId, String eventId, UpdateEventRequest request) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId + "/events/" + eventId;
            
            HttpEntity<UpdateEventRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error updating event {} in calendar {} for user: {}", eventId, calendarId, userEmail, e);
            throw new RuntimeException("Failed to update event", e);
        }
    }

    public void deleteEvent(String userEmail, String calendarId, String eventId) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            String url = GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId + "/events/" + eventId;
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
        } catch (Exception e) {
            log.error("Error deleting event {} from calendar {} for user: {}", eventId, calendarId, userEmail, e);
            throw new RuntimeException("Failed to delete event", e);
        }
    }

    public Object moveEvent(String userEmail, String calendarId, String eventId, String destinationCalendarId) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId + "/events/" + eventId + "/move")
                    .queryParam("destination", destinationCalendarId);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error moving event {} from calendar {} to {} for user: {}", eventId, calendarId, destinationCalendarId, userEmail, e);
            throw new RuntimeException("Failed to move event", e);
        }
    }

    public Object quickAddEvent(String userEmail, String calendarId, String text) {
        try {
            HttpHeaders headers = createAuthHeaders(userEmail);
            
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(GOOGLE_CALENDAR_API_BASE_URL + "/calendars/" + calendarId + "/events/quickAdd")
                    .queryParam("text", text);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    Object.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error quick adding event to calendar {} for user: {}", calendarId, userEmail, e);
            throw new RuntimeException("Failed to quick add event", e);
        }
    }
}
