package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDto {
    private String id;
    private String summary;
    private String description;
    private String location;
    private String timeZone;
    private String accessRole;
    private Boolean primary;
    private String backgroundColor;
    private String foregroundColor;
    private Boolean selected;
}
