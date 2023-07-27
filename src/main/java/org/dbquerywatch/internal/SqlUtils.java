package org.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

import static org.dbquerywatch.internal.Strings.suffixedBy;

@UtilityClass
public class SqlUtils {

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
