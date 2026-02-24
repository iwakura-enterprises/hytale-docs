package enterprises.iwakura.docs.object;

import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class InterfacePreferences {

    private final UUID playerUuid;
    private String lastOpenedTopicIdentifier;
    private String lastTopicSearchQuery;

}
