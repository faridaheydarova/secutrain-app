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
public class ProgressDTO {
    private Long id;
    private Long assignmentId;
    private Long videoId;
    private String videoTitle;
    private Long userId;
    private String username;
    private String status;
    private Boolean isCompleted;
    private Integer watchedDuration;
    private String watchedDurationFormatted;
    private Integer lastWatchedPosition;
    private Double playbackSpeed;
    private Integer completionPercentage;
    private Integer attempts;
    private Long timeSpentSeconds;
    private Long timeRemainingSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
