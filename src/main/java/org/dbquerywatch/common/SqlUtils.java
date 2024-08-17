package org.dbquerywatch.common;

import org.jspecify.annotations.Nullable;

import static org.dbquerywatch.common.Strings.suffixedBy;

public final class SqlUtils {
    private SqlUtils() {
    }

    public static boolean tableNameMatch(@Nullable String targetName, @Nullable String objectName) {
        if (targetName == null || objectName == null) {
            return false;
        }
        if (objectName.length() == targetName.length()) {
            return targetName.equalsIgnoreCase(objectName);
        }
        if (targetName.length() > objectName.length()) {
            return suffixedBy(targetName, objectName, true, '.');
        }
        return suffixedBy(objectName, targetName, true, '.');
    }
}
