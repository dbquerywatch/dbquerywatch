package org.dbquerywatch.application.domain.service;

import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestMethodIdSupport {
    private static final char HASH_ID_CHAR0 = 'Q'; /* 0x51 */
    private static final char HASH_ID_CHAR1 = 'W'; /* 0x57 */
    private static final String HASH_ID_PREFIX = "5157";

    private TestMethodIdSupport() {
    }

    public static String generateTestMethodId(Class<?> clazz) {
        return generateTestMethodId(clazz.getCanonicalName());
    }

    public static String generateTestMethodId(String uniqueId) {
        byte[] digest = Arrays.copyOf(messageDigest(uniqueId.getBytes(UTF_8)), 16);
        digest[0] = HASH_ID_CHAR0;
        digest[1] = HASH_ID_CHAR1;
        return String.format("%032x", new BigInteger(1, digest));
    }

    static boolean isValidTestMethodHashId(@Nullable String traceId) {
        return traceId != null && traceId.length() == 32 &&
            traceId.startsWith(HASH_ID_PREFIX);
    }

    private static byte[] messageDigest(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
