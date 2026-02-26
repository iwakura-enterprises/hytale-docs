package enterprises.iwakura.docs.service;

import java.util.List;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class FallbackTopicService {

    private final MarkdownService markdownService;

    public static Topic noTopicSet() {
        return Topic.builder()
            .id("internal_no_topic_set")
            .name("No topic set")
            .description("No topic has been set yet")
            .author("Docs")
            .markdownContent(
                """
                    # No topic set
                    <red>We're sorry but no topic has been set yet.</red>
                    
                    Please open a topic from the documentation tree.
                    """
            )
            .build();
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
        var documentationTree = markdownService.createDocumentationTreeMarkdown(documentations);

        return Topic.builder()
            .id("internal_topic_not_found")
            .name("Topic not found")
            .description("Could not found requested topic")
            .author("Docs")
            .markdownContent(
                """
                    # Topic not found
                    <red>We're sorry but topic with identifier `%s` could not be found.</red>
                    
                    ## Documentation tree
                    ```
                    %s
                    ```
                    """.formatted(topicIdentifier, documentationTree)
            )
            .build();
    }
}
