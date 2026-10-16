package com.securityawareness.secutrainapp.dto.VideomanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStatsDTO {
    private Long totalVideos;
    private Long activeVideos;
    private Long totalStorage;
    private String totalStorageFormatted;
    private Long videoViewCount;
    private Integer averageDuration;
    private Long totalDuration;
}