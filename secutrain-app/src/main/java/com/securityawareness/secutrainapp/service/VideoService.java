package com.securityawareness.secutrainapp.service;

import com.securityawareness.secutrainapp.dto.VideomanagementDTOs.VideoDTO;
import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.model.Video;
import com.securityawareness.secutrainapp.repository.UserRepository;
import com.securityawareness.secutrainapp.repository.VideoRepository;
import com.securityawareness.secutrainapp.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final FileUploadUtil fileUploadUtil;
    private final AuditService auditService;

    @Transactional
    public VideoDTO uploadVideo(MultipartFile file, String title, String description, Integer durationSeconds, Long uploadedBy) throws IOException {
        fileUploadUtil.validateVideoFile(file);
        User uploader = userRepository.findById(uploadedBy).orElseThrow(() -> new RuntimeException("User not found"));
        String filePath = fileUploadUtil.saveVideoFile(file);
        String fileType = fileUploadUtil.getFileExtension(file.getOriginalFilename());
        Video video = Video.builder()
                .title(title)
                .description(description)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType(fileType)
                .durationSeconds(durationSeconds)
                .createdBy(uploader)
                .isActive(true)
                .viewCount(0L)
                .build();
        videoRepository.save(video);
        log.info("Video uploaded: {} by {}", video.getTitle(), uploader.getUsername());
        auditService.logAction(uploadedBy, "UPLOAD_VIDEO", "VIDEO", video.getId(), "Video uploaded: " + video.getTitle());
        return toDTO(video);
    }

    @Transactional(readOnly = true)
    public Page<VideoDTO> getAllVideos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return videoRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public VideoDTO getVideoById(Long id) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        return toDTO(video);
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> getActiveVideos() {
        return videoRepository.findByIsActive(true).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> getVideosByCreator(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return videoRepository.findByCreatedBy(user).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public VideoDTO updateVideoMetadata(Long id, String title, String description, Integer durationSeconds, Long updatedBy) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        video.setTitle(title);
        video.setDescription(description);
        if (durationSeconds != null) {
            video.setDurationSeconds(durationSeconds);
        }
        videoRepository.save(video);
        log.info("Video metadata updated: {}", video.getTitle());
        auditService.logAction(updatedBy, "UPDATE_VIDEO", "VIDEO", id, "Video updated: " + video.getTitle());
        return toDTO(video);
    }

    @Transactional
    public void deleteVideo(Long id, Long deletedBy) throws IOException {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        try {
            fileUploadUtil.deleteFile(video.getFilePath());
        } catch (IOException e) {
            log.warn("Could not delete video file: {}", video.getFilePath(), e);
        }
        if (video.getThumbnailPath() != null) {
            try {
                fileUploadUtil.deleteFile(video.getThumbnailPath());
            } catch (IOException e) {
                log.warn("Could not delete thumbnail: {}", video.getThumbnailPath(), e);
            }
        }
        videoRepository.delete(video);
        log.info("Video deleted: {}", video.getTitle());
        auditService.logAction(deletedBy, "DELETE_VIDEO", "VIDEO", id, "Video deleted: " + video.getTitle());
    }

    @Transactional
    public void bulkDeleteVideos(List<Long> videoIds, Long deletedBy) throws IOException {
        for (Long videoId : videoIds) {
            deleteVideo(videoId, deletedBy);
        }
        log.info("Bulk deleted {} videos", videoIds.size());
    }

    @Transactional
    public byte[] streamVideo(Long id) throws IOException {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);
        log.debug("Streaming video: {} (view count: {})", video.getTitle(), video.getViewCount());
        return fileUploadUtil.readFile(video.getFilePath());
    }

    @Transactional
    public void recordVideoView(Long videoId) {
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);
        log.debug("Video view recorded: {}", videoId);
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> searchByTitle(String title) {
        return videoRepository.searchByTitle(title).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> searchByDescription(String keyword) {
        return videoRepository.searchByDescription(keyword).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> findVideosByType(String fileType) {
        return videoRepository.findByFileType(fileType).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> getMostViewedVideos() {
        return videoRepository.findMostViewedVideos().stream().limit(10).map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> getLatestVideos() {
        return videoRepository.findLatestVideos().stream().limit(10).map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoDTO> findVideosByDurationRange(Integer minDuration, Integer maxDuration) {
        List<Video> videos = videoRepository.findByDurationSecondsGreaterThan(minDuration);
        if (maxDuration != null) {
            videos = videos.stream().filter(v -> v.getDurationSeconds() <= maxDuration).collect(Collectors.toList());
        }
        return videos.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void activateVideo(Long id, Long updatedBy) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        video.setIsActive(true);
        videoRepository.save(video);
        log.info("Video activated: {}", video.getTitle());
        auditService.logAction(updatedBy, "ACTIVATE_VIDEO", "VIDEO", id, "Video activated: " + video.getTitle());
    }

    @Transactional
    public void deactivateVideo(Long id, Long updatedBy) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        video.setIsActive(false);
        videoRepository.save(video);
        log.info("Video deactivated: {}", video.getTitle());
        auditService.logAction(updatedBy, "DEACTIVATE_VIDEO", "VIDEO", id, "Video deactivated: " + video.getTitle());
    }

    @Transactional(readOnly = true)
    public long getTotalVideos() {
        return videoRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveVideosCount() {
        return videoRepository.countByIsActive(true);
    }

    @Transactional(readOnly = true)
    public long getTotalStorageUsed() {
        Long total = videoRepository.getTotalStorageUsed();
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public long getTotalViewCount() {
        return videoRepository.findAll().stream().mapToLong(Video::getViewCount).sum();
    }

    @Transactional(readOnly = true)
    public Integer getAverageDuration() {
        List<Video> videos = videoRepository.findAll();
        if (videos.isEmpty()) return 0;
        return (int) videos.stream().mapToInt(v -> v.getDurationSeconds() != null ? v.getDurationSeconds() : 0).average().orElse(0);
    }

    @Transactional(readOnly = true)
    public long getTotalDuration() {
        return videoRepository.findAll().stream().mapToLong(v -> v.getDurationSeconds() != null ? v.getDurationSeconds() : 0).sum();
    }

    private VideoDTO toDTO(Video video) {
        return VideoDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .filePath(video.getFilePath())
                .fileSize(video.getFileSize())
                .fileType(video.getFileType())
                .durationSeconds(video.getDurationSeconds())
                .durationFormatted(video.getDurationFormatted())
                .thumbnailPath(video.getThumbnailPath())
                .videoUrl(video.getVideoUrl())
                .isActive(video.getIsActive())
                .viewCount(video.getViewCount())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .createdByUsername(video.getCreatedBy().getUsername())
                .fileSizeFormatted(video.getFileSizeInMB())
                .build();
    }
}