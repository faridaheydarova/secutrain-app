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
public class BulkAssignCampaignRequest {
    @NotEmpty(message = "Campaign IDs cannot be empty")
    private List<Long> campaignIds;

    @NotEmpty(message = "User IDs cannot be empty")
    private List<Long> userIds;
}