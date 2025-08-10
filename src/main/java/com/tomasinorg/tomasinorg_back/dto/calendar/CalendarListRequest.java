package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarListRequest {
    private Integer maxResults;
    private String pageToken;
    private Boolean showDeleted;
    private Boolean showHidden;
    private String syncToken;
}
