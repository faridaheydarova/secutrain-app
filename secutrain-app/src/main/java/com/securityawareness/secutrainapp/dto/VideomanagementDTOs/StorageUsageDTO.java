package com.securityawareness.secutrainapp.dto.VideomanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageUsageDTO {
    private Long totalUsed;
    private String totalUsedFormatted;
    private Long videosStorage;
    private String videosStorageFormatted;
    private Long documentsStorage;
    private String documentsStorageFormatted;
    private Long thumbnailsStorage;
    private String thumbnailsStorageFormatted;
    private Double percentageUsed;
}
