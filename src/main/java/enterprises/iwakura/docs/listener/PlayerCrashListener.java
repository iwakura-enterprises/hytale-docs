package enterprises.iwakura.docs.listener;

import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.io.PacketHandler.DisconnectReason;

import enterprises.iwakura.docs.service.ValidatorService;
import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class PlayerCrashListener implements BaseGlobalListener<PlayerDisconnectEvent> {

    private final ValidatorService validatorService;

    @Override
    public Class<PlayerDisconnectEvent> getEventClass() {
        return PlayerDisconnectEvent.class;
    }

    @Override
    public void onEvent(PlayerDisconnectEvent event) {
        if (event.getDisconnectReason().getClientDisconnectType() == DisconnectType.Crash) {
            validatorService.handleCrashedPlayer(event.getPlayerRef());
        }
    }
}
