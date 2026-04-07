package enterprises.iwakura.docs.command;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.config.DocsConfig.CommandShortcuts.Command;
import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.service.ServerService;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class VoileCommand extends AbstractPlayerCommand {

    private final ConfigurationService configurationService;
    private final DocumentationViewerService documentationViewerService;
    private final ServerService serverService;
    private final Logger logger;

    private final OptionalArg<String> topicIdentifierArg;

    public VoileCommand(
        ConfigurationService configurationService,
        DocumentationViewerService documentationViewerService, ServerService serverService,
        Logger logger
    ) {
        super("voile", "Opens Voile's interface");
        this.configurationService = configurationService;
        this.documentationViewerService = documentationViewerService;
        this.serverService = serverService;
        this.logger = logger;

        addAliases("docs");
        addAliases("wiki");

        topicIdentifierArg = withOptionalArg("topic", "Topic identifier to open", ArgTypes.STRING);
    }

    /**
     * Initializes command shortcuts
     */
    public void initAliases() {
        var commandShortcuts = configurationService.getDocsConfig().getCommandShortcuts();
        logger.info("Found %d command shortcuts for /voile, registering them as alias...".formatted(commandShortcuts.getCommands().size()));
        for (Command command : configurationService.getDocsConfig().getCommandShortcuts().getCommands()) {
            logger.info("[COMMAND-SHORTCUT] /voile -> /%s -> topic %s".formatted(
                command.getName(), command.getTopicIdentifier()
            ));
            addAliases(command.getName());
        }
    }

    @Override
    protected boolean canGeneratePermission() {
        var config = configurationService.getDocsConfig();
        if (!config.isVoileCommandRequiresPermission()) {
            logger.warn("voileCommandRequiresPermission is set to false, /voile won't have permission node generated.");
            return false;
        }
        return super.canGeneratePermission();
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
            logger.warn("Player %s tried opening the /voile interface but it is disabled (config -> enabled == false)".formatted(
                playerRef.getUsername()
            ));
            ChatInfo.ERROR.send(playerRef, "This command is disabled.");
            return;
        }

        var commandShortcuts = configurationService.getDocsConfig().getCommandShortcuts();
        var commandShortcutTopicIdentifier = commandShortcuts.getTopicIdentifierForCommand(commandContext.getInputString());
        var requestedTopicIdentifier = topicIdentifierArg.get(commandContext);

        if (requestedTopicIdentifier == null && commandShortcuts.isEnabled() && commandShortcutTopicIdentifier.isPresent()) {
            requestedTopicIdentifier = commandShortcutTopicIdentifier.get();
        }

        documentationViewerService.openFor(playerRef, Optional.ofNullable(requestedTopicIdentifier), true);
    }
}
