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
    CHINESE_SIMPLIFIED("Chinese (Simplified)", "中文（简体）", "zh-CN"),
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
    YORUBA("Yoruba", "Yorùbá", "yo"),
    ZULU("Zulu", "isiZulu", "zu"),
    AFRIKAANS("Afrikaans", "Afrikaans", "af"),

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

        return null;
    }
}
