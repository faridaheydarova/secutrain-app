package com.securityawareness.secutrainapp.dto.VideomanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVideoRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    @Min(value = 1)
    private Integer durationSeconds;
}
