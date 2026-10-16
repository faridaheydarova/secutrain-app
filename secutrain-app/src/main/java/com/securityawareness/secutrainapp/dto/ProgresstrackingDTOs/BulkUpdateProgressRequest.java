package com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUpdateProgressRequest {
    @NotEmpty(message = "Progress updates cannot be empty")
    private List<ProgressUpdateItem> updates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressUpdateItem {
        @NotNull
        private Long assignmentId;

        @NotNull
        private Long videoId;

        @Min(value = 0)
        private Integer watchedDuration;
    }
}
