package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod({String.class, StringUtils.class})
public class SqlUtils {

    public static boolean tableNameMatch(String targetName, String issueName) {
        String canonicalTargetName = targetName.toLowerCase();
        String canonicalIssueName = issueName.toLowerCase();
        int issueNameLen = issueName.length();
        int targetNameLen = targetName.length();
        if (issueNameLen == targetNameLen) {
            return canonicalTargetName.equals(canonicalIssueName);
        } else if (targetNameLen > issueNameLen) {
            return canonicalTargetName.suffixedBy(canonicalIssueName, '.');
        } else {
            return canonicalIssueName.suffixedBy(canonicalTargetName, '.');
        }
    }
}
