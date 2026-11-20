package com.auditrail.repository;

import com.auditrail.entity.ChainHead;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChainHeadRepository extends JpaRepository<ChainHead, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ch FROM ChainHead ch WHERE ch.organizationId = :organizationId")
    Optional<ChainHead> findByOrganizationIdForUpdate(@Param("organizationId") String organizationId);

    Optional<ChainHead> findByOrganizationId(String organizationId);
}
