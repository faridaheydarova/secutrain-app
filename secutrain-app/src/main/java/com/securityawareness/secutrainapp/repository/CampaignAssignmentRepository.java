package com.securityawareness.secutrainapp.repository;

import com.securityawareness.secutrainapp.model.Campaign;
import com.securityawareness.secutrainapp.model.CampaignAssignment;
import com.securityawareness.secutrainapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignAssignmentRepository extends JpaRepository<CampaignAssignment, Long> {

    Optional<CampaignAssignment> findByCampaignAndUser(Campaign campaign, User user);
    List<CampaignAssignment> findByCampaign(Campaign campaign);
    List<CampaignAssignment> findByUser(User user);
    List<CampaignAssignment> findByAssignedBy(User user);
    List<CampaignAssignment> findByIsCompleted(Boolean isCompleted);
    long countByCampaignAndIsCompleted(Campaign campaign, Boolean isCompleted);
    long countByIsCompleted(Boolean isCompleted);

    @Query("SELECT ca FROM CampaignAssignment ca WHERE ca.campaign.id = :campaignId AND ca.user.id = :userId")
    Optional<CampaignAssignment> findByCampaignIdAndUserId(
            @Param("campaignId") Long campaignId,
            @Param("userId") Long userId);

    @Query("SELECT COUNT(ca) FROM CampaignAssignment ca WHERE ca.campaign.id = :campaignId AND ca.user.id = :userId AND ca.isCompleted = true")
    long countCompletedByUserAndCampaign(
            @Param("campaignId") Long campaignId,
            @Param("userId") Long userId);

    @Query("SELECT CAST(COUNT(CASE WHEN ca.isCompleted = true THEN 1 END) AS DOUBLE) / COUNT(ca) FROM CampaignAssignment ca WHERE ca.campaign.id = :campaignId")
    Double getCompletionRateForCampaign(@Param("campaignId") Long campaignId);
}
