package enterprises.iwakura.docs.object;

import java.util.List;
import java.util.Locale;

import enterprises.iwakura.docs.util.LocaleUtils;
import lombok.Getter;

@Getter
public enum LocaleType {
    // Major European Languages
    ENGLISH("English", "English", "en"),
    SPANISH("Spanish", "Español", "es"),
    PORTUGUESE("Portuguese", "Português", "pt"),
    FRENCH("French", "Français", "fr"),
    GERMAN("German", "Deutsch", "de"),
    ITALIAN("Italian", "Italiano", "it"),
    DUTCH("Dutch", "Nederlands", "nl"),
    POLISH("Polish", "Polski", "pl"),
    RUSSIAN("Russian", "Русский", "ru"),
    UKRAINIAN("Ukrainian", "Українська", "uk"),
    SWEDISH("Swedish", "Svenska", "sv"),
    NORWEGIAN("Norwegian", "Norsk", "no"),
    DANISH("Danish", "Dansk", "da"),
    FINNISH("Finnish", "Suomi", "fi"),
    GREEK("Greek", "Ελληνικά", "el"),
    CZECH("Czech", "Čeština", "cs"),
    SLOVAK("Slovak", "Slovenčina", "sk"),
    HUNGARIAN("Hungarian", "Magyar", "hu"),
    ROMANIAN("Romanian", "Română", "ro"),
    BULGARIAN("Bulgarian", "Български", "bg"),
    CROATIAN("Croatian", "Hrvatski", "hr"),
    SERBIAN("Serbian", "Српски", "sr"),
    TURKISH("Turkish", "Türkçe", "tr"),

    // Asian Languages
    CHINESE_SIMPLIFIED("Chinese", "中文（简体）", "zh-CN"),
    CHINESE_TRADITIONAL("Chinese (Traditional)", "中文（繁體）", "zh-TW"),
    JAPANESE("Japanese", "日本語", "ja"),
    KOREAN("Korean", "한국어", "ko"),
    VIETNAMESE("Vietnamese", "Tiếng Việt", "vi"),
    THAI("Thai", "ภาษาไทย", "th"),
    INDONESIAN("Indonesian", "Bahasa Indonesia", "id"),
    MALAY("Malay", "Bahasa Melayu", "ms"),
    HINDI("Hindi", "हिन्दी", "hi"),
    ARABIC("Arabic", "العربية", "ar"),
    HEBREW("Hebrew", "עברית", "he"),

    // African Languages
    SWAHILI("Swahili", "Kiswahili", "sw"),
    AMHARIC("Amharic", "አማርኛ", "am"),
    ZULU("Zulu", "isiZulu", "zu"),

    // Easter-eggs :P
    LOLCAT("LOLCAT", "Teh best language :3", "lol"),
    TRUE_LANGUAGE("True Language", "that holds no lies", "true"),
    CHAOS("Chaos", "2B, or not 2B?", "chaos");

    public static final List<LocaleType> ALL = List.of(LocaleType.values());
    private final String englishName;
    private final String nativeName;
    private final String normalizedNativeName;
    private final String code;

    LocaleType(String englishName, String nativeName, String code) {
        this.englishName = englishName;
        this.nativeName = nativeName;
        this.normalizedNativeName = LocaleUtils.normalize(nativeName);
        this.code = code;
    }

    /**
     * Gets locale type by its code
     *
     * @param code Code
     *
     * @return Nullable locale type (null if not found)
     */
    public static LocaleType byCode(String code) {
        for (LocaleType localeType : ALL) {
            if (localeType.code.equals(code)) {
                return localeType;
            }
        }

        return null;
    }

    public static LocaleType safeValueOf(String value) {
        for (LocaleType localeType : ALL) {
            if (localeType.name().equals(value)) {
                return localeType;
            }
        }

        return null;
    }

    /**
     * Returns Voile's {@link LocaleType} from language tag
     *
     * @param languageTag Language tag
     *
     * @return Never null {@link LocaleType} (defaults to {@link LocaleType#ENGLISH})
     */
    public static LocaleType fromHytaleLanguage(String languageTag) {
        if (languageTag != null && !languageTag.isBlank()) {
            try {
                String langCode = Locale.forLanguageTag(languageTag).getLanguage();
                for (LocaleType localeType : ALL) {
                    if (localeType.code.equalsIgnoreCase(languageTag)
                        || localeType.code.equalsIgnoreCase(langCode)
                    ) {
                        return localeType;
                    }
                }
            } catch (Exception ignored) {
                // Fallback to default
            }
        }
        return ENGLISH;
    }

    /**
     * Checks whenever specified search query matches the current locale type by english name, normalized native name or
     * a code.
     *
     * @param languageSearchQuery Language search query
     *
     * @return True if yes, false otherwise
     */
    public boolean matchesSearch(String languageSearchQuery) {
        var normalizedSearchQuery = LocaleUtils.normalize(languageSearchQuery).toLowerCase();
        return englishName.toLowerCase().contains(normalizedSearchQuery)
            || normalizedNativeName.toLowerCase().contains(normalizedSearchQuery)
            || code.contains(normalizedSearchQuery);
    }
}
