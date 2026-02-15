package enterprises.iwakura.docs.command;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class ReloadCommand extends CommandBase {

    private final ConfigurationService configurationService;
    private final DocumentationService documentationService;
    private final Logger logger;

    public ReloadCommand(
        ConfigurationService configurationService,
        DocumentationService documentationService,
        Logger logger
    ) {
        super("docs-reload", "Reloads Docs' configuration and registered documentations");
        this.configurationService = configurationService;
        this.documentationService = documentationService;
        this.logger = logger;
    }

    @Override
    protected void executeSync(@NonNull CommandContext ctx) {
        try {
            configurationService.reload();
            documentationService.reloadDocumentations();
            ChatInfo.SUCCESS.send(ctx, "Reload done.");
        } catch (Exception exception) {
            logger.error("Failed to reload documentations!", exception);
            ChatInfo.ERROR.send(ctx, "There was an error reloading the documentations: " + exception);
        }
    }
}
