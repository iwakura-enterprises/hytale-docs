package enterprises.iwakura.docs.service;

import com.hypixel.hytale.server.core.Constants;

import enterprises.iwakura.sigewine.core.annotations.Bean;
import lombok.RequiredArgsConstructor;

@Bean
@RequiredArgsConstructor
public class ServerService {

    /**
     * Checks if we're running on dedicated server
     *
     * @return True if yes, false otherwise
     */
    public boolean isRunningOnDedicatedServer() {
        return !Constants.SINGLEPLAYER;
    }

}
