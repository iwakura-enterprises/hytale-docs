package enterprises.iwakura.docs.command;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.service.DocumentationService;
import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.docs.service.ValidatorService;
import enterprises.iwakura.docs.ui.LocaleTypeSelectorPage;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.docs.util.Logger;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class VoileLocaleCommand extends AbstractPlayerCommand {

    private final DocumentationService documentationService;
    private final DocumentationViewerService documentationViewerService;
    private final ValidatorService validatorService;
    private final Logger logger;

    public VoileLocaleCommand(
        DocumentationService documentationService,
        DocumentationViewerService documentationViewerService, ValidatorService validatorService,
        Logger logger
    ) {
        super("voile-locale", "Allows you to change your preferred locale/language");
        this.documentationService = documentationService;
        this.validatorService = validatorService;
        this.logger = logger;
        addAliases("voile-language", "voile-lang", "docs-language", "docs-lang");
        this.documentationViewerService = documentationViewerService;
    }

    @Override
    protected void execute(
        @NonNull CommandContext ctx,
        @NonNull Store<EntityStore> store,
        @NonNull Ref<EntityStore> ref,
        @NonNull PlayerRef playerRef,
        @NonNull World world
    ) {
        var player = store.getComponent(ref, Player.getComponentType());

        if (player == null) {
            ChatInfo.ERROR.send(ctx, "No Player component in the store.");
            return;
        }

        player.getPageManager().openCustomPage(ref, store, new LocaleTypeSelectorPage(playerRef, documentationViewerService, documentationService, validatorService, logger, false));
    }
}
