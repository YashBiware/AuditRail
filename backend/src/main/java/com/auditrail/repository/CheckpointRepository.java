package com.auditrail.repository;

import com.auditrail.entity.Checkpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, String> {

    Optional<Checkpoint> findByOrganizationIdAndSequenceNumber(String organizationId, long sequenceNumber);

    Optional<Checkpoint> findTopByOrganizationIdOrderBySequenceNumberDesc(String organizationId);

    List<Checkpoint> findAllByOrganizationIdOrderBySequenceNumberDesc(String organizationId);

    Page<Checkpoint> findAllByOrganizationId(String organizationId, Pageable pageable);
}
