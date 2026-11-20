package com.auditrail.util;

import java.time.Instant;

import java.time.temporal.ChronoUnit;

public final class CanonicalPayloadBuilder {

    private CanonicalPayloadBuilder() {}

    public static String buildCanonicalString(
        String organizationId,
        long sequenceNumber,
        String actorReference,
        String action,
        String resourceReference,
        Instant eventTimestamp,
        String metadataHash,
        String previousHash
    ) {
        String safeActor = escape(actorReference);
        String safeAction = action == null ? "" : action.trim().toUpperCase();
        String safeResource = escape(resourceReference);
        Instant normalizedTimestamp = eventTimestamp != null ? eventTimestamp.truncatedTo(ChronoUnit.MICROS) : Instant.EPOCH;
        String isoTimestamp = normalizedTimestamp.toString();
        String safeMetaHash = metadataHash == null ? "" : metadataHash.toLowerCase();
        String safePrevHash = previousHash == null ? "" : previousHash.toLowerCase();

        return String.format(
            "v1|%s|%d|%s|%s|%s|%s|%s|%s",
            organizationId,
            sequenceNumber,
            safeActor,
            safeAction,
            safeResource,
            isoTimestamp,
            safeMetaHash,
            safePrevHash
        );
    }

    public static String calculateEventHash(
        String organizationId,
        long sequenceNumber,
        String actorReference,
        String action,
        String resourceReference,
        Instant eventTimestamp,
        String metadataHash,
        String previousHash
    ) {
        String canonicalString = buildCanonicalString(
            organizationId,
            sequenceNumber,
            actorReference,
            action,
            resourceReference,
            eventTimestamp,
            metadataHash,
            previousHash
        );
        return HashUtil.sha256Hex(canonicalString);
    }

    private static String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("|", "\\|");
    }
}
