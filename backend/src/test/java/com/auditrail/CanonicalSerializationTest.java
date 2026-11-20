package com.auditrail;

import com.auditrail.util.CanonicalJsonUtil;
import com.auditrail.util.CanonicalPayloadBuilder;
import com.auditrail.util.HashUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class CanonicalSerializationTest {

    @Test
    void testCanonicalJsonDeterministicSorting() {
        String json1 = "{\"b\": 2, \"a\": 1, \"c\": 3}";
        String json2 = "{\n  \"c\": 3,\n  \"a\": 1,\n  \"b\": 2\n}";

        String canon1 = CanonicalJsonUtil.canonicalize(json1);
        String canon2 = CanonicalJsonUtil.canonicalize(json2);

        assertEquals("{\"a\":1,\"b\":2,\"c\":3}", canon1);
        assertEquals(canon1, canon2);
        assertEquals(HashUtil.sha256Hex(canon1), HashUtil.sha256Hex(canon2));
    }

    @Test
    void testCanonicalPayloadHashDeterminism() {
        Instant timestamp = Instant.parse("2026-11-17T10:00:00Z");
        String metaHash = HashUtil.sha256Hex("{\"env\":\"prod\"}");

        String hash1 = CanonicalPayloadBuilder.calculateEventHash(
            "org-1",
            1L,
            "admin@acme.com",
            "USER_LOGIN",
            "auth:session_101",
            timestamp,
            metaHash,
            HashUtil.GENESIS_HASH
        );

        String hash2 = CanonicalPayloadBuilder.calculateEventHash(
            "org-1",
            1L,
            "admin@acme.com",
            "USER_LOGIN",
            "auth:session_101",
            timestamp,
            metaHash,
            HashUtil.GENESIS_HASH
        );

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
    }

    @Test
    void testDelimitedPayloadEscapesPipesAndBackslashes() {
        String canonical = CanonicalPayloadBuilder.buildCanonicalString(
            "org-1",
            2L,
            "user|with|pipe",
            "TEST_ACTION",
            "resource\\with\\slash",
            Instant.parse("2026-11-17T10:00:00Z"),
            "hash1",
            "hash2"
        );

        assertTrue(canonical.contains("user\\|with\\|pipe"));
        assertTrue(canonical.contains("resource\\\\with\\\\slash"));
    }
}
