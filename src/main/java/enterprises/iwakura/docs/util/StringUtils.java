package enterprises.iwakura.docs.util;

import java.util.Locale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static final double DEFAULT_THRESHOLD = 0.85;

    /**
     * Computes the Jaro-Winkler similarity between two strings.
     * Returns a value between 0.0 (no similarity) and 1.0 (identical).
     * Comparison is case-insensitive and locale-independent.
     *
     * @param originalString the original string
     * @param otherString    the string to compare against
     *
     * @return similarity score between 0.0 and 1.0
     */
    public static double getSimilarityScore(String originalString, String otherString) {
        if (originalString == null || otherString == null) {
            return 0.0;
        }

        // Locale-independent lowercase
        final String s1 = originalString.toLowerCase(Locale.ROOT);
        final String s2 = otherString.toLowerCase(Locale.ROOT);

        if (s1.equals(s2)) {
            return 1.0;
        }

        final int len1 = s1.length();
        final int len2 = s2.length();

        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }

        // Maximum distance for characters to be considered matching
        final int matchDistance = Math.max(len1, len2) / 2 - 1;

        final boolean[] s1Matched = new boolean[len1];
        final boolean[] s2Matched = new boolean[len2];

        int matches = 0;
        int transpositions = 0;

        // Find matches
        for (int i = 0; i < len1; i++) {
            final int start = Math.max(0, i - matchDistance);
            final int end = Math.min(i + matchDistance + 1, len2);

            for (int j = start; j < end; j++) {
                if (s2Matched[j] || s1.charAt(i) != s2.charAt(j)) {
                    continue;
                }
                s1Matched[i] = true;
                s2Matched[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) {
            return 0.0;
        }

        // Count transpositions
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matched[i]) {
                continue;
            }
            while (!s2Matched[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }

        // Jaro similarity
        final double jaro = (
            (double) matches / len1 +
                (double) matches / len2 +
                (matches - transpositions / 2.0) / matches
        ) / 3.0;

        // Winkler prefix bonus (up to 4 common prefix chars)
        int prefixLength = 0;
        for (int i = 0; i < Math.min(4, Math.min(len1, len2)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }

        return jaro + prefixLength * 0.1 * (1.0 - jaro);
    }

    /**
     * Returns true if the two strings are considered similar
     * based on a given threshold (0.0 to 1.0).
     *
     * @param originalString the original string
     * @param otherString    the string to compare against
     * @param threshold      minimum similarity score to be considered similar
     *
     * @return true if similarity >= threshold
     */
    public static boolean isSimilar(String originalString, String otherString, double threshold) {
        return getSimilarityScore(originalString, otherString) >= threshold;
    }

    /**
     * Returns true if the two strings are considered similar
     * based on the default threshold ({@link #DEFAULT_THRESHOLD})
     *
     * @param originalString the original string
     * @param otherString    the string to compare against
     *
     * @return true if similarity >= threshold
     */
    public static boolean isSimilar(String originalString, String otherString) {
        return getSimilarityScore(originalString, otherString) >= DEFAULT_THRESHOLD;
    }
}
