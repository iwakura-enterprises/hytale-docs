package enterprises.iwakura.docs.systems;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import enterprises.iwakura.docs.service.DocumentationViewerService;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class PlayerSystem extends HolderSystem<EntityStore> {

    private final DocumentationViewerService documentationViewerService;

    @Override
    public void onEntityAdd(
        @NonNull Holder<EntityStore> holder,
        @NonNull AddReason reason,
        @NonNull Store<EntityStore> store
    ) {
        var playerRef = holder.getComponent(PlayerRef.getComponentType());
        assert playerRef != null : "PlayerRef should not be null here";
        documentationViewerService.loadInterfacePreferences(playerRef, holder);
    }

    @Override
    public void onEntityRemoved(
        @NonNull Holder<EntityStore> holder,
        @NonNull RemoveReason reason,
        @NonNull Store<EntityStore> store
    ) {
        var playerRef = holder.getComponent(PlayerRef.getComponentType());
        assert playerRef != null : "PlayerRef should not be null here";
        documentationViewerService.saveInterfacePreferences(playerRef, holder);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Query.and(
            PlayerRef.getComponentType(),
            Player.getComponentType()
        );
    }
}
