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
public class CampaignProgressReportDTO {
    private Long campaignId;
    private String campaignName;
    private Integer totalAssigned;
    private Integer completedAssignments;
    private Integer inProgressAssignments;
    private Integer notStartedAssignments;
    private Double completionRate;
    private Long totalWatchedDuration;
    private Double averageWatchedPerUser;
    private Integer averageDurationPerVideo;
    private List<UserProgressDTO> userProgress;
}
