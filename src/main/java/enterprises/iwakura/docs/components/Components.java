package enterprises.iwakura.docs.components;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Components {

    private static @Setter ComponentType<EntityStore, InterfacePreferencesComponent> interfacePreferencesComponentType;

    public static ComponentType<EntityStore, InterfacePreferencesComponent> getInterfacePreferencesComponent() {
        if (interfacePreferencesComponentType == null) {
            throw new IllegalStateException("InterfacePreferencesComponent not initialized yet");
        }
        return interfacePreferencesComponentType;
    }
}
