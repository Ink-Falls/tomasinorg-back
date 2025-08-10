package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeBusyRequest {
    private String timeMin;
    private String timeMax;
    private String timeZone;
    private String groupExpansionMax;
    private String calendarExpansionMax;
    private java.util.List<FreeBusyRequestItem> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FreeBusyRequestItem {
        private String id;
    }
}
