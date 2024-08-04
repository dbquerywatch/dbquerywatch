package org.dbquerywatch.application.domain.service;

import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ClassIdSupport {
    private static final char CLASS_HASH_ID_CHAR0 = 'Q'; /* 0x51 */
    private static final char CLASS_HASH_ID_CHAR1 = 'W'; /* 0x57 */
    private static final String CLASS_HASH_ID_PREFIX = "5157";

    private ClassIdSupport() {
    }

    public static String generateClassId(Class<?> clazz) {
        byte[] digest = Arrays.copyOf(messageDigest(clazz.getCanonicalName().getBytes(UTF_8)), 16);
        digest[0] = CLASS_HASH_ID_CHAR0;
        digest[1] = CLASS_HASH_ID_CHAR1;
        return String.format("%032x", new BigInteger(1, digest));
    }

    static boolean isValidClassHashId(@Nullable String traceId) {
        return traceId != null && traceId.length() == 32 &&
            traceId.startsWith(CLASS_HASH_ID_PREFIX);
    }

    private static byte[] messageDigest(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
