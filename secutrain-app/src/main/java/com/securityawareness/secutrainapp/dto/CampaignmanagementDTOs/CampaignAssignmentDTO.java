package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignAssignmentDTO {
    private Long id;
    private Long campaignId;
    private String campaignName;
    private Long userId;
    private String username;
    private Boolean isCompleted;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private Integer completionPercentage;
}


