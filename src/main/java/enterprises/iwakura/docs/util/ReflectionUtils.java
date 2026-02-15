package enterprises.iwakura.docs.util;

import java.util.List;

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {

    @SneakyThrows
    public static void mergeInto(
        UICommandBuilder fromCommandBuilder,
        UICommandBuilder toCommandBuilder,
        UIEventBuilder fromEventBuilder,
        UIEventBuilder toEventBuilder
    ) {
        var commandBuilderClass = UICommandBuilder.class;
        var commandsField = commandBuilderClass.getDeclaredField("commands");
        commandsField.setAccessible(true);
        //noinspection unchecked
        ((List<CustomUICommand>) commandsField.get(toCommandBuilder)).addAll(List.of(fromCommandBuilder.getCommands()));

        var eventBuilderClass = UIEventBuilder.class;
        var eventsField = eventBuilderClass.getDeclaredField("events");
        eventsField.setAccessible(true);
        //noinspection unchecked
        ((List<CustomUIEventBinding>) eventsField.get(toEventBuilder)).addAll(List.of(fromEventBuilder.getEvents()));
    }

    @SneakyThrows
    public static List<CustomUICommand> getCommands(UICommandBuilder commandBuilder) {
        var commandBuilderClass = UICommandBuilder.class;
        var commandsField = commandBuilderClass.getDeclaredField("commands");
        commandsField.setAccessible(true);
        //noinspection unchecked
        return ((List<CustomUICommand>) commandsField.get(commandBuilder));
    }
}
