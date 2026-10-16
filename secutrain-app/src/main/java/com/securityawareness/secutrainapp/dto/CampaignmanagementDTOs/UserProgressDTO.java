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
public class UserProgressDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private Boolean isCompleted;
    private Integer completionPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer videosWatched;
    private Integer totalVideos;
}
