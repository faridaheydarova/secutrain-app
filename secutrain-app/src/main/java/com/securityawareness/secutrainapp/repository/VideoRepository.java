package com.securityawareness.secutrainapp.repository;

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
public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByTitle(String title);

    List<Video> findByCreatedBy(User user);

    List<Video> findByIsActive(Boolean isActive);

    long countByIsActive(Boolean isActive);

    List<Video> findByCreatedAtAfter(LocalDateTime createdAt);

    @Query("SELECT v FROM Video v WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Video> searchByTitle(@Param("title") String title);

    @Query("SELECT v FROM Video v WHERE LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Video> searchByDescription(@Param("keyword") String keyword);

    @Query("SELECT v FROM Campaign c JOIN c.videos v WHERE c.id = :campaignId")
    List<Video> findVideosByCampaignId(@Param("campaignId") Long campaignId);

    List<Video> findByFileType(String fileType);

    @Query("SELECT COUNT(v) FROM Video v WHERE v.isActive = true")
    long countActiveVideos();

    @Query("SELECT v FROM Video v WHERE v.isActive = true ORDER BY v.viewCount DESC")
    List<Video> findMostViewedVideos();

    @Query("SELECT v FROM Video v WHERE v.isActive = true ORDER BY v.createdAt DESC")
    List<Video> findLatestVideos();

    @Query("SELECT SUM(v.fileSize) FROM Video v WHERE v.isActive = true")
    Long getTotalStorageUsed();

    @Query("SELECT v FROM Video v WHERE v.id NOT IN (SELECT cv.id FROM Campaign c JOIN c.videos cv WHERE c.id = :campaignId)")
    List<Video> findVideosNotInCampaign(@Param("campaignId") Long campaignId);

    List<Video> findByDurationSecondsGreaterThan(Integer seconds);
    List<Video> findByDurationSecondsLessThan(Integer seconds);
}