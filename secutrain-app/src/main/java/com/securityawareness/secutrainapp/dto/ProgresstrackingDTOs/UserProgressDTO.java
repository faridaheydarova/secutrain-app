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
public class UserProgressDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private Integer completedVideos;
    private Integer totalVideos;
    private Double completionPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long timeSpentSeconds;
}
