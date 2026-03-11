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

import enterprises.iwakura.docs.service.DebugService;
import enterprises.iwakura.docs.ui.AboutVoilePage;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.sigewine.core.annotations.Bean;

@Bean
public class AboutVoileCommand extends AbstractPlayerCommand {

    private final DebugService debugService;

    public AboutVoileCommand(DebugService debugService) {
        super("debug-voile", "Shows debug information for Voile. Contains sensitive data about the server.");
        this.debugService = debugService;
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
            ChatInfo.ERROR.send(ctx, "No Player component found on in the store.");
            return;
        }

        player.getPageManager().openCustomPage(ref, store, new AboutVoilePage(playerRef, true, debugService));
    }
}
