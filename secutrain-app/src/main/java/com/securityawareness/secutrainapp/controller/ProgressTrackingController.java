package com.securityawareness.secutrainapp.controller;

import com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs.*;
import com.securityawareness.secutrainapp.model.ProgressTracking;
import com.securityawareness.secutrainapp.security.jwt.UserDetailsImpl;
import com.securityawareness.secutrainapp.service.ProgressTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Progress Tracking", description = "User APIs for progress tracking")
@SecurityRequirement(name = "bearerAuth")

public class ProgressTrackingController {

    private final ProgressTrackingService progressService;

    @PostMapping("/{assignmentId}/{videoId}/start")
    @Operation(summary = "Start video", description = "Mark video as started")
    public ResponseEntity<Map<String, Object>> startVideo(
            @Parameter(description = "Assignment ID") @PathVariable Long assignmentId,
            @Parameter(description = "Video ID") @PathVariable Long videoId,
            Authentication authentication) {
        log.info("Starting video: assignment={}, video={}", assignmentId, videoId);
        Long userId = getUserIdFromAuthentication(authentication);
        ProgressDTO progress = progressService.startVideo(assignmentId, videoId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Video started successfully");
        response.put("progress", progress);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{assignmentId}/{videoId}/update")
    @Operation(summary = "Update video progress", description = "Update watched duration, position, speed")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable Long assignmentId,
            @PathVariable Long videoId,
            @Valid @RequestBody UpdateProgressRequest request,
            Authentication authentication) {
        log.info("Updating progress: assignment={}, video={}", assignmentId, videoId);
        Long userId = getUserIdFromAuthentication(authentication);
        ProgressDTO progress = progressService.updateProgress(
                assignmentId, videoId, userId,
                request.getWatchedDuration(),
                request.getLastWatchedPosition(),
                request.getPlaybackSpeed());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Progress updated successfully");
        response.put("progress", progress);
        response.put("completionPercentage", progress.getCompletionPercentage());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/{videoId}/complete")
    @Operation(summary = "Complete video", description = "Mark video as completed")
    public ResponseEntity<Map<String, Object>> completeVideo(
            @PathVariable Long assignmentId,
            @PathVariable Long videoId,
            @Valid @RequestBody CompleteVideoRequest request,
            Authentication authentication) {
        log.info("Completing video: assignment={}, video={}", assignmentId, videoId);
        Long userId = getUserIdFromAuthentication(authentication);
        ProgressDTO progress = progressService.completeVideo(
                assignmentId, videoId, userId,
                request.getTotalWatchedDuration());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Video completed successfully");
        response.put("progress", progress);
        response.put("completionPercentage", progress.getCompletionPercentage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{assignmentId}/{videoId}")
    @Operation(summary = "Get video progress", description = "Get current progress for a video")
    public ResponseEntity<ProgressDTO> getProgress(
            @PathVariable Long assignmentId,
            @PathVariable Long videoId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Getting progress: assignment={}, video={}", assignmentId, videoId);
        ProgressDTO progress = progressService.getProgress(assignmentId, videoId, userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/my-progress/all")
    @Operation(summary = "Get all my progress", description = "Get user's all video progress")
    public ResponseEntity<List<ProgressDTO>> getUserProgress(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Fetching user progress: user={}", userId);
        List<ProgressDTO> progress = progressService.getUserProgress(userId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/campaign/{assignmentId}")
    @Operation(summary = "Get campaign progress", description = "Get all videos progress in campaign")
    public ResponseEntity<List<ProgressDTO>> getAssignmentProgress(
            @Parameter(description = "Assignment ID") @PathVariable Long assignmentId) {
        log.info("Fetching assignment progress: assignment={}", assignmentId);
        List<ProgressDTO> progress = progressService.getAssignmentProgress(assignmentId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/campaign/{campaignId}/details")
    @Operation(summary = "Get detailed campaign progress", description = "Detailed progress for all videos in campaign")
    public ResponseEntity<CampaignProgressDTO> getUserCampaignProgress(
            @PathVariable Long campaignId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Fetching campaign details: user={}, campaign={}", userId, campaignId);
        CampaignProgressDTO progress = progressService.getUserCampaignProgress(userId, campaignId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/report/my-report")
    @Operation(summary = "Get my progress report", description = "Comprehensive progress report")
    public ResponseEntity<UserProgressReportDTO> getUserProgressReport(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("Generating user report: user={}", userId);
        UserProgressReportDTO report = progressService.getUserProgressReport(userId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/admin/campaign/{campaignId}/report")
    @Operation(summary = "Get campaign progress report", description = "Admin: detailed campaign report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampaignProgressReportDTO> getCampaignProgressReport(@PathVariable Long campaignId) {
        log.info("Generating campaign report: campaign={}", campaignId);
        CampaignProgressReportDTO report = progressService.getCampaignProgressReport(campaignId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{assignmentId}/{videoId}/resume")
    public ResponseEntity<ResumePositionDTO> getResumePosition(
            @PathVariable Long assignmentId,
            @PathVariable Long videoId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        ResumePositionDTO resumePos = progressService.getResumePosition(
                        assignmentId,
                        videoId,
                        userId);
        return ResponseEntity.ok(resumePos);
    }

    @GetMapping("/admin/at-risk")
    @Operation(summary = "Get at-risk users", description = "Users with abandoned progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAtRiskUsers(
            @Parameter(description = "Days inactive") @RequestParam(defaultValue = "7") Integer daysInactive) {
        log.info("Fetching at-risk users: daysInactive={}", daysInactive);
        List<ProgressTracking> abandonedProgress = progressService.getAbandonedProgress(daysInactive);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "At-risk users retrieved");
        response.put("count", abandonedProgress.size());
        response.put("daysInactive", daysInactive);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/campaign/{campaignId}/completion-rate")
    @Operation(summary = "Get campaign completion rate", description = "Overall completion rate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCampaignCompletionRate(@PathVariable Long campaignId) {
        log.info("Getting completion rate: campaign={}", campaignId);
        Double rate = progressService.getCampaignCompletionRate(campaignId);
        Map<String, Object> response = new HashMap<>();
        response.put("campaignId", campaignId);
        response.put("completionRate", rate);
        response.put("completionPercentage", String.format("%.2f%%", rate));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "Get progress statistics", description = "Overall progress statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("Generating progress statistics");
        Map<String, Object> stats = new HashMap<>();
        return ResponseEntity.ok(stats);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}