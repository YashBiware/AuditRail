package com.auditrail.repository;

import com.auditrail.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyHashAndStatus(String keyHash, String status);
    List<ApiKey> findAllByOrganizationId(String organizationId);
}
