package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignReportDTO {
    private Long campaignId;
    private String campaignName;
    private Long totalAssigned;
    private Long completedAssignments;
    private Double completionRate;
    private Long totalVideos;
    private List<UserProgressDTO> userProgress;
}