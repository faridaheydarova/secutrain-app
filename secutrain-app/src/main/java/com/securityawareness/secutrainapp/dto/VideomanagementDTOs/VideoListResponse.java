package com.securityawareness.secutrainapp.dto.VideomanagementDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoListResponse {
    private List<VideoDTO> videos;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
