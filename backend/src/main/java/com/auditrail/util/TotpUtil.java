package com.auditrail.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public final class TotpUtil {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int MODULO = 1_000_000;
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private TotpUtil() {}

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    public static boolean verifyCode(String base32Secret, String inputCode) {
        if (base32Secret == null || inputCode == null) {
            return false;
        }
        String cleanCode = inputCode.trim();
        if (cleanCode.length() != CODE_DIGITS) {
            return false;
        }

        long currentWindow = System.currentTimeMillis() / 1000L / TIME_STEP_SECONDS;
        // Check window -1, 0, +1 to accommodate minor clock drift
        for (int i = -1; i <= 1; i++) {
            String expectedCode = generateCode(base32Secret, currentWindow + i);
            if (HashUtil.constantTimeEquals(cleanCode, expectedCode)) {
                return true;
            }
        }
        return false;
    }

    public static String generateCode(String base32Secret, long timeWindow) {
        byte[] key = decodeBase32(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                       | ((hash[offset + 1] & 0xff) << 16)
                       | ((hash[offset + 2] & 0xff) << 8)
                       | (hash[offset + 3] & 0xff);

            int otp = binary % MODULO;
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to generate TOTP code", e);
        }
    }

    public static String getOtpAuthUrl(String email, String organizationName, String base32Secret) {
        return String.format(
            "otpauth://totp/AuditRail:%s?secret=%s&issuer=AuditRail%%20(%s)&algorithm=SHA1&digits=%d&period=%d",
            email, base32Secret, organizationName, CODE_DIGITS, TIME_STEP_SECONDS
        );
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xff);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_ALPHABET.charAt((buffer >> bitsLeft) & 0x1f));
            }
        }
        if (bitsLeft > 0) {
            buffer <<= (5 - bitsLeft);
            result.append(BASE32_ALPHABET.charAt(buffer & 0x1f));
        }
        return result.toString();
    }

    private static byte[] decodeBase32(String base32) {
        String clean = base32.trim().toUpperCase().replaceAll("[^A-Z2-7]", "");
        int outputLength = (clean.length() * 5) / 8;
        byte[] result = new byte[outputLength];

        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;

        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            int val = BASE32_ALPHABET.indexOf(c);
            if (val < 0) continue;

            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (index < outputLength) {
                    result[index++] = (byte) ((buffer >> bitsLeft) & 0xff);
                }
            }
        }
        return result;
    }
}
