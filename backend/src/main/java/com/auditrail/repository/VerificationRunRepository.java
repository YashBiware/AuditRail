package com.auditrail.repository;

import com.auditrail.entity.VerificationRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRunRepository extends JpaRepository<VerificationRun, String> {

    List<VerificationRun> findAllByOrganizationIdOrderByStartedAtDesc(String organizationId);

    Page<VerificationRun> findAllByOrganizationId(String organizationId, Pageable pageable);

    Optional<VerificationRun> findTopByOrganizationIdOrderByStartedAtDesc(String organizationId);
}
