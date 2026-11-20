package com.auditrail;

import com.auditrail.util.TotpUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TotpUtilTest {

    @Test
    void testSecretGenerationAndVerification() {
        String secret = TotpUtil.generateSecretKey();
        assertNotNull(secret);
        assertTrue(secret.length() >= 16);

        long currentWindow = System.currentTimeMillis() / 1000L / 30L;
        String validCode = TotpUtil.generateCode(secret, currentWindow);

        assertEquals(6, validCode.length());
        assertTrue(TotpUtil.verifyCode(secret, validCode));
        assertFalse(TotpUtil.verifyCode(secret, "000000"));
        assertFalse(TotpUtil.verifyCode(secret, "abcdef"));
    }

    @Test
    void testOtpAuthUrlFormat() {
        String url = TotpUtil.getOtpAuthUrl("admin@acme.com", "Acme Logistics", "JBSWY3DPEHPK3PXP");
        assertTrue(url.startsWith("otpauth://totp/AuditRail:admin@acme.com"));
        assertTrue(url.contains("secret=JBSWY3DPEHPK3PXP"));
    }
}
