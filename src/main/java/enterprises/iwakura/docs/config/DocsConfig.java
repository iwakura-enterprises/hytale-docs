package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import enterprises.iwakura.docs.object.CacheIndex;
import enterprises.iwakura.docs.object.CacheIndex.Entry.Type;
import enterprises.iwakura.docs.object.DocumentationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DocsConfig {

    private boolean enabled = true;
    private boolean outOfBoxExperience = true;
    private boolean updateCheckerEnabled = true;
    private String loadDocumentationsFromDirectory = "documentation";
    private String defaultTopicIdentifier;
    private final List<DocumentationType> enabledTypes = new ArrayList<>(DocumentationType.ALL);
    private Validator validator = new Validator();
    private CommandShortcuts commandShortcuts = new CommandShortcuts();
    private RuntimeImageAssets runtimeImageAssets = new RuntimeImageAssets();
    private Integration integration = new Integration();
    private FileSystemCache fileSystemCache = new FileSystemCache();

    @Data
    public static class Validator {

        private boolean enabled = true;
        private boolean aggressive = true;
        private boolean dumpInvalidUI = true;
        private boolean showErrorsToPlayers = false;
        private boolean checkForRecentlyCrashedPlayers = true;
        private long inMemoryDumpTimeToLiveMillis = 2000;
    }

    @Data
    public static class CommandShortcuts {

        private boolean enabled = true;
        private boolean overrideHytaleCommands = false;
        private final List<Command> commands = new ArrayList<>();

        public Optional<String> getTopicIdentifierForCommand(String commandName) {
            return commands.stream()
                .filter(command -> command.getName().equalsIgnoreCase(commandName))
                .findFirst()
                .map(Command::getTopicIdentifier);
        }

        @Data
        public static class Command {

            private String name;
            private String topicIdentifier;
        }
    }

    @Data
    public static class RuntimeImageAssets {

        private boolean enabled = true;
        private int maxImageDownloadFileSizeKb = 1024 * 2; // 2MB
        private int inMemoryTimeToLiveSeconds = 3600; // Hour
    }

    @Data
    public static class Integration {

        private HytaleModdingWiki hytaleModdingWiki = new HytaleModdingWiki();

        @Data
        public static class HytaleModdingWiki {

            private boolean enabled = true;
        }
    }

    @Data
    public static class FileSystemCache {

        public static final Map<Type, Long> DEFAULT_TTL = Map.of(
            Type.IMAGE, 86400L,
            Type.HYTALE_MODDING_MOD_INDEX, 86400L,
            Type.HYTALE_MODDING_PAGE_CONTENT, 86400L
        );

        private boolean enabled = true;
        private Map<Type, Long> cacheTypeTimeToLiveSeconds = new HashMap<>(DEFAULT_TTL);

        public void ensureAllTypes() {
            DEFAULT_TTL.forEach((type, ttl) -> cacheTypeTimeToLiveSeconds.putIfAbsent(type, ttl));
        }
    }
}
