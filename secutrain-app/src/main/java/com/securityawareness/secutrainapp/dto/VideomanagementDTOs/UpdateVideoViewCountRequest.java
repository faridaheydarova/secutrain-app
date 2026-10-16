package com.securityawareness.secutrainapp.dto.VideomanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVideoViewCountRequest {
    private Long videoId;
    private Boolean increment;
}
