package enterprises.iwakura.docs.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import enterprises.iwakura.docs.object.CacheIndex.Entry.CacheFileType;
import enterprises.iwakura.docs.object.DocumentationType;
import enterprises.iwakura.docs.object.InterfaceMode;
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
    private final List<DocumentationType> disabledDocumentationTypes = new ArrayList<>();
    private Validator validator = new Validator();
    private CommandShortcuts commandShortcuts = new CommandShortcuts();
    private RuntimeImageAssets runtimeImageAssets = new RuntimeImageAssets();
    private Integration integration = new Integration();
    private FileSystemCache fileSystemCache = new FileSystemCache();

    public List<InterfaceMode> getAvailableInterfaceModes() {
        return InterfaceMode.ALL.stream()
            .filter(mode -> mode != InterfaceMode.HYTALE_MODDING_WIKI || integration.getHytaleModdingWiki().isEnabled())
            .toList();
    }

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
            private boolean preLoadModsInBackground = true;
        }
    }

    @Data
    public static class FileSystemCache {

        public static final Map<CacheFileType, Long> DEFAULT_TTL = Arrays.stream(CacheFileType.values())
            .collect(Collectors.toMap(k -> k, CacheFileType::getDefaultTtlSeconds));

        private boolean enabled = true;
        private Map<String, Long> cacheTypeTimeToLiveSeconds = new HashMap<>();

        public void ensureAllTypes() {
            DEFAULT_TTL.forEach((type, ttl) -> cacheTypeTimeToLiveSeconds.putIfAbsent(type.name(), ttl));
        }

        public Long getCacheTypeTtlSafe(CacheFileType cacheFileType) {
            var ttl = cacheTypeTimeToLiveSeconds.get(cacheFileType.name());

            if (ttl == null) {
                ttl = cacheFileType.getDefaultTtlSeconds();
            }

            return ttl;
        }
    }
}
