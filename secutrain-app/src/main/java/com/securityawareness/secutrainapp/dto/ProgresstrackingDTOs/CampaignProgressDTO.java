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
public class CampaignProgressDTO {
    private Long campaignId;
    private String campaignName;
    private Long userId;
    private String username;
    private Integer totalVideos;
    private Integer completedVideos;
    private Integer inProgressVideos;
    private Integer notStartedVideos;
    private Double completionPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<VideoProgressDTO> videoProgress;
}
