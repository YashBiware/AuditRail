package com.auditrail.config;

import com.auditrail.entity.ApiKey;
import com.auditrail.entity.ChainHead;
import com.auditrail.entity.Organization;
import com.auditrail.entity.User;
import com.auditrail.repository.ApiKeyRepository;
import com.auditrail.repository.ChainHeadRepository;
import com.auditrail.repository.OrganizationRepository;
import com.auditrail.repository.UserRepository;
import com.auditrail.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Bean
    public CommandLineRunner initDevSeedData(
            OrganizationRepository orgRepo,
            UserRepository userRepo,
            ChainHeadRepository chainHeadRepo,
            ApiKeyRepository apiKeyRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            log.info("Checking development seed organizations...");

            // 1. Acme Global Logistics
            String acmeOrgId = "00000000-0000-0000-0000-000000000001";
            if (!orgRepo.existsById(acmeOrgId)) {
                Organization org = new Organization();
                org.setId(acmeOrgId);
                org.setName("Acme Global Logistics");
                org.setSlug("acme-global");
                org.setStatus("ACTIVE");
                orgRepo.save(org);

                ChainHead head = new ChainHead();
                head.setOrganizationId(acmeOrgId);
                head.setLatestSequenceNumber(0L);
                head.setLatestEventHash("0000000000000000000000000000000000000000000000000000000000000000");
                chainHeadRepo.save(head);

                User admin = new User();
                admin.setId("00000000-0000-0000-0000-000000000010");
                admin.setOrganizationId(acmeOrgId);
                admin.setEmail("admin@acme-logistics.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin@AuditRail2026!"));
                admin.setRole("ADMIN");
                admin.setAccountStatus("ACTIVE");
                admin.setTotpEnabled(false);
                userRepo.save(admin);

                ApiKey key = new ApiKey();
                key.setId("00000000-0000-0000-0000-000000000020");
                key.setOrganizationId(acmeOrgId);
                key.setKeyPrefix("ar_live_demo01");
                key.setKeyHash(HashUtil.sha256Hex("ar_live_demo01_acme_secret_key_9981"));
                key.setLabel("Acme SAP Gateway");
                key.setStatus("ACTIVE");
                key.setCreatedByUserId(admin.getId());
                apiKeyRepo.save(key);

                log.info("Initialized default Acme Global Logistics organization.");
            }

            // 2. Aarohi Global Bank
            String aarohiOrgId = "11111111-1111-1111-1111-111111111111";
            if (!orgRepo.existsById(aarohiOrgId)) {
                Organization org = new Organization();
                org.setId(aarohiOrgId);
                org.setName("Aarohi Global Bank");
                org.setSlug("aarohi-bank");
                org.setStatus("ACTIVE");
                orgRepo.save(org);

                ChainHead head = new ChainHead();
                head.setOrganizationId(aarohiOrgId);
                head.setLatestSequenceNumber(0L);
                head.setLatestEventHash("0000000000000000000000000000000000000000000000000000000000000000");
                chainHeadRepo.save(head);

                User admin = new User();
                admin.setId("11111111-1111-1111-1111-111111111110");
                admin.setOrganizationId(aarohiOrgId);
                admin.setEmail("admin@aarohibank.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin@AuditRail2026!"));
                admin.setRole("ADMIN");
                admin.setAccountStatus("ACTIVE");
                admin.setTotpEnabled(false);
                userRepo.save(admin);

                ApiKey key = new ApiKey();
                key.setId("11111111-1111-1111-1111-111111111120");
                key.setOrganizationId(aarohiOrgId);
                key.setKeyPrefix("ar_live_admin");
                key.setKeyHash("91cc2e501534a12e9831f1e87f6c21970be151b275fb34e16b6e9a6dbf8a2593");
                key.setLabel("Aarohi Core Banking Ingestion Key");
                key.setStatus("ACTIVE");
                key.setCreatedByUserId(admin.getId());
                apiKeyRepo.save(key);

                log.info("Initialized default Aarohi Global Bank organization with user key.");
            } else {
                // Ensure existing org name and API key match
                Organization existing = orgRepo.findById(aarohiOrgId).get();
                existing.setName("Aarohi Global Bank");
                existing.setSlug("aarohi-bank");
                orgRepo.save(existing);

                ApiKey key = apiKeyRepo.findAllByOrganizationId(aarohiOrgId).stream().findFirst().orElseGet(() -> {
                    ApiKey k = new ApiKey();
                    k.setId(java.util.UUID.randomUUID().toString());
                    k.setOrganizationId(aarohiOrgId);
                    return k;
                });
                key.setKeyPrefix("ar_live_admin");
                key.setKeyHash("91cc2e501534a12e9831f1e87f6c21970be151b275fb34e16b6e9a6dbf8a2593");
                key.setLabel("Aarohi Core Banking Ingestion Key");
                key.setStatus("ACTIVE");
                apiKeyRepo.save(key);
                log.info("Updated Aarohi Global Bank API key.");
            }

            log.info("Dev seed organizations check complete.");
        };
    }
}
