package com.securityawareness.secutrainapp.service;
import com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs.CampaignDTO;
import com.securityawareness.secutrainapp.dto.CampaignmanagementDTOs.CampaignVideoDTO;
import com.securityawareness.secutrainapp.event.TrainingAssignedEvent;
import com.securityawareness.secutrainapp.model.Campaign;
import com.securityawareness.secutrainapp.model.CampaignAssignment;
import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.model.Video;
import com.securityawareness.secutrainapp.repository.CampaignAssignmentRepository;
import com.securityawareness.secutrainapp.repository.CampaignRepository;
import com.securityawareness.secutrainapp.repository.UserRepository;
import com.securityawareness.secutrainapp.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final CampaignAssignmentRepository assignmentRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CampaignDTO createCampaign(CampaignDTO request, Long createdBy) {
        User creator = userRepository.findById(createdBy)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Campaign campaign = Campaign.builder()
                .username((request.getName()))
                .description(request.getDescription())
                .createdBy(creator)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();

        campaignRepository.save(campaign);

        log.info("Campaign created: {} by {}", campaign.getUsername(), creator.getUsername());
        auditService.logAction(createdBy, "CREATE_CAMPAIGN", "CAMPAIGN",
                campaign.getId(), "Campaign created: " + campaign.getUsername());

        return toDTO(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignDTO> getAllCampaigns(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return campaignRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public CampaignDTO getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        return toDTO(campaign);
    }

    @Transactional(readOnly = true)
    public CampaignDTO getCampaignByIdForUser(Long campaignId, Long userId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow(() -> new RuntimeException("Campaign not found with id: " + campaignId));
        CampaignDTO dto = toDTO(campaign);

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            assignmentRepository.findByCampaignAndUser(campaign, user).ifPresent(a -> dto.setAssignmentId(a.getId()));
        }
        return dto;
    }
    @Transactional(readOnly = true)
    public List<CampaignDTO> getActiveCampaigns() {
        return campaignRepository.findByIsActive(true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public CampaignDTO updateCampaign(Long id, CampaignDTO request, Long updatedBy) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaign.setUsername(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());

        campaignRepository.save(campaign);

        log.info("Campaign updated: {}", campaign.getUsername());
        auditService.logAction(updatedBy, "UPDATE_CAMPAIGN", "CAMPAIGN",
                campaign.getId(), "Campaign updated: " + campaign.getUsername());

        return toDTO(campaign);
    }

    @Transactional
    public void deleteCampaign(Long id, Long deletedBy) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        campaignRepository.delete(campaign);

        log.info("Campaign deleted: {}", campaign.getUsername());
        auditService.logAction(deletedBy, "DELETE_CAMPAIGN", "CAMPAIGN", id,
                "Campaign deleted: " + campaign.getUsername());
    }

    @Transactional
    public void addVideoCampaign(Long campaignId, Long videoId, Long userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        campaign.addVideo(video);
        campaignRepository.save(campaign);

        log.info("Video added to campaign: {} -> {}", campaign.getUsername(), video.getTitle());
        auditService.logAction(userId, "ADD_VIDEO", "CAMPAIGN", campaignId,
                "Video added: " + video.getTitle());
    }

    @Transactional
    public void removeVideoFromCampaign(Long campaignId, Long videoId, Long userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        campaign.removeVideo(video);
        campaignRepository.save(campaign);

        log.info("Video removed from campaign: {} -> {}", campaign.getUsername(), video.getTitle());
        auditService.logAction(userId, "REMOVE_VIDEO", "CAMPAIGN", campaignId,
                "Video removed: " + video.getTitle());
    }

    @Transactional(readOnly = true)
    public List<Video> getCampaignVideos(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
        return campaign.getVideos().stream().collect(Collectors.toList());
    }

    @Transactional
    public void assignCampaignToUsers(Long campaignId, List<Long> userIds, Long assignedBy) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        User assignedByUser = userRepository.findById(assignedBy)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            if (assignmentRepository.findByCampaignAndUser(campaign, user).isEmpty()) {
                CampaignAssignment assignment = CampaignAssignment.builder()
                        .campaign(campaign)
                        .user(user)
                        .assignedBy(assignedByUser)
                        .isCompleted(false)
                        .build();

                assignmentRepository.save(assignment);

                eventPublisher.publishEvent(new TrainingAssignedEvent(
                        this,
                        user.getEmail(),
                        user.getUsername(),
                        campaign.getUsername()
                ));

                log.info("Campaign assigned: {} -> {}", campaign.getUsername(), user.getUsername());
                auditService.logAction(assignedBy, "ASSIGN_CAMPAIGN", "CAMPAIGN",
                        campaignId, "Assigned to: " + user.getUsername());
            }
        }
    }
    @Transactional(readOnly = true)
    public List<CampaignDTO> getUserAssignedCampaigns(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        return assignmentRepository.findByUser(user).stream().map(assignment -> {
                    CampaignDTO dto = toDTO(assignment.getCampaign());
                    dto.setAssignmentId(assignment.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void completeCampaign(Long campaignId, Long userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CampaignAssignment assignment = assignmentRepository.findByCampaignAndUser(campaign, user)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        assignment.setIsCompleted(true);
        assignment.setCompletedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        log.info("Campaign completed: {} by {}", campaign.getUsername(), user.getUsername());
        auditService.logAction(userId, "COMPLETE_CAMPAIGN", "CAMPAIGN",
                campaignId, "Campaign completed");
    }

    @Transactional(readOnly = true)
    public Double getCompletionPercentage(Long campaignId, Long userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalVideos = campaign.getVideos().size();
        if (totalVideos == 0) return 100.0;

        return 0.0;
    }

    @Transactional(readOnly = true)
    public long getCompletedAssignmentCount(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return assignmentRepository.countByCampaignAndIsCompleted(campaign, true);
    }

    @Transactional(readOnly = true)
    public long getTotalAssignmentCount(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return assignmentRepository.findByCampaign(campaign).size();
    }

    @Transactional(readOnly = true)
    public Double getCompletionRate(Long campaignId) {
        Double rate = assignmentRepository.getCompletionRateForCampaign(campaignId);
        return rate != null ? rate * 100 : 0.0;
    }

    @Transactional(readOnly = true)
    public long getTotalCampaigns() {
        return campaignRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveCampaignsCount() {
        return campaignRepository.countByIsActive(true);
    }

    @Transactional(readOnly = true)
    public long getTotalAssignments() {
        return assignmentRepository.count();
    }

    @Transactional(readOnly = true)
    public long getCompletedAssignments() {
        return assignmentRepository.countByIsCompleted(true);
    }

    private CampaignDTO toDTO(Campaign campaign) {
        long totalAssigned = assignmentRepository.findByCampaign(campaign).size();
        long totalCompleted = assignmentRepository.countByCampaignAndIsCompleted(campaign, true);
        Double completionRate = totalAssigned > 0 ? (totalCompleted * 100.0) / totalAssigned : 0.0;

        List<CampaignVideoDTO> videoDTOs = campaign.getVideos().stream()
                .map(v -> CampaignVideoDTO.builder()
                        .videoId(v.getId())
                        .title(v.getTitle())
                        .description(v.getDescription())
                        .durationSeconds(v.getDurationSeconds())
                        .build())
                .collect(Collectors.toList());

        return CampaignDTO.builder()
                .id(campaign.getId())
                .name(campaign.getUsername())
                .description(campaign.getDescription())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .isActive(campaign.getIsActive())
                .totalVideos((long) campaign.getVideos().size())
                .totalAssigned(totalAssigned)
                .totalCompleted(totalCompleted)
                .completionRate(completionRate)
                .createdAt(campaign.getCreatedAt())
                .createdByUsername(campaign.getCreatedBy().getUsername())
                .videos(videoDTOs)
                .build();
    }
}