package com.securityawareness.secutrainapp.repository;
import com.securityawareness.secutrainapp.model.CampaignAssignment;
import com.securityawareness.secutrainapp.model.ProgressTracking;
import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressTrackingRepository extends JpaRepository<ProgressTracking, Long> {

    Optional<ProgressTracking> findByAssignmentAndVideo(CampaignAssignment assignment, Video video);

    Optional<ProgressTracking> findByAssignment_IdAndVideo_IdAndUser_Id(Long assignmentId, Long videoId, Long userId);

    List<ProgressTracking> findByUser(User user);

    List<ProgressTracking> findByAssignment(CampaignAssignment assignment);

    List<ProgressTracking> findByVideo(Video video);

    List<ProgressTracking> findByStatus(String status);

    List<ProgressTracking> findByIsCompleted(Boolean isCompleted);

    long countByStatus(String status);

    long countByIsCompleted(Boolean isCompleted);

    long countByUserAndIsCompleted(User user, Boolean isCompleted);

    long countByAssignmentAndStatus(CampaignAssignment assignment, String status);


    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.assignment.id = :assignmentId AND pt.video.id = :videoId")
    Optional<ProgressTracking> findByAssignmentIdAndVideoId(
            @Param("assignmentId") Long assignmentId,
            @Param("videoId") Long videoId);

    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.user.id = :userId AND pt.assignment.campaign.id = :campaignId")
    List<ProgressTracking> findUserCampaignProgress(
            @Param("userId") Long userId,
            @Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(pt) FROM ProgressTracking pt WHERE pt.user.id = :userId AND pt.assignment.campaign.id = :campaignId AND pt.isCompleted = true")
    long countCompletedVideosInCampaign(
            @Param("userId") Long userId,
            @Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(pt) FROM ProgressTracking pt WHERE pt.assignment.campaign.id = :campaignId AND pt.isCompleted = true")
    long countTotalCompletedInCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT AVG(pt.watchedDuration) FROM ProgressTracking pt WHERE pt.video.id = :videoId")
    Double getAverageWatchedDuration(@Param("videoId") Long videoId);

    @Query("SELECT AVG((CAST(pt.watchedDuration AS double) / pt.video.durationSeconds) * 100) " +
            "FROM ProgressTracking pt WHERE pt.user.id = :userId")
    Double getAverageCompletionPercentage(@Param("userId") Long userId);

    @Query("SELECT CAST(COUNT(CASE WHEN pt.isCompleted = true THEN 1 END) AS DOUBLE) / COUNT(pt) FROM ProgressTracking pt WHERE pt.assignment.campaign.id = :campaignId")
    Double getCampaignCompletionRate(@Param("campaignId") Long campaignId);

    @Query("SELECT SUM(pt.watchedDuration) FROM ProgressTracking pt WHERE pt.user.id = :userId")
    Long getTotalWatchedByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(pt.watchedDuration) FROM ProgressTracking pt WHERE pt.assignment.campaign.id = :campaignId")
    Long getTotalWatchedInCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.startedAt >= :startDate AND pt.startedAt <= :endDate")
    List<ProgressTracking> findProgressBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.completedAt IS NOT NULL ORDER BY pt.completedAt DESC")
    List<ProgressTracking> findRecentlyCompleted();

    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.assignment.campaign.id = :campaignId")
    List<ProgressTracking> findCampaignProgress(@Param("campaignId") Long campaignId);

    @Query("SELECT DISTINCT pt.user FROM ProgressTracking pt WHERE pt.assignment.campaign.id = :campaignId AND pt.isCompleted = true")
    List<User> findUsersWhoCompletedCampaign(@Param("campaignId") Long campaignId);

    @Query("SELECT COUNT(DISTINCT pt.video) FROM ProgressTracking pt WHERE pt.user.id = :userId AND pt.isCompleted = true")
    long countCompletedVideosByUser(@Param("userId") Long userId);

    @Query("SELECT AVG(pt.attempts) FROM ProgressTracking pt WHERE pt.user.id = :userId")
    Double getAverageAttemptsPerUser(@Param("userId") Long userId);

    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.watchedDuration > pt.video.durationSeconds ORDER BY pt.watchedDuration DESC")
    List<ProgressTracking> findVideosWatchedMultipleTimes();

    @Query("SELECT pt FROM ProgressTracking pt WHERE pt.status = 'IN_PROGRESS' AND pt.startedAt < :thresholdDate")
    List<ProgressTracking> findAbandonedProgress(@Param("thresholdDate") LocalDateTime thresholdDate);


}