package enterprises.iwakura.docs.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
    HEBREW("Hebrew", "עברית", "he"),

    // African Languages
    SWAHILI("Swahili", "Kiswahili", "ke"),
    AMHARIC("Amharic", "አማርኛ", "et"),
    ZULU("Zulu", "isiZulu", "za"),

    // Easter-eggs :P
    LOLCAT("LOLCAT", "LOLCAT", "lol");

    public static final LocaleType[] ALL = LocaleType.values();
    private final String englishName;
    private final String nativeName;
    private final String code;

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
}
