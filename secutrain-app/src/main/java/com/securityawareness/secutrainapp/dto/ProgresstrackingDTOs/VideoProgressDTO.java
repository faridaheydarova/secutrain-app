package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoProgressDTO {
    private Long videoId;
    private String videoTitle;
    private Integer durationSeconds;
    private Integer watchedDuration;
    private Integer completionPercentage;
    private String status;
    private Boolean isCompleted;
    private Integer attempts;
}
