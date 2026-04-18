package enterprises.iwakura.docs.object;

import java.util.Optional;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class TopicIdentifier {

    public static final Pattern TOPIC_IDENTIFIER_PATTERN = Pattern.compile("^(?:([^:$]+):(?:([^:$]+):)?)?([^:$]+)(?:\\$([^:$]+))?$");

    private String documentationGroup;
    private String documentationId;
    private @NonNull String topicId;
    private LocaleType localeType;

    /**
     * Parses the topic identifier
     *
     * @param topicIdentifier Topic identifier
     *
     * @return Optional of TopicIdentifier, empty if the specified topic identifier could not be parsed
     */
    public static Optional<TopicIdentifier> parse(String topicIdentifier) {
        var matcher = TOPIC_IDENTIFIER_PATTERN.matcher(topicIdentifier);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(new TopicIdentifier(
            matcher.group(1),
            matcher.group(2),
            matcher.group(3),
            LocaleType.byCode(matcher.group(4))
        ));
    }

    /**
     * Checks whenever specified topic identifier matches the current one. Null values are treaded as wildcards.
     *
     * @param other Other topic identifier
     *
     * @return True if yes, false otherwise
     */
    public boolean matches(TopicIdentifier other) {
        if (!this.topicId.equals(other.topicId)) {
            return false;
        }
        if (this.documentationGroup != null && other.documentationGroup != null
            && !this.documentationGroup.equals(other.documentationGroup)) {
            return false;
        }
        if (this.documentationId != null && other.documentationId != null
            && !this.documentationId.equals(other.documentationId)) {
            return false;
        }
        if (this.localeType != null && other.localeType != null
            && this.localeType != other.localeType) {
            return false;
        }
        return true;
    }
}
