package com.securityawareness.secutrainapp.repository;

import com.securityawareness.secutrainapp.model.Campaign;
import com.securityawareness.secutrainapp.model.CampaignAssignment;
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
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByUsername(String username);
    List<Campaign> findByIsActive(Boolean isActive);
    long countByIsActive(Boolean isActive);
    List<Campaign> findByCreatedBy(User user);
    List<Campaign> findByCreatedAtAfter(LocalDateTime createdAt);
    @Query("SELECT c FROM Campaign c JOIN c.videos v WHERE v.id = :videoId")
    List<Campaign> findCampaignsContainingVideo(@Param("videoId") Long videoId);
    @Query("SELECT ca.campaign FROM CampaignAssignment ca WHERE ca.user.id = :userId")
    List<Campaign> findCampaignsAssignedToUser(@Param("userId") Long userId);
}
