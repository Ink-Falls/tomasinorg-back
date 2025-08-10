package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private String id;
    private String summary;
    private String description;
    private String location;
    private EventDateTime start;
    private EventDateTime end;
    private List<EventAttendee> attendees;
    private String status;
    private String visibility;
    private List<String> recurrence;
    private String recurringEventId;
    private String hangoutLink;
    private String htmlLink;
    private String created;
    private String updated;
    private String colorId;
    private Boolean guestsCanInviteOthers;
    private Boolean guestsCanModify;
    private Boolean guestsCanSeeOtherGuests;
    private Boolean privateCopy;
    private String transparency;
    private List<EventReminder> reminders;
    private String organizer;
    private String creator;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDateTime {
        private String dateTime;
        private String date;
        private String timeZone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventAttendee {
        private String email;
        private String displayName;
        private Boolean optional;
        private String responseStatus;
        private String comment;
        private Integer additionalGuests;
        private Boolean organizer;
        private Boolean self;
        private Boolean resource;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventReminder {
        private String method;
        private Integer minutes;
    }
}
