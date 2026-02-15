package enterprises.iwakura.docs.ui;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Topics {

    public static Topic createTopicNotFound(List<Documentation> documentations, String topicIdentifier) {
        return Topic.builder()
            .id("internal_topic_not_found")
            .name("Topic not found")
            .description("Could not found requested topic")
            .author("Docs")
            .markdownContent(
                """
                <red>We're sorry but topic with identifier `%s` could not be found.</red>
                """.formatted(topicIdentifier)
            )
            .build();
    }
}
