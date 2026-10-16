package com.securityawareness.secutrainapp.service;
import com.securityawareness.secutrainapp.dto.ProgresstrackingDTOs.*;
import com.securityawareness.secutrainapp.model.*;
import com.securityawareness.secutrainapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressTrackingService {

    private final ProgressTrackingRepository progressRepository;
    private final CampaignAssignmentRepository assignmentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final AuditService auditService;

    @Transactional
    public ProgressDTO startVideo(
            Long assignmentId,
            Long videoId,
            Long userId) {
        CampaignAssignment assignment = getOwnedAssignment(assignmentId, userId);
        Video video = videoRepository.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        ProgressTracking progress = progressRepository.findByAssignmentIdAndVideoId(
                        assignmentId,
                        videoId)
                .orElse(null);
        if (progress == null) {
            progress = ProgressTracking.builder()
                    .assignment(assignment)
                    .video(video)
                    .user(user)
                    .status("IN_PROGRESS")
                    .watchedDuration(0)
                    .attempts(1)
                    .isCompleted(false)
                    .lastWatchedPosition(0)
                    .playbackSpeed(1.0)
                    .startedAt(LocalDateTime.now())
                    .build();

        } else if ("NOT_STARTED".equals(progress.getStatus())) {
            progress.setStatus("IN_PROGRESS");
            progress.setStartedAt(LocalDateTime.now());

            if (progress.getAttempts() == null ||
                    progress.getAttempts() == 0) {
                progress.setAttempts(1);
            }
        }
        progressRepository.save(progress);
        return toDTO(progress);}

    @Transactional
    public ProgressDTO updateProgress(Long assignmentId, Long videoId, Long userId, Integer watchedDuration, Integer lastPosition, Double playbackSpeed) {
        ProgressTracking progress = progressRepository
                .findByAssignment_IdAndVideo_IdAndUser_Id(assignmentId, videoId, userId)
                .orElseThrow(() -> new RuntimeException("Progress record not found"));

        if (Boolean.TRUE.equals(progress.getIsCompleted())) {
            return toDTO(progress);}
        Integer videoDuration = progress.getVideo().getDurationSeconds();
        if (videoDuration == null || videoDuration <= 0) {
            throw new RuntimeException("Video duration is invalid");}
        if (watchedDuration != null) {
            int safeWatchedDuration = Math.min(watchedDuration, videoDuration);
            int currentWatchedDuration = progress.getWatchedDuration() != null ? progress.getWatchedDuration() : 0;

            if (safeWatchedDuration > currentWatchedDuration) {
                progress.setWatchedDuration(safeWatchedDuration);
            }
        }
        if (lastPosition != null) {
            int safeLastPosition = Math.min(lastPosition, videoDuration);
            progress.setLastWatchedPosition(safeLastPosition);}

        if (playbackSpeed != null) {progress.setPlaybackSpeed(playbackSpeed);}

        progressRepository.save(progress);

        log.debug("Progress updated: assignment={}, video={}, watched={}",
                assignmentId, videoId, progress.getWatchedDuration());

        return toDTO(progress);
    }

    @Transactional
    public ProgressDTO completeVideo(Long assignmentId, Long videoId, Long userId, Integer totalWatchedDuration) {
        CampaignAssignment ownedAssignment = getOwnedAssignment(assignmentId, userId);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        ProgressTracking progress = progressRepository
                .findByAssignmentAndVideo(ownedAssignment, video)
                .orElseThrow(() -> new RuntimeException("Progress record not found"));

        if (Boolean.TRUE.equals(progress.getIsCompleted())) {
            return toDTO(progress);
        }

        int currentWatchedDuration = progress.getWatchedDuration() != null
                ? progress.getWatchedDuration()
                : 0;

        if (totalWatchedDuration != null && totalWatchedDuration > currentWatchedDuration) {
            currentWatchedDuration = totalWatchedDuration;
        }

        int videoDuration = video.getDurationSeconds() != null
                ? video.getDurationSeconds()
                : 0;

        if (videoDuration <= 0) {
            throw new RuntimeException("Video duration is invalid");
        }

        int safeWatchedDuration = Math.min(currentWatchedDuration, videoDuration);
        double completionPercentage = safeWatchedDuration * 100.0 / videoDuration;

        if (completionPercentage < 90) {
            throw new RuntimeException("Video must be watched at least 90% before completion");
        }

        progress.setWatchedDuration(safeWatchedDuration);
        progress.setStatus("COMPLETED");
        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        progressRepository.save(progress);

        log.info("Video completed: assignment={}, video={}", assignmentId, videoId);
        auditService.logAction(userId, "COMPLETE_VIDEO", "PROGRESS", videoId, "Video completed");

        return toDTO(progress);
    }

    @Transactional(readOnly = true)
    public ProgressDTO getProgress(Long assignmentId, Long videoId, Long userId) {
        CampaignAssignment ownedAssignment = getOwnedAssignment(assignmentId, userId);
        CampaignAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        ProgressTracking progress = progressRepository
                .findByAssignmentAndVideo(assignment, video)
                .orElseThrow(() -> new RuntimeException("Progress record not found"));
        return toDTO(progress);
    }

    @Transactional(readOnly = true)
    public List<ProgressDTO> getUserProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return progressRepository.findByUser(user).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgressDTO> getAssignmentProgress(Long assignmentId) {
        CampaignAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return progressRepository.findByAssignment(assignment).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CampaignProgressDTO getUserCampaignProgress(Long userId, Long campaignId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        List<ProgressTracking> progressList = progressRepository.findUserCampaignProgress(userId, campaignId);
        int totalVideos = campaign.getVideos().size();
        long completedVideos = progressList.stream().filter(ProgressTracking::getIsCompleted).count();
        long inProgressVideos = progressList.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();
        double completionRate = totalVideos > 0 ? (completedVideos * 100.0) / totalVideos : 0;
        List<VideoProgressDTO> videoProgress = progressList.stream()
                .map(p -> VideoProgressDTO.builder()
                        .videoId(p.getVideo().getId())
                        .videoTitle(p.getVideo().getTitle())
                        .durationSeconds(p.getVideo().getDurationSeconds())
                        .watchedDuration(p.getWatchedDuration())
                        .completionPercentage(p.getCompletionPercentage())
                        .status(p.getStatus())
                        .isCompleted(p.getIsCompleted())
                        .attempts(p.getAttempts())
                        .build())
                .collect(Collectors.toList());
        return CampaignProgressDTO.builder()
                .campaignId(campaignId)
                .userId(userId)
                .username(user.getUsername())
                .totalVideos(totalVideos)
                .completedVideos((int) completedVideos)
                .inProgressVideos((int) inProgressVideos)
                .notStartedVideos(totalVideos - (int) completedVideos - (int) inProgressVideos)
                .completionPercentage(completionRate)
                .videoProgress(videoProgress)
                .build();
    }

    @Transactional(readOnly = true)
    public UserProgressReportDTO getUserProgressReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ProgressTracking> allProgress = progressRepository.findByUser(user);
        long completedCount = allProgress.stream().filter(ProgressTracking::getIsCompleted).count();
        long inProgressCount = allProgress.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();
        long totalWatchedDuration = allProgress.stream()
                .mapToLong(p -> p.getWatchedDuration() != null ? p.getWatchedDuration() : 0)
                .sum();
        double avgCompletion = allProgress.isEmpty() ? 0 :
                allProgress.stream().mapToDouble(ProgressTracking::getCompletionPercentage).average().orElse(0);
        return UserProgressReportDTO.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalAssignments((int) assignmentRepository.findByUser(user).size())
                .completedAssignments((int) completedCount)
                .inProgressAssignments((int) inProgressCount)
                .notStartedAssignments((int) (assignmentRepository.findByUser(user).size() - completedCount - inProgressCount))
                .overallCompletionRate((completedCount * 100.0) / Math.max(1, allProgress.size()))
                .totalWatchedDuration(totalWatchedDuration)
                .averageCompletionPercentage(avgCompletion)
                .lastActivityAt(allProgress.stream().map(ProgressTracking::getUpdatedAt).max(LocalDateTime::compareTo).orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public CampaignProgressReportDTO getCampaignProgressReport(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        List<ProgressTracking> allProgress = progressRepository.findCampaignProgress(campaignId);
        long completedCount = allProgress.stream().filter(ProgressTracking::getIsCompleted).count();
        long inProgressCount = allProgress.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();
        long totalWatchedDuration = allProgress.stream()
                .mapToLong(p -> p.getWatchedDuration() != null ? p.getWatchedDuration() : 0)
                .sum();
        List<User> uniqueUsers = allProgress.stream().map(ProgressTracking::getUser).distinct().collect(Collectors.toList());
        return CampaignProgressReportDTO.builder()
                .campaignId(campaignId)
                .campaignName(campaign.getUsername())
                .totalAssigned(uniqueUsers.size())
                .completedAssignments((int) completedCount)
                .inProgressAssignments((int) inProgressCount)
                .notStartedAssignments(Math.max(0, uniqueUsers.size() - (int) completedCount - (int) inProgressCount))
                .completionRate((completedCount * 100.0) / Math.max(1, allProgress.size()))
                .totalWatchedDuration(totalWatchedDuration)
                .averageWatchedPerUser(uniqueUsers.isEmpty() ? 0 : (double) totalWatchedDuration / uniqueUsers.size())
                .build();
    }

    @Transactional(readOnly = true)
    public Double getCampaignCompletionRate(Long campaignId) {
        Double rate = progressRepository.getCampaignCompletionRate(campaignId);
        return rate != null ? rate * 100 : 0.0;
    }

    @Transactional(readOnly = true)
    public long getCompletedVideoCount(Long userId, Long campaignId) {
        return progressRepository.countCompletedVideosInCampaign(userId, campaignId);
    }

    @Transactional(readOnly = true)
    public Double getAverageCompletionPercentage(Long userId) {
        Double avg = progressRepository.getAverageCompletionPercentage(userId);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public Long getTotalWatchedByUser(Long userId) {
        Long total = progressRepository.getTotalWatchedByUser(userId);
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public Double getAverageWatchedDuration(Long videoId) {
        Double avg = progressRepository.getAverageWatchedDuration(videoId);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public ResumePositionDTO getResumePosition(
            Long assignmentId,
            Long videoId,
            Long userId) {

        CampaignAssignment assignment =
                getOwnedAssignment(assignmentId, userId);

        ProgressTracking progress = progressRepository
                .findByAssignmentIdAndVideoId(
                        assignment.getId(),
                        videoId)
                .orElse(null);

        if (progress == null) {
            return ResumePositionDTO.builder()
                    .lastWatchedPosition(0)
                    .watchedDuration(0)
                    .playbackSpeed(1.0)
                    .resumeMessage("Start from beginning")
                    .build();
        }

        return ResumePositionDTO.builder()
                .lastWatchedPosition(
                        progress.getLastWatchedPosition())
                .watchedDuration(
                        progress.getWatchedDuration())
                .totalDuration(
                        progress.getVideo()
                                .getDurationSeconds())
                .playbackSpeed(
                        progress.getPlaybackSpeed())
                .resumeMessage(
                        progress.getLastWatchedPosition() != null &&
                                progress.getLastWatchedPosition() > 0
                                ? "Resume from " +
                                formatSeconds(
                                        progress.getLastWatchedPosition())
                                : "Start from beginning")
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProgressTracking> getAbandonedProgress(Integer daysInactive) {
        LocalDateTime thresholdDate = LocalDateTime.now().minus(daysInactive, ChronoUnit.DAYS);
        return progressRepository.findAbandonedProgress(thresholdDate);
    }

    private ProgressDTO toDTO(ProgressTracking progress) {
        return ProgressDTO.builder()
                .id(progress.getId())
                .assignmentId(progress.getAssignment().getId())
                .videoId(progress.getVideo().getId())
                .videoTitle(progress.getVideo().getTitle())
                .userId(progress.getUser().getId())
                .username(progress.getUser().getUsername())
                .status(progress.getStatus())
                .isCompleted(progress.getIsCompleted())
                .watchedDuration(progress.getWatchedDuration())
                .watchedDurationFormatted(progress.getWatchedDurationFormatted())
                .lastWatchedPosition(progress.getLastWatchedPosition())
                .playbackSpeed(progress.getPlaybackSpeed())
                .completionPercentage(progress.getCompletionPercentage())
                .attempts(progress.getAttempts())
                .timeSpentSeconds(progress.getTimeSpent())
                .timeRemainingSeconds(progress.getTimeRemainingSeconds())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }

    private String formatSeconds(Integer seconds) {
        if (seconds == null || seconds == 0) return "00:00";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private CampaignAssignment getOwnedAssignment(
            Long assignmentId,
            Long userId) {
        CampaignAssignment assignment = assignmentRepository
                .findById(assignmentId)
                .orElseThrow(() ->
                        new RuntimeException("Assignment not found"));
        if (assignment.getUser() == null ||
                !assignment.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You are not allowed to access this assignment");
        }
        return assignment;
    }
}
