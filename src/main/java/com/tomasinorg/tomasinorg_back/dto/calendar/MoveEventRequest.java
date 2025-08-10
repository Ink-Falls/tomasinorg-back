package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.Data;

@Data
public class MoveEventRequest {
    private String destinationCalendarId;
}
