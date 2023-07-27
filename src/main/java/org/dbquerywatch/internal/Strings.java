package org.dbquerywatch.internal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Strings {

    public static boolean prefixedBy(String str, String prefix, boolean ignoreCase, char separator) {
        if (prefix.length() > str.length()) {
            return false;
        }
        if (prefix.length() == str.length()) {
            return str.regionMatches(ignoreCase, 0, prefix, 0, str.length());
        }
        return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length()) &&
            str.charAt(prefix.length()) == separator;
    }

    public static boolean suffixedBy(String str, String suffix, boolean ignoreCase, char separator) {
        if (suffix.length() > str.length()) {
            return false;
        }
        if (suffix.length() == str.length()) {
            return str.equalsIgnoreCase(suffix);
        }
        return str.regionMatches(ignoreCase, str.length() - suffix.length(), suffix, 0, suffix.length()) &&
            str.charAt(str.length() - suffix.length() - 1) == separator;
    }
}
