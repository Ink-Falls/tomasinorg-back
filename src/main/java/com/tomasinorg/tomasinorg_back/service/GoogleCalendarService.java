package com.tomasinorg.tomasinorg_back.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.tomasinorg.tomasinorg_back.config.GoogleCalendarConfig;
import com.tomasinorg.tomasinorg_back.dto.calendar.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private final GoogleCalendarConfig googleCalendarConfig;
    private final OAuth2AuthorizedClientService authorizedClientService;

    private Calendar getCalendarService(String userEmail) throws Exception {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient("google", userEmail);
        
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new RuntimeException("User not authenticated with Google");
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        
        @SuppressWarnings("deprecation")
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        
        return googleCalendarConfig.calendarBuilder(
                googleCalendarConfig.httpTransport(),
                googleCalendarConfig.jsonFactory()
        ).setHttpRequestInitializer(credential).build();
    }

    // Calendar List Operations
    public List<CalendarDto> getCalendarList(String userEmail, CalendarListRequest request) throws Exception {
        Calendar service = getCalendarService(userEmail);
        
        Calendar.CalendarList.List listRequest = service.calendarList().list();
        
        if (request.getMaxResults() != null) {
            listRequest.setMaxResults(request.getMaxResults());
        }
        if (request.getPageToken() != null) {
            listRequest.setPageToken(request.getPageToken());
        }
        if (request.getShowDeleted() != null) {
            listRequest.setShowDeleted(request.getShowDeleted());
        }
        if (request.getShowHidden() != null) {
            listRequest.setShowHidden(request.getShowHidden());
        }
        if (request.getSyncToken() != null) {
            listRequest.setSyncToken(request.getSyncToken());
        }

        CalendarList calendarList = listRequest.execute();
        
        return calendarList.getItems().stream()
                .map(this::convertToCalendarDto)
                .collect(Collectors.toList());
    }

    public CalendarDto getCalendar(String userEmail, String calendarId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        com.google.api.services.calendar.model.Calendar calendar = service.calendars().get(calendarId).execute();
        return convertToCalendarDto(calendar);
    }

    public CalendarDto createCalendar(String userEmail, CalendarDto calendarDto) throws Exception {
        Calendar service = getCalendarService(userEmail);
        
        com.google.api.services.calendar.model.Calendar calendar = 
                new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(calendarDto.getSummary());
        calendar.setDescription(calendarDto.getDescription());
        calendar.setLocation(calendarDto.getLocation());
        calendar.setTimeZone(calendarDto.getTimeZone());

        com.google.api.services.calendar.model.Calendar createdCalendar = 
                service.calendars().insert(calendar).execute();
        
        return convertToCalendarDto(createdCalendar);
    }

    public CalendarDto updateCalendar(String userEmail, String calendarId, CalendarDto calendarDto) throws Exception {
        Calendar service = getCalendarService(userEmail);
        
        com.google.api.services.calendar.model.Calendar calendar = service.calendars().get(calendarId).execute();
        
        if (calendarDto.getSummary() != null) {
            calendar.setSummary(calendarDto.getSummary());
        }
        if (calendarDto.getDescription() != null) {
            calendar.setDescription(calendarDto.getDescription());
        }
        if (calendarDto.getLocation() != null) {
            calendar.setLocation(calendarDto.getLocation());
        }
        if (calendarDto.getTimeZone() != null) {
            calendar.setTimeZone(calendarDto.getTimeZone());
        }

        com.google.api.services.calendar.model.Calendar updatedCalendar = 
                service.calendars().update(calendarId, calendar).execute();
        
        return convertToCalendarDto(updatedCalendar);
    }

    public void deleteCalendar(String userEmail, String calendarId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        service.calendars().delete(calendarId).execute();
    }

    // Event Operations
    public List<EventDto> getEvents(String userEmail, EventListRequest request) throws Exception {
        Calendar service = getCalendarService(userEmail);
        
        Calendar.Events.List listRequest = service.events().list(request.getCalendarId());
        
        if (request.getMaxResults() != null) {
            listRequest.setMaxResults(request.getMaxResults());
        }
        if (request.getPageToken() != null) {
            listRequest.setPageToken(request.getPageToken());
        }
        if (request.getTimeMin() != null) {
            listRequest.setTimeMin(new DateTime(request.getTimeMin()));
        }
        if (request.getTimeMax() != null) {
            listRequest.setTimeMax(new DateTime(request.getTimeMax()));
        }
        if (request.getQ() != null) {
            listRequest.setQ(request.getQ());
        }
        if (request.getOrderBy() != null) {
            listRequest.setOrderBy(request.getOrderBy());
        }
        if (request.getSingleEvents() != null) {
            listRequest.setSingleEvents(request.getSingleEvents());
        }
        if (request.getShowDeleted() != null) {
            listRequest.setShowDeleted(request.getShowDeleted());
        }
        if (request.getTimeZone() != null) {
            listRequest.setTimeZone(request.getTimeZone());
        }
        if (request.getUpdatedMin() != null) {
            listRequest.setUpdatedMin(new DateTime(request.getUpdatedMin()));
        }
        if (request.getSyncToken() != null) {
            listRequest.setSyncToken(request.getSyncToken());
        }

        Events events = listRequest.execute();
        
        return events.getItems().stream()
                .map(this::convertToEventDto)
                .collect(Collectors.toList());
    }

    public EventDto getEvent(String userEmail, String calendarId, String eventId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        Event event = service.events().get(calendarId, eventId).execute();
        return convertToEventDto(event);
    }

    public EventDto createEvent(String userEmail, String calendarId, CreateEventRequest request) throws Exception {
        Calendar service = getCalendarService(userEmail);
        
        Event event = new Event();
        event.setSummary(request.getSummary());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        
        if (request.getStart() != null) {
            EventDateTime start = new EventDateTime();
            if (request.getStart().getDateTime() != null) {
                start.setDateTime(new DateTime(request.getStart().getDateTime()));
            }
            if (request.getStart().getDate() != null) {
                start.setDate(new DateTime(request.getStart().getDate()));
            }
            if (request.getStart().getTimeZone() != null) {
                start.setTimeZone(request.getStart().getTimeZone());
            }
            event.setStart(start);
        }
        
        if (request.getEnd() != null) {
            EventDateTime end = new EventDateTime();
            if (request.getEnd().getDateTime() != null) {
                end.setDateTime(new DateTime(request.getEnd().getDateTime()));
            }
            if (request.getEnd().getDate() != null) {
                end.setDate(new DateTime(request.getEnd().getDate()));
            }
            if (request.getEnd().getTimeZone() != null) {
                end.setTimeZone(request.getEnd().getTimeZone());
            }
            event.setEnd(end);
        }
        
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            List<EventAttendee> attendees = request.getAttendees().stream()
                    .map(this::convertToGoogleEventAttendee)
                    .collect(Collectors.toList());
            event.setAttendees(attendees);
        }
        
        if (request.getRecurrence() != null) {
            event.setRecurrence(request.getRecurrence());
        }
        
        if (request.getVisibility() != null) {
            event.setVisibility(request.getVisibility());
        }
        
        if (request.getGuestsCanInviteOthers() != null) {
            event.setGuestsCanInviteOthers(request.getGuestsCanInviteOthers());
        }
        
        if (request.getGuestsCanModify() != null) {
            event.setGuestsCanModify(request.getGuestsCanModify());
        }
        
        if (request.getGuestsCanSeeOtherGuests() != null) {
            event.setGuestsCanSeeOtherGuests(request.getGuestsCanSeeOtherGuests());
        }
        
        if (request.getTransparency() != null) {
            event.setTransparency(request.getTransparency());
        }
        
        if (request.getColorId() != null) {
            event.setColorId(request.getColorId());
        }

        Event createdEvent = service.events().insert(calendarId, event).execute();
        return convertToEventDto(createdEvent);
    }

    public EventDto updateEvent(String userEmail, String calendarId, String eventId, UpdateEventRequest request) throws Exception {
        Calendar service = getCalendarService(userEmail);
        
        Event event = service.events().get(calendarId, eventId).execute();
        
        if (request.getSummary() != null) {
            event.setSummary(request.getSummary());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        
        if (request.getStart() != null) {
            EventDateTime start = new EventDateTime();
            if (request.getStart().getDateTime() != null) {
                start.setDateTime(new DateTime(request.getStart().getDateTime()));
            }
            if (request.getStart().getDate() != null) {
                start.setDate(new DateTime(request.getStart().getDate()));
            }
            if (request.getStart().getTimeZone() != null) {
                start.setTimeZone(request.getStart().getTimeZone());
            }
            event.setStart(start);
        }
        
        if (request.getEnd() != null) {
            EventDateTime end = new EventDateTime();
            if (request.getEnd().getDateTime() != null) {
                end.setDateTime(new DateTime(request.getEnd().getDateTime()));
            }
            if (request.getEnd().getDate() != null) {
                end.setDate(new DateTime(request.getEnd().getDate()));
            }
            if (request.getEnd().getTimeZone() != null) {
                end.setTimeZone(request.getEnd().getTimeZone());
            }
            event.setEnd(end);
        }

        Event updatedEvent = service.events().update(calendarId, eventId, event).execute();
        return convertToEventDto(updatedEvent);
    }

    public void deleteEvent(String userEmail, String calendarId, String eventId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        service.events().delete(calendarId, eventId).execute();
    }

    public EventDto moveEvent(String userEmail, String calendarId, String eventId, String destinationCalendarId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        Event movedEvent = service.events().move(calendarId, eventId, destinationCalendarId).execute();
        return convertToEventDto(movedEvent);
    }

    public EventDto quickAddEvent(String userEmail, String calendarId, String text) throws Exception {
        Calendar service = getCalendarService(userEmail);
        Event quickEvent = service.events().quickAdd(calendarId, text).execute();
        return convertToEventDto(quickEvent);
    }

    // ACL (Access Control List) Operations
    public List<AclRule> getAclList(String userEmail, String calendarId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        Acl acl = service.acl().list(calendarId).execute();
        return acl.getItems();
    }

    public AclRule insertAcl(String userEmail, String calendarId, AclRule rule) throws Exception {
        Calendar service = getCalendarService(userEmail);
        return service.acl().insert(calendarId, rule).execute();
    }

    public AclRule updateAcl(String userEmail, String calendarId, String ruleId, AclRule rule) throws Exception {
        Calendar service = getCalendarService(userEmail);
        return service.acl().update(calendarId, ruleId, rule).execute();
    }

    public void deleteAcl(String userEmail, String calendarId, String ruleId) throws Exception {
        Calendar service = getCalendarService(userEmail);
        service.acl().delete(calendarId, ruleId).execute();
    }

    // Utility Methods
    private CalendarDto convertToCalendarDto(com.google.api.services.calendar.model.Calendar calendar) {
        return CalendarDto.builder()
                .id(calendar.getId())
                .summary(calendar.getSummary())
                .description(calendar.getDescription())
                .location(calendar.getLocation())
                .timeZone(calendar.getTimeZone())
                .build();
    }

    private CalendarDto convertToCalendarDto(CalendarListEntry calendarListEntry) {
        return CalendarDto.builder()
                .id(calendarListEntry.getId())
                .summary(calendarListEntry.getSummary())
                .description(calendarListEntry.getDescription())
                .location(calendarListEntry.getLocation())
                .timeZone(calendarListEntry.getTimeZone())
                .accessRole(calendarListEntry.getAccessRole())
                .primary(calendarListEntry.getPrimary())
                .backgroundColor(calendarListEntry.getBackgroundColor())
                .foregroundColor(calendarListEntry.getForegroundColor())
                .selected(calendarListEntry.getSelected())
                .build();
    }

    private EventDto convertToEventDto(Event event) {
        EventDto.EventDtoBuilder builder = EventDto.builder()
                .id(event.getId())
                .summary(event.getSummary())
                .description(event.getDescription())
                .location(event.getLocation())
                .status(event.getStatus())
                .visibility(event.getVisibility())
                .recurrence(event.getRecurrence())
                .recurringEventId(event.getRecurringEventId())
                .hangoutLink(event.getHangoutLink())
                .htmlLink(event.getHtmlLink())
                .colorId(event.getColorId())
                .guestsCanInviteOthers(event.getGuestsCanInviteOthers())
                .guestsCanModify(event.getGuestsCanModify())
                .guestsCanSeeOtherGuests(event.getGuestsCanSeeOtherGuests())
                .privateCopy(event.getPrivateCopy())
                .transparency(event.getTransparency());

        if (event.getCreated() != null) {
            builder.created(event.getCreated().toString());
        }
        if (event.getUpdated() != null) {
            builder.updated(event.getUpdated().toString());
        }

        if (event.getStart() != null) {
            builder.start(convertToEventDateTime(event.getStart()));
        }
        if (event.getEnd() != null) {
            builder.end(convertToEventDateTime(event.getEnd()));
        }

        if (event.getAttendees() != null) {
            List<EventDto.EventAttendee> attendees = event.getAttendees().stream()
                    .map(this::convertToEventAttendeeDto)
                    .collect(Collectors.toList());
            builder.attendees(attendees);
        }

        return builder.build();
    }

    private EventDto.EventDateTime convertToEventDateTime(EventDateTime eventDateTime) {
        EventDto.EventDateTime.EventDateTimeBuilder builder = EventDto.EventDateTime.builder()
                .timeZone(eventDateTime.getTimeZone());

        if (eventDateTime.getDateTime() != null) {
            builder.dateTime(eventDateTime.getDateTime().toString());
        }
        if (eventDateTime.getDate() != null) {
            builder.date(eventDateTime.getDate().toString());
        }

        return builder.build();
    }

    private EventDto.EventAttendee convertToEventAttendeeDto(EventAttendee attendee) {
        return EventDto.EventAttendee.builder()
                .email(attendee.getEmail())
                .displayName(attendee.getDisplayName())
                .optional(attendee.getOptional())
                .responseStatus(attendee.getResponseStatus())
                .comment(attendee.getComment())
                .additionalGuests(attendee.getAdditionalGuests())
                .organizer(attendee.getOrganizer())
                .self(attendee.getSelf())
                .resource(attendee.getResource())
                .build();
    }

    private EventAttendee convertToGoogleEventAttendee(EventDto.EventAttendee attendee) {
        EventAttendee googleAttendee = new EventAttendee();
        googleAttendee.setEmail(attendee.getEmail());
        googleAttendee.setDisplayName(attendee.getDisplayName());
        googleAttendee.setOptional(attendee.getOptional());
        googleAttendee.setResponseStatus(attendee.getResponseStatus());
        googleAttendee.setComment(attendee.getComment());
        googleAttendee.setAdditionalGuests(attendee.getAdditionalGuests());
        return googleAttendee;
    }
}
