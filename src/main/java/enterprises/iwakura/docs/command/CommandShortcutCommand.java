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

    private final DocsCommand docsCommand;

    public CommandShortcutCommand(String name, DocsCommand docsCommand) {
        super(name, "Opens Voile's interface");
        this.docsCommand = docsCommand;
    }

    @Override
    protected void execute(
        @NonNull CommandContext commandContext,
        @NonNull Store<EntityStore> store,
        @NonNull Ref<EntityStore> ref,
        @NonNull PlayerRef playerRef,
        @NonNull World world
    ) {
        docsCommand.execute(commandContext, store, ref, playerRef, world);
    }
}
