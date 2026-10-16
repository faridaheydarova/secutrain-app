package com.securityawareness.secutrainapp.controller;

import com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs.*;
import com.securityawareness.secutrainapp.security.jwt.UserDetailsImpl;
import com.securityawareness.secutrainapp.service.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campaign Management", description = "Admin APIs for campaign management")
@SecurityRequirement(name = "bearerAuth")
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public ResponseEntity<Page<CampaignDTO>> getAllCampaigns(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching all campaigns - page: {}, size: {}", page, size);
        Page<CampaignDTO> campaigns = campaignService.getAllCampaigns(page, size);

        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignDTO> getCampaignById(@PathVariable Long id, Authentication authentication) {
        log.info("Fetching campaign with ID: {}", id);
        Long userId = getUserIdFromAuthentication(authentication);
        CampaignDTO campaign = campaignService.getCampaignByIdForUser(id, userId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request,
            Authentication authentication) {

        log.info("Creating new campaign: {}", request.getName());

        Long userId = getUserIdFromAuthentication(authentication);
        CampaignDTO createdCampaign = campaignService.createCampaign(
                toCampaignDTO(request), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Campaign created successfully");
        response.put("campaign", createdCampaign);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")

    public ResponseEntity<Map<String, Object>> updateCampaign(
            @Parameter(description = "Campaign ID")
            @PathVariable Long id,
            @Valid @RequestBody UpdateCampaignRequest request,
            Authentication authentication) {

        log.info("Updating campaign with ID: {}", id);

        Long userId = getUserIdFromAuthentication(authentication);
        CampaignDTO updatedCampaign = campaignService.updateCampaign(
                id, toCampaignDTO(request), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Campaign updated successfully");
        response.put("campaign", updatedCampaign);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<Map<String, String>> deleteCampaign(
            @Parameter(description = "Campaign ID")
            @PathVariable Long id,
            Authentication authentication) {

        log.info("Deleting campaign with ID: {}", id);

        Long userId = getUserIdFromAuthentication(authentication);
        campaignService.deleteCampaign(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Campaign deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/assigned")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my assigned campaigns")
    public ResponseEntity<List<CampaignDTO>> getMyAssignedCampaigns(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<CampaignDTO> campaigns = campaignService.getUserAssignedCampaigns(userId);
        return ResponseEntity.ok(campaigns);
    }

    @PostMapping("/{id}/videos")
    public ResponseEntity<Map<String, Object>> addVideosCampaign(
            @Parameter(description = "Campaign ID")
            @PathVariable Long id,
            @Valid @RequestBody AddVideosToCampaignRequest request,
            Authentication authentication) {

        log.info("Adding videos to campaign {}: {}", id, request.getVideoIds());

        Long userId = getUserIdFromAuthentication(authentication);

        for (Long videoId : request.getVideoIds()) {
            campaignService.addVideoCampaign(id, videoId, userId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Videos added to campaign successfully");
        response.put("campaignId", id);
        response.put("videosAdded", request.getVideoIds().size());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{campaignId}/videos/{videoId}")
    public ResponseEntity<Map<String, String>> removeVideoFromCampaign(
            @Parameter(description = "Campaign ID")
            @PathVariable Long campaignId,
            @Parameter(description = "Video ID")
            @PathVariable Long videoId,
            Authentication authentication) {

        log.info("Removing video {} from campaign {}", videoId, campaignId);

        Long userId = getUserIdFromAuthentication(authentication);
        campaignService.removeVideoFromCampaign(campaignId, videoId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Video removed from campaign successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/assign")
    @Operation(
            summary = "Assign campaign to users",
            description = "Assign a campaign to one or multiple users"
    )
    @ApiResponse(responseCode = "200", description = "Campaign assigned successfully")
    public ResponseEntity<Map<String, Object>> assignCampaignToUsers(
            @Parameter(description = "Campaign ID")
            @PathVariable Long id,
            @Valid @RequestBody AssignCampaignRequest request,
            Authentication authentication) {

        log.info("Assigning campaign {} to users: {}", id, request.getUserIds());

        Long userId = getUserIdFromAuthentication(authentication);
        campaignService.assignCampaignToUsers(id, request.getUserIds(), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Campaign assigned to users successfully");
        response.put("campaignId", id);
        response.put("usersAssigned", request.getUserIds().size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active/list")
    @Operation(
            summary = "Get active campaigns",
            description = "Retrieve all active campaigns"
    )
    @ApiResponse(responseCode = "200", description = "Campaigns retrieved")
    public ResponseEntity<List<CampaignDTO>> getActiveCampaigns() {

        log.info("Fetching active campaigns");
        List<CampaignDTO> campaigns = campaignService.getActiveCampaigns();

        return ResponseEntity.ok(campaigns);
    }

    @GetMapping("/{id}/report")
    @Operation(
            summary = "Get campaign report",
            description = "Get detailed report of campaign completion and user progress"
    )
    @ApiResponse(responseCode = "200", description = "Report retrieved")
    public ResponseEntity<CampaignReportDTO> getCampaignReport(
            @Parameter(description = "Campaign ID")
            @PathVariable Long id) {

        log.info("Generating report for campaign: {}", id);

        CampaignReportDTO report = new CampaignReportDTO();

        return ResponseEntity.ok(report);
    }

    @GetMapping("/stats/overview")
    @Operation(
            summary = "Get campaign statistics",
            description = "Get overall campaign statistics"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    public ResponseEntity<CampaignStatsDTO> getCampaignStats() {

        log.info("Fetching campaign statistics");

        CampaignStatsDTO stats = CampaignStatsDTO.builder()
                .totalCampaigns(0L)
                .activeCampaigns(0L)
                .completedCampaigns(0L)
                .averageCompletionRate(0.0)
                .totalAssignments(0L)
                .completedAssignments(0L)
                .build();

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/bulk/assign")
    @Operation(
            summary = "Bulk assign campaigns",
            description = "Assign multiple campaigns to multiple users at once"
    )
    @ApiResponse(responseCode = "200", description = "Campaigns assigned")
    public ResponseEntity<Map<String, Object>> bulkAssignCampaigns(
            @Valid @RequestBody BulkAssignCampaignRequest request,
            Authentication authentication) {

        log.info("Bulk assigning campaigns: {} to users: {}",
                request.getCampaignIds(), request.getUserIds());

        Long userId = getUserIdFromAuthentication(authentication);

        for (Long campaignId : request.getCampaignIds()) {
            campaignService.assignCampaignToUsers(
                    campaignId, request.getUserIds(), userId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Campaigns assigned successfully");
        response.put("campaignsCount", request.getCampaignIds().size());
        response.put("usersCount", request.getUserIds().size());
        response.put("totalAssignments",
                request.getCampaignIds().size() * request.getUserIds().size());

        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUserId();
    }

    private CampaignDTO toCampaignDTO(CreateCampaignRequest request) {
        return CampaignDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    private CampaignDTO toCampaignDTO(UpdateCampaignRequest request) {
        return CampaignDTO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }
}
