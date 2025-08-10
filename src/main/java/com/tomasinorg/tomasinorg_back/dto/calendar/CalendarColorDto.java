package com.tomasinorg.tomasinorg_back.dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarColorDto {
    private String kind;
    private String updated;
    private java.util.Map<String, CalendarColorInfo> calendar;
    private java.util.Map<String, CalendarColorInfo> event;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarColorInfo {
        private String background;
        private String foreground;
    }
}
