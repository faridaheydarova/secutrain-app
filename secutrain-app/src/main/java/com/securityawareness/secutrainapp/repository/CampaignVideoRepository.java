package com.securityawareness.secutrainapp.repository;

import com.securityawareness.secutrainapp.model.CampaignAssignment;
import com.securityawareness.secutrainapp.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignVideoRepository extends JpaRepository<CampaignAssignment, Long> {
    @Query("SELECT DISTINCT cv FROM Campaign c JOIN c.videos cv WHERE c.id = :campaignId")
    List<Video> findVideosByCampaignId(@Param("campaignId") Long campaignId);
}
