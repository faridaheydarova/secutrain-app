package com.securityawareness.secutrainapp.service;

import com.securityawareness.secutrainapp.model.AuditLog;
import com.securityawareness.secutrainapp.model.User;
import com.securityawareness.secutrainapp.repository.AuditLogRepository;
import com.securityawareness.secutrainapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logAction(Long userId, String action, String resource, Long resourceId, String details) {
        try {
            User user = userId != null ?
                    userRepository.findById(userId).orElse(null) : null;

            String ipAddress = getClientIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .resource(resource)
                    .resourceId(resourceId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit logged - Action: {}, Resource: {}, User: {}",
                    action, resource, userId);
        } catch (Exception e) {
            log.error("Error while logging audit: ", e);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(int limit) {
        return auditLogRepository.findLatestAuditLogs(limit);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return auditLogRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getResourceAuditLogs(String resource, Long resourceId) {
        return auditLogRepository.findByResourceAndResourceId(resource, resourceId);
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }

                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP: ", e);
        }

        return "UNKNOWN";
    }
}