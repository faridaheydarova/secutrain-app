package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddVideosToCampaignRequest {
    @NotEmpty(message = "Video IDs cannot be empty")
    private List<Long> videoIds;
}