package com.securityawareness.secutrainapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "progress_tracking", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "video_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private CampaignAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "watched_duration")
    private Integer watchedDuration = 0;

    @Column(name = "last_watched_position")
    private Integer lastWatchedPosition = 0;

    @Column(name = "playback_speed")
    private Double playbackSpeed = 1.0;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "NOT_STARTED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getCompletionPercentage() {
        if (video == null || video.getDurationSeconds() == null || video.getDurationSeconds() == 0) {
            return 0;
        }
        return (int) ((watchedDuration * 100L) / video.getDurationSeconds());
    }

    public Long getTimeSpent() {
        if (startedAt == null) {
            return 0L;
        }
        LocalDateTime end = completedAt != null ? completedAt : LocalDateTime.now();
        return Duration.between(startedAt, end).getSeconds();
    }

    public Boolean isVideoCompleted() {
        if (video == null || video.getDurationSeconds() == null) {
            return false;
        }
        return getCompletionPercentage() >= 95;
    }

    public String getStatusFormatted() {
        return status.replace("_", " ");
    }

    public Long getTimeRemainingSeconds() {
        if (video == null || video.getDurationSeconds() == null) {
            return 0L;
        }
        long remaining = video.getDurationSeconds() - watchedDuration;
        return Math.max(0, remaining);
    }

    public String getWatchedDurationFormatted() {
        int hours = watchedDuration / 3600;
        int minutes = (watchedDuration % 3600) / 60;
        int seconds = watchedDuration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}