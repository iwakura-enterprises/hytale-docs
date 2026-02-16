package enterprises.iwakura.docs.command;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.config.DocsConfig.CommandShortcuts.Command;
import enterprises.iwakura.docs.object.DocsContext;
import enterprises.iwakura.docs.object.Topic;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.service.FallbackTopicService;
import enterprises.iwakura.docs.ui.DocumentationViewerPage;
import enterprises.iwakura.docs.ui.render.DocumentationTreeRenderer;
import enterprises.iwakura.docs.ui.render.DocumentationViewerRenderer;
import enterprises.iwakura.docs.ui.render.TopicChapterTreeRenderer;
import enterprises.iwakura.docs.ui.render.TopicRenderer;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class DocsCommand extends AbstractPlayerCommand {

    private final ConfigurationService configurationService;
    private final DocumentationService documentationService;
    private final FallbackTopicService fallbackTopicService;
    private final DocumentationViewerService documentationViewerService;
    private final Logger logger;

    private final OptionalArg<String> topicIdentifierArg;

    public DocsCommand(
        ConfigurationService configurationService,
        DocumentationService documentationService, FallbackTopicService fallbackTopicService,
        DocumentationViewerService documentationViewerService,
        Logger logger
    ) {
        super("docs", "Opens Voile's interface");
        this.configurationService = configurationService;
        this.documentationService = documentationService;
        this.fallbackTopicService = fallbackTopicService;
        this.documentationViewerService = documentationViewerService;
        this.logger = logger;

        topicIdentifierArg = withOptionalArg("topic", "Topic identifier to open", ArgTypes.STRING);
    }

    /**
     * Initializes command shortcuts
     */
    public void initAliases() {
        var commandShortcuts = configurationService.getDocsConfig().getCommandShortcuts();

        if (commandShortcuts.isEnabled()) {
            logger.info("Found %d command shortcuts for /docs".formatted(commandShortcuts.getCommands().size()));
            for (Command command : configurationService.getDocsConfig().getCommandShortcuts().getCommands()) {
                logger.info("[COMMAND-SHORTCUT] /docs -> /%s -> topic %s".formatted(
                    command.getName(), command.getTopicIdentifier()
                ));
                addAliases(command.getName());
            }
        } else {
            logger.warn("Command shortcuts are disabled, /docs will not have any aliases. You must restart the server if you have enabled the feature.");
        }
    }

    @Override
    protected void execute(
        @NonNull CommandContext commandContext,
        @NonNull Store<EntityStore> store,
        @NonNull Ref<EntityStore> ref,
        @NonNull PlayerRef playerRef,
        @NonNull World world
    ) {
        var docsConfig = configurationService.getDocsConfig();

        if (!docsConfig.isEnabled()) {
            logger.warn("Player %s tried opening the /docs interface but it is disabled (config -> enabled == false)".formatted(
                playerRef.getUsername()
            ));
            ChatInfo.ERROR.send(playerRef, "This command is disabled.");
            return;
        }

        var commandShortcuts = configurationService.getDocsConfig().getCommandShortcuts();
        var documentations = documentationService.getDocumentations();
        var commandShortcutTopicIdentifier = commandShortcuts.getTopicIdentifierForCommand(commandContext.getInputString());
        var topicIdentifier = topicIdentifierArg.get(commandContext);
        Topic topic;

        if (topicIdentifier == null && commandShortcuts.isEnabled() && commandShortcutTopicIdentifier.isPresent()) {
            topicIdentifier = commandShortcutTopicIdentifier.get();
        }

        if (topicIdentifier != null) {
            var topicByIdentifier = documentationService.findTopic(documentations, topicIdentifier);
            if (topicByIdentifier.isPresent()) {
                topic = topicByIdentifier.get();
            } else {
                topic = fallbackTopicService.createTopicNotFound(documentations, topicIdentifier);
            }
        } else {
            var defaultTopic = documentationService.getDefaultTopic();
            if (defaultTopic.isEmpty()) {
                ChatInfo.ERROR.send(commandContext, "There are no loaded documentations!");
                return;
            }
            topic = defaultTopic.get();
        }

        documentationViewerService.openFor(playerRef, DocsContext.of(
            documentationService.getDocumentations(),
            topic
        ));
    }
}
