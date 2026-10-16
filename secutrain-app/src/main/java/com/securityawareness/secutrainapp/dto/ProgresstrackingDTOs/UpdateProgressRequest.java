package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProgressRequest {
    @Min(value = 0, message = "Watched duration must be >= 0")
    private Integer watchedDuration;

    @Min(value = 0, message = "Last position must be >= 0")
    private Integer lastWatchedPosition;

    @DecimalMin(value = "0.25", message = "Playback speed must be >= 0.25")
    @DecimalMax(value = "2.0", message = "Playback speed must be <= 2.0")
    private Double playbackSpeed;
}
