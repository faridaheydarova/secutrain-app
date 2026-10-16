package com.securityawareness.secutrainapp.controller;

import com.securityawareness.secutrainapp.dto.VideomanagementDTOs.BulkDeleteVideosRequest;
import com.securityawareness.secutrainapp.dto.VideomanagementDTOs.UpdateVideoRequest;
import com.securityawareness.secutrainapp.dto.VideomanagementDTOs.VideoDTO;
import com.securityawareness.secutrainapp.security.jwt.UserDetailsImpl;
import com.securityawareness.secutrainapp.service.VideoService;
import com.securityawareness.secutrainapp.util.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/videos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Video Management", description = "Admin APIs for video management")
@SecurityRequirement(name = "bearerAuth")

public class VideoController {
    private final VideoService videoService;
    private final FileUploadUtil fileUploadUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload new video", description = "Upload a video file with metadata")
    @ApiResponse(responseCode = "201", description = "Video uploaded successfully")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "durationSeconds", required = false) Integer durationSeconds,
            Authentication authentication) throws IOException {
        log.info("Uploading video: {}", title);
        Long userId = getUserIdFromAuthentication(authentication);
        VideoDTO uploadedVideo = videoService.uploadVideo(file, title, description, durationSeconds, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Video uploaded successfully");
        response.put("video", uploadedVideo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all videos", description = "Retrieve all videos with pagination")
    @ApiResponse(responseCode = "200", description = "Videos retrieved successfully")
    public ResponseEntity<Page<VideoDTO>> getAllVideos(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all videos - page: {}, size: {}", page, size);
        Page<VideoDTO> videos = videoService.getAllVideos(page, size);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get video by ID", description = "Retrieve specific video details")
    @ApiResponse(responseCode = "200", description = "Video found")
    @ApiResponse(responseCode = "404", description = "Video not found")
    public ResponseEntity<VideoDTO> getVideoById(@Parameter(description = "Video ID") @PathVariable Long id) {
        log.info("Fetching video with ID: {}", id);
        VideoDTO video = videoService.getVideoById(id);
        return ResponseEntity.ok(video);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update video metadata", description = "Update title, description, duration")
    @ApiResponse(responseCode = "200", description = "Video updated successfully")
    public ResponseEntity<Map<String, Object>> updateVideo(
            @Parameter(description = "Video ID") @PathVariable Long id,
            @Valid @RequestBody UpdateVideoRequest request,
            Authentication authentication) {
        log.info("Updating video with ID: {}", id);
        Long userId = getUserIdFromAuthentication(authentication);
        VideoDTO updatedVideo = videoService.updateVideoMetadata(id, request.getTitle(), request.getDescription(), request.getDurationSeconds(), userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Video updated successfully");
        response.put("video", updatedVideo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete video", description = "Delete video and associated files")
    @ApiResponse(responseCode = "200", description = "Video deleted successfully")
    public ResponseEntity<Map<String, String>> deleteVideo(
            @Parameter(description = "Video ID") @PathVariable Long id,
            Authentication authentication) throws IOException {
        log.info("Deleting video with ID: {}", id);
        Long userId = getUserIdFromAuthentication(authentication);
        videoService.deleteVideo(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Video deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk/delete")
    @Operation(summary = "Bulk delete videos", description = "Delete multiple videos at once")
    @ApiResponse(responseCode = "200", description = "Videos deleted")
    public ResponseEntity<Map<String, Object>> bulkDeleteVideos(
            @Valid @RequestBody BulkDeleteVideosRequest request,
            Authentication authentication) throws IOException {
        log.info("Bulk deleting {} videos", request.getVideoIds().size());
        Long userId = getUserIdFromAuthentication(authentication);
        videoService.bulkDeleteVideos(request.getVideoIds(), userId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Videos deleted successfully");
        response.put("deletedCount", request.getVideoIds().size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/stream")
    @Operation(summary = "Stream video", description = "Stream video file for playback")
    @ApiResponse(responseCode = "200", description = "Video stream")
    public ResponseEntity<byte[]> streamVideo(@Parameter(description = "Video ID") @PathVariable Long id) throws IOException {
        log.info("Streaming video with ID: {}", id);
        VideoDTO video = videoService.getVideoById(id);
        byte[] content = videoService.streamVideo(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getTitle() + ".mp4\"")
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .contentLength(content.length)
                .body(content);
    }

    @GetMapping("/active/list")
    @Operation(summary = "Get active videos", description = "Retrieve all active videos")
    @ApiResponse(responseCode = "200", description = "Videos retrieved")
    public ResponseEntity<List<VideoDTO>> getActiveVideos() {
        log.info("Fetching active videos");
        List<VideoDTO> videos = videoService.getActiveVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/search/title")
    @Operation(summary = "Search videos by title", description = "Find videos by title")
    @ApiResponse(responseCode = "200", description = "Search results")
    public ResponseEntity<List<VideoDTO>> searchByTitle(@Parameter(description = "Search term") @RequestParam String title) {
        log.info("Searching videos with title: {}", title);
        List<VideoDTO> videos = videoService.searchByTitle(title);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/search/description")
    @Operation(summary = "Search videos by description")
    @ApiResponse(responseCode = "200", description = "Search results")
    public ResponseEntity<List<VideoDTO>> searchByDescription(@RequestParam String keyword) {
        log.info("Searching videos with keyword: {}", keyword);
        List<VideoDTO> videos = videoService.searchByDescription(keyword);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/filter/type")
    @Operation(summary = "Filter videos by type", description = "Find videos by file type")
    @ApiResponse(responseCode = "200", description = "Videos retrieved")
    public ResponseEntity<List<VideoDTO>> filterByType(@Parameter(description = "File type (mp4, avi, etc)") @RequestParam String fileType) {
        log.info("Finding videos with type: {}", fileType);
        List<VideoDTO> videos = videoService.findVideosByType(fileType);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/trending/most-viewed")
    @Operation(summary = "Get most viewed videos", description = "Top 10 most viewed videos")
    @ApiResponse(responseCode = "200", description = "Videos retrieved")
    public ResponseEntity<List<VideoDTO>> getMostViewedVideos() {
        log.info("Fetching most viewed videos");
        List<VideoDTO> videos = videoService.getMostViewedVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/trending/latest")
    @Operation(summary = "Get latest videos", description = "Recently uploaded videos")
    @ApiResponse(responseCode = "200", description = "Videos retrieved")
    public ResponseEntity<List<VideoDTO>> getLatestVideos() {
        log.info("Fetching latest videos");
        List<VideoDTO> videos = videoService.getLatestVideos();
        return ResponseEntity.ok(videos);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate video", description = "Make video active")
    @ApiResponse(responseCode = "200", description = "Video activated")
    public ResponseEntity<Map<String, String>> activateVideo(@PathVariable Long id, Authentication authentication) {
        log.info("Activating video: {}", id);
        Long userId = getUserIdFromAuthentication(authentication);
        videoService.activateVideo(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Video activated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate video", description = "Make video inactive")
    @ApiResponse(responseCode = "200", description = "Video deactivated")
    public ResponseEntity<Map<String, String>> deactivateVideo(@PathVariable Long id, Authentication authentication) {
        log.info("Deactivating video: {}", id);
        Long userId = getUserIdFromAuthentication(authentication);
        videoService.deactivateVideo(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Video deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/overview")
    @Operation(summary = "Get video statistics", description = "Overall video stats")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    public ResponseEntity<Map<String, Object>> getVideoStats() {
        log.info("Fetching video statistics");
        long totalVideos = videoService.getTotalVideos();
        long activeVideos = videoService.getActiveVideosCount();
        long totalStorage = videoService.getTotalStorageUsed();
        long totalViews = videoService.getTotalViewCount();
        Integer avgDuration = videoService.getAverageDuration();
        long totalDuration = videoService.getTotalDuration();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVideos", totalVideos);
        stats.put("activeVideos", activeVideos);
        stats.put("inactiveVideos", totalVideos - activeVideos);
        stats.put("totalStorage", totalStorage);
        stats.put("totalStorageFormatted", fileUploadUtil.getFileSizeFormatted(totalStorage));
        stats.put("totalViews", totalViews);
        stats.put("averageDuration", avgDuration);
        stats.put("totalDuration", totalDuration);
        return ResponseEntity.ok(stats);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
