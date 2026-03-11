package enterprises.iwakura.docs.service;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.InternalTopic;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class FallbackTopicService {

    private final DebugService debugService;

    public static Topic noTopicSet() {
        return new InternalTopic(
            "no_topic_set",
            "No topic set",
            "No topic has been set yet",
            """
                # No topic set
                <red>We're sorry but no topic has been set yet.</red>
                
                Please open a topic from the documentation tree.
                """
        );
    }

    /**
     * Creates "not found topic" topic
     *
     * @param documentations  Documentations
     * @param topicIdentifier Topic identifier that could not have been found
     *
     * @return Topic
     */
    public Topic createTopicNotFound(List<Documentation> documentations, String topicIdentifier) {
        var documentationTree = debugService.createDocumentationTreeMarkdown(documentations);

        return new InternalTopic(
            "topic_not_found",
            "Topic not found",
            "Could not found requested topic",
            """
                # Topic not found
                <red>We're sorry but topic with identifier `%s` could not be found.</red>
                
                ## Documentation tree
                ```
                %s
                ```
                """.formatted(topicIdentifier, documentationTree)
        );
    }
}
