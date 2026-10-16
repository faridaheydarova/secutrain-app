package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressSummaryDTO {
    private Long assignmentId;
    private Long campaignId;
    private String campaignName;
    private Long userId;
    private String username;
    private Integer totalVideos;
    private Integer watchedVideos;
    private Integer completedVideos;
    private Double overallProgress;
    private LocalDateTime campaignStartedAt;
    private LocalDateTime campaignCompletedAt;
    private Long totalTimeSpent;
    private String totalTimeSpentFormatted;
}
