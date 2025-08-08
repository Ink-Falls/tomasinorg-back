package com.tomasinorg.tomasinorg_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriveItem {
    
    private String id;
    private String name;
    private String mimeType;
    private boolean isFolder;
    private String webViewLink;
    private Long size;
    private String modifiedTime;
}
