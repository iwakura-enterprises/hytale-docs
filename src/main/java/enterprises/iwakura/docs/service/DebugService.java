package enterprises.iwakura.docs.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;

import enterprises.iwakura.docs.Version;
import enterprises.iwakura.docs.object.CacheIndex;
import enterprises.iwakura.docs.object.Documentation;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class DebugService {

    private static final List<Pattern> REDACT_PATTERNS = List.of(
        Pattern.compile("(\"[^\"]*(?:token|secret|password|key|auth|apiKey|override)[^\"]*\"\\s*:\\s*)\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    );

    @Getter
    private final Map<String, String> installedPluginsMap = new HashMap<>();

    private final Gson gson;

    private final FileSystemCacheService fileSystemCacheService;
    private final ServerService serverService;
    private final ConfigurationService configurationService;
    private final DocumentationService documentationService;

    public void init() {
        installedPluginsMap.putAll(PluginManager.get().getPlugins().stream()
            .filter(plugin -> !plugin.getIdentifier().getGroup().equals("Hytale"))
            .collect(Collectors.toMap(
                k -> k.getIdentifier().toString(),
                v -> v.getManifest().getVersion().toString()
            )));
    }

    /**
     * Creates debug string
     *
     * @return Debug string
     */
    public String createDebugString() {
        var serverId = configurationService.getDocsConfig().getSentry().getServerId();

        StringBuilder content = new StringBuilder();
        content.append("====== Voile Debug Information ======\n");
        content.append("Voile Version: ").append(Version.VERSION).append("\n");
        content.append("Server ID: ").append(serverId).append("\n");
        content.append("\n");

        content.append("=== System Info ===\n");
        content.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        content.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        content.append("OS Name: ").append(System.getProperty("os.name")).append("\n");
        content.append("OS Arch: ").append(System.getProperty("os.arch")).append("\n");
        content.append("\n");

        content.append("=== Hytale Info ===\n");
        content.append("Dedicated Server: ").append(serverService.isRunningOnDedicatedServer()).append("\n");
        content.append("Server Version: ").append(ManifestUtil.getVersion()).append("\n");
        content.append("Online Players: ").append(Universe.get().getPlayerCount()).append("\n");
        content.append("Plugin Count: ").append(installedPluginsMap.size()).append("\n");
        content.append("Installed Plugins:\n");
        installedPluginsMap.forEach((identifier, version) -> {
            content.append("- ").append(identifier).append(" @ ").append(version).append("\n");
        });
        content.append("\n");

        content.append("=== Service Status ===\n");
        content.append("# FileSystemCacheService\n");
        content.append("Size: ").append(Optional.ofNullable(fileSystemCacheService.getCacheIndex()).map(CacheIndex::size).orElse(-1)).append("\n");
        content.append("\n");

        content.append("# RuntimeImageAssertService\n");
        content.append("Size: ").append(RuntimeImageAssetService.getRUNTIME_IMAGE_ASSET_MAP().size()).append("\n");
        content.append("Players: ").append(RuntimeImageAssetService.getPLAYER_RUNTIME_IMAGE_ASSETS_MAP().size()).append("\n");
        content.append("\n");

        content.append("=== Voile Configuration ===\n");
        content.append(redactJson(gson.toJson(configurationService.getDocsConfig()))).append("\n");
        content.append("\n");

        content.append("=== Documentation Dump ===\n");
        content.append(createDocumentationTreeMarkdown(documentationService.getDocumentations(DocumentationType.ALL)));
        content.append("\n");

        return content.toString();
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
            markdown.append("# %s:%s (%s)\n".formatted(documentation.getGroup(), documentation.getId(), documentation.getType()));

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

    private String redactJson(String json) {
        for (var pattern : REDACT_PATTERNS) {
            json = pattern.matcher(json).replaceAll("$1\"[REDACTED]\"");
        }
        return json;
    }
}
