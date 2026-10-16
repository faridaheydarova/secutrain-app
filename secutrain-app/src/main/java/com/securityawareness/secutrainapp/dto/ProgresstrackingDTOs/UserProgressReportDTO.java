package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressReportDTO {
    private Long userId;
    private String username;
    private Integer totalAssignments;
    private Integer completedAssignments;
    private Integer inProgressAssignments;
    private Integer notStartedAssignments;
    private Double overallCompletionRate;
    private Long totalWatchedDuration;
    private String totalWatchedDurationFormatted;
    private Double averageCompletionPercentage;
    private Integer averageAttemptsPerVideo;
    private LocalDateTime lastActivityAt;
    private List<CampaignProgressDTO> campaignProgress;
}
