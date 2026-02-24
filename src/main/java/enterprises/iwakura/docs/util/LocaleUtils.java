package enterprises.iwakura.docs.util;

import java.text.Normalizer;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LocaleUtils {

    /**
     * Normalizes a string for search by removing diacritics and converting to lowercase
     *
     * @param input Input string
     *
     * @return Normalized string
     */
    public static String normalize(String input) {
        if (input == null) {
            return null;
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.toLowerCase();
    }

}
