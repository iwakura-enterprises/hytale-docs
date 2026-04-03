package enterprises.iwakura.docs.object;

import java.util.List;

import enterprises.iwakura.docs.util.LocaleUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    UKRAINIAN("Ukrainian", "Українська", "ua"),
    SWEDISH("Swedish", "Svenska", "se"),
    NORWEGIAN("Norwegian", "Norsk", "no"),
    DANISH("Danish", "Dansk", "dk"),
    FINNISH("Finnish", "Suomi", "fi"),
    GREEK("Greek", "Ελληνικά", "gr"),
    CZECH("Czech", "Čeština", "cz"),
    SLOVAK("Slovak", "Slovenčina", "sk"),
    HUNGARIAN("Hungarian", "Magyar", "hu"),
    ROMANIAN("Romanian", "Română", "ro"),
    BULGARIAN("Bulgarian", "Български", "bg"),
    CROATIAN("Croatian", "Hrvatski", "hr"),
    SERBIAN("Serbian", "Српски", "rs"),
    TURKISH("Turkish", "Türkçe", "tr"),

    // Asian Languages
    CHINESE_SIMPLIFIED("Chinese", "中文（简体）", "zh-cn"),
    CHINESE_TRADITIONAL("Chinese (Traditional)", "中文（繁體）", "zh-tw"),
    JAPANESE("Japanese", "日本語", "jp"),
    KOREAN("Korean", "한국어", "kr"),
    VIETNAMESE("Vietnamese", "Tiếng Việt", "vn"),
    THAI("Thai", "ภาษาไทย", "th"),
    INDONESIAN("Indonesian", "Bahasa Indonesia", "id"),
    MALAY("Malay", "Bahasa Melayu", "my"),
    HINDI("Hindi", "हिन्दी", "in"),
    ARABIC("Arabic", "العربية", "sa"),
    HEBREW("Hebrew", "עברית", "il"),

    // African Languages
    SWAHILI("Swahili", "Kiswahili", "ke"),
    AMHARIC("Amharic", "አማርኛ", "et"),
    ZULU("Zulu", "isiZulu", "za"),

    // Easter-eggs :P
    LOLCAT("LOLCAT", "Teh best language :3", "lol");

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

        // Fallback to English
        return LocaleType.ENGLISH;
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
