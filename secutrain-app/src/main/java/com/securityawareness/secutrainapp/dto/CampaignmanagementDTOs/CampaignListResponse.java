package com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignListResponse {
    private List<CampaignDTO> campaigns;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
