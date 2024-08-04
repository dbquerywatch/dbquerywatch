package org.dbquerywatch.common;

import org.jspecify.annotations.Nullable;

import static org.dbquerywatch.common.Strings.suffixedBy;

public final class SqlUtils {
    private SqlUtils() {
    }

    public static boolean tableNameMatch(@Nullable String targetName, @Nullable String issueName) {
        if (targetName == null || issueName == null) {
            return false;
        }
        if (issueName.length() == targetName.length()) {
            return targetName.equalsIgnoreCase(issueName);
        }
        if (targetName.length() > issueName.length()) {
            return suffixedBy(targetName, issueName, true, '.');
        }
        return suffixedBy(issueName, targetName, true, '.');
    }
}
