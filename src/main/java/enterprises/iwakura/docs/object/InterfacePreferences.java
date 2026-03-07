package enterprises.iwakura.docs.object;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class InterfacePreferences {

    private final UUID playerUuid;
    private String lastOpenedTopicIdentifier;
    private String lastTopicSearchQuery;
    private List<String> topicIdentifierHistory;
    private int topicIdentifierHistoryIndex;
    private InterfaceMode lastInterfaceMode;

}
