package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCampaignRequest {
    @NotBlank(message = "Campaign name is required")
    @Size(min = 3, max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;
}