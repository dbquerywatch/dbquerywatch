package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;
import org.springframework.util.DigestUtils;

import java.math.BigInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

@UtilityClass
public class ClassIdSupport {
    private static final char CLASS_HASH_ID_CHAR0 = 'Q'; /* 0x51 */
    private static final char CLASS_HASH_ID_CHAR1 = 'W'; /* 0x57 */
    private static final String CLASS_HASH_ID_PREFIX = "5157";

    public static String generateClassId(Class<?> clazz) {
        byte[] digest = DigestUtils.md5Digest(clazz.getCanonicalName().getBytes(UTF_8));
        digest[0] = CLASS_HASH_ID_CHAR0;
        digest[1] = CLASS_HASH_ID_CHAR1;
        return String.format("%032x", new BigInteger(1, digest));
    }

    static boolean isValidClassHashId(String traceId) {
        return traceId != null && traceId.length() == 32 &&
            traceId.startsWith(CLASS_HASH_ID_PREFIX);
    }
}
