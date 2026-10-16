package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignVideoDTO {
    private Long videoId;
    private String title;
    private String description;
    private Integer durationSeconds;
    private Integer sequenceOrder;
    private String thumbnail;
}