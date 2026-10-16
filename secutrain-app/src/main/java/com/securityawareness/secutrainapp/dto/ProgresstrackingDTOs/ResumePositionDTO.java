package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumePositionDTO {
    private Integer lastWatchedPosition;
    private Integer watchedDuration;
    private Integer totalDuration;
    private Double playbackSpeed;
    private String resumeMessage;
}
