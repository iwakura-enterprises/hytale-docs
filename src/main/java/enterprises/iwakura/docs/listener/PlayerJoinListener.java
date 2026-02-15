package enterprises.iwakura.docs.listener;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;

import enterprises.iwakura.docs.service.ConfigurationService;
import enterprises.iwakura.docs.util.ChatInfo;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class PlayerJoinListener implements BaseGlobalListener<PlayerConnectEvent> {

    private final ConfigurationService configurationService;

    @Override
    public Class<PlayerConnectEvent> getEventClass() {
        return PlayerConnectEvent.class;
    }

    @Override
    public void onEvent(PlayerConnectEvent event) {
        var docsConfig = configurationService.getDocsConfig();
        if (docsConfig.isOutOfBoxExperience()) {
            var playerRef = event.getPlayerRef();
            ChatInfo.SUCCESS.send(playerRef, "Thank you for installing Docs!");
            ChatInfo.WARN.send(playerRef, "Please, use <yellow>/docs</yellow> to open the interface.");
            ChatInfo.INFO.send(playerRef,
                "All documentation for Docs is available in-game. But there's a web version as well (<#5562ea><b><click:https://docs.iwakura.enterprises/hytale-docs.html>link</click></b></#5562ea>)");
            ChatInfo.WARN.send(playerRef,
                "You can disable this message in config by setting <yellow>outOfBoxExperience</yellow> to "
                    + "<yellow>false</yellow>.");
        }
    }
}
