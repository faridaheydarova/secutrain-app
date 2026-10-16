package com.securityawareness.secutrainapp.repository;

import com.securityawareness.secutrainapp.model.AuditLog;
import com.securityawareness.secutrainapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUser(User user);
    List<AuditLog> findByResourceAndResourceId(String resource, Long resourceId);
    @Query(value = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<AuditLog> findLatestAuditLogs(@Param("limit") int limit);
}