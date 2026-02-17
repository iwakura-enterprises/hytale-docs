package enterprises.iwakura.docs.service.loader;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.LoaderContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceMarkdownFileDocumentationLoader extends DocumentationLoader {

    private final Supplier<Documentation> documentationSupplier;
    private final Map<JavaPlugin, URL> resourceMarkdownFiles;

    @Override
    public List<Documentation> load(LoaderContext loaderContext) {
        var logger = loaderContext.getLogger();
        var documentation = documentationSupplier.get();

        logger.info("Loading %d simple markdown documentations for various mods into %s".formatted(
            resourceMarkdownFiles.size(), documentation
        ));

        var markdownFileContents = new HashMap<JavaPlugin, String>();

        resourceMarkdownFiles.forEach((plugin, resourceMarkdownFile) -> {
            try (var inputStream = resourceMarkdownFile.openStream()) {
                var markdownContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                markdownFileContents.put(plugin, markdownContent);
            } catch (Exception exception) {
                logger.error("Failed to load markdown content resource %s from mods %s".formatted(
                    resourceMarkdownFile, plugin.getIdentifier()
                ), exception);
            }
        });

        logger.info("Loaded %d markdown file contents".formatted(
            markdownFileContents.size()
        ));

        markdownFileContents.forEach((plugin, markdownContent) -> {
            try {
                var topicConfig = loaderContext.getMarkdownService().readMarkdownAsTopicConfig(
                    "%s_%s".formatted(plugin.getIdentifier().getGroup(), plugin.getIdentifier().getName()),
                    markdownContent
                );
                var topic = topicConfig.toTopic(documentation);
                topic.setDescription(topic.getDescription());
                topic.setSortIndex(0); // Will be sorted alphabetically
                documentation.addTopics(topic);
            } catch (Exception exception) {
                logger.error("Failed to load topic config from mods %s: %s".formatted(
                    plugin.getIdentifier(), markdownContent
                ), exception);
            }
        });

        logger.info("Loaded %d topics from simple markdown files supplied by various mods".formatted(
            documentation.countTopics()
        ));

        return List.of(documentation);
    }
}
