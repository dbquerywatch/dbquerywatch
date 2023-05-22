package com.parolisoft.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static boolean prefixedBy(String str, String prefix, Character separator) {
        if (prefix.length() > str.length()) {
            return false;
        }
        if (prefix.length() == str.length()) {
            return str.equals(prefix);
        }
        return str.startsWith(prefix) && str.charAt(prefix.length()) == separator;
    }

    public static boolean suffixedBy(String str, String suffix, Character separator) {
        if (suffix.length() > str.length()) {
            return false;
        }
        if (suffix.length() == str.length()) {
            return str.equals(suffix);
        }
        return str.endsWith(suffix) && str.charAt(str.length() - suffix.length() - 1) == separator;
    }
}
