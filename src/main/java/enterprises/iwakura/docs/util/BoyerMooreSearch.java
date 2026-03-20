package enterprises.iwakura.docs.util;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

/**
 * Boyer-Moore string search algorithm implementation. Should be faster than naive way
 * {@link String#contains(CharSequence)} for longer patterns in large texts.
 * <p>
 * Falls back to {@link String#contains(CharSequence)} for short patterns where the
 * JVM intrinsic is faster than the table-building overhead.
 * <p>
 * Use {@link SearchPattern#of(String)} to pre-compute the lookup tables once and
 * reuse them across many texts (e.g. searching the same query across many topics).
 */
@UtilityClass
public class BoyerMooreSearch {

    /**
     * Minimum pattern length to use Boyer-Moore.
     */
    private static final int BM_THRESHOLD = 4;

    private static Map<Character, Integer> buildBadCharTable(String pattern, int m) {
        Map<Character, Integer> table = new HashMap<>(m * 2);
        for (int i = 0; i < m; i++) {
            table.put(pattern.charAt(i), i);
        }
        return table;
    }

    private static int[] buildGoodSuffixTable(String pattern, int m) {
        int[] shift = new int[m + 1];
        int[] border = new int[m + 1];

        // Phase 1: compute the border array (right-to-left)
        int i = m;
        int j = m + 1;
        border[i] = j;

        while (i > 0) {
            while (j <= m && pattern.charAt(i - 1) != pattern.charAt(j - 1)) {
                if (shift[j - 1] == 0) {
                    shift[j - 1] = j - i;
                }
                j = border[j];
            }
            i--;
            j--;
            border[i] = j;
        }

        // Phase 2: fill remaining entries
        j = border[0];
        for (i = 0; i <= m; i++) {
            if (shift[i] == 0) {
                shift[i] = j;
            }
            if (i == j) {
                j = border[j];
            }
        }

        return shift;
    }

    /**
     * Pre-computed Boyer-Moore search tables for a given pattern. Build once with
     * {@link #of(String)} and reuse across multiple texts to avoid redundant
     * table construction.
     *
     * @param pattern    the original search pattern
     * @param badChar    bad-character lookup table (only populated when pattern >= {@link #BM_THRESHOLD})
     * @param goodSuffix good-suffix shift table (only populated when pattern >= {@link #BM_THRESHOLD})
     * @param useNaive   true when the pattern is short enough that the JVM intrinsic is faster
     */
    public record SearchPattern(String pattern, Map<Character, Integer> badChar, int[] goodSuffix, boolean useNaive) {

        /**
         * Pre-computes the Boyer-Moore tables for the given pattern.
         *
         * @param pattern the search pattern (must not be null)
         *
         * @return a reusable {@link SearchPattern}
         */
        public static SearchPattern of(String pattern) {
            pattern = LocaleUtils.normalize(pattern);
            int m = pattern.length();
            if (m < BM_THRESHOLD) {
                return new SearchPattern(pattern, null, null, true);
            }
            return new SearchPattern(pattern, buildBadCharTable(pattern, m), buildGoodSuffixTable(pattern, m), false);
        }

        /**
         * Returns true if the pre-computed pattern occurs anywhere inside the given text.
         *
         * @param text the text to search in (must not be null)
         *
         * @return true if the pattern is found, false otherwise
         */
        public boolean containedIn(String text) {
            int n = text.length();
            int m = pattern.length();

            if (m == 0) {
                return true;
            }
            if (m > n) {
                return false;
            }
            if (useNaive) {
                return text.contains(pattern);
            }

            int s = 0;
            while (s <= n - m) {
                int j = m - 1;

                while (j >= 0 && pattern.charAt(j) == text.charAt(s + j)) {
                    j--;
                }

                if (j < 0) {
                    return true;
                }

                int badShift = j - badChar.getOrDefault(text.charAt(s + j), -1);
                int goodShift = goodSuffix[j];
                s += Math.max(1, Math.max(badShift, goodShift));
            }

            return false;
        }
    }
}