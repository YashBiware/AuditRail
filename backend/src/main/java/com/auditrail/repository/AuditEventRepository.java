package com.auditrail.repository;

import com.auditrail.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, String>, JpaSpecificationExecutor<AuditEvent> {

    Optional<AuditEvent> findByOrganizationIdAndSequenceNumber(String organizationId, long sequenceNumber);

    Optional<AuditEvent> findByOrganizationIdAndId(String organizationId, String id);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.organizationId = :organizationId AND ae.sequenceNumber BETWEEN :startSeq AND :endSeq ORDER BY ae.sequenceNumber ASC")
    List<AuditEvent> findEventsInRange(
        @Param("organizationId") String organizationId,
        @Param("startSeq") long startSeq,
        @Param("endSeq") long endSeq
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.organizationId = :organizationId ORDER BY ae.sequenceNumber ASC")
    List<AuditEvent> findAllByOrganizationIdOrderBySequenceNumberAsc(@Param("organizationId") String organizationId);

    Page<AuditEvent> findAllByOrganizationId(String organizationId, Pageable pageable);

    long countByOrganizationId(String organizationId);
}
