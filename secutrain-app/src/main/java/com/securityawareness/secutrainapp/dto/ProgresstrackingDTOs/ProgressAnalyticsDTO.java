package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressAnalyticsDTO {
    private Long totalUsers;
    private Long usersStarted;
    private Long usersCompleted;
    private Double averageCompletionRate;
    private Long totalVideosWatched;
    private Double averageWatchDuration;
    private Integer averageAttemptsPerVideo;
    private Long totalTimeSpent;
    private List<String> topPerformers;
    private List<String> atRiskUsers;
}
