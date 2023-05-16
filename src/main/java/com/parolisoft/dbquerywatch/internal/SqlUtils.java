package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SqlUtils {

    public static boolean tableNameMatch(String targetName, String issueName) {
        String canonicalTargetName = targetName.toLowerCase();
        String canonicalIssueName = issueName.toLowerCase();
        int issueNameLen = issueName.length();
        int targetNameLen = targetName.length();
        if (issueNameLen == targetNameLen) {
            return canonicalTargetName.equals(canonicalIssueName);
        } else if (targetNameLen > issueNameLen) {
            return canonicalTargetName.endsWith("." + canonicalIssueName);
        } else {
            return canonicalIssueName.endsWith("." + canonicalTargetName);
        }
    }
}
