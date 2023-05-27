package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod({String.class, Strings.class})
public class SqlUtils {

    public static boolean tableNameMatch(String targetName, String issueName) {
        if (issueName.length() == targetName.length()) {
            return targetName.equalsIgnoreCase(issueName);
        } else if (targetName.length() > issueName.length()) {
            return targetName.suffixedBy(issueName, true, '.');
        } else {
            return issueName.suffixedBy(targetName, true, '.');
        }
    }
}
