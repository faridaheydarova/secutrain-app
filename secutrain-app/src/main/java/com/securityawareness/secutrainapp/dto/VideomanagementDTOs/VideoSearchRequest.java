package com.securityawareness.secutrainapp.dto.VideomanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoSearchRequest {
    private String title;
    private String fileType;
    private Integer minDuration;
    private Integer maxDuration;
    private Boolean isActive = true;
}
