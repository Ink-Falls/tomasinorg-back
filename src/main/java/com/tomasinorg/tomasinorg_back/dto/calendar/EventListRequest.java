package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListRequest {
    private String calendarId;
    private Integer maxResults;
    private String pageToken;
    private String timeMin;
    private String timeMax;
    private String q;
    private String orderBy;
    private Boolean singleEvents;
    private Boolean showDeleted;
    private String timeZone;
    private String updatedMin;
    private String syncToken;
}
