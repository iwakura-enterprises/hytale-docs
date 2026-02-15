package enterprises.iwakura.docs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Node;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;

import enterprises.iwakura.docs.config.TopicConfig;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class MarkdownService {

    private final Logger logger;

    /**
     * Parses markdown content
     *
     * @param content        Content
     * @param catchException If method should return null on error
     *
     * @return Resulting document node (null if catchException is set to true and parsing failed)
     */
    public Node parseMarkdown(String content, boolean catchException) {
        try {
            var parser = Parser.builder()
                .extensions(List.of(YamlFrontMatterExtension.create()))
                .build();
            return parser.parse(content);
        } catch (Exception exception) {
            if (catchException) {
                logger.error("Failed to parse markdown! Returning null. (allowFail=true): %s\n%s".formatted(
                    exception.getMessage(), content), exception);
                return null;
            } else {
                throw exception;
            }
        }
    }

    /**
     * Extracts HTML Inline tags in node
     *
     * @param node    Node
     * @param forward If should look forward or backwards
     *
     * @return Extracted html tags, non-null
     */
    public String extractHtmlTags(Node node, boolean forward) {
        StringBuilder tags = new StringBuilder();
        Node current = forward ? node.getNext() : node.getPrevious();

        while (current instanceof HtmlInline htmlInline) {
            if (forward) {
                tags.append(htmlInline.getLiteral());
                current = current.getNext();
            } else {
                tags.insert(0, htmlInline.getLiteral());
                current = current.getPrevious();
            }
        }

        return tags.toString();
    }

    /**
     * Extracts any literal text from Text nodes
     *
     * @param node Node
     *
     * @return Extracted text, non-null
     */
    public String extractText(Node node) {
        StringBuilder text = new StringBuilder();
        Node child = node.getFirstChild();
        while (child != null) {
            if (child instanceof Text) {
                text.append(((Text) child).getLiteral());
            } else if (child instanceof SoftLineBreak) {
                text.append(" ");
            } else {
                text.append(extractText(child));
            }
            child = child.getNext();
        }
        return text.toString();
    }

    /**
     * Extracts heading nodes
     * @param node Node
     * @return List of headings
     */
    public List<Heading> extractHeadings(Node node) {
        List<Heading> headings = new ArrayList<>();
        var child = node.getFirstChild();
        while (child != null) {
            if (child instanceof Heading heading) {
                headings.add(heading);
            } else {
                headings.addAll(extractHeadings(child));
            }
            child = child.getNext();
        }

        return headings;
    }

    /**
     * Escapes the text for Hytale UI.
     *
     * @param text Text
     *
     * @return Escaped text
     */
    public String escapeText(String text) {
        return text.replace("\"", "\\\"");
    }

    /**
     * Reads the Markdown file as {@link Topic}
     *
     * @param markdownFileName The file name from which Markdown was loaded
     * @param markdownFileContent Markdown file content
     *
     * @return Topic
     */
    public TopicConfig readMarkdownAsTopicConfig(String markdownFileName, String markdownFileContent) {
        var node = parseMarkdown(markdownFileContent, false);
        var visitor = new YamlFrontMatterVisitor();
        node.accept(visitor);

        var metadata = visitor.getData();

        var topic = new TopicConfig();
        topic.setId(getFirstOrThrow(metadata, "id", markdownFileName.replace(".md", "")));
        topic.setName(getFirstOrThrow(metadata, "name", null));
        topic.setDescription(getFirstOrThrow(metadata, "description", "No description supplied."));
        topic.setAuthor(getFirstOrThrow(metadata, "author", "unknown author"));
        topic.setSortIndex(Integer.parseInt(getFirstOrThrow(metadata, "sort-index", "0")));
        topic.setSubTopics(metadata.get("sub-topics"));
        topic.setMarkdownContent(stripFrontMatter(markdownFileContent));
        return topic;
    }

    private String getFirstOrThrow(Map<String, List<String>> metadata, String field, String fallback) {
        var data = metadata.get(field);
        if (data == null || data.isEmpty()) {
            return fallback;
        }
        return data.getFirst();
    }

    private String stripFrontMatter(String content) {
        if (content.startsWith("---")) {
            int endIndex = content.indexOf("---", 3);
            if (endIndex != -1) {
                return content.substring(endIndex + 3).stripLeading();
            }
        }
        return content;
    }

    /**
     * Creates Markdown tree for list of documentations
     *
     * @param documentations Documentations
     *
     * @return Markdown content
     */
    public String createDocumentationTreeMarkdown(List<Documentation> documentations) {
        var markdown = new StringBuilder();

        for (Documentation documentation : documentations) {
            markdown.append("# %s:%s\n".formatted(documentation.getGroup(), documentation.getId()));

            for (Topic topic : documentation.getTopics()) {
                markdown.append(createTopicTreeMarkdown(topic, 2));
            }
        }

        return markdown.toString();
    }

    /**
     * Creates Markdown tree for topic
     *
     * @param topic  Topic
     * @param indent Number of space indents before the topic
     *
     * @return Markdown content
     */
    public String createTopicTreeMarkdown(Topic topic, int indent) {
        var markdown = new StringBuilder();
        markdown.repeat(" ", indent).append("- %s\n".formatted(topic.getId()));

        for (Topic subTopic : topic.getTopics()) {
            markdown.append(createTopicTreeMarkdown(subTopic, indent + 2));
        }

        return markdown.toString();
    }
}
