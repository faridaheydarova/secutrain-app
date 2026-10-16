package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoProgressStatsDTO {
    private Long videoId;
    private String videoTitle;
    private Long totalViews;
    private Long completedViews;
    private Double completionRate;
    private Double averageWatchedDuration;
    private Integer averageAttempts;
    private Long totalWatchedDuration;
}
