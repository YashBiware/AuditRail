package com.auditrail.repository;

import com.auditrail.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String> {

    Optional<Invitation> findByTokenHash(String tokenHash);

    List<Invitation> findAllByOrganizationIdOrderByCreatedAtDesc(String organizationId);
}
