package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {
    private String summary;
    private String description;
    private String location;
    private EventDto.EventDateTime start;
    private EventDto.EventDateTime end;
    private java.util.List<EventDto.EventAttendee> attendees;
    private String visibility;
    private java.util.List<String> recurrence;
    private Boolean guestsCanInviteOthers;
    private Boolean guestsCanModify;
    private Boolean guestsCanSeeOtherGuests;
    private String transparency;
    private java.util.List<EventDto.EventReminder> reminders;
    private String colorId;
    private String status;
}
