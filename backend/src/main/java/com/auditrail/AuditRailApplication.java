package com.auditrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuditRailApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditRailApplication.class, args);
    }
}
