package enterprises.iwakura.docs.command;

import org.jspecify.annotations.NonNull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class CommandShortcutCommand extends AbstractPlayerCommand {

    private final VoileCommand voileCommand;

    public CommandShortcutCommand(String name, VoileCommand voileCommand) {
        super(name, "Opens Voile's interface");
        this.voileCommand = voileCommand;
    }

    @Override
    protected void execute(
        @NonNull CommandContext commandContext,
        @NonNull Store<EntityStore> store,
        @NonNull Ref<EntityStore> ref,
        @NonNull PlayerRef playerRef,
        @NonNull World world
    ) {
        voileCommand.execute(commandContext, store, ref, playerRef, world);
    }
}
