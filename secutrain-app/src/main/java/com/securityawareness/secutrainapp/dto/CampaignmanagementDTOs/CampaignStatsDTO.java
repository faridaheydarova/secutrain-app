package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignStatsDTO {
    private Long totalCampaigns;
    private Long activeCampaigns;
    private Long completedCampaigns;
    private Double averageCompletionRate;
    private Long totalAssignments;
    private Long completedAssignments;
}